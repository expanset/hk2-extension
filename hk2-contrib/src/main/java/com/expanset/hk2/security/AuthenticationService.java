package com.expanset.hk2.security;

import java.util.Optional;

import javax.annotation.Nonnull;

import org.jvnet.hk2.annotations.Contract;

/**
 *  Contract for authentication services.
 */
@Contract
public interface AuthenticationService {

	/**
	 * Process authentication. If authentication is success you should return {@link AuthenicationResult}.
	 * @param credentials User credentials extracted from current request.
	 * @return Result of user authentication.
	 */
	Optional<AuthenicationResult> authenticate(@Nonnull AbstractCredentials credentials);
}
