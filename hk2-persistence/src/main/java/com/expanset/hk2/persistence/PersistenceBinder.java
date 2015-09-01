package com.expanset.hk2.persistence;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

import com.expanset.hk2.persistence.transactions.TransactionProvider;
import com.expanset.hk2.persistence.transactions.TransactionalInterceptor;
import com.expanset.hk2.persistence.transactions.TransactionalInterceptorService;
import com.expanset.hk2.utils.PerCallScope;

/**
 * Registration of base services for support entity persistence.
 * Also it registers support of {@link javax.transaction.Transactional} annotation, 
 * injection of {@link javax.transaction.Transaction}.
 */
public abstract class PersistenceBinder extends AbstractBinder {

	@Override
	protected void configure() {		
		addActiveDescriptor(TransactionalInterceptor.class);
		addActiveDescriptor(TransactionalInterceptorService.class);
		addActiveDescriptor(PersistenceContextFactoryAccessor.class);
		
		addActiveDescriptor(PerCallScope.class);
		addActiveFactoryDescriptor(TransactionProvider.class);
	}
}
