/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.exception;

public class ResourceNotFoundException extends Exception {

	private static final long serialVersionUID = 1;

	public ResourceNotFoundException(final Throwable cause) {
		super(cause);
	}

	public ResourceNotFoundException(final String message) {
		super(message);
	}

	public ResourceNotFoundException(final String message, final Throwable cause) {
		super(message, cause);
	}

}

