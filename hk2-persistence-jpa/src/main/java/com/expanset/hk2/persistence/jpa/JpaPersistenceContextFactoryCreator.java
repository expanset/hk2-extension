package com.expanset.hk2.persistence.jpa;

import java.util.HashMap;
import java.util.Map;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.jvnet.hk2.annotations.Service;

import com.expanset.hk2.persistence.PersistenceContextFactoryKey;
import com.expanset.hk2.persistence.PersistenceContextFactoryWrapper;
import com.expanset.hk2.persistence.PersistenceContextFactoryCreator;

/**
 * Implementation of {@link PersistenceContextFactoryCreator} for JPA engine.
 */
@Service
public class JpaPersistenceContextFactoryCreator implements PersistenceContextFactoryCreator {
	
	private final static String JPA_URL_PROPERTY = "javax.persistence.jdbc.url";
	
	@Override
	public PersistenceContextFactoryWrapper create(
			PersistenceContextFactoryKey key, 
			Map<String, String> factoryProperties,
			Map<String, String> commonProperties) {
		Validate.notNull(key, "key");
		Validate.notNull(commonProperties, "commonProperties");

		final String url = (String) factoryProperties.get(JPA_URL_PROPERTY);
		if(url != null) {
			factoryProperties = new HashMap<>(factoryProperties);
			factoryProperties.put(
					JPA_URL_PROPERTY, 
					commonProperties != null ? StrSubstitutor.replace(url, commonProperties) : url);
		}		
		
		final EntityManagerFactory entityManagerFactory = 
				Persistence.createEntityManagerFactory(
						((JpaPersistenceContextFactoryKey)key).getUnitName(), factoryProperties);
		
		return new JpaPersistenceContextFactoryWrapper(entityManagerFactory);
	}
}
