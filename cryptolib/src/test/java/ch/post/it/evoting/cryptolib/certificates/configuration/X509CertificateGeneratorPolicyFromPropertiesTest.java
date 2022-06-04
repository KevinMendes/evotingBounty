/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.certificates.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.condition.OS.LINUX;
import static org.junit.jupiter.api.condition.OS.WINDOWS;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;

import ch.post.it.evoting.cryptolib.primitives.securerandom.configuration.ConfigSecureRandomAlgorithmAndProvider;

class X509CertificateGeneratorPolicyFromPropertiesTest {

	private X509CertificateGeneratorPolicyFromProperties certificateGeneratorPolicyFromProperties;

	@BeforeEach
	void setup() {
		certificateGeneratorPolicyFromProperties = new X509CertificateGeneratorPolicyFromProperties();
	}

	@Test
	@EnabledOnOs(LINUX)
	void whenGetAX509CertificateGeneratorPolicyFromLinux() {
		assertEquals(getCertificateAlgorithmAndProviderAndSecureRandomFromLinux().getCertificateAlgorithmAndProvider(),
				certificateGeneratorPolicyFromProperties.getCertificateAlgorithmAndProvider());

		assertEquals(getCertificateAlgorithmAndProviderAndSecureRandomFromLinux().getSecureRandomAlgorithmAndProvider(),
				certificateGeneratorPolicyFromProperties.getSecureRandomAlgorithmAndProvider());
	}

	@Test
	@EnabledOnOs(WINDOWS)
	void whenGetAX509CertificateGeneratorPolicyFromWindows() {
		assertEquals(getCertificateAlgorithmAndProviderAndSecureRandomFromWindows().getCertificateAlgorithmAndProvider(),
				certificateGeneratorPolicyFromProperties.getCertificateAlgorithmAndProvider());

		assertEquals(getCertificateAlgorithmAndProviderAndSecureRandomFromWindows().getSecureRandomAlgorithmAndProvider(),
				certificateGeneratorPolicyFromProperties.getSecureRandomAlgorithmAndProvider());
	}

	private X509CertificateGeneratorPolicy getCertificateAlgorithmAndProviderAndSecureRandomFromLinux() {
		return new X509CertificateGeneratorPolicy() {

			@Override
			public ConfigX509CertificateAlgorithmAndProvider getCertificateAlgorithmAndProvider() {
				return ConfigX509CertificateAlgorithmAndProvider.SHA256_WITH_RSA_BC;
			}

			@Override
			public ConfigSecureRandomAlgorithmAndProvider getSecureRandomAlgorithmAndProvider() {
				return ConfigSecureRandomAlgorithmAndProvider.NATIVE_PRNG_SUN;
			}
		};
	}

	private X509CertificateGeneratorPolicy getCertificateAlgorithmAndProviderAndSecureRandomFromWindows() {
		return new X509CertificateGeneratorPolicy() {

			@Override
			public ConfigX509CertificateAlgorithmAndProvider getCertificateAlgorithmAndProvider() {
				return ConfigX509CertificateAlgorithmAndProvider.SHA256_WITH_RSA_BC;
			}

			@Override
			public ConfigSecureRandomAlgorithmAndProvider getSecureRandomAlgorithmAndProvider() {
				return ConfigSecureRandomAlgorithmAndProvider.PRNG_SUN_MSCAPI;
			}
		};
	}
}
