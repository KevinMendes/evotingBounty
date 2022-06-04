/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.election.model.messaging;

/**
 * Implementation of the payload signature verification
 */
public class PayloadVerificationException extends Exception {

	private static final long serialVersionUID = 5003672338761264586L;

	public PayloadVerificationException(final Throwable cause) {
		super("The payload signature could not be verified", cause);
	}

	public PayloadVerificationException(final String cause) {
		super(cause);
	}
}
