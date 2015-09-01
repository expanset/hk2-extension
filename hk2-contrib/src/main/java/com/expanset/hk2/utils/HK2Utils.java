package com.expanset.hk2.utils;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.Validate;
import org.glassfish.hk2.api.ActiveDescriptor;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.hk2.utilities.AliasDescriptor;

/**
 * Some utility methods for HK2 injection framework.
 */
public final class HK2Utils {

	/**
	 * Create new {@link ActiveDescriptor} with proxy's support based on existing {@link ActiveDescriptor}.
	 * @param serviceLocator HK2 service locator.
	 * @param descriptor Existing {@link ActiveDescriptor} that need to be proxyable.
	 * @param scope Name of scope for object in {@link ActiveDescriptor}.
	 * @param <T> Type of object for {@link ActiveDescriptor}.
	 * @return New {@link ActiveDescriptor} that has proxy support.
	 */
	public static <T> ActiveDescriptor<T> createProxyableActiveDescriptor(
			@Nonnull ServiceLocator serviceLocator, 
			@Nonnull ActiveDescriptor<T> descriptor,
			@Nonnull String scope) {
		Validate.notNull(serviceLocator, "serviceLocator");
		Validate.notNull(descriptor, "descriptor");
		Validate.notEmpty(scope, "scope");
		
		return new AliasDescriptor<T>(serviceLocator, (ActiveDescriptor<T>) descriptor, null, null) {
        	
			@Override
        	public String getScope() {
        		return scope;
        	}
        	
        	@Override
        	public Boolean isProxiable() {
        		return true;
        	}
        	
        	@Override
        	public Boolean isProxyForSameScope() {
        		return false;
        	}
        };
	}
	
	private HK2Utils() {};
}
