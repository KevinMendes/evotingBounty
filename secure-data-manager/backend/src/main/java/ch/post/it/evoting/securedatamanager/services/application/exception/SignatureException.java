/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.exception;

public class SignatureException extends RuntimeException {

	public SignatureException(final String message, final Throwable e) {
		super(message, e);
	}

	public SignatureException(final String message) {
		super(message);
	}

}
