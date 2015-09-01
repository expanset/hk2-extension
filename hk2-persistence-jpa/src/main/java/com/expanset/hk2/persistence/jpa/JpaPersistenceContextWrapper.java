package com.expanset.hk2.persistence.jpa;

import java.sql.Connection;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.transaction.Transaction;

import org.apache.commons.lang.Validate;

import com.expanset.hk2.persistence.ConnectionProvider;
import com.expanset.hk2.persistence.PersistenceContextWrapper;

/**
 * Implementation of {@link PersistenceContextWrapper} for JPA engine.
 */
public class JpaPersistenceContextWrapper implements PersistenceContextWrapper {

	protected final EntityManager entityManager;

	/**
	 * @param entityManager Wrapped JPA entity manager.
	 */
	public JpaPersistenceContextWrapper(@Nonnull EntityManager entityManager) {
		Validate.notNull(entityManager, "entityManager");
		
		this.entityManager = entityManager;
	}

	@Override
	public Transaction beginTransaction() 
			throws Exception {
		final EntityTransaction transaction = entityManager.getTransaction();
		return new JpaTransaction(transaction);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> T unwrap(Class<T> iface) 
			throws Exception {
		Validate.notNull(iface, "iface");
		
		if(iface.equals(EntityManager.class)) {
			return (T) entityManager;
		}
		if(iface.equals(ConnectionProvider.class)) {
			return (T) new ConnectionProvider() {
				
				final EntityTransaction transaction = entityManager.getTransaction();
				
				@Override
				public Connection provide() {
					transaction.begin();
					return entityManager.unwrap(Connection.class);
				}

				@Override
				public void close() throws Exception {
					transaction.commit();
				}
			};
		}
		return null;
	}
	
	@Override
	public boolean isWrapperFor(Class<?> iface) {
		Validate.notNull(iface, "iface");
		
		return iface.equals(EntityManager.class) || 
				iface.equals(ConnectionProvider.class);
	}

	@Override
	public void close() 
			throws Exception {
		entityManager.close();
	}
}
