/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.service.exception;

/**
 * Exception for election event.
 */
public class ElectionEventException extends Exception {

	private static final long serialVersionUID = 3742467963200989016L;

	/**
	 * Constructs a new exception with the specified detail message.
	 *
	 * @param message the detail message.
	 */
	public ElectionEventException(String message) {
		super(message);
	}

	public ElectionEventException(Throwable cause) {
		super(cause);
	}

	/**
	 * Constructs a new exception with the specified detail message and cause.
	 *
	 * @param message the detail message.
	 * @param cause   the cause of the exception.
	 */
	public ElectionEventException(String message, Throwable cause) {
		super(message, cause);
	}
}
