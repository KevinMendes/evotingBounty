/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.primitives.derivation.factory;

import javax.xml.bind.DatatypeConverter;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.api.derivation.CryptoAPIDerivedKey;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.primitives.derivation.configuration.ConfigKDFDerivationParameters;
import ch.post.it.evoting.cryptolib.primitives.derivation.configuration.ConfigPBKDFDerivationParameters;
import ch.post.it.evoting.cryptolib.primitives.derivation.configuration.DerivationPolicy;
import ch.post.it.evoting.cryptolib.primitives.derivation.constants.DerivationConstants;
import ch.post.it.evoting.cryptolib.primitives.securerandom.configuration.ConfigSecureRandomAlgorithmAndProvider;

class CryptoKDFDeriverTest {

	private static final String SEED_AS_HEX = "deadbeeffeebdaed";

	private static final int KEY_BYTE_LENGTH = 32;

	private static final String EXPECTED_DERIVED_KEY_AS_HEX = "2a598c866ba1914fbf19f9528c1a676936a1567dd27c51cd3bf4a561c60be610";

	private final CryptoKeyDeriverFactory _cryptoKeyDeriverFactory = new CryptoKeyDeriverFactory(getKeyDerivationPolicy());

	@Test
	void testThatDerivesKeyForKDF2SHA256BCProviderKL128() throws GeneralCryptoLibException {

		final CryptoKDFDeriver cryptoKDFDeriver = _cryptoKeyDeriverFactory.createKDFDeriver();

		final CryptoAPIDerivedKey cryptoKDFDerivedKey;
		cryptoKDFDerivedKey = cryptoKDFDeriver.deriveKey(DatatypeConverter.parseHexBinary(SEED_AS_HEX), KEY_BYTE_LENGTH);

		final String derivedKeyAsHex = DatatypeConverter.printHexBinary(cryptoKDFDerivedKey.getEncoded()).toLowerCase();

		Assertions.assertNotNull(cryptoKDFDerivedKey);
		Assertions.assertEquals(KEY_BYTE_LENGTH, cryptoKDFDerivedKey.getEncoded().length);
		Assertions.assertEquals(EXPECTED_DERIVED_KEY_AS_HEX, derivedKeyAsHex);
	}

	@Test
	void createCryptoKDFDeriverTest() throws GeneralCryptoLibException {
		final CryptoKDFDeriver cryptoKDFDeriver = new CryptoKDFDeriver(ConfigKDFDerivationParameters.MGF1_SHA256_BC);
		Assertions.assertNotNull(cryptoKDFDeriver);
	}

	private DerivationPolicy getKeyDerivationPolicy() {
		return new DerivationPolicy() {

			@Override
			public ConfigKDFDerivationParameters getKDFDerivationParameters() {
				return ConfigKDFDerivationParameters.MGF1_SHA256_BC;
			}

			@Override
			public ConfigPBKDFDerivationParameters getPBKDFDerivationParameters() {
				return ConfigPBKDFDerivationParameters.PBKDF2_32000_SHA256_256_BC_KL128;
			}

			@Override
			public int getPBKDFDerivationMinPasswordLength() {
				return DerivationConstants.MINIMUM_PBKDF_PASSWORD_LENGTH;
			}

			@Override
			public int getPBKDFDerivationMaxPasswordLength() {
				return DerivationConstants.MAXIMUM_PBKDF_PASSWORD_LENGTH;
			}

			@Override
			public ConfigSecureRandomAlgorithmAndProvider getSecureRandomAlgorithmAndProvider() {
				return ConfigSecureRandomAlgorithmAndProvider.NATIVE_PRNG_SUN;
			}
		};
	}
}
