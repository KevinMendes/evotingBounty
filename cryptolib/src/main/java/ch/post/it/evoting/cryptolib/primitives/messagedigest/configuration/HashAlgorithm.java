/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.primitives.messagedigest.configuration;

/**
 * Enum which defines the key derivation function hash algorithm.
 */
public enum HashAlgorithm {
	SHA256("SHA-256"),

	/**
	 * These options are only available for the BouncyCastle cryptographic service provider.
	 */
	SHA512_224("SHA-512/224"),

	SHA3_256("SHA3-256"),

	SHA3_384("SHA3-384"),

	SHA3_512("SHA3-512");

	private final String algorithm;

	HashAlgorithm(final String algorithm) {
		this.algorithm = algorithm;
	}

	public String getAlgorithm() {
		return algorithm;
	}
}
