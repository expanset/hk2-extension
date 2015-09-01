package com.expanset.hk2.persistence;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jvnet.hk2.annotations.Contract;

/**
 * Factory of persistence context factories.
 */
@Contract
public interface PersistenceContextFactoryCreator {

	/**
	 * Creates new persistence factory.
	 * @param key Name and other data for new persistence factory.
	 * @param factoryProperties Persistence factory properties.
	 * @param commonProperties Common persistence engine properties (like path to databases).
	 * @return New persistence factory.
	 */
	PersistenceContextFactoryWrapper create(
			@Nonnull PersistenceContextFactoryKey key, 
			@Nonnull Map<String, String> factoryProperties, 
			@Nullable Map<String, String> commonProperties);
}
