/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.beans.exceptions;

/**
 * Authentication Token Repository Exception
 */
public class AuthTokenRepositoryException extends Exception {

	private static final long serialVersionUID = -7600894241581995072L;

	public AuthTokenRepositoryException(final Throwable cause) {
		super(cause);
	}

	public AuthTokenRepositoryException(String message) {
		super(message);
	}

	public AuthTokenRepositoryException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
