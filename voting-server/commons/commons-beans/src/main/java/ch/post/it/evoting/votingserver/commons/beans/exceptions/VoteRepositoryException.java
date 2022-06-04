/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.beans.exceptions;

public class VoteRepositoryException extends Exception {

	private static final long serialVersionUID = 608860693382896490L;

	public VoteRepositoryException(Throwable cause) {
		super(cause);
	}

	public VoteRepositoryException(String message) {
		super(message);
	}

	public VoteRepositoryException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
