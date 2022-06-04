/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.cryptolib.certificates.utils;

/**
 * Use this exception to wrap cryptographic exceptions thrown by underlying cryptographic libraries
 */
public class CryptographicOperationException extends Exception {

	private static final long serialVersionUID = 6001780884444927706L;

	/**
	 * This constructor allows to specify an exception message;
	 *
	 * @param message the exception message.
	 */
	public CryptographicOperationException(final String message) {
		super(message);
	}

	/**
	 * This constructor allows to specify both an exception message and another throwable as exception cause.
	 *
	 * @param message the exception message.
	 * @param cause   another throwable that is the cause to launch this exception.
	 */
	public CryptographicOperationException(final String message, final Throwable cause) {
		super(message, cause);
	}

}
