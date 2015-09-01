package com.expanset.hk2.security;

import java.util.Date;

import javax.annotation.Nonnull;

import org.apache.commons.lang.Validate;

/**
 * Credentials based on decrypted token.
 */
public class TokenCredentials extends AbstractCredentials {

	private final Date tokenCreationDate; 
	
	private final String token;
	
	/**
	 * @param token New token.
	 */
	public TokenCredentials(@Nonnull String token) {
		super(false);
		
		Validate.notNull(token, "token");
		
		this.tokenCreationDate = new Date();
		this.token = token;
	}
	
	/**
	 * @param tokenCreationDate Token creation date.
	 * @param token Decrypted token.
	 * @param secure Credentials is received via the protected channel.
	 */
	public TokenCredentials(@Nonnull Date tokenCreationDate, @Nonnull String token, boolean secure) {
		super(secure);
		
		Validate.notNull(tokenCreationDate, "tokenCreationDate");
		Validate.notNull(token, "token");
		
		this.tokenCreationDate = tokenCreationDate;
		this.token = token;
	}

	/**
	 * @return Token creation date.
	 */
	public Date getTokenCreationDate() {
		return tokenCreationDate;
	}

	/**
	 * @return Decrypted token.
	 */
	public String getToken() {
		return token;
	}
}
