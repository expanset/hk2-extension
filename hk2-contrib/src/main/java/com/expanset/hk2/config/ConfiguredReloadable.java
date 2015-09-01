package com.expanset.hk2.config;

import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.ThreadSafe;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.Validate;

/**
 * Type for the field that support retrieving values from the configuration at the access moment.
 * <p>Can be used for value retrieving which can be changed while the program is working.</p>  
 * @param <T> Field value type.
 */
@ThreadSafe
public class ConfiguredReloadable<T> implements Supplier<T> {
	
	protected final String name;

	protected final T defaultValue;
	
	protected final boolean required;

	protected final Configuration config;
	
	/**
	 * @param config Access to configuration.
	 * @param name Key to property value in configuration.
	 * @param defaultValue Default value, if property value is not found in the configuration.
	 * @param required Required value flag.
	 */
	public ConfiguredReloadable(
			@Nonnull Configuration config, 
			@Nonnull String name, 
			T defaultValue,
			boolean required) {
		Validate.notNull(config, "config");
		Validate.notEmpty(name, "name");
		
		this.config = config;
		this.name = name;
		this.defaultValue = defaultValue;
		this.required = required;
	}
	
	/**
	 * Returns value from the configuration file.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public T get() {
		if(defaultValue instanceof Integer) {
			return required ? (T)Integer.valueOf(config.getInt(name)) : (T)config.getInteger(name, (Integer)defaultValue);	
		} else if(defaultValue instanceof Long) {
			return required ? (T)Long.valueOf(config.getLong(name)) : (T)config.getLong(name, (Long)defaultValue);	
		} else if(defaultValue instanceof Boolean) {
			return required ? (T)Boolean.valueOf(config.getBoolean(name)) : (T)config.getBoolean(name, (Boolean)defaultValue);	
		} else if(defaultValue instanceof String) {
			return required ? (T)config.getString(name) : (T)config.getString(name, (String)defaultValue);	
		} else {
			throw new IllegalStateException(String.format("Unknown type %s", defaultValue.getClass()));
		}
	}
}
