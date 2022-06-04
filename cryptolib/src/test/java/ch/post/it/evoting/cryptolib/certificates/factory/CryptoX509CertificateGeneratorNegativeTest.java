/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.certificates.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.security.PrivateKey;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.bean.CertificatePublicData;
import ch.post.it.evoting.cryptolib.certificates.bean.extensions.AbstractCertificateExtension;
import ch.post.it.evoting.cryptolib.commons.configuration.Provider;
import ch.post.it.evoting.cryptolib.primitives.securerandom.factory.CryptoRandomInteger;

@ExtendWith(MockitoExtension.class)
class CryptoX509CertificateGeneratorNegativeTest {

	private static CryptoX509CertificateGenerator target;

	@Mock
	private static Provider provider;

	@Mock
	private static CryptoRandomInteger cryptoRandomInteger;

	@Mock
	private CertificatePublicData mockedCertificateData;

	@Mock
	private AbstractCertificateExtension mockedCertificateExtension;

	@Mock
	private PrivateKey mockedIssuerPrivateKey;

	@BeforeAll
	static void initAll() {
		target = new CryptoX509CertificateGenerator("signatureAlgorithm", provider, cryptoRandomInteger);
	}

	@Test
	void generateNullCertificateDataTest() {
		final AbstractCertificateExtension[] extensions = new AbstractCertificateExtension[] { mockedCertificateExtension };

		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> target.generate(null, extensions, mockedIssuerPrivateKey));
		assertEquals("Certificate generation parameters is null.", exception.getMessage());
	}

	@Test
	void generateNullExtensionTest() {
		final AbstractCertificateExtension[] extensions = new AbstractCertificateExtension[] { null };

		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> target.generate(mockedCertificateData, extensions, mockedIssuerPrivateKey));
		assertEquals("Certificate extensions contains one or more null elements.", exception.getMessage());
	}

	@Test
	void generateNullIssuerPrivateKeyTest() {
		final AbstractCertificateExtension[] extensions = new AbstractCertificateExtension[] { mockedCertificateExtension };

		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> target.generate(mockedCertificateData, extensions, null));
		assertEquals("Issuer private key is null.", exception.getMessage());
	}

}
