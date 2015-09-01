package com.expanset.hk2.persistence.config;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.configuration.Configuration;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.Service;

/**
 * Register supporting of multi databases configuration.
 * Information about how to configure multi database environment can be read in the
 * {@link com.expanset.hk2.persistence.PersistenceSessionManager} description.
 */
@Service
@Contract
public class PersistenceConfiguratorSettings {
	
	protected Map<String, String> commonProperties;

	/**
	 * @param config External configuration.
	 * @return Persistence factories properties.
	 */
	public Map<String, Map<String, String>> getConfiguration(@Nonnull Configuration config) {
		return null;
	}
	
	/**
	 * @param config External configuration.
	 * @return Additional properties for the persistence engine.
	 */
	public Map<String, String> getCommonProperties(@Nullable Configuration config) {
		return commonProperties;
	}

	public void setCommonProperties(Map<String, String> commonProperties) {
		this.commonProperties = commonProperties;
	}
}
