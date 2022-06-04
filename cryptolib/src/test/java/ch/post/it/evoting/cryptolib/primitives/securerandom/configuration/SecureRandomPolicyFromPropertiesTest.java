/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.primitives.securerandom.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.condition.OS.LINUX;
import static org.junit.jupiter.api.condition.OS.WINDOWS;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;

class SecureRandomPolicyFromPropertiesTest {

	private SecureRandomPolicyFromProperties secureRandomPolicyFromProperties;

	@BeforeEach
	public void setup() throws GeneralCryptoLibException {
		secureRandomPolicyFromProperties = new SecureRandomPolicyFromProperties();
	}

	@Test
	@EnabledOnOs(LINUX)
	void whenGetSecureRandomPolicyFromLinux() {
		assertEquals(getSecureRandomPolicyNativePrngSun().getSecureRandomAlgorithmAndProvider(),
				secureRandomPolicyFromProperties.getSecureRandomAlgorithmAndProvider());
	}

	@Test
	@EnabledOnOs(WINDOWS)
	void whenGetSecureRandomPolicyFromWindows() {
		assertEquals(getSecureRandomPolicyWindowsPrngSunMSCAPI().getSecureRandomAlgorithmAndProvider(),
				secureRandomPolicyFromProperties.getSecureRandomAlgorithmAndProvider());
	}

	private SecureRandomPolicy getSecureRandomPolicyNativePrngSun() {
		return () -> ConfigSecureRandomAlgorithmAndProvider.NATIVE_PRNG_SUN;
	}

	private SecureRandomPolicy getSecureRandomPolicyWindowsPrngSunMSCAPI() {
		return () -> ConfigSecureRandomAlgorithmAndProvider.PRNG_SUN_MSCAPI;
	}
}
