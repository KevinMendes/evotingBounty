/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.domain.service.exception;

public class VerificationCardDataGeneratorServiceException extends RuntimeException {

	private static final long serialVersionUID = 262169831436423910L;

	public VerificationCardDataGeneratorServiceException(final Throwable cause) {
		super(cause);
	}

	public VerificationCardDataGeneratorServiceException(final String message) {
		super(message);
	}

	public VerificationCardDataGeneratorServiceException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
