/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.exception;

public class ElectionEventServiceException extends RuntimeException {

	private static final long serialVersionUID = -1380844286483578202L;

	public ElectionEventServiceException(final Throwable cause) {
		super(cause);
	}

	public ElectionEventServiceException(final String message) {
		super(message);
	}

	public ElectionEventServiceException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
