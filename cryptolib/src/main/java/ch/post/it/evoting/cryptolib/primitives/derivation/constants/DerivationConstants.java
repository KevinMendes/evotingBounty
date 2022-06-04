/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.primitives.derivation.constants;

/**
 * Class which contains constants used for key derivation.
 */
public final class DerivationConstants {

	/**
	 * Specifies a MGF1 algorithm.
	 */
	public static final String MGF1 = "MGF1";

	/**
	 * Specifies a PBKDF algorithm and the hash algorithm. It is supported by SunJCE since 1.8 version.
	 */
	public static final String PBKDF2_HMAC_SHA256 = "PBKDF2WithHmacSHA256";

	/**
	 * Minimum password length for PBKDF derivation.
	 */
	public static final int MINIMUM_PBKDF_PASSWORD_LENGTH = 16;

	/**
	 * Maximum password length for PBKDF derivation.
	 */
	public static final int MAXIMUM_PBKDF_PASSWORD_LENGTH = 1000;

	/**
	 * Minimum salt length in bytes for PBKDF derivation.
	 */
	public static final int MINIMUM_PBKDF_SALT_LENGTH_IN_BYTES = 32;

	private DerivationConstants() {
	}
}
