/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.infrastructure.exception;

public class RestClientException extends RuntimeException {

	private static final long serialVersionUID = 5798774659744716331L;

	public RestClientException(Throwable cause) {
		super(cause);
	}

	public RestClientException(String cause) {
		super(cause);
	}

	public RestClientException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
