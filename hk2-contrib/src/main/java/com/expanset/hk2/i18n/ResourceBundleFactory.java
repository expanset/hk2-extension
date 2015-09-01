package com.expanset.hk2.i18n;

import java.util.Locale;
import java.util.ResourceBundle;

import javax.inject.Inject;
import javax.inject.Provider;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.PerLookup;
import org.jvnet.hk2.annotations.Optional;
import org.jvnet.hk2.annotations.Service;

/**
 * Factory for {@link ResourceBundle}.
 * <p>{@link ResourceBundle} retrieved from configured {@link ResourceBundleProvider}.</p>
 */
@Service
public class ResourceBundleFactory implements Factory<ResourceBundle> {

	@Inject
	@Optional
	protected Provider<Locale> localeProvider;
	
	@Inject	
	protected Provider<ResourceBundleProvider> resourceBundleProvider;
	
	@Override
	@PerLookup
	public ResourceBundle provide() {		
		Locale locale = localeProvider != null ? localeProvider.get() : null;		
		if(locale == null) {
			locale = Locale.getDefault();
		}
		
		final ResourceBundle bundle = resourceBundleProvider.get().get(locale);
		return bundle;
	}

	@Override
	public void dispose(ResourceBundle instance) {
	}
}
