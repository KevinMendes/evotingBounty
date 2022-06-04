/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.asymmetric.cipher.constants;

/**
 * Class which defines some constants that are used by asymmetric ciphers.
 *
 * <p>Instances of this class are immutable.
 */
public class AsymmetricCipherConstants {

	/**
	 * Delimiter for the fields of the asymmetric cipher specification.
	 */
	public static final String CIPHER_SPECIFICATION_DELIMITER = "/";

	/**
	 * Index of the algorithm field in the asymmetric cipher specification.
	 */
	public static final int ALGORITHM_FIELD_INDEX = 0;

	/**
	 * Index of the encryption mode field in the asymmetric cipher specification.
	 */
	public static final int ENCRYPTION_MODE_FIELD_INDEX = 1;

	/**
	 * Index of the padding field in the asymmetric cipher specification.
	 */
	public static final int PADDING_FIELD_INDEX = 2;

	/**
	 * RSA key encapsulation mechanism.
	 */
	public static final String RSA_KEM = "RSA-KEM";

	/**
	 * RSA key encapsulation mechanism with KDF1 key derivation function and SHA-256 message digest.
	 */
	public static final String RSA_KEM_WITH_KDF1_AND_SHA256 = "RSA-KEMWITHKDF1ANDSHA-256";

	/**
	 * RSA key encapsulation mechanism with KDF2 key derivation function and SHA-256 message digest.
	 */
	public static final String RSA_KEM_WITH_KDF2_AND_SHA256 = "RSA-KEMWITHKDF2ANDSHA-256";

	private AsymmetricCipherConstants() {
	}
}
