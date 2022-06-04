/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.symmetric.key.constants;

/**
 * Class which defines some constants that are used by symmetric keys.
 *
 * <p>Instances of this class are immutable.
 */
public final class SecretKeyConstants {

	/**
	 * The AES algorithm name.
	 */
	public static final String AES_ALG = "AES";

	/**
	 * HMAC with SHA256 algorithm name.
	 */
	public static final String HMAC_SHA256_ALG = "HmacSHA256";

	/**
	 * This class cannot be instantiated.
	 */
	private SecretKeyConstants() {
	}
}
