package com.expanset.hk2.persistence;

import javax.transaction.Transaction;

import org.jvnet.hk2.annotations.Contract;

import com.expanset.common.Wrapper;

/**
 * Persistence context holder.
 */
@Contract
public interface PersistenceContextWrapper extends Wrapper, AutoCloseable {
	
	/**
	 * Starts database transaction for this persistence context.
	 * @return New transaction for this persistence context.
	 * @throws Exception Error when start transaction.
	 */
	Transaction beginTransaction() throws Exception;
}
