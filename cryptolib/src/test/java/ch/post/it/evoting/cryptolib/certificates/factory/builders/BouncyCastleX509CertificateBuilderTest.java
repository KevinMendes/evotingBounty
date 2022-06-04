/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.certificates.factory.builders;

import static java.text.MessageFormat.format;

import java.security.KeyPair;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;

import javax.security.auth.x500.X500Principal;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.api.certificates.CertificatesServiceAPI;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricService;
import ch.post.it.evoting.cryptolib.certificates.bean.CertificateData;
import ch.post.it.evoting.cryptolib.certificates.bean.ValidityDates;
import ch.post.it.evoting.cryptolib.certificates.bean.X509DistinguishedName;
import ch.post.it.evoting.cryptolib.certificates.configuration.ConfigX509CertificateAlgorithmAndProvider;
import ch.post.it.evoting.cryptolib.certificates.cryptoapi.CryptoAPIX509Certificate;
import ch.post.it.evoting.cryptolib.certificates.service.CertificatesService;
import ch.post.it.evoting.cryptolib.primitives.securerandom.factory.CryptoRandomInteger;
import ch.post.it.evoting.cryptolib.primitives.service.PrimitivesService;

/**
 * Tests {@link BouncyCastleX509CertificateBuilder} class
 */
class BouncyCastleX509CertificateBuilderTest {

	private static PrivateKey _issuerPrivateKey;
	private static PublicKey _publicKey;
	private static X509DistinguishedName _subjectDn;
	private static X509DistinguishedName _issuerDn;
	private static ValidityDates _validityDates;
	private static CryptoRandomInteger _cryptoRandomInteger;
	private static Principal _orderedIssuerDn;
	private final ConfigX509CertificateAlgorithmAndProvider _configDefault = ConfigX509CertificateAlgorithmAndProvider.SHA256_WITH_RSA_DEFAULT;
	private final ConfigX509CertificateAlgorithmAndProvider _configBC = ConfigX509CertificateAlgorithmAndProvider.SHA256_WITH_RSA_BC;

	@BeforeAll
	public static void setUp() throws GeneralCryptoLibException {

		final AsymmetricService asymmetricService = new AsymmetricService();

		final KeyPair keyPairForSigning = asymmetricService.getKeyPairForSigning();

		_publicKey = keyPairForSigning.getPublic();
		_issuerPrivateKey = keyPairForSigning.getPrivate();

		final PrimitivesService primitivesService = new PrimitivesService();

		_cryptoRandomInteger = (CryptoRandomInteger) primitivesService.getCryptoRandomInteger();

		final String commonName = "Test certificate";
		final String country = "XX";
		final String organizationalUnit = "Test";
		final String organization = "Organization";
		final String locality = "Locality";

		_orderedIssuerDn = new X500Principal(
				"C=" + country + ",L=" + locality + ",O=" + organization + ",OU=" + organizationalUnit + ",CN=" + commonName);

		final X509DistinguishedName.Builder builder = new X509DistinguishedName.Builder(commonName, country);
		builder.addOrganization(organization);
		builder.addOrganizationalUnit(organizationalUnit);
		builder.addLocality(locality);

		_subjectDn = builder.build();
		_issuerDn = builder.build();

		final Date notBefore = new Date(System.currentTimeMillis());
		final Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, 1);
		final Date notAfter = calendar.getTime();

		_validityDates = new ValidityDates(notBefore, notAfter);
	}

	private static CertificateData newCertificateData() throws GeneralCryptoLibException {
		final CertificateData data = new CertificateData();
		data.setSubjectPublicKey(_publicKey);
		data.setSubjectDn(_subjectDn);
		data.setValidityDates(_validityDates);
		data.setIssuerDn(_orderedIssuerDn);

		return data;
	}

	@Test
	void whenCreateBouncyCastleX509CertificateBuilderGivenDefaultProviderThenBuild() {

		final X509Certificate cert = createAndBuildCertificateWithOrderedIssuerDn(_configDefault);

		Assertions.assertNotNull(cert);
	}

	@Test
	void whenCreateBouncyCastleX509CertificateBuilderGivenBCProviderThenBuild() {

		final X509Certificate cert = createAndBuildCertificateWithOrderedIssuerDn(_configBC);

		Assertions.assertNotNull(cert);
	}

	@Test
	void testSupportForOptionalDNFields() throws GeneralCryptoLibException {
		final String commonName = "Common name";
		final String country = "XX";
		final String expectedDN = format("C={0},CN={1}", country, commonName);

		// Only required attributes.
		final X509DistinguishedName.Builder builder = new X509DistinguishedName.Builder(commonName, country);

		_subjectDn = builder.build();
		_orderedIssuerDn = new X500Principal("C=" + country + ",CN=" + commonName);
		final X509Certificate sut = new BouncyCastleX509CertificateBuilder(_configBC.getAlgorithm(), _configBC.getProvider(), _publicKey, _subjectDn,
				_orderedIssuerDn, _validityDates, _cryptoRandomInteger).build(_issuerPrivateKey);

		Assertions.assertNotNull(sut);
		Assertions.assertEquals(expectedDN, sut.getSubjectDN().getName());
	}

	@Test
	void BouncyCastleX509CertificateBuilderKeepsIssuerDnOrdered() {

		final X509Certificate cert = createAndBuildCertificateWithOrderedIssuerDn(_configDefault);

		Assertions.assertEquals(_orderedIssuerDn.getName(), cert.getIssuerDN().getName());
	}

	@Test
	void WhenCreateCertificateWithOrderedIssuerDn() throws GeneralCryptoLibException {
		final CertificateData data = newCertificateData();
		final CertificatesServiceAPI service = new CertificatesService();
		final CryptoAPIX509Certificate certificate = service.createEncryptionX509Certificate(data, _issuerPrivateKey);
		final X509Certificate cert = certificate.getCertificate();
		Assertions.assertEquals(_orderedIssuerDn.getName(), cert.getIssuerDN().getName());
	}

	@Test
	void testEmtpyFieldsOnDNs() throws GeneralCryptoLibException {
		final String commonName = "Common Name";
		final String country = "XX";
		final X509DistinguishedName dn = new X509DistinguishedName.Builder(commonName, country).build();
		final Principal principal = BouncyCastleX509CertificateBuilder.getPrincipalIssuerDn(dn);
		Assertions.assertEquals(principal.getName(), String.format("C=%s,CN=%s", country, commonName));
	}

	private X509Certificate createAndBuildCertificateWithOrderedIssuerDn(final ConfigX509CertificateAlgorithmAndProvider config) {

		return new BouncyCastleX509CertificateBuilder(config.getAlgorithm(), config.getProvider(), _publicKey, _subjectDn, _orderedIssuerDn,
				_validityDates, _cryptoRandomInteger).build(_issuerPrivateKey);
	}

}
