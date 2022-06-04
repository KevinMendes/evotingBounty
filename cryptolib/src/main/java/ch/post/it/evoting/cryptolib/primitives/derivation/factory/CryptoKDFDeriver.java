/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.primitives.derivation.factory;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SHA256Digest;
import org.bouncycastle.crypto.generators.MGF1BytesGenerator;
import org.bouncycastle.crypto.params.MGFParameters;

import ch.post.it.evoting.cryptolib.api.derivation.CryptoAPIDerivedKey;
import ch.post.it.evoting.cryptolib.api.derivation.CryptoAPIKDFDeriver;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.commons.configuration.Provider;
import ch.post.it.evoting.cryptolib.commons.validations.Validate;
import ch.post.it.evoting.cryptolib.primitives.derivation.configuration.ConfigKDFDerivationParameters;
import ch.post.it.evoting.cryptolib.primitives.derivation.constants.DerivationConstants;
import ch.post.it.evoting.cryptolib.primitives.messagedigest.configuration.HashAlgorithm;

/**
 * Class that provides the functionality for key derivation.
 */
public class CryptoKDFDeriver implements CryptoAPIKDFDeriver {

	private final MGF1BytesGenerator byteGenerator;

	/**
	 * Creates an instance of key deriver configured by provided {@code params}.
	 *
	 * @param params The key derivation configuration parameters.
	 */
	CryptoKDFDeriver(final ConfigKDFDerivationParameters params) throws GeneralCryptoLibException {

		byteGenerator = configureByteGenerator(params);
	}

	private static MGF1BytesGenerator configureByteGenerator(final ConfigKDFDerivationParameters params) throws GeneralCryptoLibException {
		validateProvider(params.getProvider());
		validateAlgorithm(params.getAlgorithm());

		final Digest digest = configureDigest(params.getHashAlgorithm());
		return new MGF1BytesGenerator(digest);
	}

	private static void validateProvider(final Provider provider) throws GeneralCryptoLibException {
		if (provider != Provider.BOUNCY_CASTLE && provider != Provider.DEFAULT) {
			throw new GeneralCryptoLibException("Key derivation is not presently defined for provider " + provider.getProviderName());
		}
	}

	private static void validateAlgorithm(final String algorithm) throws GeneralCryptoLibException {
		if (!DerivationConstants.MGF1.equals(algorithm)) {
			throw new GeneralCryptoLibException("Key derivation is not presently defined for algorithm " + algorithm);
		}
	}

	private static Digest configureDigest(final String hashAlgorithm) throws GeneralCryptoLibException {
		if (HashAlgorithm.SHA256.getAlgorithm().equals(hashAlgorithm)) {
			return new SHA256Digest();
		}

		throw new GeneralCryptoLibException("Key derivation is not presently defined for hash algorithm " + hashAlgorithm);
	}

	@Override
	public CryptoAPIDerivedKey deriveKey(final byte[] seed, final int lengthInBytes) throws GeneralCryptoLibException {

		Validate.notNullOrEmpty(seed, "Seed");
		Validate.isPositive(lengthInBytes, "Length in bytes");

		final byte[] keyBytes = new byte[lengthInBytes];
		byteGenerator.init(new MGFParameters(seed));
		byteGenerator.generateBytes(keyBytes, 0, lengthInBytes);
		return new CryptoDerivedKey(keyBytes);
	}
}
