/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.service.exception;

/**
 * The payload cannot be used as it is not signed.
 */
public class MissingSignatureException extends Exception {

	private static final long serialVersionUID = 1L;

	private static final String ERROR_MESSAGE_TEMPLATE = "Payload %s is not signed";

	public MissingSignatureException(final String id) {
		super(String.format(ERROR_MESSAGE_TEMPLATE, id));
	}
}
