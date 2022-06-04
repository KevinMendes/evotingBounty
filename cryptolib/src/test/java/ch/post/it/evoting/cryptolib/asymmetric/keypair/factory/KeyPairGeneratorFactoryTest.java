/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.asymmetric.keypair.factory;

import static ch.post.it.evoting.cryptolib.primitives.securerandom.configuration.ConfigSecureRandomAlgorithmAndProvider.NATIVE_PRNG_SUN;
import static ch.post.it.evoting.cryptolib.primitives.securerandom.configuration.ConfigSecureRandomAlgorithmAndProvider.PRNG_SUN_MSCAPI;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.RSAKeyGenParameterSpec;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.asymmetric.keypair.configuration.ConfigEncryptionKeyPairAlgorithmAndSpec;
import ch.post.it.evoting.cryptolib.asymmetric.keypair.configuration.ConfigSigningKeyPairAlgorithmAndSpec;
import ch.post.it.evoting.cryptolib.asymmetric.keypair.configuration.KeyPairPolicy;
import ch.post.it.evoting.cryptolib.asymmetric.keypair.constants.KeyPairConstants;
import ch.post.it.evoting.cryptolib.commons.configuration.Provider;
import ch.post.it.evoting.cryptolib.commons.system.OperatingSystem;
import ch.post.it.evoting.cryptolib.primitives.securerandom.configuration.ConfigSecureRandomAlgorithmAndProvider;

@ExtendWith(MockitoExtension.class)
class KeyPairGeneratorFactoryTest {

	public static final String BAD_ALGORITHM = "Bad algorithm";
	private static KeyPairPolicy policy;
	private static KeyPairGeneratorFactory keyPairFactory;

	@BeforeAll
	static void setUp() {
		policy = getPolicy();
		keyPairFactory = new KeyPairGeneratorFactory(policy);
	}

	private static KeyPairPolicy getPolicy() {
		return new KeyPairPolicy() {

			@Override
			public ConfigSigningKeyPairAlgorithmAndSpec getSigningKeyPairAlgorithmAndSpec() {
				return ConfigSigningKeyPairAlgorithmAndSpec.RSA_2048_F4_SUN_RSA_SIGN;
			}

			@Override
			public ConfigSecureRandomAlgorithmAndProvider getSecureRandomAlgorithmAndProvider() {
				return getOsDependentSecureRandomAlgorithmAndProvider();
			}

			@Override
			public ConfigEncryptionKeyPairAlgorithmAndSpec getEncryptingKeyPairAlgorithmAndSpec() {
				return ConfigEncryptionKeyPairAlgorithmAndSpec.RSA_2048_F4_BC;
			}
		};
	}

	private static KeyPairPolicy getBadAlgorithmSigningPolicy() {
		final ConfigSigningKeyPairAlgorithmAndSpec mockedSigningSpec = mock(ConfigSigningKeyPairAlgorithmAndSpec.class);
		when(mockedSigningSpec.getAlgorithm()).thenReturn(BAD_ALGORITHM);
		when(mockedSigningSpec.getProvider()).thenReturn(Provider.BOUNCY_CASTLE);

		return new KeyPairPolicy() {

			@Override
			public ConfigSigningKeyPairAlgorithmAndSpec getSigningKeyPairAlgorithmAndSpec() {
				return mockedSigningSpec;
			}

			@Override
			public ConfigSecureRandomAlgorithmAndProvider getSecureRandomAlgorithmAndProvider() {
				return getOsDependentSecureRandomAlgorithmAndProvider();
			}

			@Override
			public ConfigEncryptionKeyPairAlgorithmAndSpec getEncryptingKeyPairAlgorithmAndSpec() {
				return ConfigEncryptionKeyPairAlgorithmAndSpec.RSA_2048_F4_BC;
			}
		};
	}

