package com.expanset.hk2.security;

/**
 * Base class for user credentials.
 */
public abstract class AbstractCredentials {
	
	protected final boolean secure;
	
	/**
 	 * @param secure Credentials is received via the protected channel.
	 */
	public AbstractCredentials(boolean secure) {
		this.secure = secure;
	}

	/**
	 * @return Credentials is received via the protected channel.
	 */
	public boolean isSecure() {
		return secure;
	}
}
