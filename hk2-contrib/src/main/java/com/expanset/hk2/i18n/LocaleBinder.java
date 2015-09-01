package com.expanset.hk2.i18n;

import java.util.Locale;

import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

/**
 * Bind {@link Locale} and {@link LocaleManager}.
 */
public class LocaleBinder extends AbstractBinder {

	@Override
	protected void configure() {
		bindFactory(LocaleFactory.class)
			.to(Locale.class)
			.in(PerLookup.class);
		
		bindLocaleManager();
	}
		
	protected void bindLocaleManager() {
		addActiveDescriptor(ThreadScopeLocaleManager.class);
	}	
}
