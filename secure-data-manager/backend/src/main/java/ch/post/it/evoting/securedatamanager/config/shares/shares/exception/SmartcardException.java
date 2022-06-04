/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.shares.shares.exception;

/**
 * Base exception for wrapping smartcard-related exceptions
 */
public class SmartcardException extends Exception {

	private static final long serialVersionUID = 1261427122008988894L;

	public SmartcardException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
