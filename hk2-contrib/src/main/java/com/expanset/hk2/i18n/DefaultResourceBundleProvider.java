package com.expanset.hk2.i18n;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.ResourceBundle.Control;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.Validate;
import org.jvnet.hk2.annotations.Service;

/**
 * Implementation of {@link ResourceBundleProvider}, which loads {@link ResourceBundle} with 
 * configured {@link ResourceBundle.Control}.
 */
@Service
public class DefaultResourceBundleProvider implements ResourceBundleProvider {

	protected final String baseName;
		
	protected final Control resourceControl;

	/**
	 * @param baseName Resource file base name, without language suffix and file extension.
	 * @param resourceControl Configured {@link ResourceBundle.Control}.
	 */
	public DefaultResourceBundleProvider(@Nonnull String baseName, @Nonnull Control resourceControl) {
		Validate.notEmpty(baseName, "baseName");
		Validate.notNull(resourceControl, "resourceControl");

		this.baseName = baseName;
		this.resourceControl = resourceControl;
	}
	
	@Override
	public ResourceBundle get(@Nullable Locale locale) {
		if(locale == null) {
			locale = Locale.getDefault();
		}
		final ResourceBundle bundle = ResourceBundle.getBundle(baseName, locale, resourceControl);		
		return bundle;
	}
}
