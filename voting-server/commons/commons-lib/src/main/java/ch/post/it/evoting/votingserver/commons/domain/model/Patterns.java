/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.domain.model;

/**
 * Patterns to validate values.
 */
public final class Patterns {

	/**
	 * Pattern for challenge which is a base64 string.
	 */
	public static final String CHALLENGE = "^(?:[A-Za-z0-9+/]{4})*(?:[A-Za-z0-9+/]{2}==|[A-Za-z0-9+/]{3}=)?$";

	/**
	 * Pattern for a timestamp of 13 digits.
	 */
	public static final String TIMESTAMP = "\\d{13}";

	/**
	 * Pattern for IDs. Complements this with @NotNull and @Size.
	 */
	public static final String ID = "^[a-zA-Z0-9]*$";

	/**
	 * Pattern digits.
	 */
	public static final String DIGITS = "^[0-9]*$";

	private Patterns() {
		// To avoid instantiation
	}
}
