/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.infrastructure.exception;

public class VerificationCardSetUploadRepositoryException extends RuntimeException {

	private static final long serialVersionUID = 1907863331648478803L;

	public VerificationCardSetUploadRepositoryException(final Throwable cause) {
		super(cause);
	}

	public VerificationCardSetUploadRepositoryException(final String message) {
		super(message);
	}

	public VerificationCardSetUploadRepositoryException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
