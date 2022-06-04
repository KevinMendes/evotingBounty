/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.certificates.factory;

import java.security.KeyPair;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Properties;

import javax.security.auth.x500.X500Principal;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.asymmetric.utils.AsymmetricTestDataGenerator;
import ch.post.it.evoting.cryptolib.certificates.bean.CertificateData;
import ch.post.it.evoting.cryptolib.certificates.bean.RootCertificateData;
import ch.post.it.evoting.cryptolib.certificates.bean.X509CertificateType;
import ch.post.it.evoting.cryptolib.certificates.constants.X509CertificateTestConstants;
import ch.post.it.evoting.cryptolib.certificates.cryptoapi.CryptoAPIX509Certificate;
import ch.post.it.evoting.cryptolib.certificates.utils.X509CertificateTestDataChecker;
import ch.post.it.evoting.cryptolib.certificates.utils.X509CertificateTestDataGenerator;
import ch.post.it.evoting.cryptolib.commons.configuration.PolicyFromPropertiesHelper;

class CryptoX509CertificateGeneratorTest {

	private static CryptoX509CertificateGenerator x509certificateGenerator;

	private static KeyPair rootKeyPair;

	private static PublicKey rootPublicKey;

	private static PrivateKey rootPrivateKey;

	private static CertificateData certificateData;

	@BeforeAll
	static void setUpAll() throws Exception {

		final X509CertificateGeneratorFactory x509CertificateGeneratorFactory = new X509CertificateGeneratorFactory();

		x509certificateGenerator = x509CertificateGeneratorFactory.create();

		rootKeyPair = AsymmetricTestDataGenerator.getKeyPairForSigning();
		rootPublicKey = rootKeyPair.getPublic();
		rootPrivateKey = rootKeyPair.getPrivate();

		certificateData = X509CertificateTestDataGenerator.getCertificateData(rootKeyPair);
	}

	@Test
	void testWhenCreateRootAuthorityCertificateFromServiceThenOk() throws GeneralCryptoLibException {

		final RootCertificateData rootCertificateData = X509CertificateTestDataGenerator.getRootCertificateData(rootKeyPair);

		final CryptoAPIX509Certificate rootCertificate = x509certificateGenerator
				.generate(rootCertificateData, X509CertificateType.CERTIFICATE_AUTHORITY.getExtensions(), rootPrivateKey);

		X509CertificateTestDataChecker
				.assertCertificateContentCorrect(rootCertificate, rootCertificateData, X509CertificateType.CERTIFICATE_AUTHORITY, rootPublicKey);
	}

	@Test
	void testWhenCreateIntermediateAuthorityCertificateFromServiceThenOk() throws GeneralCryptoLibException {

		final CryptoAPIX509Certificate intermediateCertificate = x509certificateGenerator
				.generate(certificateData, X509CertificateType.CERTIFICATE_AUTHORITY.getExtensions(), rootPrivateKey);

		X509CertificateTestDataChecker
				.assertCertificateContentCorrect(intermediateCertificate, certificateData, X509CertificateType.CERTIFICATE_AUTHORITY, rootPublicKey);
	}

	@Test
	void testWhenCreateSignCertificateFromServiceThenOk() throws GeneralCryptoLibException {

		final CryptoAPIX509Certificate signCertificate = x509certificateGenerator
				.generate(certificateData, X509CertificateType.SIGN.getExtensions(), rootPrivateKey);

		X509CertificateTestDataChecker.assertCertificateContentCorrect(signCertificate, certificateData, X509CertificateType.SIGN, rootPublicKey);
	}

	@Test
	void testWhenCreateEncryptionCertificateFromServiceThenOk() throws GeneralCryptoLibException {

		final CryptoAPIX509Certificate encryptionCertificate = x509certificateGenerator
				.generate(certificateData, X509CertificateType.ENCRYPT.getExtensions(), rootPrivateKey);

		X509CertificateTestDataChecker
				.assertCertificateContentCorrect(encryptionCertificate, certificateData, X509CertificateType.ENCRYPT, rootPublicKey);
	}

	@Test
	void testWithSetCertificatePrincipalIssuerThenOk() throws GeneralCryptoLibException {
		certificateData.setIssuerDn(createCertificateDataPrincipal());

		final CryptoAPIX509Certificate signCertificate = x509certificateGenerator
				.generate(certificateData, X509CertificateType.SIGN.getExtensions(), rootPrivateKey);

		X509CertificateTestDataChecker.assertCertificateContentCorrect(signCertificate, certificateData, X509CertificateType.SIGN, rootPublicKey);
	}

	private Principal createCertificateDataPrincipal() {
		final Properties properties = PolicyFromPropertiesHelper.loadProperties(X509CertificateTestConstants.USER_CERTIFICATE_PROPERTIES_FILE_PATH);
		final PolicyFromPropertiesHelper config = new PolicyFromPropertiesHelper(properties);

		final String country = String.valueOf(config.getPropertyValue(X509CertificateTestConstants.ISSUER_COUNTRY_PROPERTY_NAME));
		final String locality = String.valueOf(config.getPropertyValue(X509CertificateTestConstants.ISSUER_LOCALITY_PROPERTY_NAME));
		final String org = String.valueOf(config.getPropertyValue(X509CertificateTestConstants.ISSUER_ORGANIZATION_PROPERTY_NAME));
		final String orgUnit = String.valueOf(config.getPropertyValue(X509CertificateTestConstants.ISSUER_ORGANIZATIONAL_UNIT_PROPERTY_NAME));
		final String commonName = String.valueOf(config.getPropertyValue(X509CertificateTestConstants.ISSUER_COMMON_NAME_PROPERTY_NAME));

		return new X500Principal("C=" + country + ",L=" + locality + ",O=" + org + ",OU=" + orgUnit + ",CN=" + commonName);
	}

}
