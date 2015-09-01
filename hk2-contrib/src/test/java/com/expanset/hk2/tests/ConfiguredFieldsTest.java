package com.expanset.hk2.tests;

import java.io.Reader;
import java.io.Writer;
import java.util.function.Supplier;

import org.apache.commons.configuration.AbstractFileConfiguration;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.Descriptor;
import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.Service;

import com.expanset.hk2.config.ConfigurationReloadListener;
import com.expanset.hk2.config.ConfiguredBoolean;
import com.expanset.hk2.config.ConfiguredFieldsBinder;
import com.expanset.hk2.config.ConfiguredInteger;
import com.expanset.hk2.config.ConfiguredLong;
import com.expanset.hk2.config.ConfiguredString;

import static org.junit.Assert.*;

public class ConfiguredFieldsTest {

	private ServiceLocator serviceLocator;

	@Before
	public void init() {
		final Configuration config = new AbstractFileConfiguration() {

			@Override
			public void load(Reader in) throws ConfigurationException {				
			}

			@Override
			public void save(Writer out) throws ConfigurationException {				
			}
			
			@Override
			public void load(String fileName) throws ConfigurationException {				
			}			
		};
		config.addProperty("testString", "string");
		config.addProperty("testBoolean", "true");
		config.addProperty("testInteger", "1");
		config.addProperty("testLong", "2");
		serviceLocator = ServiceLocatorUtilities.bind(new ConfiguredFieldsBinder(), new AbstractBinder() {
			@Override
			protected void configure() {
				bind(config).to(Configuration.class);
				addActiveDescriptor(ConfiguredFields.class);
				addActiveDescriptor(MutableConfiguredFields.class);
				addActiveDescriptor(ConfigChangeListener.class);
			}
		});
	}

	@After
	public void done() {
		ServiceLocatorFactory.getInstance().destroy(serviceLocator);
	}
	
	@Test
	public void fillingOfConfiguredFields() {
		final ConfiguredFields configuredFields = 
				serviceLocator.getService(ConfiguredFields.class);
		
		assertEquals("string", configuredFields.testString);
		assertEquals("string1", configuredFields.testStringDef);
		
		assertTrue(configuredFields.testBoolean);
		assertTrue(configuredFields.testBooleanDef);
		
		assertEquals(1, configuredFields.testInteger);
		assertEquals(1, configuredFields.testIntegerDef);

		assertEquals(2, configuredFields.testLong);
		assertEquals(2, configuredFields.testLongDef);
	}

	@Test
	public void propertyMutationSupport() {
		final MutableConfiguredFields configuredFields = 
				serviceLocator.getService(MutableConfiguredFields.class);
		
		assertEquals("string", configuredFields.testString.get());
		assertEquals("string1", configuredFields.testStringDef.get());
		
		assertEquals(new Boolean(true), configuredFields.testBoolean.get());
		assertEquals(new Boolean(true), configuredFields.testBooleanDef.get());
		
		assertEquals(new Integer(1), configuredFields.testInteger.get());
		assertEquals(new Integer(1), configuredFields.testIntegerDef.get());

		assertEquals(new Long(2), configuredFields.testLong.get());
		assertEquals(new Long(2), configuredFields.testLongDef.get());
		
		final Configuration config = 
				serviceLocator.getService(Configuration.class);
				
		config.setProperty("testString", "string2");
		config.setProperty("testBoolean", "false");
		config.setProperty("testInteger", "11");
		config.setProperty("testLong", "22");
		
		assertEquals("string2", configuredFields.testString.get());
		assertEquals("string1", configuredFields.testStringDef.get());
		
		assertEquals(new Boolean(false), configuredFields.testBoolean.get());
		assertEquals(new Boolean(true), configuredFields.testBooleanDef.get());
		
		assertEquals(new Integer(11), configuredFields.testInteger.get());
		assertEquals(new Integer(1), configuredFields.testIntegerDef.get());

		assertEquals(new Long(22), configuredFields.testLong.get());
		assertEquals(new Long(2), configuredFields.testLongDef.get());		
	}
	
	@Test
	public void configurationChangeListening() 
			throws ConfigurationException {
		final ConfigChangeListener listener = 
				serviceLocator.getService(ConfigChangeListener.class);
		final AbstractFileConfiguration config = 
				(AbstractFileConfiguration)serviceLocator.getService(Configuration.class);
			
		config.refresh();
	
		assertTrue(listener.reloaded);
		
		listener.reloaded = false;
		
		@SuppressWarnings("unchecked")
		final ActiveDescriptor<ConfigChangeListener> descriptor = 
				(ActiveDescriptor<ConfigChangeListener>)serviceLocator.getBestDescriptor(
						(Descriptor d) -> StringUtils.equals(d.getImplementation(), ConfigChangeListener.class.getName()));
		descriptor.dispose(listener);
		
		config.refresh();
		
		assertFalse(listener.reloaded);
	}
	
	@Service
	@Contract
	@PerLookup
	private static class ConfiguredFields {
		
		@ConfiguredString("testString")
		private String testString;

		@ConfiguredString(value="testString1", def="string1")
		private String testStringDef;
		
		@ConfiguredBoolean("testBoolean")
		private boolean testBoolean;

		@ConfiguredBoolean(value="testBoolean1", def=true)
		private boolean testBooleanDef;		

		@ConfiguredInteger("testInteger")
		private int testInteger;

		@ConfiguredInteger(value="testInteger1", def=1)
		private int testIntegerDef;
		
		@ConfiguredLong("testLong")
		private long testLong;

		@ConfiguredLong(value="testLong1", def=2)
		private long testLongDef;		
	}

	@Service
	@Contract
	@PerLookup
	private static class MutableConfiguredFields {
		
		@ConfiguredString("testString")
		private Supplier<String> testString;

		@ConfiguredString(value="testString1", def="string1")
		private Supplier<String> testStringDef;
		
		@ConfiguredBoolean("testBoolean")
		private Supplier<Boolean> testBoolean;

		@ConfiguredBoolean(value="testBoolean1", def=true)
		private Supplier<Boolean> testBooleanDef;		

		@ConfiguredInteger("testInteger")
		private Supplier<Integer> testInteger;

		@ConfiguredInteger(value="testInteger1", def=1)
		private Supplier<Integer> testIntegerDef;
		
		@ConfiguredLong("testLong")
		private Supplier<Long> testLong;

		@ConfiguredLong(value="testLong1", def=2)
		private Supplier<Long> testLongDef;		
	}
	
	@Service
	@Contract
	@PerLookup
	private static class ConfigChangeListener implements ConfigurationReloadListener {

		public boolean reloaded;
		
		@Override
		public void configurationReloaded() {
			reloaded = true;
		}
	}
}
