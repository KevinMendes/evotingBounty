/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.primitives.securerandom.configuration;

import ch.post.it.evoting.cryptolib.commons.configuration.Provider;
import ch.post.it.evoting.cryptolib.commons.system.OperatingSystem;

/**
 * Enum representing a set of parameters that can be used when requesting an instance of SecureRandom.
 *
 * <p>These parameters are:
 *
 * <ol>
 *   <li>An algorithm.
 *   <li>A {@link Provider}.
 *   <li>A list of operating systems that support the algorithm and provider.
 * </ol>
 *
 * <p>Instances of this enum are immutable.
 */
public enum ConfigSecureRandomAlgorithmAndProvider {
	NATIVE_PRNG_SUN("NativePRNG", Provider.SUN, OperatingSystem.UNIX),

	PRNG_SUN_MSCAPI("Windows-PRNG", Provider.SUN_MSCAPI, OperatingSystem.WINDOWS);

	private final String algorithm;

	private final Provider provider;

	private final OperatingSystem[] operatingSystems;

	ConfigSecureRandomAlgorithmAndProvider(final String algorithm, final Provider provider, final OperatingSystem... operatingSystems) {
		this.algorithm = algorithm;
		this.provider = provider;
		this.operatingSystems = operatingSystems;
	}

	public String getAlgorithm() {
		return algorithm;
	}

	public Provider getProvider() {
		return provider;
	}

	/**
	 * Checks if the algorithm and provider are OS-compliant.
	 *
	 * @param operatingSystem The current operation system.
	 * @return {@code true} if the algorithm and provider are OS-compliant.
	 */
	public boolean isOSCompliant(final OperatingSystem operatingSystem) {

		for (final OperatingSystem oS : operatingSystems) {
			if (operatingSystem == oS) {
				return true;
			}
		}

		return false;
	}
}
