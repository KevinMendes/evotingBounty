/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.symmetric.key.factory;

import javax.crypto.SecretKey;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.commons.system.OperatingSystem;
import ch.post.it.evoting.cryptolib.primitives.securerandom.configuration.ConfigSecureRandomAlgorithmAndProvider;
import ch.post.it.evoting.cryptolib.symmetric.key.configuration.ConfigHmacSecretKeyAlgorithmAndSpec;
import ch.post.it.evoting.cryptolib.symmetric.key.configuration.ConfigSecretKeyAlgorithmAndSpec;
import ch.post.it.evoting.cryptolib.symmetric.key.configuration.SymmetricKeyPolicy;

class SecretKeyGeneratorFactoryTest {

	public static SymmetricKeyPolicy symmetricKeyPolicy;

	public static SecretKeyGeneratorFactory secretKeyGeneratorFactory;

	@BeforeAll
	public static void setUp() {

		symmetricKeyPolicy = getPolicy();

		secretKeyGeneratorFactory = new SecretKeyGeneratorFactory(symmetricKeyPolicy);
	}

	private static SymmetricKeyPolicy getPolicy() {

		return new SymmetricKeyPolicy() {

			@Override
			public ConfigSecretKeyAlgorithmAndSpec getSecretKeyAlgorithmAndSpec() {
				return ConfigSecretKeyAlgorithmAndSpec.AES_128_BC;
			}

			@Override
			public ConfigSecureRandomAlgorithmAndProvider getSecureRandomAlgorithmAndProvider() {
				return getSecureRandomConfig();
			}

			@Override
			public ConfigHmacSecretKeyAlgorithmAndSpec getHmacSecretKeyAlgorithmAndSpec() {
				return ConfigHmacSecretKeyAlgorithmAndSpec.HMAC_WITH_SHA256_256;
			}
		};
	}

	private static ConfigSecureRandomAlgorithmAndProvider getSecureRandomConfig() {

		switch (OperatingSystem.current()) {
		case WINDOWS:
			return ConfigSecureRandomAlgorithmAndProvider.PRNG_SUN_MSCAPI;
		case UNIX:
			return ConfigSecureRandomAlgorithmAndProvider.NATIVE_PRNG_SUN;
		default:
			throw new CryptoLibException("OS not supported");
		}
	}

	@Test
	void givenFactoryWhenCreateGeneratorAndSecretKeyThenExpectedKeyAttributes() {

		final SecretKey secretKey = secretKeyGeneratorFactory.createGeneratorForEncryption().genSecretKey();

		String errorMsg = "The created secret key does not have the expected algorithm";
		Assertions.assertEquals("AES", secretKey.getAlgorithm(), errorMsg);

		final int expectedNumBytes = 128 / Byte.SIZE;
		errorMsg = "The created secret key does not have the expected length";
		Assertions.assertEquals(expectedNumBytes, secretKey.getEncoded().length, errorMsg);
	}

	@Test
	void givenFactoryWhenCreateGeneratorAndHmacSecretKeyThenExpectedKeyAttributes() {

		final SecretKey secretKey = secretKeyGeneratorFactory.createGeneratorForHmac().genSecretKey();

		String errorMsg = "The created secret key does not have the expected algorithm";
		Assertions.assertEquals("HmacSHA256", secretKey.getAlgorithm(), errorMsg);

		final int expectedNumBytes = 256 / Byte.SIZE;
		errorMsg = "The created secret key does not have the expected length";
		Assertions.assertEquals(expectedNumBytes, secretKey.getEncoded().length, errorMsg);
	}
}
