/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.extendedkeystore.configuration;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.commons.configuration.Provider;
import ch.post.it.evoting.cryptolib.commons.system.OperatingSystem;

class ExtendedKeyStorePolicyFromPropertiesTest {

	@Test
	void okTest() {
		final ExtendedKeyStorePolicyFromProperties policy = new ExtendedKeyStorePolicyFromProperties("p12");

		assertEquals(Provider.SUN_JSSE, policy.getStoreTypeAndProvider().getProvider(), "created policy from file ok test");
	}

	@Test
	void okTrimKeyTest() {
		final ExtendedKeyStorePolicyFromProperties policy = new ExtendedKeyStorePolicyFromProperties(" p12 ");

		assertEquals(Provider.SUN_JSSE, policy.getStoreTypeAndProvider().getProvider(), "created policy from file ok test");
	}

	@Test
	void invalidKeyTest() {
		assertThrows(CryptoLibException.class, () -> new ExtendedKeyStorePolicyFromProperties("myKey"));
	}

	@Test
	void blankKeyTest() {
		assertThrows(CryptoLibException.class, () -> new ExtendedKeyStorePolicyFromProperties(" "));
	}

	@Test
	@SuppressWarnings("ResultOfMethodCallIgnored")
	void getOsUnixTest() {
		try (final MockedStatic<OperatingSystem> mockedOperatingSystem = Mockito.mockStatic(OperatingSystem.class)) {
			mockedOperatingSystem.when(OperatingSystem::current).thenReturn(OperatingSystem.UNIX);

			assertDoesNotThrow(() -> new ExtendedKeyStorePolicyFromProperties("p12"));
		}
	}

	@Test
	@SuppressWarnings("ResultOfMethodCallIgnored")
	void getOsWindowsTest() {
		try (final MockedStatic<OperatingSystem> mockedOperatingSystem = Mockito.mockStatic(OperatingSystem.class)) {
			mockedOperatingSystem.when(OperatingSystem::current).thenReturn(OperatingSystem.WINDOWS);

			assertDoesNotThrow(() -> new ExtendedKeyStorePolicyFromProperties("p12"));
		}
	}

	@Test
	@SuppressWarnings("ResultOfMethodCallIgnored")
	void getOsOtherTest() {
		try (final MockedStatic<OperatingSystem> mockedOperatingSystem = Mockito.mockStatic(OperatingSystem.class)) {
			mockedOperatingSystem.when(OperatingSystem::current).thenReturn(OperatingSystem.OTHER);

			assertThrows(CryptoLibException.class, () -> new ExtendedKeyStorePolicyFromProperties("p12"));
		}
	}
}
