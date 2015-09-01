package com.expanset.hk2.persistence;

import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.expanset.hk2.persistence.transactions.LocalTransactionsBinder;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class ThreadScopePersistenceSessionManagerTest {
	
	private ServiceLocator serviceLocator;
	
	@Before
	public void init() {
		serviceLocator = ServiceLocatorUtilities.bind(new PersistenceBinder() {
			@Override
			protected void configure() {
				super.configure();
				
				install(new LocalTransactionsBinder());

				addActiveDescriptor(ThreadScopePersistenceSessionManager.class);
			}
		});		
	}

	@After
	public void done() {
		ServiceLocatorFactory.getInstance().destroy(serviceLocator);
	}	
	
	@Test
	public void beginningSession() 
			throws Throwable {		
		final PersistenceContextFactoryCreator factoryCreator = 
				mock(PersistenceContextFactoryCreator.class);
		
		ServiceLocatorUtilities.bind(new AbstractBinder() {
			@Override
			protected void configure() {
				bind(factoryCreator)
					.to(PersistenceContextFactoryCreator.class);				
			}
		});			
	
		final ThreadScopePersistenceSessionManager sessionManager = 
				serviceLocator.getService(ThreadScopePersistenceSessionManager.class);
		
		try (AutoCloseable scope = sessionManager.beginSession()) {
			final PersistenceSession session = sessionManager.getCurrentSession();
			assertNotNull(session);
			try (AutoCloseable scope2 = sessionManager.beginSession()) {
				final PersistenceSession session2 = sessionManager.getCurrentSession();
				assertNotNull(session2);
				assertTrue(session != session2);
			}			
		}
		
		final PersistenceSession session = sessionManager.getCurrentSession();
		assertNull(session);
	}
	
	@Test
	public void scopingPersistenceContext() 
			throws Throwable {		
		final PersistenceContextFactoryKey factoryKey = 
				new PersistenceContextFactoryKey("test1");
		final PersistenceContextKey key = 
				new PersistenceContextKey(factoryKey);
		
		final Runnable persistenceContext = mock(Runnable.class);
		
		final PersistenceContextWrapper wrapper = 
				mock(PersistenceContextWrapper.class);		
		when(wrapper.unwrap(Runnable.class)).thenReturn(persistenceContext);
		
		final PersistenceContextFactoryWrapper factoryWrapper = 
				mock(PersistenceContextFactoryWrapper.class);		
		when(factoryWrapper.create(eq(key))).thenReturn(wrapper);
		
		final Map<String, String> factoryProperties1 = new HashMap<>();
		factoryProperties1.put("key1", "value1");	
		
		final PersistenceContextFactoryCreator factoryCreator = 
				mock(PersistenceContextFactoryCreator.class);
		when(factoryCreator.create(eq(factoryKey), eq(factoryProperties1), any()))
				.thenReturn(factoryWrapper);			
		
		ServiceLocatorUtilities.bind(new AbstractBinder() {
			@Override
			protected void configure() {
				bind(factoryCreator)
					.to(PersistenceContextFactoryCreator.class);				
			}
		});			

		final PersistenceContextFactoryAccessor accessor = 
				serviceLocator.getService(PersistenceContextFactoryAccessor.class);
		accessor.setFactoryProperties("test1", factoryProperties1);
		
		final ThreadScopePersistenceSessionManager sessionManager = 
				serviceLocator.getService(ThreadScopePersistenceSessionManager.class);
		
		try (AutoCloseable scope = sessionManager.beginSession()) {
			Runnable storedPersistenceContext = 
					sessionManager.getPersistenceContext(key, Runnable.class, PerLookup.class.getName());
			assertTrue(storedPersistenceContext == persistenceContext);

			storedPersistenceContext = 
					sessionManager.getPersistenceContext(key, Runnable.class, "scope");
			assertTrue(Proxy.isProxyClass(storedPersistenceContext.getClass()));
			
			storedPersistenceContext.run();
		}	
		
		verify(persistenceContext, times(1)).run();	
	}
}
