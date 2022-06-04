/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.certificates.service;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.asymmetric.utils.AsymmetricTestDataGenerator;
import ch.post.it.evoting.cryptolib.certificates.bean.CertificateData;
import ch.post.it.evoting.cryptolib.certificates.bean.RootCertificateData;
import ch.post.it.evoting.cryptolib.certificates.bean.X509CertificateType;
import ch.post.it.evoting.cryptolib.certificates.cryptoapi.CryptoAPIX509Certificate;
import ch.post.it.evoting.cryptolib.certificates.factory.CryptoX509Certificate;
import ch.post.it.evoting.cryptolib.certificates.utils.X509CertificateTestDataChecker;
import ch.post.it.evoting.cryptolib.certificates.utils.X509CertificateTestDataGenerator;

/**
 * Tests of the certificates service API.
 */
class CertificatesServiceTest {

	private static CertificatesService certificatesServiceForDefaultPolicy;

	private static KeyPair rootKeyPair;

	private static PublicKey rootPublicKey;

	private static PrivateKey rootPrivateKey;

	private static CertificateData certificateData;

	@BeforeAll
	public static void setUp() throws Exception {

		certificatesServiceForDefaultPolicy = new CertificatesService();

		rootKeyPair = AsymmetricTestDataGenerator.getKeyPairForSigning();
		rootPublicKey = rootKeyPair.getPublic();
		rootPrivateKey = rootKeyPair.getPrivate();

		certificateData = X509CertificateTestDataGenerator.getCertificateData(AsymmetricTestDataGenerator.getKeyPairForSigning());
	}

	@Test
	final void testWhenCreateRootAuthorityCertificateFromServiceThenOk() throws GeneralCryptoLibException {

		final RootCertificateData rootCertificateData = X509CertificateTestDataGenerator.getRootCertificateData(rootKeyPair);

		final CryptoAPIX509Certificate rootCertificate = certificatesServiceForDefaultPolicy
				.createRootAuthorityX509Certificate(rootCertificateData, rootPrivateKey);

		X509CertificateTestDataChecker
				.assertCertificateContentCorrect(rootCertificate, rootCertificateData, X509CertificateType.CERTIFICATE_AUTHORITY, rootPublicKey);
	}

	@Test
	final void testWhenCreateIntermediateAuthorityCertificateFromServiceThenOk() throws GeneralCryptoLibException {

		final CryptoAPIX509Certificate intermediateCertificate = certificatesServiceForDefaultPolicy
				.createIntermediateAuthorityX509Certificate(certificateData, rootPrivateKey);

		X509CertificateTestDataChecker
				.assertCertificateContentCorrect(intermediateCertificate, certificateData, X509CertificateType.CERTIFICATE_AUTHORITY, rootPublicKey);
	}

	@Test
	final void testWhenCreateSignCertificateFromServiceThenOk() throws GeneralCryptoLibException {

		final CryptoAPIX509Certificate signCertificate = certificatesServiceForDefaultPolicy.createSignX509Certificate(certificateData, rootPrivateKey);

		X509CertificateTestDataChecker.assertCertificateContentCorrect(signCertificate, certificateData, X509CertificateType.SIGN, rootPublicKey);
	}

	@Test
	final void testWhenCreateEncryptionCertificateFromServiceThenOk() throws GeneralCryptoLibException {

		final CryptoAPIX509Certificate encryptionCertificate = certificatesServiceForDefaultPolicy
				.createEncryptionX509Certificate(certificateData, rootPrivateKey);

		X509CertificateTestDataChecker
				.assertCertificateContentCorrect(encryptionCertificate, certificateData, X509CertificateType.ENCRYPT, rootPublicKey);
	}

	@Test
	final void testWhenCreateRootAuthorityCertificateFromX509CertificateThenOk() throws GeneralCryptoLibException {

		final CryptoAPIX509Certificate rootCertificate = certificatesServiceForDefaultPolicy
				.createIntermediateAuthorityX509Certificate(certificateData, rootPrivateKey);

		final CryptoX509Certificate newRootCertificate = new CryptoX509Certificate(rootCertificate.getCertificate());

		X509CertificateTestDataChecker
				.assertCertificateContentCorrect(newRootCertificate, certificateData, X509CertificateType.CERTIFICATE_AUTHORITY, rootPublicKey);
	}

	@Test
	final void testWhenCreateIntermediateAuthorityCertificateFromX509CertificateThenOk() throws GeneralCryptoLibException {

		final CryptoAPIX509Certificate intermediateCertificate = certificatesServiceForDefaultPolicy
				.createIntermediateAuthorityX509Certificate(certificateData, rootPrivateKey);

		final CryptoX509Certificate newIntermediateCertificate = new CryptoX509Certificate(intermediateCertificate.getCertificate());

		X509CertificateTestDataChecker
				.assertCertificateContentCorrect(newIntermediateCertificate, certificateData, X509CertificateType.CERTIFICATE_AUTHORITY,
						rootPublicKey);
	}

	@Test
	final void testWhenCreateSignCertificateFromX509CertificateThenOk() throws GeneralCryptoLibException {

		final CryptoAPIX509Certificate signCertificate = certificatesServiceForDefaultPolicy.createSignX509Certificate(certificateData, rootPrivateKey);

		final CryptoX509Certificate newSignCertificate = new CryptoX509Certificate(signCertificate.getCertificate());

		X509CertificateTestDataChecker.assertCertificateContentCorrect(newSignCertificate, certificateData, X509CertificateType.SIGN, rootPublicKey);
	}

	@Test
	final void testWhenCreateEncryptionCertificateFromX509CertificateThenOk() throws GeneralCryptoLibException {

		final CryptoAPIX509Certificate encryptionCertificate = certificatesServiceForDefaultPolicy
				.createEncryptionX509Certificate(certificateData, rootPrivateKey);

		final CryptoX509Certificate newEncryptionCertificate = new CryptoX509Certificate(encryptionCertificate.getCertificate());

		X509CertificateTestDataChecker
				.assertCertificateContentCorrect(newEncryptionCertificate, certificateData, X509CertificateType.ENCRYPT, rootPublicKey);
	}
}
