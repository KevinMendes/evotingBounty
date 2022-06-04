/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.primitives.derivation.factory;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

import javax.crypto.SecretKeyFactory;

import ch.post.it.evoting.cryptolib.api.derivation.CryptoAPIKDFDeriver;
import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.commons.configuration.Provider;
import ch.post.it.evoting.cryptolib.primitives.derivation.configuration.ConfigPBKDFDerivationParameters;
import ch.post.it.evoting.cryptolib.primitives.derivation.configuration.DerivationPolicy;
import ch.post.it.evoting.cryptolib.primitives.service.PrimitivesService;

/**
 * A factory class for generating {@link CryptoAPIKDFDeriver} objects.
 */
public class CryptoKeyDeriverFactory {

	private static final String KEY_DERIVATION_ERROR = "Error while deriving the key";
	private final DerivationPolicy derivationPolicy;

	/**
	 * Constructor which receives a {@link DerivationPolicy}.
	 *
	 * @param derivationPolicy The policy containing the properties to configure the objects that this class creates.
	 */
	public CryptoKeyDeriverFactory(final DerivationPolicy derivationPolicy) {
		this.derivationPolicy = derivationPolicy;
	}

	public CryptoKDFDeriver createKDFDeriver() {

		try {
			return new CryptoKDFDeriver(derivationPolicy.getKDFDerivationParameters());
		} catch (final GeneralCryptoLibException e) {
			throw new CryptoLibException(e);
		}
	}

	public CryptoPBKDFDeriver createPBKDFDeriver() {

		final int saltBitLength = derivationPolicy.getPBKDFDerivationParameters().getSaltBitLength();

		final int iterations = derivationPolicy.getPBKDFDerivationParameters().getIterations();

		final int keyLength = derivationPolicy.getPBKDFDerivationParameters().getKeyBitLength();

		final int minPasswordLength = derivationPolicy.getPBKDFDerivationMinPasswordLength();

		final int maxPasswordLength = derivationPolicy.getPBKDFDerivationMaxPasswordLength();

		final PrimitivesService primitivesService = new PrimitivesService();

		try {
			return new CryptoPBKDFDeriver(createSecretKeyFactory(), primitivesService, saltBitLength, iterations, keyLength, minPasswordLength,
					maxPasswordLength);
		} catch (final GeneralCryptoLibException e) {
			throw new CryptoLibException(e);
		}
	}

	/**
	 * Returns the length of salt from {@link ConfigPBKDFDerivationParameters} in bits.
	 *
	 * @return the length of salt in bits
	 */
	public int getSaltBitLength() {
		return derivationPolicy.getPBKDFDerivationParameters().getSaltBitLength();
	}

	private SecretKeyFactory createSecretKeyFactory() {
		try {
			if (Provider.DEFAULT == derivationPolicy.getSecureRandomAlgorithmAndProvider().getProvider()) {
				return SecretKeyFactory.getInstance(derivationPolicy.getPBKDFDerivationParameters().getAlgorithm());
			} else {
				return SecretKeyFactory.getInstance(derivationPolicy.getPBKDFDerivationParameters().getAlgorithm(),
						derivationPolicy.getPBKDFDerivationParameters().getProvider().getProviderName());
			}
		} catch (final NoSuchAlgorithmException | NoSuchProviderException e) {
			throw new CryptoLibException(KEY_DERIVATION_ERROR, e);
		}
	}
}
