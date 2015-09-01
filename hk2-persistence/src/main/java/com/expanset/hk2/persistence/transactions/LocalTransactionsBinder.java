package com.expanset.hk2.persistence.transactions;

import javax.inject.Singleton;
import javax.transaction.TransactionManager;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

/**
 * Register {@link LocalTransactionManager} that is a simple implementation of the {@link TransactionManager}.
 * {@link LocalTransactionManager} starts transactions for all persistence contexts in the current session and thread.
 * Otherwise you may use Atomikos for JTA/XA transactions.
 */
public class LocalTransactionsBinder extends AbstractBinder {

	@Override
	protected void configure() {
		bind(LocalTransactionManager.class)
			.to(TransactionManager.class)
			.in(Singleton.class);
	}
}
