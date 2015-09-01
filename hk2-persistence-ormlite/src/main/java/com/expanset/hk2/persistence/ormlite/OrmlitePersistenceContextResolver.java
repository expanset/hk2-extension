package com.expanset.hk2.persistence.ormlite;

import javax.inject.Inject;
import javax.inject.Provider;

import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.utilities.reflection.ReflectionHelper;
import org.jvnet.hk2.annotations.Service;

import com.expanset.hk2.persistence.PersistenceContextFactoryKey;
import com.expanset.hk2.persistence.PersistenceContextKey;
import com.expanset.hk2.persistence.PersistenceSessionManager;
import com.j256.ormlite.support.ConnectionSource;

/**
 * Support of {@link OrmlitePersistenceContext} annotation for {@link ConnectionSource} injecting.
 */
@Service
public class OrmlitePersistenceContextResolver implements InjectionResolver<OrmlitePersistenceContext> {

	@Inject
	protected Provider<PersistenceSessionManager> persistenceSessionManager;
					
	@Override
	public Object resolve(Injectee injectee, ServiceHandle<?> root) {
		final String factoryName = ReflectionHelper.getNameFromAllQualifiers(
				injectee.getRequiredQualifiers(), injectee.getParent());
		
		final ConnectionSource connectionSource = persistenceSessionManager.get().getPersistenceContext(
				new PersistenceContextKey(new PersistenceContextFactoryKey(factoryName)), 
				ConnectionSource.class, 
				injectee.getInjecteeDescriptor().getScope());
		return connectionSource;
	}

	@Override
	public boolean isConstructorParameterIndicator() {
		return false;
	}

	@Override
	public boolean isMethodParameterIndicator() {
		return false;
	}
}