	private static KeyPairPolicy getBadAlgorithmEncryptionPolicy() {
		final ConfigEncryptionKeyPairAlgorithmAndSpec mockedEncryptionSpec = mock(ConfigEncryptionKeyPairAlgorithmAndSpec.class);
		when(mockedEncryptionSpec.getAlgorithm()).thenReturn(BAD_ALGORITHM);
		when(mockedEncryptionSpec.getProvider()).thenReturn(Provider.BOUNCY_CASTLE);

		return new KeyPairPolicy() {

			@Override
			public ConfigSigningKeyPairAlgorithmAndSpec getSigningKeyPairAlgorithmAndSpec() {
				return ConfigSigningKeyPairAlgorithmAndSpec.RSA_2048_F4_SUN_RSA_SIGN;
			}

			@Override
			public ConfigSecureRandomAlgorithmAndProvider getSecureRandomAlgorithmAndProvider() {
				return getOsDependentSecureRandomAlgorithmAndProvider();
			}

			@Override
			public ConfigEncryptionKeyPairAlgorithmAndSpec getEncryptingKeyPairAlgorithmAndSpec() {
				return mockedEncryptionSpec;
			}
		};
	}

	private static KeyPairPolicy getDefaultProviderPolicy() {
		final ConfigSigningKeyPairAlgorithmAndSpec mockedSigningSpec = mock(ConfigSigningKeyPairAlgorithmAndSpec.class);
		when(mockedSigningSpec.getAlgorithm()).thenReturn(KeyPairConstants.RSA_ALG);
		when(mockedSigningSpec.getSpec()).thenReturn(new RSAKeyGenParameterSpec(2048, RSAKeyGenParameterSpec.F4));
		when(mockedSigningSpec.getProvider()).thenReturn(Provider.DEFAULT);

		return new KeyPairPolicy() {

			@Override
			public ConfigSigningKeyPairAlgorithmAndSpec getSigningKeyPairAlgorithmAndSpec() {
				return mockedSigningSpec;
			}

			@Override
			public ConfigSecureRandomAlgorithmAndProvider getSecureRandomAlgorithmAndProvider() {
				return getOsDependentSecureRandomAlgorithmAndProvider();
			}

			@Override
			public ConfigEncryptionKeyPairAlgorithmAndSpec getEncryptingKeyPairAlgorithmAndSpec() {
				return ConfigEncryptionKeyPairAlgorithmAndSpec.RSA_2048_F4_BC;
			}
		};
	}

	private static KeyPairPolicy getDefaultSecureRandomProviderPolicy() {
		final ConfigSecureRandomAlgorithmAndProvider osDependent = getOsDependentSecureRandomAlgorithmAndProvider();

		final ConfigSecureRandomAlgorithmAndProvider mockedSecureRandomSpec = mock(ConfigSecureRandomAlgorithmAndProvider.class);
		when(mockedSecureRandomSpec.getAlgorithm()).thenReturn(osDependent.getAlgorithm());
		when(mockedSecureRandomSpec.getProvider()).thenReturn(Provider.DEFAULT);
		when(mockedSecureRandomSpec.isOSCompliant(any())).thenReturn(true);

		return new KeyPairPolicy() {

			@Override
			public ConfigSigningKeyPairAlgorithmAndSpec getSigningKeyPairAlgorithmAndSpec() {
				return ConfigSigningKeyPairAlgorithmAndSpec.RSA_2048_F4_SUN_RSA_SIGN;
			}

			@Override
			public ConfigSecureRandomAlgorithmAndProvider getSecureRandomAlgorithmAndProvider() {
				return mockedSecureRandomSpec;
			}

			@Override
			public ConfigEncryptionKeyPairAlgorithmAndSpec getEncryptingKeyPairAlgorithmAndSpec() {
				return ConfigEncryptionKeyPairAlgorithmAndSpec.RSA_2048_F4_BC;
			}
		};
	}

