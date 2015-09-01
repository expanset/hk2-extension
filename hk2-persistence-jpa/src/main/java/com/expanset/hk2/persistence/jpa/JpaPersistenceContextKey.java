package com.expanset.hk2.persistence.jpa;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.expanset.hk2.persistence.PersistenceContextKey;

/**
 * Implementation of {@link PersistenceContextKey} for JPA engine.
 */
public class JpaPersistenceContextKey extends PersistenceContextKey {

	protected final Map<String, String> properties; 

	/**
	 * @param unitName JPA unit name from the injection annotation or default name.
	 */
	public JpaPersistenceContextKey(@Nonnull String unitName) {
		super(new JpaPersistenceContextFactoryKey(unitName));
		
		this.properties = null;
	}
	
	/**
	 * @param factoryName Name of the persistence factory settings.
	 * @param unitName JPA unit name from the injection annotation or default name.
	 * @param properties Persistence manager additional properties.
	 */
	public JpaPersistenceContextKey(
			@Nonnull String factoryName, 
			@Nonnull String unitName,
			@Nullable Map<String, String> properties) {
		super(new JpaPersistenceContextFactoryKey(factoryName, unitName));
		
		if(properties != null) {
			this.properties = new HashMap<>(properties);
		} else {
			this.properties = null;
		}
	}
	
	/**
	 * @return Persistence manager additional properties.
	 */
	public Map<String, String> getProperties() {
		return properties != null ? Collections.unmodifiableMap(properties) : Collections.emptyMap();
	}

	@Override
	public PersistenceContextKey clone(String newFactoryName) {
		return new JpaPersistenceContextKey(
				newFactoryName,
				((JpaPersistenceContextFactoryKey)factoryKey).getUnitName(),
				properties);
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((properties == null) ? 0 : properties.hashCode());
		
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj) || getClass() != obj.getClass()) {
			return false;
		}
		JpaPersistenceContextKey other = (JpaPersistenceContextKey) obj;
		return Objects.equals(properties, other.properties);
	}
}
