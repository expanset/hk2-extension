package com.expanset.hk2.i18n;

import java.util.ResourceBundle;

import org.glassfish.hk2.api.PerLookup;

/**
 * Base class for resource bundle binders.
 */
public class ResourceBundleBinder extends LocaleBinder {

	@Override
	protected void configure() {
		super.configure();
				
		bindFactory(ResourceBundleFactory.class)
			.to(ResourceBundle.class)
			.in(PerLookup.class);
	}
}
