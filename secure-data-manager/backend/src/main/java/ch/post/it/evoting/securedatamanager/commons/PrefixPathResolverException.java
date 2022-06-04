/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.commons;

public class PrefixPathResolverException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public PrefixPathResolverException(final Throwable cause) {
		super(cause);
	}

	public PrefixPathResolverException(final String message) {
		super(message);
	}

	public PrefixPathResolverException(final String message, final Throwable cause) {
		super(message, cause);
	}
}
