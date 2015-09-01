package com.expanset.hk2.security;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.jvnet.hk2.annotations.Contract;

import com.expanset.common.RememberOptions;

/**
 * Controls authentication session.
 */
@Contract
public interface AuthenticationManager {
	
	/**
	 * Bind credentials to the current authenticated session.
	 * @param credentials User credentials for store in the session.
	 * @param rememberOptions Tune how to save authentication data.
	 */
	void saveAuthentication(
			@Nonnull AbstractCredentials credentials, 
			@Nullable RememberOptions rememberOptions);
	
	/**
	 * Remove credentials from the authenticated session.
	 * @param rememberOptions Tune how to save authentication data.
	 */
	void removeAuthentication(@Nullable RememberOptions rememberOptions);

	/**
	 * Bind credentials to the current authenticated session.
	 * @param credentials User credentials for store in the session.
	 */
	default void saveAuthentication(@Nonnull AbstractCredentials credentials) {
		saveAuthentication(credentials, null);
	}
	
	/**
	 * Remove credentials from the authenticated session.
	 */
	default void removeAuthentication() {
		removeAuthentication(null);
	}	
}
