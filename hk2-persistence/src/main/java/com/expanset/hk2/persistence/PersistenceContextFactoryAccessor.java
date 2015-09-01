package com.expanset.hk2.persistence;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.glassfish.hk2.api.PreDestroy;
import org.jvnet.hk2.annotations.Contract;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.expanset.common.errors.ExceptionAdapter;

/**
 * Service is intended for getting factories for persistence contexts creation. 
 * Each factory is named by a unique name which corresponds with a set of settings for this factory.
 */
@Service
@Contract
@ThreadSafe
public class PersistenceContextFactoryAccessor implements PreDestroy {

	@Inject
	protected PersistenceContextFactoryCreator factoryCreator;
	
	protected final Map<String, Map<String, String>> factoryProperties = new HashMap<>();
			
	protected Map<String, String> commonProperties = new HashMap<>();	

	protected final Map<PersistenceContextFactoryKey, PersistenceContextFactoryWrapper> factories = new HashMap<>();
	
	private final static Logger log = LoggerFactory.getLogger(PersistenceContextFactoryAccessor.class);
		
	/**
	 * Returns the persistence factory wrapper (cached or created). 
	 * @param key Factory unique key (based on factory name and other data in key).
	 * @return Persistence factory wrapper.
	 */
	public synchronized PersistenceContextFactoryWrapper getFactory(@Nonnull PersistenceContextFactoryKey key) {
		Validate.notNull(key, "key");

		PersistenceContextFactoryWrapper factory = factories.get(key);
		if(factory == null) {
			Map<String, String> properties = factoryProperties.get(key.getFactoryName());
			if(properties == null) {
				throw new IllegalStateException(String.format(
						"Factory properties with name %s not found. Properties must be registered before using", 
						StringUtils.isEmpty(key.getFactoryName()) ? "(default)" : key.getFactoryName()));
			}
			
			factory = factoryCreator.create(
					key, 
					Collections.unmodifiableMap(properties), 
					Collections.unmodifiableMap(commonProperties));
			factories.put(key, factory);
		}

		return factory;
	}

	/**
	 * Gets the property value.
	 * @param key Property key.
	 * @return Property value or null. 
	 */
	public synchronized String getCommonProperty(@Nonnull String key) {
		Validate.notNull(key, "key");
		
		return commonProperties.get(key);
	}

	/**
	 * Sets the property value, that can be used by persistence engines.
	 * @param key Property key.
	 * @param value Property value.
	 */
	public synchronized void setCommonProperty(
			@Nonnull String key, 
			@Nullable String value) {
		Validate.notNull(key, "key");
		
		commonProperties.put(key, value);
	}

	/**
	 * Returns persistence factory properties, found for the specified factory name.
	 * @param factoryName Factory name to search they properties.
	 * @return Factory properties or null if not found.
	 */
	public synchronized Map<String, String> getFactoryProperties(@Nonnull String factoryName) {
		Validate.notNull(factoryName, "factoryName");

		final Map<String, String> properties = factoryProperties.get(factoryName);
		if(properties == null) {
			return null;
		}
		
		return Collections.unmodifiableMap(properties);
	}
	
	/**
	 * Sets named persistence the factory properties.
	 * @param factoryName Name of the factory.
	 * @param properties New properties for the factory.
	 */
	public synchronized void setFactoryProperties(
			@Nonnull String factoryName, 
			@Nonnull Map<String, String> properties) {
		Validate.notNull(factoryName, "factoryName");
		Validate.notNull(properties, "properties");

		closeCachedFactory(factoryName);
		factoryProperties.put(factoryName, new HashMap<>(properties));
	}
	
	/**
	 * Removes persistence factory by the factory name.
	 * @param factoryName Name of unnecessary factory.
	 */
	public synchronized void removeFactoryProperties(@Nonnull String factoryName) {
		Validate.notNull(factoryName, "factoryName");
		
		closeCachedFactory(factoryName);
		factoryProperties.remove(factoryName);
	}

	/**
	 * Resets settings of all persistence factories (configuration reloaded in example).
	 * @param newProperties Collection of the factory names and their properties.
	 * @param commonProperties Properties, that can be used by persistence engines.
	 */
	public synchronized void resetFactoriesProperties(
			@Nullable Map<String, Map<String, String>> newProperties, 
			@Nullable Map<String, String> commonProperties) {
		if(newProperties != null) {
			factoryProperties.clear();
			for(Entry<String, Map<String, String>> entry : newProperties.entrySet()) {
				factoryProperties.put(entry.getKey(), new HashMap<>(entry.getValue()));
			}
		}
		
		if(commonProperties != null) {
			this.commonProperties.clear();
			this.commonProperties.putAll(commonProperties);
		}

		for(PersistenceContextFactoryWrapper factory : factories.values()) {
			ExceptionAdapter.closeQuitely(factory, log);
		}		
		factories.clear();	
	}
	
	/**
	 * Removes factory from cache.
	 * @param factoryName Factory to remove from cache.
	 */
	public synchronized void evict(@Nonnull String factoryName) {
		Validate.notNull(factoryName, "factoryName");
		
		closeCachedFactory(factoryName);
	}
	
	/**
	 * Removes all cached factories.
	 */
	public synchronized void evictAll() {
		for(PersistenceContextFactoryWrapper factory : factories.values()) {
			ExceptionAdapter.closeQuitely(factory, log);
		}		
		factories.clear();
	}	
	
	@Override
	@javax.annotation.PreDestroy
	public void preDestroy() {
		evictAll();
	}

	private void closeCachedFactory(String factoryName) {
		for (Iterator<Entry<PersistenceContextFactoryKey, PersistenceContextFactoryWrapper>> it = 
				factories.entrySet().iterator(); it.hasNext(); ) {
		    Entry<PersistenceContextFactoryKey, PersistenceContextFactoryWrapper> entry = it.next();
			if(StringUtils.equals(entry.getKey().getFactoryName(), factoryName)) {
				ExceptionAdapter.closeQuitely(entry.getValue(), log);
				it.remove();
			}
		}
	}
}
