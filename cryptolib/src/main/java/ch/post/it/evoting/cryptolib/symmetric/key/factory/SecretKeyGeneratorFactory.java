/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.symmetric.key.factory;

import java.security.GeneralSecurityException;
import java.security.SecureRandom;

import javax.crypto.KeyGenerator;

import ch.post.it.evoting.cryptolib.CryptolibFactory;
import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.commons.system.OperatingSystem;
import ch.post.it.evoting.cryptolib.primitives.securerandom.factory.SecureRandomFactory;
import ch.post.it.evoting.cryptolib.symmetric.key.configuration.ConfigSecretKeyAlgorithmAndSpec;
import ch.post.it.evoting.cryptolib.symmetric.key.configuration.SymmetricKeyPolicy;

/**
 * A factory class that can be used for creating instances of {@link CryptoSecretKeyGeneratorForEncryption}.
 *
 * <p>Instances of this class are immutable.
 */
public final class SecretKeyGeneratorFactory extends CryptolibFactory {

	private final SymmetricKeyPolicy symmetricKeyPolicy;

	/**
	 * Creates a SecretKeyGeneratorFactory, configured according to the received {@link SymmetricKeyPolicy} .
	 *
	 * @param symmetricKeyPolicy the secret key policy that should be used for creating instances of {@link CryptoSecretKeyGeneratorForEncryption}.
	 */
	public SecretKeyGeneratorFactory(final SymmetricKeyPolicy symmetricKeyPolicy) {

		this.symmetricKeyPolicy = symmetricKeyPolicy;
	}

	/**
	 * Creates a new {@link CryptoSecretKeyGeneratorForEncryption}, configured according the policy that was received when this {@code
	 * SecretKeyGeneratorFactory} was created.
	 *
	 * @return a newly created {@link CryptoSecretKeyGeneratorForEncryption}.
	 */
	public CryptoSecretKeyGeneratorForEncryption createGeneratorForEncryption() {

		return new CryptoSecretKeyGeneratorForEncryption(createAndInitializeKeyGenerator());
	}

	/**
	 * Creates a new {@link CryptoSecretKeyGeneratorForHmac}, configured according the policy that was received when this {@code
	 * SecretKeyGeneratorFactory} was created.
	 *
	 * @return a newly created {@link CryptoSecretKeyGeneratorForHmac}.
	 */
	public CryptoSecretKeyGeneratorForHmac createGeneratorForHmac() {

		return new CryptoSecretKeyGeneratorForHmac(symmetricKeyPolicy);
	}

	private KeyGenerator createAndInitializeKeyGenerator() {

		final KeyGenerator keyGen;

		final ConfigSecretKeyAlgorithmAndSpec spec = symmetricKeyPolicy.getSecretKeyAlgorithmAndSpec();

		try {

			keyGen = KeyGenerator.getInstance(spec.getAlgorithm(), spec.getProvider());

		} catch (final GeneralSecurityException e) {
			throw new CryptoLibException(
					"Error while trying to create a key generator. Algorithm was: " + spec.getAlgorithm() + ", provider was: " + spec.getProvider(),
					e);
		}

		initialize(keyGen);

		return keyGen;
	}

	private void initialize(final KeyGenerator keyGen) {

		try {

			checkOS();

			final SecureRandom secureRandom = new SecureRandomFactory(symmetricKeyPolicy).createSecureRandom();

			keyGen.init(symmetricKeyPolicy.getSecretKeyAlgorithmAndSpec().getKeyLength(), secureRandom);

		} catch (final IllegalArgumentException e) {
			throw new CryptoLibException("IllegalArgumentException while initializing the keypair generator", e);
		}
	}

	private void checkOS() {
		if (!symmetricKeyPolicy.getSecureRandomAlgorithmAndProvider().isOSCompliant(OperatingSystem.current())) {

			throw new CryptoLibException("The given algorithm and provider are not compliant with the " + OperatingSystem.current() + " OS.");
		}
	}
}
