/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.beans.exceptions;

public class CleansedBallotBoxServiceException extends Exception {

	private static final long serialVersionUID = 2658922365557470022L;

	public CleansedBallotBoxServiceException(Throwable cause) {
		super(cause);
	}

	public CleansedBallotBoxServiceException(String message) {
		super(message);
	}

	public CleansedBallotBoxServiceException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
