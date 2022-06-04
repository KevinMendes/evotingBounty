/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.service.exception;

/**
 * Exception for authentication token signing.
 */
public class AuthenticationTokenSigningException extends AuthenticationException {

	private static final long serialVersionUID = -7431923288549561274L;

	/**
	 * Constructs a new exception with the specified detail message and cause.
	 *
	 * @param message the detail message.
	 * @param cause   the cause of the exception.
	 */
	public AuthenticationTokenSigningException(String message, Throwable cause) {
		super(message, cause);
	}
}
