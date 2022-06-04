/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.shares.shares.exception;

/**
 * Exception thrown when trying to read from/write to a smartcart and no smartcard is found in the reader
 */
public final class NoSmartcardFoundException extends SmartcardException {

	private static final long serialVersionUID = 3208202612677363214L;

	public NoSmartcardFoundException() {
		super("There is no smartcard in the reader", null);
	}
}
