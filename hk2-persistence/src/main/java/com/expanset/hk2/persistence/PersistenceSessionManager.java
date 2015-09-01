package com.expanset.hk2.persistence;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.Validate;
import org.jvnet.hk2.annotations.Contract;

import com.expanset.common.errors.ExceptionAdapter;
import com.expanset.common.errors.ExceptionAdapter.Exceptionable;
import com.expanset.common.errors.ExceptionAdapter.ExceptionableSupplier;

/**
 * Starts new persistence context sessions.
 * <p>It supports single or multi database connections. Example of a multi database environment below.</p>
 * <p>Configuration file:</p>
 * <pre>
 * databasePrefixes=db1,db2
 * databaseDefaultPrefix=db1
 * db1.javax.persistence.jdbc.url=jdbc:h2:db1
 * db2.javax.persistence.jdbc.url=jdbc:h2:db2
 * </pre>
 * <p>Multi database environment configuration:</p>
 * <pre>
 * install(new JpaPersistenceBinder());
 * install(new LocalTransactionsBinder());
 * install(new MultipleDatabasesPersistenceConfiguratorBinder("databasePrefixes", "databaseDefaultPrefix", null));
 * bindAsContract(UserDAO.class);
 * </pre>
 * <p>Persistence context injections:</p>
 * <pre>
 * {@literal @}Service
 * {@literal @}Contract
 * {@literal @}PerLookup
 * public class UserDAO {
 *	{@literal @}PersistenceContext(unitName="db")
 *	private EntityManager entityManager;
 * } 
 * </pre>
 * <p>Using sessions (maps default database - db1 to database named as db2):</p>
 * <pre>
 * {@literal @}Inject
 * PersistenceSessionManager sessionManager;
 * try(sessionManager.beginSession(Collections.singletonMap("", "db2"))) {
 *	UserDAO userDao =  serviceLocator.getService(UserDAO.class); 
 *	userDao.save(user);
 * };
 * </pre>
 */
@Contract
public interface PersistenceSessionManager {

	/**
	 * @return Names of lifecycle scope, for which persistence context proxying won't be used.
	 */
	Set<String> getScopes();
	
	/**
	 * Starts a new persistence context session.
	 * @return Object, that should be closed when persistence session is completed.
	 */
	default AutoCloseable beginSession() {
		return beginSession(null);
	}

	/**
	 * Starts a new persistence context session.
	 * @param factoryNameOverrides Factory name replacements for using in a multi database environment. 
	 * @return Object, that should be closed when persistence session is completed.
	 */
	AutoCloseable beginSession(@Nullable Map<String, String> factoryNameOverrides);

	/**
	 * @return Persistence session, that was bound to the current context (thread, web request etc).
	 */
	PersistenceSession getCurrentSession();
	
	/**
	 * Calls code in the persistence context session.
	 * @param runnable Code to call. 
	 */
	default void runInScope(Exceptionable runnable) {
		ExceptionAdapter.run(() -> {
			try( AutoCloseable scope = beginSession()) {
				runnable.run();
			}
		});
	}

	/**
	 * Calls code in the persistence context session.
	 * @param runnable Code to call. 
	 * @param factoryNameOverrides Factory name replacements for using in a multi database environment. 
	 */
	default void runInScope(Exceptionable runnable, @Nullable Map<String, String> factoryNameOverrides) {
		ExceptionAdapter.run(() -> {
			try( AutoCloseable scope = beginSession(factoryNameOverrides)) {
				runnable.run();
			}
		});
	}	
	
	/**
	 * Calls code in the persistence context session.
	 * @param runnable Code to call. 
	 * @param <T> Type of returning value.
	 * @return Result of code run.
	 */
	default <T> T getInScope(ExceptionableSupplier<T> runnable) {
		return ExceptionAdapter.get(() -> {
			try( AutoCloseable scope = beginSession()) {
				return runnable.get();
			}
		});
	}

	/**
	 * Calls code in the persistence context session.
	 * @param runnable Code to call. 
	 * @param factoryNameOverrides Factory name replacements for using in a multi database environment.
	 * @return Result of code run. 
	 * @param <T> Type of returning value.
	 */
	default <T> T getInScope(ExceptionableSupplier<T> runnable, @Nullable Map<String, String> factoryNameOverrides) {
		return ExceptionAdapter.get(() -> {
			try( AutoCloseable scope = beginSession(factoryNameOverrides)) {
				return runnable.get();
			}
		});
	}	
	
	/**
	 * Returns the persistence context, that was bound to the current sessions. 
	 * If the current scope differs from {@link com.expanset.hk2.persistence.PersistenceSessionManager#getScopes()} 
	 * proxy object for persistence context is created. 
	 * It can be used to inject session bound persistence contexts to singletons.
	 * @param key Persistence context identifier.
	 * @param persistenceContextClass Class of persistence context, that depend on persistence engine.
	 * @param currentScope Scope of injectee object. 
	 * @return Persistence context, that was bound to the current sessions. 
	 * @param <T> Type of returning value.
	 */
	@SuppressWarnings("unchecked")
	default <T> T getPersistenceContext(
			@Nonnull PersistenceContextKey key, 
			@Nonnull Class<T> persistenceContextClass,
			@Nullable String currentScope) {		
		Validate.notNull(key, "key");
		Validate.notNull(persistenceContextClass, "persistenceContextClass");

		if(getScopes() != null && currentScope != null && !getScopes().contains(currentScope)) {
			// Need to create proxy for other scopes.
            return (T)Proxy.newProxyInstance(
                    this.getClass().getClassLoader(),
                    new Class<?> [] { persistenceContextClass },
                    new InvocationHandler() {
						@Override
						public Object invoke(Object proxy, Method method, Object[] args) 
								throws Throwable {
							final PersistenceSession persistenceSession = getCurrentSession();
							if(persistenceSession == null) {
								throw new IllegalStateException("Must begin persistence scope by PersistenceContextRequestScopeManager.beginScope()");
							}								
							
							final T persistenceContext = 
									persistenceSession.getPersistenceContext(key, persistenceContextClass);
							
				            return method.invoke(persistenceContext, args);
						}
                    });			
		} else {
			final PersistenceSession persistenceSession = getCurrentSession();
			if(persistenceSession == null) {
				throw new IllegalStateException("Must begin persistence scope by PersistenceContextRequestScopeManager.beginScope()");
			}			
			
			final T persistenceContext = 
					persistenceSession.getPersistenceContext(key, persistenceContextClass);
			
			return persistenceContext;
		}
	}		
}
