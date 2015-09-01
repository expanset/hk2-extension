package com.expanset.hk2.persistence;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;
import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.expanset.common.errors.ExceptionAdapter;

/**
 * Holder for the persistence contexts that bounds to persistence session (current thread, web request etc).
 */
@ThreadSafe
public class PersistenceSession implements AutoCloseable {

	protected final Map<String, String> factoryNameOverrides;
		
	protected final Map<PersistenceContextKey, PersistenceContextHolder> persistenceContexts = new HashMap<>();
	
	@Inject
	protected PersistenceContextFactoryAccessor persistenceContextFactoryAccessor;
	
	private final static Logger log = LoggerFactory.getLogger(PersistenceSession.class);
	
	/**
	 * @param factoryNameOverrides Factory name replacements for using in multi database environment. 
	 */
	public PersistenceSession(@Nullable Map<String, String> factoryNameOverrides) {
		this.factoryNameOverrides = factoryNameOverrides;
	}
	
	/**
	 * Returns the persistence context, that cached in this session. It is created if necessary.
	 * @param key Persistence context identifier. 
	 * @param persistenceContextClass Class of persistence context, that depends on persistence engine.
	 * @return Holder of the persistence context, which holds the persistence context of selected persistence engine. 
	 * @param <T> Type of returning value.
	 */
	public synchronized <T> T getPersistenceContext(
			@Nonnull PersistenceContextKey key, 
			@Nonnull Class<T> persistenceContextClass) {
		Validate.notNull(key, "key");
		Validate.notNull(persistenceContextClass, "persistenceContextClass");
		
		key = resolveFactoryKey(key);
		
		PersistenceContextHolder result = persistenceContexts.get(key);
		if(result == null) {
			final PersistenceContextFactoryWrapper factory = 
					persistenceContextFactoryAccessor.getFactory(key.getFactoryKey());
			final PersistenceContextWrapper persistenceContext = 
					factory.create(key);
			
			result = new PersistenceContextHolder(persistenceContext);
			persistenceContexts.put(key, result);
		}
		
		final PersistenceContextWrapper wrapper = result.getPersistenceContext();
		final T persistenceCotext = (T)ExceptionAdapter.get(() -> wrapper.unwrap(persistenceContextClass));
		
		return persistenceCotext;
	}
	
	/**
	 * @return List of the persistence contexts, that was created in calling thread. 
	 */
	public synchronized List<PersistenceContextWrapper> getAllPersistenceContextsInCurrentThread() {
		final long threadId = Thread.currentThread().getId();
		final List<PersistenceContextWrapper> result = persistenceContexts.values().stream()
			.filter((PersistenceContextHolder holder) -> holder.getThreadId() == threadId)
			.map((PersistenceContextHolder holder) -> holder.getPersistenceContext())
			.collect(Collectors.toList());

		return Collections.unmodifiableList(result);
	}
	
	/**
	 * Clear session, close all persistence contexts.
	 */
	public synchronized void evict() {
		for(PersistenceContextHolder persistenceContextHolder : persistenceContexts.values()) {
			ExceptionAdapter.closeQuitely(
					persistenceContextHolder.getPersistenceContext(), log);
		}
		persistenceContexts.clear();
	}	

	/**
	 * Clear session, close all persistence contexts, that were created in calling thread. 
	 */
	public synchronized void evictInCurrentThread() {
		final long threadId = Thread.currentThread().getId();
		for (Iterator<Entry<PersistenceContextKey, PersistenceContextHolder>> it = 
				persistenceContexts.entrySet().iterator(); it.hasNext(); ) {
		    Entry<PersistenceContextKey, PersistenceContextHolder> entry = it.next();
			if(entry.getValue().getThreadId() == threadId) {
				ExceptionAdapter.closeQuitely(entry.getValue().getPersistenceContext(), log);
				it.remove();
			}
		}
	}
	
	public PersistenceContextKey resolveFactoryKey(PersistenceContextKey key) {
		if(factoryNameOverrides != null) {
			final String newFactoryName = factoryNameOverrides.get(key.getFactoryKey().getFactoryName());
			if(newFactoryName != null) {
				key = key.clone(newFactoryName);
			}
		}
		return key;
	}	
	
	public String resolveFactoryName(String factoryName) {
		if(factoryNameOverrides != null) {
			final String newFactoryName = factoryNameOverrides.get(factoryName);
			if(newFactoryName != null) {
				return newFactoryName;
			}
		}
		return factoryName;
	} 		
	
	/**
	 * Close persistence session.
	 */
	@Override
	public synchronized void close()
			throws Exception {
		evict();
	}	
	
	protected final class PersistenceContextHolder {
		
		private final PersistenceContextWrapper persistenceContext;
		
		private final long threadId = Thread.currentThread().getId();
		
		public PersistenceContextHolder(PersistenceContextWrapper persistenceContext) {
			this.persistenceContext = persistenceContext;
		}

		public PersistenceContextWrapper getPersistenceContext() {
			return persistenceContext;
		}

		public long getThreadId() {
			return threadId;
		}
	}
}
