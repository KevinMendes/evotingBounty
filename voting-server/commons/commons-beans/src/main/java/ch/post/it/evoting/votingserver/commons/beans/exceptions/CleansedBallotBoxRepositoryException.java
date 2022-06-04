/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.beans.exceptions;

public class CleansedBallotBoxRepositoryException extends Exception {

	private static final long serialVersionUID = -990713457163526110L;

	public CleansedBallotBoxRepositoryException(Throwable cause) {
		super(cause);
	}

	public CleansedBallotBoxRepositoryException(String message) {
		super(message);
	}

	public CleansedBallotBoxRepositoryException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
