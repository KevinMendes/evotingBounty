/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.symmetric.key.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.commons.system.OperatingSystem;
import ch.post.it.evoting.cryptolib.primitives.securerandom.configuration.ConfigSecureRandomAlgorithmAndProvider;

@ExtendWith(MockitoExtension.class)
class SymmetricKeyPolicyFromPropertiesTest {

	private static SymmetricKeyPolicyFromProperties symmetricKeyPolicyFromProperties;

	@BeforeAll
	static void setUp() {
		symmetricKeyPolicyFromProperties = new SymmetricKeyPolicyFromProperties();
	}

	private static ConfigSecureRandomAlgorithmAndProvider getOSDependentSecureRandomConfig() {

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
	void givenPolicyWhenGetHmacConfigThenExpectedValues() {
		final ConfigHmacSecretKeyAlgorithmAndSpec hmacSecretKeyConfig = symmetricKeyPolicyFromProperties.getHmacSecretKeyAlgorithmAndSpec();

		assertEquals("HmacSHA256", hmacSecretKeyConfig.getAlgorithm());
		assertEquals(256, hmacSecretKeyConfig.getKeyLengthInBits());
	}

	@Test
	void givenPolicyWhenGetSecretKeyConfigThenExpectedValues() {
		final ConfigSecretKeyAlgorithmAndSpec secretKeyConfig = symmetricKeyPolicyFromProperties.getSecretKeyAlgorithmAndSpec();

		assertEquals("AES", secretKeyConfig.getAlgorithm());
		assertEquals(128, secretKeyConfig.getKeyLength());
	}

	@Test
	void givenPolicyWhenGetSecureRandomConfigThenExpectedValues() {
		final ConfigSecureRandomAlgorithmAndProvider expectedSecureRandomConfig = getOSDependentSecureRandomConfig();
		final ConfigSecureRandomAlgorithmAndProvider secureRandomConfig = symmetricKeyPolicyFromProperties.getSecureRandomAlgorithmAndProvider();

		assertEquals(expectedSecureRandomConfig, secureRandomConfig);
	}

	@Test
	@SuppressWarnings("ResultOfMethodCallIgnored")
	void givenMockedUnixWhenGetSecureRandomConfigThenExpectedValue() {
		try (final MockedStatic<OperatingSystem> mockedOperatingSystem = Mockito.mockStatic(OperatingSystem.class)) {

			mockedOperatingSystem.when(OperatingSystem::current).thenReturn(OperatingSystem.UNIX);

			final SymmetricKeyPolicyFromProperties symmetricKeyPolicyFromProperties = new SymmetricKeyPolicyFromProperties();
			final ConfigSecureRandomAlgorithmAndProvider secureRandomConfig = symmetricKeyPolicyFromProperties.getSecureRandomAlgorithmAndProvider();

			assertEquals(ConfigSecureRandomAlgorithmAndProvider.NATIVE_PRNG_SUN, secureRandomConfig);
		}
	}

	@Test
	@SuppressWarnings("ResultOfMethodCallIgnored")
	void givenMockedWindowsWhenGetSecureRandomConfigThenExpectedValue() {
		try (final MockedStatic<OperatingSystem> mockedOperatingSystem = Mockito.mockStatic(OperatingSystem.class)) {

			mockedOperatingSystem.when(OperatingSystem::current).thenReturn(OperatingSystem.WINDOWS);

			final SymmetricKeyPolicyFromProperties symmetricKeyPolicyFromProperties = new SymmetricKeyPolicyFromProperties();
			final ConfigSecureRandomAlgorithmAndProvider secureRandomConfig = symmetricKeyPolicyFromProperties.getSecureRandomAlgorithmAndProvider();

			assertEquals(ConfigSecureRandomAlgorithmAndProvider.PRNG_SUN_MSCAPI, secureRandomConfig);
		}
	}

	@Test
	@SuppressWarnings("ResultOfMethodCallIgnored")
	void givenMockedUnsupportedOSWhenReadPropertiesThenException() {
		try (final MockedStatic<OperatingSystem> mockedOperatingSystem = Mockito.mockStatic(OperatingSystem.class)) {

			mockedOperatingSystem.when(OperatingSystem::current).thenReturn(OperatingSystem.OTHER);

			assertThrows(CryptoLibException.class, SymmetricKeyPolicyFromProperties::new);
		}
	}
}
