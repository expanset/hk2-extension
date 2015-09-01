package com.expanset.hk2.persistence;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.MapConfiguration;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.expanset.hk2.persistence.config.MultipleDatabasesPersistenceConfiguratorSettings;
import com.expanset.hk2.persistence.config.PersistenceConfigurator;
import com.expanset.hk2.persistence.config.PersistenceConfiguratorSettings;
import com.expanset.hk2.persistence.config.SingleDatabasePersistenceConfiguratorSettings;
import com.expanset.hk2.persistence.transactions.LocalTransactionsBinder;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class PersistenceConfiguratorTest {
	
	private ServiceLocator serviceLocator;
	
	@Before
	public void init() {
		final PersistenceContextFactoryCreator factoryCreator = 
				mock(PersistenceContextFactoryCreator.class);
		
		serviceLocator = ServiceLocatorUtilities.bind(new PersistenceBinder() {
			@Override
			protected void configure() {
				super.configure();
				
				install(new LocalTransactionsBinder());

				addActiveDescriptor(ThreadScopePersistenceSessionManager.class);
				
				bind(factoryCreator)
					.to(PersistenceContextFactoryCreator.class);					
			}
		});		
	}

	@After
	public void done() {
		ServiceLocatorFactory.getInstance().destroy(serviceLocator);
	}	
	
	@Test
	public void configureMultipleDatabases() 
			throws Throwable {		
		final Map<String, String> properties = new HashMap<>();
		properties.put("dbs", "db1,db2");
		properties.put("db1.key11", "value11");
		properties.put("db2.key12", "value12");
		final Configuration config = new MapConfiguration(properties);
		
		final Map<String, String> commonProperties = new HashMap<>();
		commonProperties.put("1", "2");
		
		ServiceLocatorUtilities.bind(serviceLocator, 
				new AbstractBinder() {
					@Override
					protected void configure() {
						bind(config).to(Configuration.class);
						MultipleDatabasesPersistenceConfiguratorSettings settings = 
								new MultipleDatabasesPersistenceConfiguratorSettings("dbs", "db2");
						settings.setCommonProperties(commonProperties);
						bind(settings).to(PersistenceConfiguratorSettings.class);
						addActiveDescriptor(PersistenceConfigurator.class);
					}
				});
		
		final PersistenceContextFactoryAccessor accessor = 
				serviceLocator.getService(PersistenceContextFactoryAccessor.class);
		
		Map<String, String> storedProperties = accessor.getFactoryProperties("db1");
		final Map<String, String> propertiesDb1 = new HashMap<>();
		propertiesDb1.put("key11", "value11");
		assertEquals(propertiesDb1, storedProperties);

		storedProperties = accessor.getFactoryProperties("db2");
		final Map<String, String> propertiesDb2 = new HashMap<>();
		propertiesDb2.put("key12", "value12");
		assertEquals(propertiesDb2, storedProperties);

		storedProperties = accessor.getFactoryProperties("");
		assertEquals(propertiesDb2, storedProperties);
		
		final String commonProperty = accessor.getCommonProperty("1");
		assertEquals("2", commonProperty);
	}

	@Test
	public void configureSingleDatabase() 
			throws Throwable {		
		final Map<String, String> properties = new HashMap<>();
		properties.put("db1.key11", "value11");
		final Configuration config = new MapConfiguration(properties);
		
		final Map<String, String> commonProperties = new HashMap<>();
		commonProperties.put("1", "2");
		
		ServiceLocatorUtilities.bind(serviceLocator, 
				new AbstractBinder() {
					@Override
					protected void configure() {
						bind(config).to(Configuration.class);						
						SingleDatabasePersistenceConfiguratorSettings settings = 
								new SingleDatabasePersistenceConfiguratorSettings("db1");
						settings.setCommonProperties(commonProperties);	
						bind(settings).to(PersistenceConfiguratorSettings.class);
						addActiveDescriptor(PersistenceConfigurator.class);
					}
				});
		
		final PersistenceContextFactoryAccessor accessor = 
				serviceLocator.getService(PersistenceContextFactoryAccessor.class);
		
		Map<String, String> storedProperties = accessor.getFactoryProperties("");
		final Map<String, String> propertiesDb1 = new HashMap<>();
		propertiesDb1.put("key11", "value11");
		assertEquals(propertiesDb1, storedProperties);

		storedProperties = accessor.getFactoryProperties("");
		assertEquals(propertiesDb1, storedProperties);
	}
}
