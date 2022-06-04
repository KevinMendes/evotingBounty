/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.exception;

public class DatabaseException extends RuntimeException {

	private static final long serialVersionUID = -9137284752915548117L;

	public DatabaseException(final String message, final Throwable e) {
		super(message, e);
	}

	public DatabaseException(final String message) {
		super(message);
	}
}
