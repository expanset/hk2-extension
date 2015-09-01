package com.expanset.hk2.persistence;

import java.util.ArrayList;
import java.util.List;

import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.expanset.common.errors.MultiErrorException;
import com.expanset.hk2.persistence.transactions.LocalTransactionsBinder;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class LocalTransactionManagerTest {
	
	private ServiceLocator serviceLocator;
	
	private TransactionImpl transaction1;
	
	private TransactionImpl transaction2;
	
	private List<PersistenceContextWrapper> persistenceContexts = new ArrayList<>();
	
	@Before
	public void init() 
			throws Exception {
		persistenceContexts = new ArrayList<>();
		transaction1 = spy(new TransactionImpl());
		transaction2 = spy(new TransactionImpl());
		final PersistenceContextWrapper persistenceContext1 = 
				mock(PersistenceContextWrapper.class);
		when(persistenceContext1.beginTransaction()).thenReturn(transaction1);
		persistenceContexts.add(persistenceContext1);
		final PersistenceContextWrapper persistenceContext2 = 
				mock(PersistenceContextWrapper.class);
		when(persistenceContext2.beginTransaction()).thenReturn(transaction2);
		persistenceContexts.add(persistenceContext2);
		
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
			}
		});		
	}

	@After
	public void done() {
		ServiceLocatorFactory.getInstance().destroy(serviceLocator);
	}
	
	@Test
	public void beginAndCommitTransaction() 
			throws Throwable {
		final TransactionManager transactionManager = 
				serviceLocator.getService(TransactionManager.class);
		
		assertTrue(transactionManager.getStatus() == Status.STATUS_NO_TRANSACTION);
		
		transactionManager.begin();
		final Transaction transaction = transactionManager.getTransaction();
		
		assertTrue(transactionManager.getStatus() == Status.STATUS_ACTIVE);
		assertNotNull(transaction);
		assertTrue(transaction.getStatus() == Status.STATUS_ACTIVE);
		assertTrue(transaction1.getStatus() == Status.STATUS_ACTIVE);
		assertTrue(transaction2.getStatus() == Status.STATUS_ACTIVE);
				
		transactionManager.commit();
		
		assertTrue(transactionManager.getStatus() == Status.STATUS_NO_TRANSACTION);
		assertNull(transactionManager.getTransaction());
		assertTrue(transaction.getStatus() == Status.STATUS_COMMITTED);
		assertTrue(transaction1.getStatus() == Status.STATUS_COMMITTED);
		assertTrue(transaction2.getStatus() == Status.STATUS_COMMITTED);

		transaction.rollback();
		assertTrue(transaction.getStatus() == Status.STATUS_COMMITTED);
		assertTrue(transaction1.getStatus() == Status.STATUS_COMMITTED);
		assertTrue(transaction2.getStatus() == Status.STATUS_COMMITTED);
		
		verify(transaction1, times(1)).commit();
		verify(transaction2, times(1)).commit();
		verify(transaction1, times(1)).close();
		verify(transaction2, times(1)).close();
	}

	@Test
	public void beginAndRollbackTransaction() 
			throws Throwable {
		final TransactionManager transactionManager = 
				serviceLocator.getService(TransactionManager.class);

		transactionManager.begin();
		final Transaction transaction = transactionManager.getTransaction();
		
		transactionManager.rollback();
		
		assertTrue(transactionManager.getStatus() == Status.STATUS_NO_TRANSACTION);
		assertNull(transactionManager.getTransaction());
		assertTrue(transaction.getStatus() == Status.STATUS_ROLLEDBACK);
		assertTrue(transaction1.getStatus() == Status.STATUS_ROLLEDBACK);
		assertTrue(transaction2.getStatus() == Status.STATUS_ROLLEDBACK);

		verify(transaction1, times(1)).rollback();
		verify(transaction2, times(1)).rollback();		
		verify(transaction1, times(1)).close();
		verify(transaction2, times(1)).close();
	}

	@Test
	public void setRollbackOnly() 
			throws Throwable {
		final TransactionManager transactionManager = 
				serviceLocator.getService(TransactionManager.class);
		
		transactionManager.begin();
		Transaction transaction = transactionManager.getTransaction();
		
		transactionManager.setRollbackOnly();
		assertTrue(transactionManager.getStatus() == Status.STATUS_MARKED_ROLLBACK);
		assertTrue(transaction.getStatus() == Status.STATUS_MARKED_ROLLBACK);
		assertTrue(transaction1.getStatus() == Status.STATUS_MARKED_ROLLBACK);
		assertTrue(transaction2.getStatus() == Status.STATUS_MARKED_ROLLBACK);

		transactionManager.rollback();

		verify(transaction1, times(1)).rollback();
		verify(transaction2, times(1)).rollback();		
		verify(transaction1, times(1)).close();
		verify(transaction2, times(1)).close();
	}

	@Test(expected=RollbackException.class)
	public void setRollbackOnlyAndCommit() 
			throws Throwable {
		final TransactionManager transactionManager = 
				serviceLocator.getService(TransactionManager.class);
		
		transactionManager.begin();
		Transaction transaction = transactionManager.getTransaction();
		
		transactionManager.setRollbackOnly();
		try {
			transactionManager.commit();	
		} catch(RollbackException e) {
			assertTrue(transaction.getStatus() == Status.STATUS_ROLLEDBACK);
			assertTrue(transaction1.getStatus() == Status.STATUS_ROLLEDBACK);
			assertTrue(transaction2.getStatus() == Status.STATUS_ROLLEDBACK);
			
			throw e;
		}
	}

	@Test
	@SuppressWarnings("unchecked")
	public void beginTransactionErrror() 
			throws Throwable {
		final PersistenceContextWrapper persistenceContext3 = 
				mock(PersistenceContextWrapper.class);
		when(persistenceContext3.beginTransaction()).thenThrow(RuntimeException.class);
		persistenceContexts.add(persistenceContext3);
		
		final TransactionManager transactionManager = 
				serviceLocator.getService(TransactionManager.class);
		try {
			transactionManager.begin();
		} catch (RuntimeException e) {
			verify(transaction1, times(1)).rollback();
			verify(transaction2, times(1)).rollback();		
			verify(transaction1, times(1)).close();
			verify(transaction2, times(1)).close();			
		}
	}

	@Test
	public void commitTransactionErrror() 
			throws Throwable {
		final TransactionImpl transaction3 = spy(new TransactionImpl(true, false));
		final PersistenceContextWrapper persistenceContext3 = 
				mock(PersistenceContextWrapper.class);
		when(persistenceContext3.beginTransaction()).thenReturn(transaction3);
		persistenceContexts.add(persistenceContext3);
		
		final TransactionManager transactionManager = 
				serviceLocator.getService(TransactionManager.class);
		transactionManager.begin();
		try {
			transactionManager.commit();
		} catch (MultiErrorException e) {
			verify(transaction1, times(1)).commit();
			verify(transaction2, times(1)).commit();		
			verify(transaction3, times(1)).commit();
			verify(transaction1, times(1)).rollback();
			verify(transaction2, times(1)).rollback();		
			verify(transaction3, times(1)).rollback();
			verify(transaction1, times(1)).close();
			verify(transaction2, times(1)).close();			
			verify(transaction3, times(1)).close();
		}
	}
	
	@Test(expected=MultiErrorException.class)
	public void rollbackTransactionErrror() 
			throws Throwable {
		final TransactionImpl transaction3 = spy(new TransactionImpl(false, true));
		final PersistenceContextWrapper persistenceContext3 = 
				mock(PersistenceContextWrapper.class);
		when(persistenceContext3.beginTransaction()).thenReturn(transaction3);
		persistenceContexts.add(persistenceContext3);
		
		final TransactionManager transactionManager = 
				serviceLocator.getService(TransactionManager.class);
		transactionManager.begin();
		try {
			transactionManager.rollback();
		} catch (MultiErrorException e) {
			verify(transaction1, times(0)).commit();
			verify(transaction2, times(0)).commit();		
			verify(transaction3, times(0)).commit();
			verify(transaction1, times(1)).rollback();
			verify(transaction2, times(1)).rollback();		
			verify(transaction3, times(1)).rollback();
			verify(transaction1, times(1)).close();
			verify(transaction2, times(1)).close();			
			verify(transaction3, times(1)).close();
			
			throw e;
		}
	}	

	@Test(expected=MultiErrorException.class)
	public void commitAndRollbackTransactionErrror() 
			throws Throwable {
		final TransactionImpl transaction3 = spy(new TransactionImpl(true, true));
		final PersistenceContextWrapper persistenceContext3 = 
				mock(PersistenceContextWrapper.class);
		when(persistenceContext3.beginTransaction()).thenReturn(transaction3);
		persistenceContexts.add(persistenceContext3);
		
		final TransactionManager transactionManager = 
				serviceLocator.getService(TransactionManager.class);
		transactionManager.begin();
		try {
			transactionManager.commit();
		} catch (MultiErrorException e) {
			verify(transaction1, times(1)).commit();
			verify(transaction2, times(1)).commit();		
			verify(transaction3, times(1)).commit();
			verify(transaction1, times(1)).rollback();
			verify(transaction2, times(1)).rollback();		
			verify(transaction3, times(1)).rollback();
			verify(transaction1, times(1)).close();
			verify(transaction2, times(1)).close();			
			verify(transaction3, times(1)).close();
			
			throw e;
		}
	}	
}
