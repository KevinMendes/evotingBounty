/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.primitives.derivation.configuration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.commons.configuration.Provider;
import ch.post.it.evoting.cryptolib.primitives.derivation.constants.DerivationConstants;
import ch.post.it.evoting.cryptolib.primitives.messagedigest.configuration.HashAlgorithm;

class ConfigPBKDFDerivationParametersTest {

	public static final int KEY_BITS_LENGTH_128 = 128;

	public static final int ITERATIONS_32000 = 32000;

	@Test
	void whenGivenPBKBF2WithSunJCEProviderAndHashFunctionKL128() {

		Assertions.assertEquals(DerivationConstants.PBKDF2_HMAC_SHA256,
				ConfigPBKDFDerivationParameters.PBKDF2_32000_SHA256_256_SUNJCE_KL128.getAlgorithm());

		Assertions.assertEquals(Provider.SUN_JCE, ConfigPBKDFDerivationParameters.PBKDF2_32000_SHA256_256_SUNJCE_KL128.getProvider());

		Assertions.assertEquals(ITERATIONS_32000, ConfigPBKDFDerivationParameters.PBKDF2_32000_SHA256_256_SUNJCE_KL128.getIterations());

		Assertions.assertEquals(HashAlgorithm.SHA256.getAlgorithm(),
				ConfigPBKDFDerivationParameters.PBKDF2_32000_SHA256_256_SUNJCE_KL128.getHashAlgorithm());

		Assertions.assertEquals(KEY_BITS_LENGTH_128, ConfigPBKDFDerivationParameters.PBKDF2_32000_SHA256_256_SUNJCE_KL128.getKeyBitLength());
	}

	@Test
	void whenGivenPBKBF2WithBCProviderAndHashFunctionKL128() {

		Assertions.assertEquals(DerivationConstants.PBKDF2_HMAC_SHA256,
				ConfigPBKDFDerivationParameters.PBKDF2_32000_SHA256_256_BC_KL128.getAlgorithm());

		Assertions.assertEquals(Provider.BOUNCY_CASTLE, ConfigPBKDFDerivationParameters.PBKDF2_32000_SHA256_256_BC_KL128.getProvider());

		Assertions.assertEquals(ITERATIONS_32000, ConfigPBKDFDerivationParameters.PBKDF2_32000_SHA256_256_BC_KL128.getIterations());

		Assertions.assertEquals(HashAlgorithm.SHA256.getAlgorithm(),
				ConfigPBKDFDerivationParameters.PBKDF2_32000_SHA256_256_BC_KL128.getHashAlgorithm());

		Assertions.assertEquals(KEY_BITS_LENGTH_128, ConfigPBKDFDerivationParameters.PBKDF2_32000_SHA256_256_BC_KL128.getKeyBitLength());
	}
}
