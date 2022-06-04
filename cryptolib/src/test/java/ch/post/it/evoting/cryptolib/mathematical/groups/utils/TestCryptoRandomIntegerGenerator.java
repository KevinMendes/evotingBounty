/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.cryptolib.mathematical.groups.utils;

import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.commons.system.OperatingSystem;
import ch.post.it.evoting.cryptolib.primitives.securerandom.configuration.ConfigSecureRandomAlgorithmAndProvider;
import ch.post.it.evoting.cryptolib.primitives.securerandom.configuration.SecureRandomPolicy;
import ch.post.it.evoting.cryptolib.primitives.securerandom.factory.CryptoRandomInteger;
import ch.post.it.evoting.cryptolib.primitives.securerandom.factory.SecureRandomFactory;

public interface TestCryptoRandomIntegerGenerator {
	static CryptoRandomInteger getRNG() {
		SecureRandomPolicy secureRandomPolicy = getSecureRandomPolicy();
		return new SecureRandomFactory(secureRandomPolicy).createIntegerRandom();
	}

	static SecureRandomPolicy getSecureRandomPolicy() {
		return () -> {
			switch (OperatingSystem.current()) {
			case WINDOWS:
				return ConfigSecureRandomAlgorithmAndProvider.PRNG_SUN_MSCAPI;
			case UNIX:
				return ConfigSecureRandomAlgorithmAndProvider.NATIVE_PRNG_SUN;
			default:
				throw new CryptoLibException("OS not supported");
			}
		};
	}
}