	private static KeyPairPolicy getNonCompliantSecureRandomProviderPolicy() {
		final ConfigSecureRandomAlgorithmAndProvider mockedSecureRandomSpec = mock(ConfigSecureRandomAlgorithmAndProvider.class);
		when(mockedSecureRandomSpec.getProvider()).thenReturn(Provider.BOUNCY_CASTLE);
		when(mockedSecureRandomSpec.isOSCompliant(any())).thenReturn(false);

		return new KeyPairPolicy() {

			@Override
			public ConfigSigningKeyPairAlgorithmAndSpec getSigningKeyPairAlgorithmAndSpec() {
				return ConfigSigningKeyPairAlgorithmAndSpec.RSA_2048_F4_SUN_RSA_SIGN;
			}

			@Override
			public ConfigSecureRandomAlgorithmAndProvider getSecureRandomAlgorithmAndProvider() {
				return mockedSecureRandomSpec;
			}

			@Override
			public ConfigEncryptionKeyPairAlgorithmAndSpec getEncryptingKeyPairAlgorithmAndSpec() {
				return ConfigEncryptionKeyPairAlgorithmAndSpec.RSA_2048_F4_BC;
			}
		};
	}

	private static ConfigSecureRandomAlgorithmAndProvider getOsDependentSecureRandomAlgorithmAndProvider() {

		switch (OperatingSystem.current()) {
		case WINDOWS:
			return PRNG_SUN_MSCAPI;
		case UNIX:
			return NATIVE_PRNG_SUN;
		default:
			throw new CryptoLibException("OS not supported");
		}
	}

	@Test
	void givenKeyPairGeneratorFactoryWhenCreateEncryptionKeyPairThenExpectedValues() {
		final CryptoKeyPairGenerator keyPairGenerator = keyPairFactory.createEncryption();
		final KeyPair keyPair = keyPairGenerator.genKeyPair();

		assertEncryptionKeyPairHasExpectedValues(keyPair);
	}

	@Test
	void givenKeyPairGeneratorFactoryWhenCreateSigningKeyPairThenExpectedValues() {
		final CryptoKeyPairGenerator keyPairGenerator = keyPairFactory.createSigning();
		final KeyPair keyPair = keyPairGenerator.genKeyPair();

		assertSigningKeyPairHasExpectedValues(keyPair);
	}

	// ===============================================================================================================================================
	// Different testing policies.
	// ===============================================================================================================================================

	@Test
	void singingPolicyWithDefaultProvider() {
		final KeyPairGeneratorFactory keyPairFactory = new KeyPairGeneratorFactory(getDefaultProviderPolicy());
		final CryptoKeyPairGenerator keyPairGenerator = keyPairFactory.createSigning();
		final KeyPair keyPair = keyPairGenerator.genKeyPair();

		assertSigningKeyPairHasExpectedValues(keyPair);
	}

	@Test
	void singingPolicyWithBadAlgorithmShouldThrow() {
		final KeyPairGeneratorFactory keyPairFactory = new KeyPairGeneratorFactory(getBadAlgorithmSigningPolicy());

		final CryptoLibException exception = assertThrows(CryptoLibException.class, keyPairFactory::createSigning);
		assertTrue(exception.getCause() instanceof GeneralSecurityException);
		assertTrue(exception.getMessage().contains("Exception while trying to get an instance of KeyPairGenerator. Algorithm was: " + BAD_ALGORITHM));
	}

	@Test
	void encryptionPolicyWithBadAlgorithmShouldThrow() {
		final KeyPairGeneratorFactory keyPairFactory = new KeyPairGeneratorFactory(getBadAlgorithmEncryptionPolicy());

		final CryptoLibException exception = assertThrows(CryptoLibException.class, keyPairFactory::createEncryption);
		assertTrue(exception.getCause() instanceof GeneralSecurityException);
		assertTrue(exception.getMessage().contains("Exception while trying to get an instance of KeyPairGenerator. Algorithm was: " + BAD_ALGORITHM));
	}

