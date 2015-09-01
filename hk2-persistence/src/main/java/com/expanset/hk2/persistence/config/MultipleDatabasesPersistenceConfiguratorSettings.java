package com.expanset.hk2.persistence.config;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jvnet.hk2.annotations.Service;

import com.expanset.common.ConfigurationUtils;

/**
 * Implementation of {@link PersistenceConfiguratorSettings} for multi database environment.
 */
@Service
public class MultipleDatabasesPersistenceConfiguratorSettings extends PersistenceConfiguratorSettings {

	protected final String configPrefixesProperty;
	
	protected final String configDefaultPrefixProperty;
	
	/**
	 * @param configPrefixesProperty Configuration file property that contains a list of database settings prefixes.
	 * @param configDefaultPrefixProperty Configuration file property that contains a name of default database prefix.
	 */
	public MultipleDatabasesPersistenceConfiguratorSettings(
			@Nonnull String configPrefixesProperty,
			@Nullable String configDefaultPrefixProperty) {	
		Validate.notEmpty(configPrefixesProperty, "configPrefixesProperty");
				
		this.configPrefixesProperty = configPrefixesProperty;
		this.configDefaultPrefixProperty = configDefaultPrefixProperty;
	}
	
	@Override
	public Map<String, Map<String, String>> getConfiguration(@Nonnull Configuration config) {
		Validate.notNull(config, "config");
		
		final Map<String, Map<String, String>> result = new HashMap<>();
		for(String configPrefix : config.getStringArray(configPrefixesProperty)) {
			Map<String, String> settings = ConfigurationUtils.getMap(config.subset(configPrefix));
			if(configDefaultPrefixProperty == null && result.isEmpty()) {
				// Put settings to empty factory name, that cat be used as default.
				result.put(StringUtils.EMPTY, settings);
			} else if(configDefaultPrefixProperty != null && StringUtils.equals(configPrefix, configDefaultPrefixProperty)) {
				// Put settings to empty factory name, that cat be used as default.
				result.put(StringUtils.EMPTY, settings);				
			}
			result.put(configPrefix, settings);
		}

		return result;
	}
}
