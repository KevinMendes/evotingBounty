/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.extendedkeystore.constants;

/**
 * Contains constants used in Stores.
 */
public final class ExtendedKeyStoreConstants {

	/**
	 * Minimum alias length for a Extended key store entry.
	 */
	public static final int MINIMUM_SKS_ENTRY_ALIAS_LENGTH = 1;

	/**
	 * Maximum alias length for a Extended key store entry.
	 */
	public static final int MAXIMUM_SKS_ENTRY_ALIAS_LENGTH = 50;

	/**
	 * Minimum password length for a Extended key store entry.
	 */
	public static final int MINIMUM_SKS_PASSWORD_LENGTH = 16;

	/**
	 * Maximum password length for a Extended key store entry.
	 */
	public static final int MAXIMUM_SKS_PASSWORD_LENGTH = 1000;

	/**
	 * Allowed set of Extended key store entry alias characters (specified as a regular expression).
	 */
	public static final String ALLOWED_SKS_ENTRY_ALIAS_CHARACTERS = "[a-z0-9_-]+";

	/**
	 * This class cannot be instantiated.
	 */
	private ExtendedKeyStoreConstants() {
	}
}
