package com.expanset.hk2.persistence.ormlite;

import javax.annotation.Nonnull;

import org.apache.commons.lang.Validate;

import com.expanset.hk2.persistence.PersistenceContextFactoryWrapper;
import com.expanset.hk2.persistence.PersistenceContextKey;
import com.expanset.hk2.persistence.PersistenceContextWrapper;
import com.j256.ormlite.support.ConnectionSource;

/**
 * Implementation of {@link PersistenceContextFactoryWrapper} for Ormlite persistence engine.
 */
public class OrmlitePersistenceContextFactoryWrapper implements PersistenceContextFactoryWrapper {

	protected final ConnectionSource connectionSource;

	/**
	 * @param connectionSource Wrapped connection source.
	 */
	public OrmlitePersistenceContextFactoryWrapper(@Nonnull ConnectionSource connectionSource) {
		Validate.notNull(connectionSource, "connectionSource");
		
		this.connectionSource = connectionSource;
	}

	@Override
	public PersistenceContextWrapper create(PersistenceContextKey key) {
		Validate.notNull(key, "key");
		
		return new OrmlitePersistenceContextWrapper(connectionSource);
	}

	@Override
	public void close() 
			throws Exception {
		connectionSource.close();
	}
}
