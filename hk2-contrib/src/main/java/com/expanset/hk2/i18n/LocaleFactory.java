package com.expanset.hk2.i18n;

import java.util.Locale;
import javax.inject.Inject;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Service;

/**
 * Helps to inject {@link Locale}.
 * <p>{@link Locale} retrieved from configured {@link LocaleManager}.</p>
 */
@Service
public class LocaleFactory implements Factory<Locale> {
	
	@Inject
	protected LocaleManager localeManager;

	@Override
	@PerLookup
	public Locale provide() {
		return localeManager.getCurrentLocale();
	}

	@Override
	public void dispose(Locale instance) {
	}
}
