/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.beans.exceptions;

public class VoteCastCodeRepositoryException extends Exception {

	private static final long serialVersionUID = -3420274252661357260L;

	public VoteCastCodeRepositoryException(Throwable cause) {
		super(cause);
	}

	public VoteCastCodeRepositoryException(String message) {
		super(message);
	}

	public VoteCastCodeRepositoryException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
