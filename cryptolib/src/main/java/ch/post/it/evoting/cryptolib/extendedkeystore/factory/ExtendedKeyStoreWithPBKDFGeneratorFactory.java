/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.extendedkeystore.factory;

import ch.post.it.evoting.cryptolib.CryptolibFactory;
import ch.post.it.evoting.cryptolib.extendedkeystore.configuration.ExtendedKeyStorePolicy;
import ch.post.it.evoting.cryptolib.primitives.derivation.factory.CryptoKeyDeriverFactory;
import ch.post.it.evoting.cryptolib.symmetric.cipher.factory.SymmetricAuthenticatedCipherFactory;

/**
 * A factory class to create {@link CryptoExtendedKeyStoreWithPBKDF} generators.
 *
 * <p>This factory receives as a parameter a {@link ExtendedKeyStorePolicy}.
 *
 * <p>Instances of this class are immutable.
 */
public final class ExtendedKeyStoreWithPBKDFGeneratorFactory extends CryptolibFactory {

	private final ExtendedKeyStorePolicy storePolicy;

	/**
	 * Constructor which receives a {@link ExtendedKeyStorePolicy}.
	 *
	 * @param storePolicy the policy to be used by this factory.
	 */
	public ExtendedKeyStoreWithPBKDFGeneratorFactory(final ExtendedKeyStorePolicy storePolicy) {

		this.storePolicy = storePolicy;
	}

	/**
	 * Creates a {@link ExtendedKeyStoreWithPBKDFGenerator}.
	 *
	 * @return {@link ExtendedKeyStoreWithPBKDFGenerator}
	 */
	public ExtendedKeyStoreWithPBKDFGenerator create() {
		final CryptoKeyDeriverFactory cryptoPBKDFDeriver = new CryptoKeyDeriverFactory(storePolicy);
		final SymmetricAuthenticatedCipherFactory symmetricAuthenticatedCipherFactory = new SymmetricAuthenticatedCipherFactory(storePolicy);

		return new ExtendedKeyStoreWithPBKDFGenerator(cryptoPBKDFDeriver, storePolicy.getStoreTypeAndProvider().getType(),
				storePolicy.getStoreTypeAndProvider().getProvider(), symmetricAuthenticatedCipherFactory, storePolicy.getSecretKeyAlgorithmAndSpec());
	}
}
