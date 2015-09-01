package com.expanset.hk2.persistence.ormlite;

import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnull;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.Validate;

import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.support.DatabaseConnection;

/**
 * Implementation of {@link Transaction} for Ormlite persistence engine.
 */
public class OrmliteTransaction implements Transaction, AutoCloseable {

	protected int status = Status.STATUS_ACTIVE;
	
	protected final ConnectionSource connectionSource;
	
	protected final DatabaseConnection connection;
			
	protected final Savepoint savepoint;
	
	protected final boolean autoCommitAtStart;	
		
	protected final static String SAVE_POINT_PREFIX = "ORMLITE";
	
	protected final static AtomicInteger savepointCounter = new AtomicInteger();
	
	/**
	 * @param connectionSource {@link ConnectionSource} for transaction.
	 * @throws SQLException Connection error.
	 */
	public OrmliteTransaction(@Nonnull ConnectionSource connectionSource) 
			throws SQLException {
		Validate.notNull(connectionSource, "connectionSource");
		
		this.connectionSource = connectionSource;	
		this.connection = this.connectionSource.getReadWriteConnection();
		
		final boolean saved = this.connectionSource.saveSpecialConnection(connection);	
		if (saved || connectionSource.getDatabaseType().isNestedSavePointsSupported()) {
			if (connection.isAutoCommitSupported()) {
				autoCommitAtStart = connection.isAutoCommit();
				if (autoCommitAtStart) {
					connection.setAutoCommit(false);
				}
			} else {
				autoCommitAtStart = false;
			}
			savepoint = connection.setSavePoint(SAVE_POINT_PREFIX + savepointCounter.incrementAndGet());
		} else {
			savepoint = null;
			autoCommitAtStart = false;
		}		
	} 
	
	@Override
	public int getStatus() 
			throws SystemException {
		return status;
	}
	
	@Override
	public void setRollbackOnly() 
			throws IllegalStateException, SystemException {
		status = Status.STATUS_MARKED_ROLLBACK;
	}
	
	@Override
	public void registerSynchronization(Synchronization sync)
			throws RollbackException, IllegalStateException, SystemException {
		throw new NotImplementedException();
	}
	
	@Override
	public void commit() 
			throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException {
		if(status == Status.STATUS_MARKED_ROLLBACK) {
			throw new RollbackException();
		}
		if(status != Status.STATUS_ACTIVE) {
			throw new IllegalStateException();
		}
		
		status = Status.STATUS_COMMITTING;
		try {
			connection.commit(savepoint);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		} finally {
			status = Status.STATUS_COMMITTED;
		}
	}

	@Override
	public void rollback() 
			throws IllegalStateException, SystemException {
		if(status == Status.STATUS_ROLLEDBACK || status == Status.STATUS_COMMITTED) {
			return;
		}			
		
		status = Status.STATUS_ROLLING_BACK;
		try {
			try {
				connection.rollback(savepoint);
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		} finally {
			status = Status.STATUS_ROLLEDBACK;
		}
	}
	
	@Override
	public boolean enlistResource(XAResource xaRes) 
			throws RollbackException, IllegalStateException, SystemException {
		throw new NotImplementedException();
	}

	@Override
	public boolean delistResource(XAResource xaRes, int flag)
			throws IllegalStateException, SystemException {
		throw new NotImplementedException();
	}
	
	@Override
	public void close() 
			throws Exception {
		try {
			if (autoCommitAtStart) {
				connection.setAutoCommit(true);
			}			
		} finally {
			connectionSource.clearSpecialConnection(connection);
			connectionSource.releaseConnection(connection);
		}
	}
}
