/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.service;

public class AdminBoardServiceException extends RuntimeException {

	private static final long serialVersionUID = -5601384812692612498L;

	public AdminBoardServiceException(final Throwable cause) {
		super(cause);
	}

	public AdminBoardServiceException(final String message) {
		super(message);
	}

	public AdminBoardServiceException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
