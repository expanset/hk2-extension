package com.expanset.hk2.persistence;

import java.util.HashMap;
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

public class PersistenceContextFactoryAccessorTest {

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
	public void commonPropertyManagement() {
		bindDumbFactoryCreator();
		
		final PersistenceContextFactoryAccessor accessor = 
				serviceLocator.getService(PersistenceContextFactoryAccessor.class);
		
		String property = accessor.getCommonProperty("none");
		assertNull(property);

		accessor.setCommonProperty("property", "1");
		
		property = accessor.getCommonProperty("property");
		assertEquals("1", property);
		
		final Map<String, String> commonProperties = new HashMap<>();
		commonProperties.put("property", "2");
		accessor.resetFactoriesProperties(null, commonProperties);
		
		property = accessor.getCommonProperty("property");
		assertEquals("2", property);
	}
	
	@Test
	public void factoryPropertiesManagement() {
		bindDumbFactoryCreator();
		
		final PersistenceContextFactoryAccessor accessor = 
				serviceLocator.getService(PersistenceContextFactoryAccessor.class);
		
		final Map<String, String> factoryProperties = new HashMap<>();
		factoryProperties.put("key", "value");
		accessor.setFactoryProperties("test", factoryProperties);
		
		Map<String, String> storedFactoryProperties = accessor.getFactoryProperties("test");
		assertEquals(factoryProperties, storedFactoryProperties);
		
		accessor.removeFactoryProperties("test");
		
		storedFactoryProperties = accessor.getFactoryProperties("test");
		assertNull(storedFactoryProperties);

		accessor.setFactoryProperties("test", factoryProperties);
		
		final Map<String, Map<String, String>> newFactoryies = new HashMap<>();
		final Map<String, String> newFactoryProperties = new HashMap<>();
		newFactoryProperties.put("key1", "value1");		
		newFactoryies.put("test", newFactoryProperties);
		accessor.resetFactoriesProperties(newFactoryies, null);
		
		storedFactoryProperties = accessor.getFactoryProperties("test");
		assertEquals(newFactoryProperties, storedFactoryProperties);
	}

	@Test
	public void factoryCreation() 
			throws Exception {
		final PersistenceContextFactoryWrapper wrapper = 
				mock(PersistenceContextFactoryWrapper.class);
		final PersistenceContextFactoryWrapper wrapper2 = 
				mock(PersistenceContextFactoryWrapper.class);
		final PersistenceContextFactoryWrapper wrapper3 = 
				mock(PersistenceContextFactoryWrapper.class);
		
		final PersistenceContextFactoryKey key = new PersistenceContextFactoryKey("test");
		final PersistenceContextFactoryKey key2 = new PersistenceContextFactoryKey("test2");
		final PersistenceContextFactoryKey key3 = new PersistenceContextFactoryKey("test3");
		
		final Map<String, Map<String, String>> factories = new HashMap<>();
		final Map<String, String> factoryProperties = new HashMap<>();
		factoryProperties.put("key", "value");
		factories.put("test", factoryProperties);
		factories.put("test2", factoryProperties);
		factories.put("test3", factoryProperties);
		
		final Map<String, String> commonProperties = new HashMap<>();
		commonProperties.put("ckey", "cvalue");

		final PersistenceContextFactoryCreator factoryCreator = 
				mock(PersistenceContextFactoryCreator.class);
		when(factoryCreator.create(key, factoryProperties, commonProperties))
				.thenReturn(wrapper);
		when(factoryCreator.create(key2, factoryProperties, commonProperties))
				.thenReturn(wrapper2);
		when(factoryCreator.create(key3, factoryProperties, commonProperties))
				.thenReturn(wrapper3);
		
		ServiceLocatorUtilities.bind(new AbstractBinder() {
			@Override
			protected void configure() {
				bind(factoryCreator)
					.to(PersistenceContextFactoryCreator.class);
			}
		});
		
		final PersistenceContextFactoryAccessor accessor = 
				serviceLocator.getService(PersistenceContextFactoryAccessor.class);
				
		accessor.resetFactoriesProperties(factories, commonProperties);
		
		// Create 1 (key)
		PersistenceContextFactoryWrapper storedWrapper = accessor.getFactory(key);
		assertTrue(storedWrapper == wrapper);	
		
		// Returns cached.
		storedWrapper = accessor.getFactory(key);
		assertTrue(storedWrapper == wrapper);	
		
		accessor.setFactoryProperties("test", factoryProperties);

		// Create 2 (key)
		storedWrapper = accessor.getFactory(key);
		assertTrue(storedWrapper == wrapper);	
		
		accessor.resetFactoriesProperties(factories, commonProperties);

		// Create 3 (key)
		storedWrapper = accessor.getFactory(key);
		assertTrue(storedWrapper == wrapper);	
		
		// Create 1 (key2)
		storedWrapper = accessor.getFactory(key2);
		assertTrue(storedWrapper == wrapper2);	
		
		accessor.evict("test");
		
		// Returns cached.
		storedWrapper = accessor.getFactory(key2);
		assertTrue(storedWrapper == wrapper2);			

		// Create 4 (key)
		storedWrapper = accessor.getFactory(key);
		assertTrue(storedWrapper == wrapper);	
		
		accessor.evictAll();

		// Create 5 (key)
		storedWrapper = accessor.getFactory(key);
		assertTrue(storedWrapper == wrapper);	

		// Create 2 (key2)
		storedWrapper = accessor.getFactory(key2);
		assertTrue(storedWrapper == wrapper2);	
		
		// Create 1 (key3)
		storedWrapper = accessor.getFactory(key3);
		assertTrue(storedWrapper == wrapper3);			
		
		accessor.removeFactoryProperties("test3");
		verify(wrapper3, times(1)).close();
		
		boolean cached = false;
		try {
			storedWrapper = accessor.getFactory(key3);
		} catch (IllegalStateException e) {
			cached = true;
		}
		assertTrue(cached);
		
		verify(factoryCreator, times(5))
			.create(key, factoryProperties, commonProperties);
		verify(factoryCreator, times(2))
			.create(key2, factoryProperties, commonProperties);

		accessor.preDestroy();
		
		verify(wrapper, times(5)).close();
		verify(wrapper2, times(2)).close();
	}	

	private void bindDumbFactoryCreator() {
		ServiceLocatorUtilities.bind(new AbstractBinder() {
			@Override
			protected void configure() {
				bind(mock(PersistenceContextFactoryCreator.class))
					.to(PersistenceContextFactoryCreator.class);
			}
		});
	}	
}
