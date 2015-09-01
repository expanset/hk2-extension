package com.expanset.hk2.persistence.transactions;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAResource;

import org.apache.commons.lang.NotImplementedException;
import org.jvnet.hk2.annotations.Service;

import com.expanset.common.errors.ExceptionAdapter;
import com.expanset.hk2.persistence.PersistenceContextWrapper;
import com.expanset.hk2.persistence.PersistenceSession;
import com.expanset.hk2.persistence.PersistenceSessionManager;

/**
 * The simple transaction manager for the current persistence context session.
 * Transactions are launched sequentially for all persistence contexts in the current session and thread.
 */
@Service
@ThreadSafe
public class LocalTransactionManager implements TransactionManager {

	@Inject
	protected PersistenceSessionManager persistenceSessionManager;
	
	protected ThreadLocal<CompositeTransaction> transactionHolder = new ThreadLocal<CompositeTransaction>();
	
	@Override
	public int getStatus() 
			throws SystemException {
		final CompositeTransaction currenTransaction = transactionHolder.get();
		if(currenTransaction != null) {
			return currenTransaction.getStatus();
		}
		
		return Status.STATUS_NO_TRANSACTION;
	}

	@Override
	public Transaction getTransaction() 
			throws SystemException {
		return transactionHolder.get();
	}

	@Override
	public void setRollbackOnly() 
			throws IllegalStateException, SystemException {
		final CompositeTransaction currenTransaction = transactionHolder.get();
		if(currenTransaction != null) {
			currenTransaction.setRollbackOnly();
		}
	}

	@Override
	public void setTransactionTimeout(int seconds) 
			throws SystemException {
		throw new NotImplementedException();
	}

	@Override
	public void begin() 
			throws NotSupportedException, SystemException {
		if(transactionHolder.get() != null) {
			return;
		}		
		
		final PersistenceSession persistenceSession = persistenceSessionManager.getCurrentSession();
		if(persistenceSession == null) {
			throw new IllegalStateException("Persistence context scope not initialized");
		}
		
		final List<PersistenceContextWrapper> persistenceContexts = 
				persistenceSession.getAllPersistenceContextsInCurrentThread();
		
		final List<Transaction> transactions = new ArrayList<>(persistenceContexts.size());
		for(PersistenceContextWrapper wrapper : persistenceContexts) {
			try {
				transactions.add(wrapper.beginTransaction());
			} catch (Throwable e) {
				ExceptionAdapter.run(e, transactions, 
						t -> ExceptionAdapter.run(() -> t.rollback()),  
						t -> ExceptionAdapter.close(t));
			}
		}
		
		transactionHolder.set(new CompositeTransaction(transactions));
	}

	@Override
	public void commit() 
			throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException {
		final CompositeTransaction currentTransaction = transactionHolder.get();
		if(currentTransaction != null) {
			try {
				currentTransaction.commit();
			} catch (Throwable e) {
				currentTransaction.rollback();				
				throw e;
			} finally {
				transactionHolder.remove();
				ExceptionAdapter.run(() -> currentTransaction.close());
			}
		}
	}
	
	@Override
	public void rollback() 
			throws IllegalStateException, SecurityException, SystemException {
		final CompositeTransaction currentTransaction = transactionHolder.get();
		if(currentTransaction != null) {
			try {
				currentTransaction.rollback();
			} finally {
				transactionHolder.remove();
				ExceptionAdapter.run(() -> currentTransaction.close());
			}
		}
	}
	
	@Override
	public Transaction suspend() 
			throws SystemException {
		throw new NotImplementedException();
	}

	@Override
	public void resume(Transaction tobj) 
			throws InvalidTransactionException, IllegalStateException, SystemException {
		throw new NotImplementedException();
	}
	
	protected class CompositeTransaction implements Transaction, AutoCloseable {

		private final List<Transaction> transactions;
		
		private int status = Status.STATUS_ACTIVE;
				
		public CompositeTransaction(List<Transaction> transactions) {
			this.transactions = transactions;
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
			ExceptionAdapter.run(transactions, 
					(Transaction t) -> ExceptionAdapter.run(() -> t.setRollbackOnly()));
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
				ExceptionAdapter.run(transactions, 
						(Transaction t) -> ExceptionAdapter.run(() -> t.commit()));
			} catch (Throwable e) {
				status = Status.STATUS_MARKED_ROLLBACK;
						
				throw e;
			}

			status = Status.STATUS_COMMITTED;
		}
		
		@Override
		public void rollback() 
				throws IllegalStateException, SystemException {
			if(status == Status.STATUS_ROLLEDBACK || status == Status.STATUS_COMMITTED) {
				return;
			}			
			
			status = Status.STATUS_ROLLING_BACK;
			try {
				ExceptionAdapter.run(transactions, 
						(Transaction t) -> ExceptionAdapter.run(() -> t.rollback()));
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
			if(status == Status.STATUS_ACTIVE) {
				status = Status.STATUS_UNKNOWN;
			}

			ExceptionAdapter.run(transactions, 
					(Transaction t) -> ExceptionAdapter.close(t));
		}
	}
}
