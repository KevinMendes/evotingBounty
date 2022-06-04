/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.asymmetric.keypair.factory;

import java.security.GeneralSecurityException;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;

import ch.post.it.evoting.cryptolib.CryptolibFactory;
import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.asymmetric.keypair.configuration.KeyPairPolicy;
import ch.post.it.evoting.cryptolib.commons.configuration.Provider;
import ch.post.it.evoting.cryptolib.commons.system.OperatingSystem;
import ch.post.it.evoting.cryptolib.primitives.securerandom.configuration.ConfigSecureRandomAlgorithmAndProvider;

/**
 * A factory class for generating {@link CryptoKeyPairGenerator} objects.
 */
public final class KeyPairGeneratorFactory extends CryptolibFactory {

	private final KeyPairPolicy keyPairPolicy;

	/**
	 * Constructs a {@code KeyPairGeneratorFactory} using the provided {@link KeyPairPolicy}.
	 *
	 * @param keyPairPolicy the {@link KeyPairPolicy} to be used to configure this {@code KeyPairGeneratorFactory}.
	 *                      <p>NOTE: The received {@link KeyPairPolicy} should be an immutable object. If this is the
	 *                      case, then the entire {@code KeyPairGeneratorFactory} class is thread safe.
	 */
	public KeyPairGeneratorFactory(final KeyPairPolicy keyPairPolicy) {

		this.keyPairPolicy = keyPairPolicy;
	}

	/**
	 * Generates a new {@link CryptoKeyPairGenerator}, which is configured to generate signing key pairs.
	 *
	 * @return a new {@link CryptoKeyPairGenerator}.
	 */
	public CryptoKeyPairGenerator createSigning() {

		final String algorithm = keyPairPolicy.getSigningKeyPairAlgorithmAndSpec().getAlgorithm();

		final Provider provider = keyPairPolicy.getSigningKeyPairAlgorithmAndSpec().getProvider();

		final AlgorithmParameterSpec spec = keyPairPolicy.getSigningKeyPairAlgorithmAndSpec().getSpec();

		return new CryptoKeyPairGenerator(createKeyPairGenerator(algorithm, provider, spec));
	}

	/**
	 * Generates a new {@link CryptoKeyPairGenerator}, which is configured to generate encryption key pairs.
	 *
	 * @return a new {@link CryptoKeyPairGenerator}.
	 */
	public CryptoKeyPairGenerator createEncryption() {

		final String algorithm = keyPairPolicy.getEncryptingKeyPairAlgorithmAndSpec().getAlgorithm();

		final Provider provider = keyPairPolicy.getEncryptingKeyPairAlgorithmAndSpec().getProvider();

		final AlgorithmParameterSpec spec = keyPairPolicy.getEncryptingKeyPairAlgorithmAndSpec().getSpec();

		return new CryptoKeyPairGenerator(createKeyPairGenerator(algorithm, provider, spec));
	}

	private KeyPairGenerator createKeyPairGenerator(final String algorithm, final Provider provider, final AlgorithmParameterSpec spec) {

		final KeyPairGenerator keyPairGenerator;

		try {
			switch (provider) {
			case DEFAULT:
				keyPairGenerator = KeyPairGenerator.getInstance(algorithm);
				break;
			default:
				keyPairGenerator = KeyPairGenerator.getInstance(algorithm, provider.getProviderName());
				break;
			}

		} catch (final GeneralSecurityException e) {

			throw new CryptoLibException(
					"Exception while trying to get an instance of KeyPairGenerator. Algorithm was: " + algorithm + ", provider was: " + provider, e);
		}

		initialize(keyPairGenerator, spec);

		return keyPairGenerator;
	}

	private void initialize(final KeyPairGenerator keyPairGenerator, final AlgorithmParameterSpec spec) {

		final ConfigSecureRandomAlgorithmAndProvider secureRandomAlgorithmAndProvider = keyPairPolicy.getSecureRandomAlgorithmAndProvider();

		try {
			final SecureRandom sr;
			switch (secureRandomAlgorithmAndProvider.getProvider()) {
			case DEFAULT:
				checkOS();
				sr = SecureRandom.getInstance(secureRandomAlgorithmAndProvider.getAlgorithm());
				break;
			default:
				checkOS();
				sr = SecureRandom.getInstance(secureRandomAlgorithmAndProvider.getAlgorithm(),
						secureRandomAlgorithmAndProvider.getProvider().getProviderName());
				break;
			}

			keyPairGenerator.initialize(spec, sr);

		} catch (final GeneralSecurityException e) {
			throw new CryptoLibException("Exception while initializing the KeyPairGenerator", e);
		}
	}

	private void checkOS() {
		if (!keyPairPolicy.getSecureRandomAlgorithmAndProvider().isOSCompliant(OperatingSystem.current())) {

			throw new CryptoLibException("The given algorithm and provider are not compliant with the " + OperatingSystem.current() + " OS.");
		}
	}
}
