/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.beans.exceptions;

public class ResourceStreamException extends RuntimeException {

	private static final long serialVersionUID = 2058471223984941357L;

	public ResourceStreamException(Throwable cause) {
		super(cause);
	}

	public ResourceStreamException(String message) {
		super(message);
	}

	public ResourceStreamException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
