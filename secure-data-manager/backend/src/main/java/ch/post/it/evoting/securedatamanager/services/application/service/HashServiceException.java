/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.service;

public class HashServiceException extends Exception {
	public HashServiceException(final String message) {
		super(message);
	}

	public HashServiceException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public HashServiceException(final Throwable cause) {
		super(cause);
	}
}
