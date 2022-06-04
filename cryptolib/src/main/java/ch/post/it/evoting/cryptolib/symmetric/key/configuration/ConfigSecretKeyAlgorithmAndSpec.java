/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.symmetric.key.configuration;

import ch.post.it.evoting.cryptolib.symmetric.key.constants.SecretKeyConstants;

/**
 * Defines the supported secret keys. A secret key is a key that can be used for symmetric encryption by a particular algorithm.
 *
 * <p>Each enum type defines the following attributes:
 *
 * <ul>
 *   <li>Algorithm name.
 *   <li>Key length.
 *   <li>Provider.
 * </ul>
 */
public enum ConfigSecretKeyAlgorithmAndSpec {
	AES_128_BC(SecretKeyConstants.AES_ALG, 128, SecretKeyProvider.BOUNCY_CASTLE),

	AES_128_SUNJCE(SecretKeyConstants.AES_ALG, 128, SecretKeyProvider.SUNJCE),

	AES_256_BC(SecretKeyConstants.AES_ALG, 256, SecretKeyProvider.BOUNCY_CASTLE),

	AES_256_SUNJCE(SecretKeyConstants.AES_ALG, 256, SecretKeyProvider.SUNJCE);

	private final String algorithm;

	private final int keyLength;

	private final String provider;

	ConfigSecretKeyAlgorithmAndSpec(final String algorithm, final int keyLength, final SecretKeyProvider provider) {

		this.algorithm = algorithm;
		this.keyLength = keyLength;
		this.provider = provider.getProvider();
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public int getKeyLength() {
		return keyLength;
	}

	public String getProvider() {
		return provider;
	}
}
