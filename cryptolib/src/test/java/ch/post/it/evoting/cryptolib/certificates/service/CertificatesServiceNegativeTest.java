/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.certificates.service;

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Date;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.asymmetric.utils.AsymmetricTestDataGenerator;
import ch.post.it.evoting.cryptolib.certificates.bean.CertificateData;
import ch.post.it.evoting.cryptolib.certificates.cryptoapi.CryptoAPIX509Certificate;
import ch.post.it.evoting.cryptolib.certificates.utils.X509CertificateTestDataGenerator;

/**
 * Tests of the certificates service API for negative cases.
 */
class CertificatesServiceNegativeTest {

	private static CertificatesService certificatesServiceForDefaultPolicy;
	private static PrivateKey rootPrivateKey;
	private static PublicKey publicKey;
	private static CryptoAPIX509Certificate certificate;

	@BeforeAll
	static void setUp() throws Exception {
		certificatesServiceForDefaultPolicy = new CertificatesService();

		final KeyPair rootKeyPair = AsymmetricTestDataGenerator.getKeyPairForSigning();
		rootPrivateKey = rootKeyPair.getPrivate();

		final KeyPair keyPair = AsymmetricTestDataGenerator.getKeyPairForSigning();
		publicKey = keyPair.getPublic();

		final CertificateData certificateData = X509CertificateTestDataGenerator.getCertificateData(keyPair);
		certificate = certificatesServiceForDefaultPolicy.createSignX509Certificate(certificateData, rootPrivateKey);
	}

	@Test
	void testCertificateVerificationWithInvalidPublicKey() throws GeneralCryptoLibException {
		assertFalse(certificate.verify(publicKey));
	}

	@Test
	void testCertificateDateValidityForInvalidCertificate() throws GeneralCryptoLibException {
		final KeyPair keyPair = AsymmetricTestDataGenerator.getKeyPairForSigning();
		final CertificateData invalidCertificateData = X509CertificateTestDataGenerator.getExpiredCertificateData(keyPair);
		final CryptoAPIX509Certificate invalidCertificate = certificatesServiceForDefaultPolicy
				.createSignX509Certificate(invalidCertificateData, rootPrivateKey);

		assertFalse(invalidCertificate.checkValidity().isOk());
	}

	@Test
	void testCertificateDateValidityCheckWithInvalidDate() throws GeneralCryptoLibException {
		final Date invalidDate = X509CertificateTestDataGenerator.getDateOutsideValidityPeriod();

		assertFalse(certificate.checkValidity(invalidDate));
	}
}
