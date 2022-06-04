/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.extendedauthentication.domain.model.extendedauthentication;

public class AllowedAttemptsExceededException extends Exception {

	private static final long serialVersionUID = 1L;

	private static final String DEFAULT_MSG = "Invalid authentication: max allowed number of attempts reached.";

	public AllowedAttemptsExceededException() {
		super(DEFAULT_MSG);
	}

	public AllowedAttemptsExceededException(final String message) {
		super(message);
	}

	public AllowedAttemptsExceededException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public AllowedAttemptsExceededException(final Throwable cause) {
		super(cause);
	}
}
