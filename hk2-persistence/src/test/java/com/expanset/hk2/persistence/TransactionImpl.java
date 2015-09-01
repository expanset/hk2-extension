package com.expanset.hk2.persistence;

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.xa.XAResource;

public class TransactionImpl implements Transaction, AutoCloseable {

	private int status = Status.STATUS_ACTIVE;
	
	private final boolean throwOnCommit;
	
	private final boolean throwOnRollback;
	
	public TransactionImpl() {
		throwOnCommit = false;
		throwOnRollback = false;
	}

	public TransactionImpl(boolean throwOnCommit, boolean throwOnRollback) {
		this.throwOnCommit = throwOnCommit;
		this.throwOnRollback = throwOnRollback;			
	}
	
	@Override
	public void commit() throws 
			RollbackException, 
			HeuristicMixedException,
			HeuristicRollbackException, 
			SecurityException,
			IllegalStateException, 
			SystemException {
		if(status == Status.STATUS_MARKED_ROLLBACK) {
			throw new RollbackException();
		}
		if(throwOnCommit) {
			throw new SystemException();
		}
		status = Status.STATUS_COMMITTED;
	}

	@Override
	public boolean delistResource(XAResource xaRes, int flag) 
			throws IllegalStateException, SystemException {
		return false;
	}

	@Override
	public boolean enlistResource(XAResource xaRes) 
			throws RollbackException, IllegalStateException, SystemException {
		return false;
	}

	@Override
	public int getStatus() 
			throws SystemException {
		return status;
	}

	@Override
	public void registerSynchronization(Synchronization sync)
			throws RollbackException, IllegalStateException, SystemException {
	}

	@Override
	public void rollback() 
			throws IllegalStateException, SystemException {
		if(status == Status.STATUS_ROLLEDBACK || status == Status.STATUS_COMMITTED) {
			return;
		}
		if(throwOnRollback) {
			throw new SystemException();
		}
		
		status = Status.STATUS_ROLLEDBACK;
	}

	@Override
	public void setRollbackOnly() 
			throws IllegalStateException, SystemException {
		status = Status.STATUS_MARKED_ROLLBACK;
	}

	@Override
	public void close() 
			throws Exception {
	}
}
