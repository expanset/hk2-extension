package com.expanset.hk2.persistence;

import javax.annotation.Nonnull;
import org.jvnet.hk2.annotations.Contract;

import com.expanset.common.Wrapper;

/**
 * Holder for persistence context factory.
 */
@Contract
public interface PersistenceContextFactoryWrapper extends Wrapper, AutoCloseable {

	/**
	 * Creates new persistence context.
	 * @param key Persistence context data to create new persistence context.
	 * @return New persistence context.
	 */
	PersistenceContextWrapper create(@Nonnull PersistenceContextKey key);
}
