/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.exception;

public class CCKeysAlreadyExistException extends Exception {

	private static final long serialVersionUID = 1;

	public CCKeysAlreadyExistException(final Throwable cause) {
		super(cause);
	}

	public CCKeysAlreadyExistException(final String message) {
		super(message);
	}

	public CCKeysAlreadyExistException(final String message, final Throwable cause) {
		super(message, cause);
	}

}

