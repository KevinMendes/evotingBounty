/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.primitives.securerandom.factory;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;

import ch.post.it.evoting.cryptolib.CryptolibFactory;
import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.commons.configuration.Provider;
import ch.post.it.evoting.cryptolib.commons.system.OperatingSystem;
import ch.post.it.evoting.cryptolib.primitives.securerandom.configuration.SecureRandomPolicy;
import ch.post.it.evoting.cryptolib.primitives.securerandom.configuration.SecureRandomPolicyFromProperties;
import ch.post.it.evoting.cryptolib.primitives.securerandom.constants.SecureRandomConstants;

/**
 * A factory class for creating random generators according to a given {@link SecureRandomPolicy}. At the moment the following generators are
 * considered:
 *
 * <ul>
 *   <li>{@link CryptoRandomInteger}
 *   <li>{@link CryptoRandomString}
 * </ul>
 */
public final class SecureRandomFactory extends CryptolibFactory {

	private final SecureRandomPolicy secureRandomPolicy;

	/**
	 * Constructs a SecureRandomFactory using the standard {@link SecureRandomPolicy}.
	 */
	public SecureRandomFactory() {
		this(new SecureRandomPolicyFromProperties());
	}

	/**
	 * Constructs a SecureRandomFactory using the provided {@link SecureRandomPolicy}.
	 *
	 * @param secureRandomPolicy The SecureRandomPolicy to be used to configure this SecureRandomFactory.
	 *                           <p>NOTE: The received {@link SecureRandomPolicy} should be an immutable object. If this is
	 *                           the case, then the entire class is thread safe.
	 */
	public SecureRandomFactory(final SecureRandomPolicy secureRandomPolicy) {
		this.secureRandomPolicy = secureRandomPolicy;
	}

	/**
	 * Instantiates a random integer generator.
	 *
	 * @return a {@link CryptoRandomInteger} object.
	 */
	public CryptoRandomInteger createIntegerRandom() {
		return new CryptoRandomInteger(createSecureRandom());
	}

	/**
	 * Instantiates a random string generator. Check the {@link CryptoRandomString} class description for further information about the accepted
	 * alphabets.
	 *
	 * <p>Two alphabets are provided in {@link
	 * SecureRandomConstants}: {@link SecureRandomConstants#ALPHABET_BASE32} and {@link SecureRandomConstants#ALPHABET_BASE64}
	 *
	 * @param alphabet The alphabet to be used to generate the random strings.
	 * @return a {@link CryptoRandomString} object.
	 */
	public CryptoRandomString createStringRandom(final String alphabet) {

		return new CryptoRandomString(createSecureRandom(), alphabet);
	}

	/**
	 * Create a {@link java.security.SecureRandom} according to the given policy.
	 *
	 * @return A {@link java.security.SecureRandom} object.
	 */
	public SecureRandom createSecureRandom() {
		try {

			checkOS();

			if (Provider.DEFAULT == secureRandomPolicy.getSecureRandomAlgorithmAndProvider().getProvider()) {
				return SecureRandom.getInstance(secureRandomPolicy.getSecureRandomAlgorithmAndProvider().getAlgorithm());
			} else {
				return SecureRandom.getInstance(secureRandomPolicy.getSecureRandomAlgorithmAndProvider().getAlgorithm(),
						secureRandomPolicy.getSecureRandomAlgorithmAndProvider().getProvider().getProviderName());
			}

		} catch (final GeneralSecurityException e) {
			throw new CryptoLibException("Failed to create SecureRandom in this environment. Attempted to use the provider: " + secureRandomPolicy
					.getSecureRandomAlgorithmAndProvider().getProvider() + ", and the algorithm: " + secureRandomPolicy
					.getSecureRandomAlgorithmAndProvider().getAlgorithm() + ". Error message was " + e.getMessage(), e);
		}
	}

	private void checkOS() {
		if (!secureRandomPolicy.getSecureRandomAlgorithmAndProvider().isOSCompliant(OperatingSystem.current())) {
			throw new CryptoLibException("The given algorithm and provider are not compliant with the " + OperatingSystem.current() + " OS.");
		}
	}
}
