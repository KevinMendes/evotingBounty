/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.infrastructure.cc;

/**
 * An error condition while storing or retrieving a payload.
 */
public class PayloadStorageException extends Exception {

	public PayloadStorageException(final Throwable cause) {
		super(cause);
	}
}
