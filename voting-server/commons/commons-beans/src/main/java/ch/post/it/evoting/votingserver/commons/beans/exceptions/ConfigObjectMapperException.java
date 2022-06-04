/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.beans.exceptions;

public class ConfigObjectMapperException extends RuntimeException {

	private static final long serialVersionUID = -770251491690246586L;

	public ConfigObjectMapperException(Throwable cause) {
		super(cause);
	}

	public ConfigObjectMapperException(String message) {
		super(message);
	}

	public ConfigObjectMapperException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
