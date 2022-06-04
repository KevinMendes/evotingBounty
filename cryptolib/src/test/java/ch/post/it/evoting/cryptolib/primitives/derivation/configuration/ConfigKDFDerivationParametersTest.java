/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.primitives.derivation.configuration;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.commons.configuration.Provider;
import ch.post.it.evoting.cryptolib.primitives.derivation.constants.DerivationConstants;
import ch.post.it.evoting.cryptolib.primitives.messagedigest.configuration.HashAlgorithm;

class ConfigKDFDerivationParametersTest {

	@Test
	void whenGivenMGF1WithBCProviderAndHashFunctionSha256() {

		Assertions.assertEquals(DerivationConstants.MGF1, ConfigKDFDerivationParameters.MGF1_SHA256_BC.getAlgorithm());

		Assertions.assertEquals(HashAlgorithm.SHA256.getAlgorithm(), ConfigKDFDerivationParameters.MGF1_SHA256_BC.getHashAlgorithm());

		Assertions.assertEquals(Provider.BOUNCY_CASTLE, ConfigKDFDerivationParameters.MGF1_SHA256_BC.getProvider());
	}
}
