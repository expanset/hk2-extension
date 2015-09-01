package com.expanset.hk2.persistence.jpa;

import javax.annotation.Nonnull;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang3.StringUtils;

import com.expanset.hk2.persistence.PersistenceContextFactoryKey;

/**
 * Implementation of {@link PersistenceContextFactoryKey} for JPA engine.
 */
public class JpaPersistenceContextFactoryKey extends PersistenceContextFactoryKey {

	protected final String unitName;
	
	/**
	 * @param unitName JPA unit name from the injection annotation or default name.
	 */
	public JpaPersistenceContextFactoryKey(@Nonnull String unitName) {
		super();
		
		Validate.notEmpty(unitName, "unitName");

		this.unitName = unitName;
	}	
	
	/**
	 * @param factoryName Name of the persistence factory settings.
	 * @param unitName JPA unit name from the injection annotation or default name.
	 */
	public JpaPersistenceContextFactoryKey(@Nonnull String factoryName, @Nonnull String unitName) {
		super(factoryName);
		
		Validate.notEmpty(unitName, "unitName");

		this.unitName = unitName;
	}
	
	/**
	 * @return JPA unit name from the injection annotation or default name.
	 */
	public String getUnitName() {
		return unitName;
	}

	@Override
	public PersistenceContextFactoryKey clone(String newFactoryName) {
		return new JpaPersistenceContextFactoryKey(newFactoryName, unitName);
	}	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((unitName == null) ? 0 : unitName.hashCode());
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
		JpaPersistenceContextFactoryKey other = (JpaPersistenceContextFactoryKey) obj;
		return StringUtils.equals(unitName, other.unitName);
	}
}
