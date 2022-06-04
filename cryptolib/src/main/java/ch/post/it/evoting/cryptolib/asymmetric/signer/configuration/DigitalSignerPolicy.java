/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.asymmetric.signer.configuration;

import ch.post.it.evoting.cryptolib.primitives.securerandom.configuration.ConfigSecureRandomAlgorithmAndProvider;

/**
 * Configuration policy for digitally signing and verifying a digital signature.
 *
 * <p>Implementations must be immutable.
 */
public interface DigitalSignerPolicy {

	/**
	 * Returns configuration for digitally signing and verifying a digital signature.
	 *
	 * @return configuration.
	 */
	ConfigDigitalSignerAlgorithmAndSpec getDigitalSignerAlgorithmAndSpec();

	/**
	 * Returns the {@link ConfigSecureRandomAlgorithmAndProvider} that should be used when getting instances of {@link java.security.SecureRandom}.
	 *
	 * @return The {@link ConfigSecureRandomAlgorithmAndProvider}.
	 */
	ConfigSecureRandomAlgorithmAndProvider getSecureRandomAlgorithmAndProvider();
}
