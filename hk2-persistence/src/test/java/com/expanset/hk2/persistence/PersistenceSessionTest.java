package com.expanset.hk2.persistence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public class PersistenceSessionTest {
	
	private ServiceLocator serviceLocator;
	
	private Throwable lastError;
	
	@Before
	public void init() {
		lastError = null;
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
	public void persistenceContextCreation() 
			throws Throwable {
		final PersistenceContextFactoryKey factoryKey = 
				new PersistenceContextFactoryKey("test1");
		final PersistenceContextFactoryKey factoryKey2 = 
				new PersistenceContextFactoryKey("test2");		
		final PersistenceContextKey key = 
				new PersistenceContextKey(factoryKey);
		final PersistenceContextKey key2 = 
				new PersistenceContextKey(factoryKey2);
		
		final Object persistenceContext = new Object();
		final Object persistenceContext2 = new Object();
		
		final PersistenceContextWrapper wrapper = 
				mock(PersistenceContextWrapper.class);		
		when(wrapper.unwrap(Object.class)).thenReturn(persistenceContext);
		
		final PersistenceContextWrapper wrapper2 = 
				mock(PersistenceContextWrapper.class);		
		when(wrapper2.unwrap(Object.class)).thenReturn(persistenceContext2);		
		
		final PersistenceContextFactoryWrapper factoryWrapper = 
				mock(PersistenceContextFactoryWrapper.class);		
		when(factoryWrapper.create(eq(key2))).thenReturn(wrapper);
		when(factoryWrapper.create(eq(key))).thenReturn(wrapper2);
		
		final Map<String, String> factoryProperties1 = new HashMap<>();
		factoryProperties1.put("key1", "value1");
		
		final Map<String, String> factoryProperties2 = new HashMap<>();
		factoryProperties2.put("key2", "value2");			
		
		final PersistenceContextFactoryCreator factoryCreator = 
				mock(PersistenceContextFactoryCreator.class);
		when(factoryCreator.create(eq(factoryKey2), eq(factoryProperties2), any()))
				.thenReturn(factoryWrapper);
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
		accessor.setFactoryProperties("test2", factoryProperties2);

		final Map<String, String> nameOverrides = new HashMap<>();
		nameOverrides.put("test1", "test2");	
		nameOverrides.put("test2", "test1");	
		
		final PersistenceSession session = new PersistenceSession(nameOverrides);
		serviceLocator.inject(session);
		
		Object storedPersistenceContext = session.getPersistenceContext(key, Object.class);
		assertTrue(storedPersistenceContext == persistenceContext);

		storedPersistenceContext = session.getPersistenceContext(key, Object.class);
		assertTrue(storedPersistenceContext == persistenceContext);
		
		Thread thread = new Thread(() -> {
			try {
				Object otherPersistenceContext = session.getPersistenceContext(key2, Object.class);
				assertTrue(otherPersistenceContext == persistenceContext2);	
				
				List<PersistenceContextWrapper> contexts = session.getAllPersistenceContextsInCurrentThread();
				assertTrue(contexts.size() == 1);
				assertTrue(contexts.get(0) == wrapper2);	
				
				session.evictInCurrentThread();
				
				contexts = session.getAllPersistenceContextsInCurrentThread();
				assertTrue(contexts.size() == 0);	
				
				otherPersistenceContext = session.getPersistenceContext(key2, Object.class);
				assertTrue(otherPersistenceContext == persistenceContext2);					
			} catch (Throwable e) {
				lastError = e;
			}
		});
		thread.start();
		thread.join();
		if(lastError != null) {
			throw lastError;
		}
		
		session.evict();
		
		storedPersistenceContext = session.getPersistenceContext(key, Object.class);
		assertTrue(storedPersistenceContext == persistenceContext);		
		
		session.close();
		
		verify(factoryWrapper, times(2)).create(eq(key2));	
		
		verify(wrapper, times(2)).close();	
		verify(wrapper2, times(2)).close();			
	}	
}
