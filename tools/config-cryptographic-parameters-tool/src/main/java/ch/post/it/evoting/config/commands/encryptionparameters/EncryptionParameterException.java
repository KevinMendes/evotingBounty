/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.config.commands.encryptionparameters;

public class EncryptionParameterException extends RuntimeException {

	public EncryptionParameterException(final String message) {
		super(message);
	}

	public EncryptionParameterException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public EncryptionParameterException(final Throwable cause) {
		super(cause);
	}
}
