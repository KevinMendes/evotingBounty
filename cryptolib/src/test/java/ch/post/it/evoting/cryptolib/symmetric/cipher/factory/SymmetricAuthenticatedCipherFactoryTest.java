/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.symmetric.cipher.factory;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mockStatic;

import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.commons.system.OperatingSystem;
import ch.post.it.evoting.cryptolib.primitives.securerandom.configuration.ConfigSecureRandomAlgorithmAndProvider;
import ch.post.it.evoting.cryptolib.symmetric.cipher.configuration.ConfigSymmetricCipherAlgorithmAndSpec;
import ch.post.it.evoting.cryptolib.symmetric.cipher.configuration.SymmetricCipherPolicy;

@ExtendWith(MockitoExtension.class)
class SymmetricAuthenticatedCipherFactoryTest {

	private static ConfigSecureRandomAlgorithmAndProvider getSecureRandomConfig() {
		switch (OperatingSystem.current()) {
		case WINDOWS:
			return ConfigSecureRandomAlgorithmAndProvider.PRNG_SUN_MSCAPI;
		case UNIX:
			return ConfigSecureRandomAlgorithmAndProvider.NATIVE_PRNG_SUN;
		default:
			throw new CryptoLibException("OS not supported.");
		}
	}

	@Test
	void whenCreateSymmetricCipherFactoryUsingPolicyAesGcmNoPaddingAndBc() {
		final SymmetricAuthenticatedCipherFactory symmetricAuthenticatedCipherFactoryByPolicy = new SymmetricAuthenticatedCipherFactory(
				getSymmetricCipherPolicyAesGcmNoPaddingAndBc());

		assertNotNull(symmetricAuthenticatedCipherFactoryByPolicy.create());
	}

	@Test
	void whenSymmetricCipherFailsToConstructCreateShouldThrow() {
		try (final MockedStatic<Cipher> mockedChiper = mockStatic(Cipher.class)) {
			mockedChiper.when(() -> Cipher.getInstance(any(String.class), any(String.class))).thenThrow(NoSuchAlgorithmException.class);
			final SymmetricAuthenticatedCipherFactory symmetricAuthenticatedCipherFactoryByPolicy = new SymmetricAuthenticatedCipherFactory(
					getSymmetricCipherPolicyAesGcmNoPaddingAndBc());

			assertThrows(CryptoLibException.class, symmetricAuthenticatedCipherFactoryByPolicy::create);
		}
	}

	private SymmetricCipherPolicy getSymmetricCipherPolicyAesGcmNoPaddingAndBc() {
		return new SymmetricCipherPolicy() {

			@Override
			public ConfigSecureRandomAlgorithmAndProvider getSecureRandomAlgorithmAndProvider() {
				return getSecureRandomConfig();
			}

			@Override
			public ConfigSymmetricCipherAlgorithmAndSpec getSymmetricCipherAlgorithmAndSpec() {
				return ConfigSymmetricCipherAlgorithmAndSpec.AES_WITH_GCM_AND_NOPADDING_96_128_BC;
			}
		};
	}
}
