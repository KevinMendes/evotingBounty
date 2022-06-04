/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.messaging;

/**
 * Destination does not exist.
 */
@SuppressWarnings("serial")
public final class DestinationNotFoundException extends MessagingException {

	/**
	 * Constructor.
	 *
	 * @param message
	 */
	public DestinationNotFoundException(String message) {
		super(message);
	}

	/**
	 * Constructor.
	 *
	 * @param message
	 * @param cause
	 */
	public DestinationNotFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
