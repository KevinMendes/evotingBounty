/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.exception;

public class KeyStoreServiceException extends RuntimeException {

	private static final long serialVersionUID = 3880324231542129840L;

	public KeyStoreServiceException(final Throwable cause) {
		super(cause);
	}

	public KeyStoreServiceException(final String message) {
		super(message);
	}

	public KeyStoreServiceException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
