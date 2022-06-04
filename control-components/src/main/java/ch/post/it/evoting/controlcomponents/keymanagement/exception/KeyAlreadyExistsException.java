/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.keymanagement.exception;

import java.security.KeyManagementException;

/**
 * Key already exists.
 */
public final class KeyAlreadyExistsException extends KeyManagementException {

	private static final long serialVersionUID = 1L;

	public KeyAlreadyExistsException(final String message) {
		super(message);
	}

}
