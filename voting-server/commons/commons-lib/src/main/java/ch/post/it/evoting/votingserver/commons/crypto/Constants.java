/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.crypto;

/**
 * This class contains constants used in the cryptographic part.
 */
public final class Constants {

	/**
	 * The separator used in the encrypted options string.
	 */
	public static final String SEPARATOR_ENCRYPTED_OPTIONS = ";";

	/**
	 * The position of cyphertext C0 in the encrypted options.
	 */
	public static final int POSITION_C0 = 0;

	/**
	 * The position of cyphertext C1 in the encrypted options.
	 */
	public static final int POSITION_C1 = 1;

	// Avoid instantiation.
	private Constants() {
	}
}
