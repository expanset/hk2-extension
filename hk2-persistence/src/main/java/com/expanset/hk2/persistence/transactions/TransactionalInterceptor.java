package com.expanset.hk2.persistence.transactions;

import javax.inject.Inject;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionRequiredException;
import javax.transaction.Transactional;
import javax.transaction.TransactionalException;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.jvnet.hk2.annotations.Service;

/**
 * Interceptor for methods that are annotated by {@link javax.transaction.Transactional}. 
 * Registered {@link javax.transaction.TransactionManager} is used for transaction management. 
 */
@Service
public class TransactionalInterceptor implements MethodInterceptor {

	@Inject
	protected TransactionManager transactionManager;
	
	@Override
	public Object invoke(MethodInvocation invocation) 
			throws Throwable {
		final Transactional ann = invocation.getMethod().getAnnotation(Transactional.class);
		
		assert ann != null;
		
		boolean needCommit = false;
		Transaction suspendedTransaction = null;
		
		switch(ann.value()) {
		case MANDATORY:
			if(transactionManager.getStatus() != Status.STATUS_ACTIVE) {
				throw new TransactionalException("Transaction required", new TransactionRequiredException());
			}			
			break;
		case NEVER:
			if(transactionManager.getStatus() != Status.STATUS_NO_TRANSACTION) {
				throw new TransactionalException("Cannot run inside transaction", new InvalidTransactionException());
			}			
			break;
		case NOT_SUPPORTED:
			suspendedTransaction = transactionManager.suspend();
			break;
		case REQUIRED:
			if(transactionManager.getStatus() == Status.STATUS_NO_TRANSACTION) {
				transactionManager.begin();
				needCommit = true;
			}
			break;
		case REQUIRES_NEW:
			if(transactionManager.getStatus() != Status.STATUS_NO_TRANSACTION) {
				suspendedTransaction = transactionManager.suspend();
			}
			transactionManager.begin();
			needCommit = true;
			break;
		default:
			break;
		}

		boolean rollbackOnError = false;
		try {
			Object result;
			try {
				result = invocation.proceed();
				
				rollbackOnError = true;
				
				if(needCommit) {
					commitOrRollback();				
				}
				
				return result;
			} catch(Throwable e) {
				if(needRollback(ann, e)) {
					transactionManager.setRollbackOnly();
				} 
				
				if(needCommit) {
					if(rollbackOnError) {
						transactionManager.rollback();
					} else {
						commitOrRollback();				
					}
				}
				
				throw e;
			}
		} finally {
			if(suspendedTransaction != null) {
				transactionManager.resume(suspendedTransaction);
			}
		}
	}

	protected boolean needRollback(Transactional ann, Throwable e) 
			throws Exception {
		assert ann != null;
		assert e != null;
		
		if(transactionManager.getStatus() == Status.STATUS_MARKED_ROLLBACK) {
			return true;
		}
		
		if(ann.dontRollbackOn() != null) {
			for(Class<?> dontRollback : ann.dontRollbackOn()) {
				if(dontRollback.isAssignableFrom(e.getClass())) {
					return false;
				}
			}
		}
		if(ann.rollbackOn() != null) {
			for(Class<?> rollback : ann.rollbackOn()) {
				if(rollback.isAssignableFrom(e.getClass())) {
					return true;
				}
			}
		}
		if(ann.rollbackOn() == null || ann.rollbackOn().length == 0) {
			return true;
		}
		
		return false;
	}
	
	protected void commitOrRollback() 
			throws SystemException, RollbackException, HeuristicMixedException, HeuristicRollbackException {
		if(transactionManager.getStatus() == Status.STATUS_MARKED_ROLLBACK) {
			transactionManager.rollback();
		} else {
			transactionManager.commit();
		}
	}	
}
