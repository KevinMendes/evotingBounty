/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain;

public class InvalidPayloadSignatureException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private static final String SIGNATURE_OF_PAYLOAD = "Signature of payload ";
	private static final String IS_INVALID = " is invalid. ";

	public InvalidPayloadSignatureException(final Class<?> clazz, final String additionalInfo) {
		super(SIGNATURE_OF_PAYLOAD + clazz.getSimpleName() + IS_INVALID + additionalInfo);
	}

	public InvalidPayloadSignatureException(final Class<?> clazz) {
		super(SIGNATURE_OF_PAYLOAD + clazz.getSimpleName() + IS_INVALID);
	}

}
