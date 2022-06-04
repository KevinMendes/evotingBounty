/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.primitives.securerandom.factory;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.commons.system.OperatingSystem;
import ch.post.it.evoting.cryptolib.primitives.securerandom.configuration.ConfigSecureRandomAlgorithmAndProvider;
import ch.post.it.evoting.cryptolib.primitives.securerandom.configuration.SecureRandomPolicy;
import ch.post.it.evoting.cryptolib.primitives.securerandom.constants.SecureRandomConstants;

class SecureRandomFactoryTest {

	@Test
	void whenCreateSecureRandomFactoryUsingAGivenPolicy() {
		assumeTrue(OperatingSystem.UNIX.isCurrent());

		final SecureRandomFactory secureRandomFactoryByPolicy = new SecureRandomFactory(getSecureRandomPolicyNativePrngSun());

		assertNotNull(secureRandomFactoryByPolicy);
	}

	@Test
	void whenCreateCryptoIntegerRandom() {
		assumeTrue(OperatingSystem.UNIX.isCurrent());

		final CryptoRandomInteger cryptoIntegerRandom = new SecureRandomFactory(getSecureRandomPolicyNativePrngSun()).createIntegerRandom();

		assertSame(cryptoIntegerRandom.getClass(), CryptoRandomInteger.class);
		assertNotNull(cryptoIntegerRandom);
	}

	@Test
	void whenCreateCryptoStringRandom() {
		assumeTrue(OperatingSystem.UNIX.isCurrent());

		final CryptoRandomString cryptoStringRandom = new SecureRandomFactory(getSecureRandomPolicyNativePrngSun())
				.createStringRandom(SecureRandomConstants.ALPHABET_BASE32);

		assertSame(cryptoStringRandom.getClass(), CryptoRandomString.class);
		assertNotNull(cryptoStringRandom);
	}

	@Test
	void whenCreateCryptoStringRandomGivenANullAlphabet() {
		assumeTrue(OperatingSystem.UNIX.isCurrent());

		assertThrows(CryptoLibException.class, () -> new SecureRandomFactory(getSecureRandomPolicyNativePrngSun()).createStringRandom(null));
	}

	private SecureRandomPolicy getSecureRandomPolicyNativePrngSun() {
		return () -> ConfigSecureRandomAlgorithmAndProvider.NATIVE_PRNG_SUN;
	}

	private SecureRandomPolicy getSecureRandomPolicyWindowsPrng() {
		return () -> ConfigSecureRandomAlgorithmAndProvider.PRNG_SUN_MSCAPI;
	}
}
