/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.config.commands.keystore;

public class KeyStoreGeneratorException extends RuntimeException {

	public KeyStoreGeneratorException(final String message) {
		super(message);
	}

	public KeyStoreGeneratorException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
