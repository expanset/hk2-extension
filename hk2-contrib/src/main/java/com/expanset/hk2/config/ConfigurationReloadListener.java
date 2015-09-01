package com.expanset.hk2.config;

import org.jvnet.hk2.annotations.Contract;

/**
 * Object contract for listening configuration file changes.
 */
@Contract
public interface ConfigurationReloadListener {

	/**
	 * Called while configuration file is changed.
	 */
	void configurationReloaded();
}
