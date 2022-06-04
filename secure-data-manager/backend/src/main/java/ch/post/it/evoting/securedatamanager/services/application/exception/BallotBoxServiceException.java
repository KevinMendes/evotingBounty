/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.exception;

public class BallotBoxServiceException extends RuntimeException {

	private static final long serialVersionUID = -188943882949381942L;

	public BallotBoxServiceException(final Throwable cause) {
		super(cause);
	}

	public BallotBoxServiceException(final String message) {
		super(message);
	}

	public BallotBoxServiceException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
