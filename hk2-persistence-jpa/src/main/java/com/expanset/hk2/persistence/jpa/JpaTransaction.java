package com.expanset.hk2.persistence.jpa;

import javax.annotation.Nonnull;
import javax.persistence.EntityTransaction;
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

/**
 * Implementation of {@link Transaction} for JPA engine.
 */
public class JpaTransaction implements Transaction, AutoCloseable {

	protected int status = Status.STATUS_ACTIVE;
	
	protected final EntityTransaction transaction;
	
	/**
	 * @param transaction Started JPA transaction. 
	 */
	public JpaTransaction(@Nonnull EntityTransaction transaction) {
		Validate.notNull(transaction, "transaction");
		
		this.transaction = transaction;	
		
		transaction.begin();
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
		transaction.setRollbackOnly();
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
			transaction.commit();
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
			transaction.rollback();
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
	}
}
