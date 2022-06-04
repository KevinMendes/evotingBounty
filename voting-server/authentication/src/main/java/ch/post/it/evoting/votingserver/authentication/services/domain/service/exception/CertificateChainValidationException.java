/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.service.exception;

/**
 * Exception for certificate chain validation.
 */
public class CertificateChainValidationException extends AuthenticationException {

	private static final long serialVersionUID = 3947410817498850571L;

	/**
	 * Constructs a new exception with the specified detail message.
	 *
	 * @param message the detail message.
	 */
	public CertificateChainValidationException(String message) {
		super(message);
	}

	/**
	 * Constructs a new exception with the specified detail message and cause.
	 *
	 * @param message the detail message.
	 * @param cause   the cause of the exception.
	 */
	public CertificateChainValidationException(String message, Throwable cause) {
		super(message, cause);
	}
}
