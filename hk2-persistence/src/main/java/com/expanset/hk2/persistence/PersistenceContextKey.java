package com.expanset.hk2.persistence;

import java.util.Objects;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.Validate;

/**
 * Persistence context key for using to create or search persistence contexts.
 */
public class PersistenceContextKey {
	
	protected final PersistenceContextFactoryKey factoryKey;

	/**
	 * Default constructor.
	 */
	public PersistenceContextKey() {
		this.factoryKey = new PersistenceContextFactoryKey();
	}
	
	/**
	 * @param factoryKey Persistence context factory key to search or create factories for the persistence contexts.
	 */
	public PersistenceContextKey(@Nonnull PersistenceContextFactoryKey factoryKey) {
		Validate.notNull(factoryKey, "factoryKey");
		
		this.factoryKey = factoryKey;
	}

	/**
	 * @return Persistence context factory key to search or create factories for the persistence contexts.
	 */
	public PersistenceContextFactoryKey getFactoryKey() {
		return factoryKey;
	}
	
	/**
	 * Create new key instance with other factory name.
	 * @param newFactoryName New factory name for existing key.
	 * @return New key instance with other factory name.
	 */
	public PersistenceContextKey clone(String newFactoryName) {
		return new PersistenceContextKey(factoryKey.clone(newFactoryName));
	}

	@Override
	public int hashCode() {
		return factoryKey.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		PersistenceContextKey other = (PersistenceContextKey) obj;
		return Objects.equals(factoryKey, other.factoryKey);
	}	
}
