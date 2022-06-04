/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.exception;

public class BallotBoxDownloadServiceException extends RuntimeException {

	private static final long serialVersionUID = -5397322233066334805L;

	public BallotBoxDownloadServiceException(final Throwable cause) {
		super(cause);
	}

	public BallotBoxDownloadServiceException(final String message) {
		super(message);
	}

	public BallotBoxDownloadServiceException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
