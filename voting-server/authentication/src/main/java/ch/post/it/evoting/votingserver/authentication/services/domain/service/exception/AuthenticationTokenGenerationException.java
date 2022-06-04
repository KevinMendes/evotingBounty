/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.service.exception;

/**
 * Exception for authentication token generation.
 */
public class AuthenticationTokenGenerationException extends AuthenticationException {

	private static final long serialVersionUID = -742449581243073466L;

	/**
	 * Constructs a new exception with the specified detail message and cause.
	 *
	 * @param message the detail message.
	 * @param cause   the cause of the exception.
	 */
	public AuthenticationTokenGenerationException(String message, Throwable cause) {
		super(message, cause);
	}
}
