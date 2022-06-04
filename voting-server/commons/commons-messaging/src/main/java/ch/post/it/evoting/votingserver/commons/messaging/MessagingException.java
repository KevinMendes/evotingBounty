/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.messaging;

/**
 * General messaging exception.
 */
@SuppressWarnings("serial")
public class MessagingException extends Exception {

	/**
	 * Constructor.
	 *
	 * @param message
	 */
	public MessagingException(String message) {
		super(message);
	}

	/**
	 * Constructor.
	 *
	 * @param message
	 * @param cause
	 */
	public MessagingException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructor.
	 *
	 * @param cause the underlying exception
	 */
	public MessagingException(Throwable cause) {
		super(cause);
	}
}
