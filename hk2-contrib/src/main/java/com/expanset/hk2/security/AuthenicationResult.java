package com.expanset.hk2.security;

import java.security.Principal;
import java.util.function.Function;

import javax.annotation.Nonnull;

import org.apache.commons.lang.Validate;

/**
 * User authentication result.
 */
public class AuthenicationResult {

	protected final Principal principal; 
	
	protected final Function<String, Boolean> userInRoleCallback;
	
	/**
	 * @param principal The user principal received after authentication.
	 */
	public AuthenicationResult(@Nonnull Principal principal) {
		Validate.notNull(principal, "principal");
		
		this.principal = principal;
		this.userInRoleCallback = role -> true;
	}
	
	/**
	 * @param principal The user principal received after authentication.
	 * @param userInRoleCallback Callback for test user roles.
	 */
	public AuthenicationResult(
			@Nonnull Principal principal, 
			@Nonnull Function<String, Boolean> userInRoleCallback) {
		Validate.notNull(principal, "principal");
		Validate.notNull(userInRoleCallback, "userInRoleCallback");
		
		this.principal = principal;
		this.userInRoleCallback = userInRoleCallback;
	}

	/**
	 * @return The user principal received after authentication.
	 */
	public Principal getPrincipal() {
		return principal;
	}

	/**
	 * @return Callback for test user roles.
	 */
	public Function<String, Boolean> getUserInRoleCallback() {
		return userInRoleCallback;
	}
}
