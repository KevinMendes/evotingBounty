/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

package ch.post.it.evoting.votingserver.messagebrokerorchestrator.tally;

public class MissingInitialPayloadException extends RuntimeException {
	public MissingInitialPayloadException(final String message) {
		super(message);
	}

	public MissingInitialPayloadException(String message, Throwable cause) {
		super(message, cause);
	}

}
