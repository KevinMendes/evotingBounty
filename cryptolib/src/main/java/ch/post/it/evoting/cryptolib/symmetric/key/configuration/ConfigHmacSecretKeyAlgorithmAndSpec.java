/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.symmetric.key.configuration;

import ch.post.it.evoting.cryptolib.symmetric.key.constants.SecretKeyConstants;

/**
 * Enum which defines the supported HMAC secret keys.
 *
 * <p>Each enum type defines the following attributes:
 *
 * <ul>
 *   <li>Algorithm name.
 *   <li>Key length.
 * </ul>
 */
public enum ConfigHmacSecretKeyAlgorithmAndSpec {
	HMAC_WITH_SHA256_256(SecretKeyConstants.HMAC_SHA256_ALG, 256);

	private final String algorithm;

	private final int keyLengthInBits;

	ConfigHmacSecretKeyAlgorithmAndSpec(final String algorithm, final int keyLengthInBits) {

		this.algorithm = algorithm;
		this.keyLengthInBits = keyLengthInBits;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public int getKeyLengthInBits() {
		return keyLengthInBits;
	}
}
