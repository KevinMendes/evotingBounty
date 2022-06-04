/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.primitives.derivation.configuration;

import ch.post.it.evoting.cryptolib.commons.configuration.Provider;
import ch.post.it.evoting.cryptolib.primitives.derivation.constants.DerivationConstants;
import ch.post.it.evoting.cryptolib.primitives.messagedigest.configuration.HashAlgorithm;

/**
 * Enum which defines the key derivation function parameters.
 *
 * <p>Each element of the enum contains the following attributes:
 *
 * <ol>
 *   <li>An algorithm.
 *   <li>A {@link HashAlgorithm}.
 *   <li>A {@link Provider}.
 * </ol>
 *
 * <p>Instances of this enum are immutable.
 */
public enum ConfigKDFDerivationParameters {
	MGF1_SHA256_BC(DerivationConstants.MGF1, HashAlgorithm.SHA256, Provider.BOUNCY_CASTLE),

	MGF1_SHA256_DEFAULT(DerivationConstants.MGF1, HashAlgorithm.SHA256, Provider.DEFAULT);

	private final String algorithm;

	private final String hashAlgorithm;

	private final Provider provider;

	ConfigKDFDerivationParameters(final String algorithm, final HashAlgorithm hashAlgorithm, final Provider provider) {

		this.algorithm = algorithm;
		this.hashAlgorithm = hashAlgorithm.getAlgorithm();
		this.provider = provider;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public String getHashAlgorithm() {
		return hashAlgorithm;
	}

	public Provider getProvider() {
		return provider;
	}
}
