package com.expanset.hk2.persistence;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.transaction.Transaction;
import javax.transaction.Transactional;
import javax.transaction.Transactional.TxType;
import javax.transaction.TransactionalException;

import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.Service;

import com.expanset.common.errors.MultiErrorException;
import com.expanset.hk2.persistence.transactions.LocalTransactionsBinder;

public class TransactionalInterceptorTest {
	
	private ServiceLocator serviceLocator;
	
	private TransactionImpl transaction1;
	
	private List<PersistenceContextWrapper> persistenceContexts = new ArrayList<>();
	
	@Before
	public void init() 
			throws Exception {
		persistenceContexts = new ArrayList<>();
		
		transaction1 = spy(new TransactionImpl());
		final PersistenceContextWrapper persistenceContext1 = 
				mock(PersistenceContextWrapper.class);
		when(persistenceContext1.beginTransaction()).thenReturn(transaction1);
		persistenceContexts.add(persistenceContext1);		
		
		final PersistenceSession persistenceSession = 
				mock(PersistenceSession.class) ;
		when(persistenceSession.getAllPersistenceContextsInCurrentThread()).thenReturn(persistenceContexts);
		
		final PersistenceSessionManager sessionManager = 
				mock(PersistenceSessionManager.class);
		when(sessionManager.getCurrentSession()).thenReturn(persistenceSession);
		
		serviceLocator = ServiceLocatorUtilities.bind(new PersistenceBinder() {
			@Override
			protected void configure() {
				super.configure();
				
				install(new LocalTransactionsBinder());

				bind(sessionManager).to(PersistenceSessionManager.class);
				
				addActiveDescriptor(TransactionalTest.class);
			}
		});		
	}

	@After
	public void done() {
		ServiceLocatorFactory.getInstance().destroy(serviceLocator);
	}
	
	@Test
	public void normalCommit() 
			throws Throwable {
		final TransactionalTest test = 
				serviceLocator.getService(TransactionalTest.class);
		
		test.normal();
		
		verify(transaction1, times(1)).commit();
		verify(transaction1, times(0)).rollback();
	}

	@Test
	public void normalRollbackOnException() 
			throws Throwable {
		final TransactionalTest test = 
				serviceLocator.getService(TransactionalTest.class);
		
		boolean error = false;
		try {
			test.rollback();
		} catch (ArithmeticException e) {
			error = true;
		}
		assertTrue(error);
		
		verify(transaction1, times(0)).commit();
		verify(transaction1, times(1)).rollback();
	}
	
	@Test
	public void normalRollback() 
			throws Throwable {
		final TransactionalTest test = 
				serviceLocator.getService(TransactionalTest.class);
		
		test.setRollback();

		verify(transaction1, times(0)).commit();
		verify(transaction1, times(1)).rollback();
	}	
	
	@Test
	public void nestedMethod() 
			throws Throwable {
		final TransactionalTest test = 
				serviceLocator.getService(TransactionalTest.class);
		
		test.nested();

		verify(transaction1, times(1)).commit();
		verify(transaction1, times(0)).rollback();
	}	
	
	@Test
	public void dontRollbackOnError() 
			throws Throwable {
		final TransactionalTest test = 
				serviceLocator.getService(TransactionalTest.class);
		
		boolean error = false;
		try {
			test.dontRollback(false);
		} catch (ArithmeticException e) {
			error = true;
		}
		assertTrue(error);		

		verify(transaction1, times(1)).commit();
		verify(transaction1, times(0)).rollback();
	}	

	@Test
	public void dontRollbackOnErrorTestThrow() 
			throws Throwable {
		final TransactionalTest test = 
				serviceLocator.getService(TransactionalTest.class);
		
		boolean error = false;
		try {
			test.dontRollback(true);
		} catch (ArrayStoreException e) {
			error = true;
		}
		assertTrue(error);			
		
		verify(transaction1, times(0)).commit();
		verify(transaction1, times(1)).rollback();
	}	

	@Test
	public void rollbackOnError() 
			throws Throwable {
		final TransactionalTest test = 
				serviceLocator.getService(TransactionalTest.class);
		
		boolean error = false;
		try {
			test.rollbackOn(false);
		} catch (ArrayStoreException e) {
			error = true;
		}
		assertTrue(error);		

		verify(transaction1, times(1)).commit();
		verify(transaction1, times(0)).rollback();
	}	

	@Test
	public void rollbackOnErrorTestThrow() 
			throws Throwable {
		final TransactionalTest test = 
				serviceLocator.getService(TransactionalTest.class);
		
		boolean error = false;
		try {
			test.rollbackOn(true);
		} catch (ArithmeticException e) {
			error = true;
		}
		assertTrue(error);			
		
		verify(transaction1, times(0)).commit();
		verify(transaction1, times(1)).rollback();
	}	

	@Test(expected=TransactionalException.class)
	public void transactionRequired() 
			throws Throwable {
		final TransactionalTest test = 
				serviceLocator.getService(TransactionalTest.class);
		
		test.nested1();
	}	
	
	@Test(expected=TransactionalException.class)
	public void transactionNever() 
			throws Throwable {
		final TransactionalTest test = 
				serviceLocator.getService(TransactionalTest.class);
		
		test.never();
	}	
	
	@Test
	public void errorWhenCommit() 
			throws Throwable {		
		final TransactionImpl transaction2 = spy(new TransactionImpl(true, false));
		final PersistenceContextWrapper persistenceContext2 = 
				mock(PersistenceContextWrapper.class);
		when(persistenceContext2.beginTransaction()).thenReturn(transaction2);
		persistenceContexts.add(persistenceContext2);
		
		final TransactionalTest test = 
				serviceLocator.getService(TransactionalTest.class);
		
		try {
			test.normal();
		} catch(MultiErrorException e) {
		}
		
		verify(transaction2, times(1)).commit();
		verify(transaction2, times(1)).rollback();		
	}	
	
	@Service
	@Contract
	@PerLookup
	public static class TransactionalTest {
		
		@Inject
		public Transaction currentTransaction;
		
		@Transactional
		public void normal() {		
			assertTrue(Proxy.isProxyClass(currentTransaction.getClass()));
		}
		
		@Transactional
		public void rollback() {	
			throw new ArithmeticException();
		}

		@Transactional
		public void setRollback() 
				throws Exception {	
			currentTransaction.setRollbackOnly();
		}

		@Transactional
		public void nested() 
				throws Exception {	
			nested1();
		}

		@Transactional(value=TxType.MANDATORY)
		public void nested1() 
				throws Exception {	
		}
		
		@Transactional(dontRollbackOn=ArithmeticException.class)
		public void dontRollback(boolean throwToRollback) 
				throws Exception {	
			if(throwToRollback) {
				throw new ArrayStoreException();
			}			
			throw new ArithmeticException();
		}
		
		@Transactional(rollbackOn=ArithmeticException.class)
		public void rollbackOn(boolean throwToRollback) 
				throws Exception {	
			if(throwToRollback) {
				throw new ArithmeticException();
			}
			throw new ArrayStoreException();
		}
		
		@Transactional
		public void never() 
				throws Exception {	
			never1();
		}

		@Transactional(value=TxType.NEVER)
		public void never1() 
				throws Exception {	
		}		
	}
}
