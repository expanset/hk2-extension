package com.expanset.hk2.i18n;

import java.util.Locale;
import java.util.ResourceBundle;

import org.jvnet.hk2.annotations.Contract;

/**
 * Contract for services that implements access to {@link ResourceBundle} based on specified locale. 
 */
@Contract
public interface ResourceBundleProvider {

	/**
	 * @param locale Locale for localized resources.
	 * @return {@link ResourceBundle} based on specified locale.
	 */
	public ResourceBundle get(Locale locale);
}
