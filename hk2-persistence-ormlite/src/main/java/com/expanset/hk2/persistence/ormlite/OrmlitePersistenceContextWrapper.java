package com.expanset.hk2.persistence.ormlite;

import java.sql.Connection;

import javax.annotation.Nonnull;
import javax.transaction.Transaction;

import org.apache.commons.lang.Validate;

import com.expanset.hk2.persistence.ConnectionProvider;
import com.expanset.hk2.persistence.PersistenceContextWrapper;
import com.j256.ormlite.jdbc.JdbcDatabaseConnection;
import com.j256.ormlite.support.ConnectionSource;

/**
 * Implementation of {@link PersistenceContextWrapper} for Ormlite persistence engine.
 */
public class OrmlitePersistenceContextWrapper implements PersistenceContextWrapper {

	protected final ConnectionSource connectionSource;

	/**
	 * @param connectionSource Wrapped connection source.
	 */
	public OrmlitePersistenceContextWrapper(@Nonnull ConnectionSource connectionSource) {
		Validate.notNull(connectionSource, "connectionSource");
		
		this.connectionSource = connectionSource;
	}
	
	@Override
	public Transaction beginTransaction() 
			throws Exception {
		return new OrmliteTransaction(connectionSource);
	}	
	
	@Override
	@SuppressWarnings("unchecked")
	public <T> T unwrap(Class<T> iface) 
			throws Exception {
		Validate.notNull(iface, "iface");
			
		if(iface.equals(ConnectionSource.class)) {
			return (T)connectionSource;
		}
		if(iface.equals(ConnectionProvider.class)) {
			return (T) new ConnectionProvider () {
				private final JdbcDatabaseConnection connection = 
						(JdbcDatabaseConnection)connectionSource.getReadWriteConnection();
				@Override
				public Connection provide() {
					return connection.getInternalConnection();
				}
				@Override
				public void close() throws Exception {
					connectionSource.releaseConnection(connection);
				}
			};
		}
		return null;
	}
	
	@Override
	public boolean isWrapperFor(Class<?> iface) {
		Validate.notNull(iface, "iface");
		
		return iface.equals(ConnectionSource.class) || 
				iface.equals(ConnectionProvider.class);
	}	

	@Override
	public void close() 
			throws Exception {
	}
}
