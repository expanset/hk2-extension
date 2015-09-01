package com.expanset.hk2.persistence.ormlite;

import java.util.Map;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.jvnet.hk2.annotations.Service;

import com.expanset.common.errors.ExceptionAdapter;
import com.expanset.hk2.persistence.PersistenceContextFactoryKey;
import com.expanset.hk2.persistence.PersistenceContextFactoryWrapper;
import com.expanset.hk2.persistence.PersistenceContextFactoryCreator;
import com.j256.ormlite.jdbc.JdbcConnectionSource;
import com.j256.ormlite.support.ConnectionSource;

/**
 * Implementation of {@link PersistenceContextFactoryCreator} for Ormlite persistence engine.
 */
@Service
public class OrmlitePersistenceContextFactoryCreator implements PersistenceContextFactoryCreator {

	/**
	 * Configuration property: database URL. Required.
	 */
	public final static String URL_PROPERTY = "url";

	/**
	 * Configuration property: database login.
	 */
	public final static String USERNAME_PROPERTY = "user";
	
	/**
	 * Configuration property: database password.
	 */
	public final static String PASSWORD_PROPERTY = "password";
		
	@Override
	public PersistenceContextFactoryWrapper create(
			PersistenceContextFactoryKey key, 
			Map<String, String> factoryProperties,
			Map<String, String> commonProperties) {
		Validate.notNull(key, "key");
		Validate.notNull(factoryProperties, "factoryProperties");
		
		final String url = (String)factoryProperties.get(URL_PROPERTY);
		if(url == null) {
			throw new IllegalStateException(URL_PROPERTY + " must be filled");
		}
		
		final String finalUrl = commonProperties != null ? StrSubstitutor.replace(url, commonProperties) : url;
		final String username = (String)factoryProperties.get(USERNAME_PROPERTY);		
		final String password = (String)factoryProperties.get(PASSWORD_PROPERTY);

		final ConnectionSource connectionSource = 
				ExceptionAdapter.get(() -> new JdbcConnectionSource(finalUrl, username, password));
		
		return new OrmlitePersistenceContextFactoryWrapper(connectionSource);
	}
}
