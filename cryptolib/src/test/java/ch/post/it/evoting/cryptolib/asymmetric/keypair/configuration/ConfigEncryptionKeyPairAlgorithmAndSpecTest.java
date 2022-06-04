/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.asymmetric.keypair.configuration;

import java.security.spec.RSAKeyGenParameterSpec;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests of ConfigEncryptionKeyPairAlgorithmAndSpec.
 */
class ConfigEncryptionKeyPairAlgorithmAndSpecTest {

	@Test
	void givenRSA2048SunRsaSignThenExpectedValues() {

		assertRSAEquals("RSA", new RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4), "SunRsaSign",
				ConfigEncryptionKeyPairAlgorithmAndSpec.RSA_2048_F4_SUN_RSA_SIGN);
	}

	@Test
	void givenRSA3072SunRsaSignThenExpectedValues() {

		assertRSAEquals("RSA", new RSAKeyGenParameterSpec(3072, RSAKeyGenParameterSpec.F4), "SunRsaSign",
				ConfigEncryptionKeyPairAlgorithmAndSpec.RSA_3072_F4_SUN_RSA_SIGN);
	}

	@Test
	void givenRSA4096SunRsaSignThenExpectedValues() {

		assertRSAEquals("RSA", new RSAKeyGenParameterSpec(4096, RSAKeyGenParameterSpec.F4), "SunRsaSign",
				ConfigEncryptionKeyPairAlgorithmAndSpec.RSA_4096_F4_SUN_RSA_SIGN);
	}

	@Test
	void givenRSA2048BCThenExpectedValues() {

		assertRSAEquals("RSA", new RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4), BouncyCastleProvider.PROVIDER_NAME,
				ConfigEncryptionKeyPairAlgorithmAndSpec.RSA_2048_F4_BC);
	}

	@Test
	void givenRSA3072BCThenExpectedValues() {

		assertRSAEquals("RSA", new RSAKeyGenParameterSpec(3072, RSAKeyGenParameterSpec.F4), BouncyCastleProvider.PROVIDER_NAME,
				ConfigEncryptionKeyPairAlgorithmAndSpec.RSA_3072_F4_BC);
	}

	@Test
	void givenRSA4096BCThenExpectedValues() {

		assertRSAEquals("RSA", new RSAKeyGenParameterSpec(4096, RSAKeyGenParameterSpec.F4), BouncyCastleProvider.PROVIDER_NAME,
				ConfigEncryptionKeyPairAlgorithmAndSpec.RSA_4096_F4_BC);
	}

	private void assertRSAEquals(final String expectedAlgorithm, final RSAKeyGenParameterSpec expectedSpec, final String expectedProvider,
			final ConfigEncryptionKeyPairAlgorithmAndSpec keyPairAlgorithmAndSpec) {

		String errorMsg = "Encryption algorithm was not the expected value";
		Assertions.assertEquals(expectedAlgorithm, keyPairAlgorithmAndSpec.getAlgorithm(), errorMsg);

		errorMsg = "Encryption key size";
		Assertions.assertEquals(expectedSpec.getKeysize(), ((RSAKeyGenParameterSpec) keyPairAlgorithmAndSpec.getSpec()).getKeysize(), errorMsg);

		errorMsg = "Encryption spec public exponent";
		Assertions.assertEquals(0,
				expectedSpec.getPublicExponent().compareTo(((RSAKeyGenParameterSpec) keyPairAlgorithmAndSpec.getSpec()).getPublicExponent()),
				errorMsg);

		errorMsg = "Provider was not the expected value";
		Assertions.assertEquals(expectedProvider, keyPairAlgorithmAndSpec.getProvider().getProviderName(), errorMsg);
	}
}
