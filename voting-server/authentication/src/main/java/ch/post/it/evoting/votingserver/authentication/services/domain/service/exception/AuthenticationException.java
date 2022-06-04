/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.service.exception;

/**
 * Exception for authentication context.
 */
public class AuthenticationException extends Exception {

	private static final long serialVersionUID = 5831949965058405318L;

	/**
	 * Constructs a new exception with the specified detail message.
	 *
	 * @param message the detail message.
	 */
	public AuthenticationException(final String message) {
		super(message);
	}

	/**
	 * Constructs a new exception with the specified detail message and cause.
	 *
	 * @param message the detail message.
	 * @param cause   the cause of the exception.
	 */
	public AuthenticationException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
