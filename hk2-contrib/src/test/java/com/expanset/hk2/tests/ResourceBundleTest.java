package com.expanset.hk2.tests;

import java.util.Locale;
import java.util.ResourceBundle;

import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.api.ServiceLocatorFactory;
import org.glassfish.hk2.utilities.ServiceLocatorUtilities;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.expanset.hk2.i18n.PropertyResourceBundleBinder;
import com.expanset.hk2.i18n.ResourceBundleProvider;

import static org.junit.Assert.*;

public class ResourceBundleTest {
	
	private ServiceLocator serviceLocator;
	
	@Before
	public void init() {
		serviceLocator = ServiceLocatorUtilities.bind(
				new PropertyResourceBundleBinder("src/test/files/strings", 1000, "utf-8"), 
				new AbstractBinder() {
			@Override
			protected void configure() {

			}
		});		
	}

	@After
	public void done() {
		ServiceLocatorFactory.getInstance().destroy(serviceLocator);
	}	
	
	@Test
	public void loadLocalisedResources() {
		final ResourceBundleProvider provider = 
				serviceLocator.getService(ResourceBundleProvider.class);

		final ResourceBundle bundle = provider.get(Locale.ENGLISH);
		assertNotNull(bundle);
		
		final String testString = bundle.getString("testString");
		assertEquals("string", testString);
	}
}
