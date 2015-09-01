package com.expanset.hk2.persistence.jpa;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceProperty;

import org.apache.commons.lang3.StringUtils;
import org.glassfish.hk2.api.Injectee;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.ServiceHandle;
import org.glassfish.hk2.utilities.reflection.ReflectionHelper;
import org.jvnet.hk2.annotations.Service;

import com.expanset.hk2.persistence.PersistenceContextFactoryAccessor;
import com.expanset.hk2.persistence.PersistenceSessionManager;

/**
 * Support of {@link PersistenceContext} annotation for {@link EntityManager} injecting.
 */
@Service
public class JpaPersistenceContextResolver implements InjectionResolver<PersistenceContext>  {
	
	@Inject
	protected Provider<PersistenceSessionManager> persistenceSessionManager;
	
	@Inject
	protected Provider<PersistenceContextFactoryAccessor> persistenceContextFactoryAccessor; 
	
	@Override
	public Object resolve(Injectee injectee, ServiceHandle<?> root) {
		String factoryName = ReflectionHelper.getNameFromAllQualifiers(
				injectee.getRequiredQualifiers(), injectee.getParent());
		
		final PersistenceContext persistenceContext = 
				injectee.getParent().getAnnotation(PersistenceContext.class);

		if(StringUtils.isEmpty(factoryName) && StringUtils.isNotEmpty(persistenceContext.name())) {
			factoryName = persistenceContext.name();
		}

		String unitName = persistenceContext.unitName();
		if(StringUtils.isEmpty(unitName)) {
			unitName = persistenceContextFactoryAccessor.get().getCommonProperty(JpaPersistenceBinder.DEFAULT_UNIT_NAME);
			if(StringUtils.isEmpty(unitName)) {
				throw new IllegalStateException("You must fill unitName");	
			}
		}

		Map<String, String> properties = null;
		if(persistenceContext.properties() != null && persistenceContext.properties().length != 0) {
			properties = new HashMap<>();
			for(PersistenceProperty property : persistenceContext.properties()) {
				properties.put(property.name(), property.value());
			}
		}
		
		final EntityManager entityManager = persistenceSessionManager.get().getPersistenceContext(
				new JpaPersistenceContextKey(factoryName, unitName, properties), 
				EntityManager.class, 
				injectee.getInjecteeDescriptor().getScope());
		return entityManager;		
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
