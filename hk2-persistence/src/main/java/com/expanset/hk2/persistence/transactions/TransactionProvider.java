package com.expanset.hk2.persistence.transactions;

import javax.inject.Inject;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.UseProxy;
import org.jvnet.hk2.annotations.Service;

import com.expanset.common.errors.ExceptionAdapter;
import com.expanset.hk2.utils.PerCall;

/**
 * Provider for injected {@link Transaction}.
 */
@Service
public class TransactionProvider implements Factory<Transaction> {
	
	@Inject
	private TransactionManager transactionManager;
	
	@PerCall
	@UseProxy
	@Override
	public Transaction provide() {
		return ExceptionAdapter.get(() -> transactionManager.getTransaction());
	}

	@Override
	public void dispose(Transaction instance) {
	}
}
