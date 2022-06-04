/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.keymanagement.exception;

import java.security.KeyManagementException;

/**
 * Key store is invalid.
 */
public final class InvalidKeyStoreException extends KeyManagementException {

	private static final long serialVersionUID = 1L;

	public InvalidKeyStoreException(final String message) {
		super(message);
	}

}
