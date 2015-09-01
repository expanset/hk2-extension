package com.expanset.hk2.persistence.config;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nonnull;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jvnet.hk2.annotations.Service;

import com.expanset.common.ConfigurationUtils;

/**
 * Register supporting of a single database configuration. It is the simplest configuration.
 * <p>Example below.</p>
 * <p>Configuration file:</p>
 * <pre>
 * db.javax.persistence.jdbc.url=jdbc:h2:db1
 * db.javax.persistence.jdbc.user=username
 * </pre>
 * <p>Single database environment configuration:</p>
 * <pre>
 * install(new JpaPersistenceBinder());
 * install(new LocalTransactionsBinder());
 * install(new SingleDatabasesPersistenceConfiguratorBinder("db", null));
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
 * <p>Using sessions:</p>
 * <pre>
 * {@literal @}Inject
 * PersistenceSessionManager sessionManager;
 * try(sessionManager.beginSession()) {
 *	UserDAO userDao =  serviceLocator.getService(UserDAO.class); 
 *	userDao.save(user);
 * };
 * </pre>
 */
@Service
public class SingleDatabasePersistenceConfiguratorSettings extends PersistenceConfiguratorSettings {

	protected final String configPrefix;
	
	public SingleDatabasePersistenceConfiguratorSettings(
			@Nonnull String configPrefix) {
		Validate.notEmpty(configPrefix, "configPrefix");
				
		this.configPrefix = configPrefix;
	}
	
	@Override
	public Map<String, Map<String, String>> getConfiguration(@Nonnull Configuration config) {
		Validate.notNull(config, "config");	
		
		final Map<String, Map<String, String>> configuration = new HashMap<>();
		configuration.put(StringUtils.EMPTY, ConfigurationUtils.getMap((config.subset(configPrefix))));	
		
		return configuration;
	}
}
