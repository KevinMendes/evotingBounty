/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.exception;

public class CheckedIllegalStateException extends Exception {
	private static final long serialVersionUID = 1;

	public CheckedIllegalStateException(final Throwable cause) {
		super(cause);
	}

	public CheckedIllegalStateException(final String message) {
		super(message);
	}

	public CheckedIllegalStateException(final String message, final Throwable cause) {
		super(message, cause);
	}
}

