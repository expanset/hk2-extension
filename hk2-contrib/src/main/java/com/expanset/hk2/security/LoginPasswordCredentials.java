package com.expanset.hk2.security;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang.Validate;

/**
 * Credentials based on user login and password.
 */
public class LoginPasswordCredentials extends AbstractCredentials {
	
	private final String login; 
	
	private final String password;

	private final String realm;
	
	/**
	 * @param login User login.
	 * @param password User password.
	 * @param realm Credentials realm.
	 * @param secure Credentials is received via the protected channel.
	 */
	public LoginPasswordCredentials(
			@Nonnull String login, 
			@Nonnull String password, 
			@Nullable String realm, 
			boolean secure) {
		super(secure);
		
		Validate.notNull(login, "login");
		Validate.notNull(password, "password");
		
		this.login = login;
		this.password = password;
		this.realm = realm;
	}

	/**
	 * @return User password.
	 */
	public String getLogin() {
		return login;
	}

	/**
	 * @return Credentials is received via the protected channel.
	 */
	public String getPassword() {
		return password;
	}

	/**
	 * @return Credentials realm.
	 */
	public String getRealm() {
		return realm;
	}
}
