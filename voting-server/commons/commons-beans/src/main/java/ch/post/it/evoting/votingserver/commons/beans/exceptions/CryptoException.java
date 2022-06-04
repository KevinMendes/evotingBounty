/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.beans.exceptions;

public class CryptoException extends RuntimeException {

	private static final long serialVersionUID = 3831243369456177044L;

	public CryptoException(final String message) {
		super(message);
	}

	public CryptoException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public CryptoException(final Throwable cause) {
		super(cause);
	}
}
