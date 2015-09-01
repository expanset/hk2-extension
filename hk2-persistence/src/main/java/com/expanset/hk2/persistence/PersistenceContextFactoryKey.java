package com.expanset.hk2.persistence;

import javax.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;

/**
 * Persistence factory key for using to create or search factories. 
 */
public class PersistenceContextFactoryKey {

	protected final String factoryName;

	/**
	 * Default constructor.
	 */
	public PersistenceContextFactoryKey() {
		this.factoryName = StringUtils.EMPTY;
	}	
	
	/**
	 * @param factoryName Name of persistence factory.
	 */
	public PersistenceContextFactoryKey(@Nullable String factoryName) {	
		this.factoryName = factoryName != null ? factoryName : StringUtils.EMPTY;
	}
	
	/**
	 * @return Name of persistence factory.
	 */
	public String getFactoryName() {
		return factoryName;
	}

	/**
	 * Create new key instance with other factory name.
	 * @param newFactoryName New factory name for existing key.
	 * @return New key instance with other factory name.
	 */
	public PersistenceContextFactoryKey clone(String newFactoryName) {
		return new PersistenceContextFactoryKey(newFactoryName);
	}
	
	@Override
	public int hashCode() {
		return factoryName.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		PersistenceContextFactoryKey other = (PersistenceContextFactoryKey) obj;
		return StringUtils.equals(factoryName, other.factoryName);
	}
}
