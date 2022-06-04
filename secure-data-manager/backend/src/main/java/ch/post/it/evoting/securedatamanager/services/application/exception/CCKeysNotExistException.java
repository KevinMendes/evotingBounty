/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.exception;

public class CCKeysNotExistException extends Exception {

	private static final long serialVersionUID = 1;

	public CCKeysNotExistException(final Throwable cause) {
		super(cause);
	}

	public CCKeysNotExistException(final String message) {
		super(message);
	}

	public CCKeysNotExistException(final String message, final Throwable cause) {
		super(message, cause);
	}

}

