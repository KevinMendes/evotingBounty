/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.primitives.derivation.configuration;

import ch.post.it.evoting.cryptolib.commons.configuration.Provider;
import ch.post.it.evoting.cryptolib.primitives.derivation.constants.DerivationConstants;
import ch.post.it.evoting.cryptolib.primitives.messagedigest.configuration.HashAlgorithm;

/**
 * Enum which defines the password derivation algorithm and its parameters.
 *
 * <p>Each element of the enum contains the following attributes:
 *
 * <ol>
 *   <li>An algorithm.
 *   <li>A {@link Provider}.
 *   <li>A number of iterations.
 *   <li>A {@link HashAlgorithm}.
 *   <li>The length in bits of the salt.
 *   <li>The length in bits of the expected output.
 * </ol>
 *
 * <p>Instances of this enum are immutable.
 */
public enum ConfigPBKDFDerivationParameters {
	PBKDF2_1_SHA256_256_SUNJCE_KL128(DerivationConstants.PBKDF2_HMAC_SHA256, Provider.SUN_JCE, 1, HashAlgorithm.SHA256, 256, 128),

	PBKDF2_32000_SHA256_256_SUNJCE_KL128(DerivationConstants.PBKDF2_HMAC_SHA256, Provider.SUN_JCE, 32000, HashAlgorithm.SHA256, 256, 128),

	PBKDF2_1_SHA256_256_BC_KL128(DerivationConstants.PBKDF2_HMAC_SHA256, Provider.BOUNCY_CASTLE, 1, HashAlgorithm.SHA256, 256, 128),

	PBKDF2_32000_SHA256_256_BC_KL128(DerivationConstants.PBKDF2_HMAC_SHA256, Provider.BOUNCY_CASTLE, 32000, HashAlgorithm.SHA256, 256, 128);

	private final String algorithm;

	private final Provider provider;

	private final int iterations;

	private final String hashAlgorithm;

	private final int saltBitLength;

	private final int keyBitLength;

	ConfigPBKDFDerivationParameters(final String algorithm, final Provider provider, final int iterations, final HashAlgorithm hashAlgorithm,
			final int saltBitLength, final int keyBitLength) {

		this.algorithm = algorithm;
		this.provider = provider;
		this.iterations = iterations;
		this.hashAlgorithm = hashAlgorithm.getAlgorithm();
		this.saltBitLength = saltBitLength;
		this.keyBitLength = keyBitLength;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public Provider getProvider() {
		return provider;
	}

	public int getIterations() {
		return iterations;
	}

	public String getHashAlgorithm() {
		return hashAlgorithm;
	}

	public int getSaltBitLength() {
		return saltBitLength;
	}

	public int getKeyBitLength() {
		return keyBitLength;
	}
}