	@Test
	void badSecureRandom() {
		final KeyPairGeneratorFactory keyPairFactory = new KeyPairGeneratorFactory(getPolicy());

		try (final MockedStatic<SecureRandom> mockedSecureRandom = mockStatic(SecureRandom.class)) {
			mockedSecureRandom.when(() -> SecureRandom.getInstance(any(String.class), any(String.class))).thenThrow(NoSuchAlgorithmException.class);

			final CryptoLibException exception = assertThrows(CryptoLibException.class, keyPairFactory::createSigning);
			assertEquals("Exception while initializing the KeyPairGenerator", exception.getMessage());
		}
	}

	@Test
	void secureRandomWithDefaultProviderShouldNotThrow() {
		final KeyPairGeneratorFactory keyPairFactory = new KeyPairGeneratorFactory(getDefaultSecureRandomProviderPolicy());
		final CryptoKeyPairGenerator keyPairGenerator = keyPairFactory.createSigning();
		final KeyPair keyPair = keyPairGenerator.genKeyPair();

		assertSigningKeyPairHasExpectedValues(keyPair);
	}

	@Test
	void badSecureRandomWithDefaultProvider() {
		final KeyPairGeneratorFactory keyPairFactory = new KeyPairGeneratorFactory(getDefaultSecureRandomProviderPolicy());

		try (final MockedStatic<SecureRandom> mockedSecureRandom = mockStatic(SecureRandom.class)) {
			mockedSecureRandom.when(() -> SecureRandom.getInstance(any(String.class))).thenThrow(NoSuchAlgorithmException.class);

			final CryptoLibException exception = assertThrows(CryptoLibException.class, keyPairFactory::createSigning);
			assertEquals("Exception while initializing the KeyPairGenerator", exception.getMessage());
		}
	}

	// ===============================================================================================================================================
	// Utilities.
	// ===============================================================================================================================================

	@Test
	void systemNotCompliantShouldThrow() {
		final KeyPairGeneratorFactory keyPairFactory = new KeyPairGeneratorFactory(getNonCompliantSecureRandomProviderPolicy());

		final CryptoLibException exception = assertThrows(CryptoLibException.class, keyPairFactory::createSigning);
		assertTrue(exception.getMessage().contains("The given algorithm and provider are not compliant with the "));
	}

	private void assertEncryptionKeyPairHasExpectedValues(final KeyPair keyPair) {

		assertPublicKeyHasExpectedAlgorithmAndFormat(policy.getSigningKeyPairAlgorithmAndSpec().getAlgorithm(), keyPair.getPublic());

		assertPrivateKeyHasExpectedAlgorithmAndFormat(policy.getSigningKeyPairAlgorithmAndSpec().getAlgorithm(), keyPair.getPrivate());
	}

	private void assertSigningKeyPairHasExpectedValues(final KeyPair keyPair) {

		assertPublicKeyHasExpectedAlgorithmAndFormat(policy.getSigningKeyPairAlgorithmAndSpec().getAlgorithm(), keyPair.getPublic());

		assertPrivateKeyHasExpectedAlgorithmAndFormat(policy.getSigningKeyPairAlgorithmAndSpec().getAlgorithm(), keyPair.getPrivate());
	}

	private void assertPublicKeyHasExpectedAlgorithmAndFormat(final String expectedAlgorithm, final PublicKey publicKey) {

		assertEquals(expectedAlgorithm, publicKey.getAlgorithm());
		assertEquals("X.509", publicKey.getFormat());
	}

	private void assertPrivateKeyHasExpectedAlgorithmAndFormat(final String expectedAlgorithm, final PrivateKey privateKey) {

		assertEquals(expectedAlgorithm, privateKey.getAlgorithm());
		assertEquals("PKCS#8", privateKey.getFormat());
	}

}
