package com.expanset.hk2.persistence.jpa;

import javax.annotation.Nonnull;
import javax.persistence.EntityManagerFactory;

import org.apache.commons.lang.Validate;

import com.expanset.hk2.persistence.PersistenceContextFactoryWrapper;
import com.expanset.hk2.persistence.PersistenceContextKey;
import com.expanset.hk2.persistence.PersistenceContextWrapper;

/**
 * Implementation of {@link PersistenceContextFactoryWrapper} for JPA engine.
 */
public class JpaPersistenceContextFactoryWrapper implements PersistenceContextFactoryWrapper {

	protected final EntityManagerFactory entityManagerFactory;
	
	/**
	 * @param entityManagerFactory Wrapped JPA entity manager factory.
	 */
	public JpaPersistenceContextFactoryWrapper(@Nonnull EntityManagerFactory entityManagerFactory) {
		Validate.notNull(entityManagerFactory, "entityManagerFactory");

		this.entityManagerFactory = entityManagerFactory;
	}

	@Override
	public PersistenceContextWrapper create(PersistenceContextKey key) {
		Validate.notNull(key, "key");

		final JpaPersistenceContextWrapper result;
		
		final JpaPersistenceContextKey jpaKey = (JpaPersistenceContextKey)key;
		if(jpaKey.getProperties() == null) {
			result = new JpaPersistenceContextWrapper(entityManagerFactory.createEntityManager());
		} else {
			result = new JpaPersistenceContextWrapper(entityManagerFactory.createEntityManager(jpaKey.getProperties()));
		}
		
		return result;
	}
	
	@Override
	public void close() 
			throws Exception {
		entityManagerFactory.close();
	}
}
