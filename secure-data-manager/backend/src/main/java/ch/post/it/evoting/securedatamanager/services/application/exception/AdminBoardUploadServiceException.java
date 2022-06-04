/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.exception;

public class AdminBoardUploadServiceException extends RuntimeException {

	private static final long serialVersionUID = 8026276349431237116L;

	public AdminBoardUploadServiceException(final Throwable cause) {
		super(cause);
	}

	public AdminBoardUploadServiceException(final String message) {
		super(message);
	}

	public AdminBoardUploadServiceException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
