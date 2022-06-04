/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.config.commands.primes;

public class CreatePrimeGroupMembersException extends RuntimeException {

	private static final long serialVersionUID = 3465465753572143460L;

	public CreatePrimeGroupMembersException(final String message) {
		super(message);
	}

	public CreatePrimeGroupMembersException(final String message, final Throwable cause) {
		super(message, cause);
	}

	public CreatePrimeGroupMembersException(final Throwable cause) {
		super(cause);
	}
}
