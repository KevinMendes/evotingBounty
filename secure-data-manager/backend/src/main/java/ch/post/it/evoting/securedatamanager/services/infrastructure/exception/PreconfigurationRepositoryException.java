/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.infrastructure.exception;

public class PreconfigurationRepositoryException extends RuntimeException {

	private static final long serialVersionUID = -5271446720919937859L;

	public PreconfigurationRepositoryException(final Throwable cause) {
		super(cause);
	}

	public PreconfigurationRepositoryException(final String message) {
		super(message);
	}

	public PreconfigurationRepositoryException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
