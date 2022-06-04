/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.beans.exceptions;

public class AbstractKeystoreReaderException extends RuntimeException {

	private static final long serialVersionUID = 8214259381737828625L;

	public AbstractKeystoreReaderException(String message) {
		super(message);
	}

	public AbstractKeystoreReaderException(Throwable cause) {
		super(cause);
	}

	public AbstractKeystoreReaderException(String message, Throwable cause) {
		super(message, cause);
	}

}
