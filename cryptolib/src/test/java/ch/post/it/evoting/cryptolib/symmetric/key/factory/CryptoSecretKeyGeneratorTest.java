/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.symmetric.key.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.commons.configuration.Provider;
import ch.post.it.evoting.cryptolib.commons.system.OperatingSystem;
import ch.post.it.evoting.cryptolib.primitives.securerandom.configuration.ConfigSecureRandomAlgorithmAndProvider;
import ch.post.it.evoting.cryptolib.symmetric.key.configuration.ConfigHmacSecretKeyAlgorithmAndSpec;
import ch.post.it.evoting.cryptolib.symmetric.key.configuration.ConfigSecretKeyAlgorithmAndSpec;
import ch.post.it.evoting.cryptolib.symmetric.key.configuration.SecretKeyProvider;
import ch.post.it.evoting.cryptolib.symmetric.key.configuration.SymmetricKeyPolicy;
import ch.post.it.evoting.cryptolib.symmetric.key.constants.SecretKeyConstants;

class CryptoSecretKeyGeneratorTest {

	private static SymmetricKeyPolicy policy;
	private static CryptoSecretKeyGeneratorForEncryption cryptoSecretKeyGeneratorForEncryption;
	private static CryptoSecretKeyGeneratorForHmac cryptoSecretKeyGeneratorForHmac;

	@BeforeAll
	static void setUp() {

		policy = getPolicy();
		final SecretKeyGeneratorFactory secretKeyGeneratorFactory = new SecretKeyGeneratorFactory(policy);
		cryptoSecretKeyGeneratorForEncryption = secretKeyGeneratorFactory.createGeneratorForEncryption();
		cryptoSecretKeyGeneratorForHmac = secretKeyGeneratorFactory.createGeneratorForHmac();
	}

	private static SymmetricKeyPolicy getNonCompliantSecureRandomProviderPolicy() {
		final ConfigSecureRandomAlgorithmAndProvider mockedSecureRandomSpec = mock(ConfigSecureRandomAlgorithmAndProvider.class);
		when(mockedSecureRandomSpec.getProvider()).thenReturn(Provider.BOUNCY_CASTLE);
		when(mockedSecureRandomSpec.isOSCompliant(any())).thenReturn(false);

		return new SymmetricKeyPolicy() {
			@Override
			public ConfigSecretKeyAlgorithmAndSpec getSecretKeyAlgorithmAndSpec() {
				return ConfigSecretKeyAlgorithmAndSpec.AES_128_SUNJCE;
			}

			@Override
			public ConfigSecureRandomAlgorithmAndProvider getSecureRandomAlgorithmAndProvider() {
				return mockedSecureRandomSpec;
			}

			@Override
			public ConfigHmacSecretKeyAlgorithmAndSpec getHmacSecretKeyAlgorithmAndSpec() {
				return ConfigHmacSecretKeyAlgorithmAndSpec.HMAC_WITH_SHA256_256;
			}
		};
	}

	private static SymmetricKeyPolicy getInvalidKeyLengthPolicy() {
		final ConfigSecretKeyAlgorithmAndSpec mockedSecretKeySpec = mock(ConfigSecretKeyAlgorithmAndSpec.class);
		when(mockedSecretKeySpec.getAlgorithm()).thenReturn(SecretKeyConstants.AES_ALG);
		when(mockedSecretKeySpec.getProvider()).thenReturn(SecretKeyProvider.SUNJCE.getProvider());
		when(mockedSecretKeySpec.getKeyLength()).thenReturn(-1);

		return new SymmetricKeyPolicy() {
			@Override
			public ConfigSecretKeyAlgorithmAndSpec getSecretKeyAlgorithmAndSpec() {
				return mockedSecretKeySpec;
			}

			@Override
			public ConfigSecureRandomAlgorithmAndProvider getSecureRandomAlgorithmAndProvider() {
				return getOsDependentSecureRandomAlgorithmAndProvider();
			}

			@Override
			public ConfigHmacSecretKeyAlgorithmAndSpec getHmacSecretKeyAlgorithmAndSpec() {
				return ConfigHmacSecretKeyAlgorithmAndSpec.HMAC_WITH_SHA256_256;
			}
		};
	}

	private static SymmetricKeyPolicy getPolicy() {

		return new SymmetricKeyPolicy() {

			@Override
			public ConfigSecretKeyAlgorithmAndSpec getSecretKeyAlgorithmAndSpec() {
				return ConfigSecretKeyAlgorithmAndSpec.AES_128_SUNJCE;
			}

			@Override
			public ConfigSecureRandomAlgorithmAndProvider getSecureRandomAlgorithmAndProvider() {
				return getOsDependentSecureRandomAlgorithmAndProvider();
			}

			@Override
			public ConfigHmacSecretKeyAlgorithmAndSpec getHmacSecretKeyAlgorithmAndSpec() {
				return ConfigHmacSecretKeyAlgorithmAndSpec.HMAC_WITH_SHA256_256;
			}
		};
	}

	private static ConfigSecureRandomAlgorithmAndProvider getOsDependentSecureRandomAlgorithmAndProvider() {

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
		final SecretKey secretKey = cryptoSecretKeyGeneratorForEncryption.genSecretKey();

		final String errorMsgAlgo = "The created secret key does not have the expected algorithm";
		assertEquals("AES", secretKey.getAlgorithm(), errorMsgAlgo);

		final int expectedNumBytes = 128 / Byte.SIZE;
		final String errorMsgLength = "The created secret key does not have the expected length";
		assertEquals(expectedNumBytes, secretKey.getEncoded().length, errorMsgLength);
	}

	@Test
	void givenFactoryWhenCreateGeneratorAndHmacSecretKeyThenExpectedKeyAttributes() {
		final SecretKey secretKey = cryptoSecretKeyGeneratorForHmac.genSecretKey();

		final String errorMsgAlgo = "The created secret key does not have the expected algorithm";
		assertEquals("HmacSHA256", secretKey.getAlgorithm(), errorMsgAlgo);

		final int expectedNumBytes = 256 / Byte.SIZE;
		final String errorMsgLength = "The created secret key does not have the expected length";
		assertEquals(expectedNumBytes, secretKey.getEncoded().length, errorMsgLength);
	}

	@Test
	void givenUnsupportedAlgorithmWhenCreateSecretKeyGeneratorThenException() {
		try (final MockedStatic<KeyGenerator> mockedKeyGenerator = Mockito.mockStatic(KeyGenerator.class)) {
			mockedKeyGenerator.when(() -> KeyGenerator.getInstance(any(String.class), any(String.class))).thenThrow(NoSuchAlgorithmException.class);

			assertThrows(CryptoLibException.class, () -> new SecretKeyGeneratorFactory(policy).createGeneratorForEncryption());
		}
	}

	@Test
	void givenBadKeyLengthWhenCreateSecretKeyGeneratorThenException() {
		final SymmetricKeyPolicy policy = getInvalidKeyLengthPolicy();

		assertThrows(CryptoLibException.class, () -> new SecretKeyGeneratorFactory(policy).createGeneratorForEncryption());
	}

	@Test
	void givenBadOS() {
		final SymmetricKeyPolicy policy = getNonCompliantSecureRandomProviderPolicy();

		assertThrows(CryptoLibException.class, () -> new SecretKeyGeneratorFactory(policy).createGeneratorForEncryption());
	}

}
