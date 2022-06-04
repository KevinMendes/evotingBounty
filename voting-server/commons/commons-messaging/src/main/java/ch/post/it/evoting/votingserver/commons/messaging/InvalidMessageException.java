/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.messaging;

/**
 * Message is invalid.
 */
@SuppressWarnings("serial")
public final class InvalidMessageException extends MessagingException {

	/**
	 * Constructor.
	 *
	 * @param message
	 */
	public InvalidMessageException(String message) {
		super(message);
	}

	/**
	 * Constructor.
	 *
	 * @param message
	 * @param cause
	 */
	public InvalidMessageException(String message, Throwable cause) {
		super(message, cause);
	}
}
