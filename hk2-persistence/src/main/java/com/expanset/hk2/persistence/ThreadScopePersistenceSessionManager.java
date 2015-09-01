package com.expanset.hk2.persistence;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;
import javax.inject.Inject;

import org.glassfish.hk2.api.PerLookup;
import org.glassfish.hk2.api.ServiceLocator;
import org.jvnet.hk2.annotations.Service;

/**
 * Implementation of {@link PersistenceSessionManager} that bounds persistence session to the current thread.
 */
@Service
@ThreadSafe
public class ThreadScopePersistenceSessionManager implements PersistenceSessionManager {

	protected final ThreadLocal<Stack<PersistenceSession>> persistenceSessionStack = new ThreadLocal<>(); 
	
	@Inject
	protected ServiceLocator serviceLocator;
	
	protected final Set<String> scopes = new HashSet<>();
	
	public ThreadScopePersistenceSessionManager() {
		scopes.add(PerLookup.class.getName());
	}

	@Override
	public Set<String> getScopes() {
		return Collections.unmodifiableSet(scopes);
	}
	
	@Override
	public AutoCloseable beginSession(@Nullable Map<String, String> factoryNameOverrides) {
		Stack<PersistenceSession> stack = persistenceSessionStack.get();
		if(stack == null) {
			stack = new Stack<PersistenceSession>();
			persistenceSessionStack.set(stack);
		}
		
		final PersistenceSession persistenceSession = new PersistenceSession(factoryNameOverrides);		
		serviceLocator.inject(persistenceSession);
		
		stack.push(persistenceSession);

		final Stack<PersistenceSession> savedStack = stack;
		return new AutoCloseable() {
			@Override
			public void close() throws Exception {
				PersistenceSession persistenceSession = savedStack.pop();
				if(savedStack.empty()) {
					persistenceSessionStack.remove();
				}
				persistenceSession.close();
			}
		};
	}

	@Override
	public PersistenceSession getCurrentSession() {
		final Stack<PersistenceSession> stack = persistenceSessionStack.get();
		if(stack != null && !stack.empty()) {
			return stack.peek();
		}

		return null;
	}
}
