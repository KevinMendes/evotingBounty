/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.returncodes.crypto;

import java.security.KeyPair;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.api.certificates.CertificatesServiceAPI;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.asymmetric.service.PollingAsymmetricServiceFactory;
import ch.post.it.evoting.cryptolib.certificates.bean.CertificateData;
import ch.post.it.evoting.cryptolib.certificates.bean.RootCertificateData;
import ch.post.it.evoting.cryptolib.certificates.bean.ValidityDates;
import ch.post.it.evoting.cryptolib.certificates.bean.X509DistinguishedName;
import ch.post.it.evoting.cryptolib.certificates.service.PollingCertificatesServiceFactory;

public class TestCertificateGenerator {

	private static final String DN_ORGANIZATION = "Swiss Post";

	private static final String DN_ORGANIZATIONAL_UNIT = "Product";

	private static final String DN_LOCALITY = "Bern";

	private static final String ISSUER_CN = "Platform root certificate";

	private static final String ISSUER_COUNTRY = "xx";

	private final CertificatesServiceAPI certificatesService;

	private final ValidityDates validityDates;

	private final KeyPair rootKeyPair;

	private final X509Certificate rootCertificate;

	public TestCertificateGenerator(CertificatesServiceAPI certificatesService, ValidityDates validityDates,
			X509DistinguishedName issuerDistinguishedName, KeyPair rootKeyPair) throws GeneralCryptoLibException {
		this.certificatesService = certificatesService;
		this.validityDates = validityDates;
		this.rootKeyPair = rootKeyPair;

		this.rootCertificate = createRootCertificate(rootKeyPair, issuerDistinguishedName);
	}

	/**
	 * Creates a test certificates generator based on sane default values, which should be enough for most situations.
	 *
	 * @return a test certificate generator configured with defaults settings
	 */
	public static TestCertificateGenerator createDefault() throws GeneralCryptoLibException {
		CertificatesServiceAPI certificatesService = new PollingCertificatesServiceFactory().create();
		AsymmetricServiceAPI asymmetricService = new PollingAsymmetricServiceFactory().create();

		// Test certificates are valid for one hour.
		ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
		ZonedDateTime end = now.plusHours(1);
		ValidityDates validityDates = new ValidityDates(Date.from(now.toInstant()), Date.from(end.toInstant()));

		KeyPair rootCAKeyPair = asymmetricService.getKeyPairForSigning();

		X509DistinguishedName issuerDistinguishedName = buildDistinguishedName(ISSUER_CN);

		return new TestCertificateGenerator(certificatesService, validityDates, issuerDistinguishedName, rootCAKeyPair);
	}

	/**
	 * Builds a standardised distinguished name.
	 *
	 * @param principal a principal represented by a distinguished name
	 * @return an x509 distinguished name
	 */
	private static X509DistinguishedName buildDistinguishedName(Principal principal) throws GeneralCryptoLibException, InvalidNameException {
		LdapName ldapName = new LdapName(principal.getName());

		// Build map of attributes
		Map<String, String> attributes = new HashMap<>();
		for (Rdn rdn : ldapName.getRdns()) {
			String value = (rdn.getValue() instanceof String) ?
					(String) rdn.getValue() :
					((List<String>) rdn.getValue()).stream().collect(Collectors.joining(", "));
			attributes.put(rdn.getType().toLowerCase(), value);
		}

		return new X509DistinguishedName.Builder(attributes.get("cn"), attributes.get("c")).addOrganization(attributes.get("o"))
				.addOrganizationalUnit(attributes.get("ou")).addLocality(attributes.get("l")).build();
	}

	/**
	 * Builds a standardised distinguished name.
	 *
	 * @param commonName the CN field of the distinguished name
	 * @return a distinguished name
	 */
	public static X509DistinguishedName buildDistinguishedName(String commonName) throws GeneralCryptoLibException {
		return new X509DistinguishedName.Builder(commonName, ISSUER_COUNTRY).addOrganization(DN_ORGANIZATION)
				.addOrganizationalUnit(DN_ORGANIZATIONAL_UNIT).addLocality(DN_LOCALITY).build();
	}

	/**
	 * Creates a self-signed CA certificate.
	 *
	 * @param keyPair                 the CA's key pair
	 * @param issuerDistinguishedName the distinguished name for this certificate
	 * @return a certificate for the platform CA
	 */
	public X509Certificate createRootCertificate(KeyPair keyPair, X509DistinguishedName issuerDistinguishedName) throws GeneralCryptoLibException {
		RootCertificateData rootCertificateData = new RootCertificateData();
		rootCertificateData.setSubjectDn(issuerDistinguishedName);
		rootCertificateData.setSubjectPublicKey(keyPair.getPublic());
		rootCertificateData.setValidityDates(validityDates);

		return certificatesService.createRootAuthorityX509Certificate(rootCertificateData, keyPair.getPrivate()).getCertificate();
	}

	/**
	 * Creates a certificate for an intermediate CA, signed by this generator's root CA.
	 *
	 * @param keyPair    the signing key pair
	 * @param commonName this certificate's common name
	 * @return the validation key certificate
	 */
	public X509Certificate createCACertificate(KeyPair keyPair, String commonName) throws GeneralCryptoLibException, InvalidNameException {

		return createCACertificate(keyPair, getRootKeyPair().getPrivate(), getRootCertificate(), commonName);
	}

	/**
	 * Creates a certificate for an intermediate CA.
	 *
	 * @param keyPair       the signing key pair
	 * @param caPrivateKey  the private key of the CA issuing this certificate
	 * @param caCertificate the certificate of the CA issuing this certificate
	 * @param commonName    this certificate's common name
	 * @return the validation key certificate
	 */
	public X509Certificate createCACertificate(KeyPair keyPair, PrivateKey caPrivateKey, X509Certificate caCertificate, String commonName)
			throws GeneralCryptoLibException, InvalidNameException {
		return createCACertificate(keyPair, caPrivateKey, caCertificate, buildDistinguishedName(commonName));
	}

	/**
	 * Creates a certificate for an intermediate CA.
	 *
	 * @param keyPair       the signing key pair
	 * @param caPrivateKey  the private key of the CA issuing this certificate
	 * @param caCertificate the certificate of the CA issuing this certificate
	 * @param commonName    this common name to set for the distinguished name
	 * @return the validation key certificate
	 */
	public X509Certificate createCACertificate(KeyPair keyPair, PrivateKey caPrivateKey, X509Certificate caCertificate,
			X509DistinguishedName distinguishedName) throws GeneralCryptoLibException, InvalidNameException {
		CertificateData certificateData = new CertificateData();
		certificateData.setIssuerDn(buildDistinguishedName(caCertificate.getSubjectDN()));
		certificateData.setSubjectDn(distinguishedName);
		certificateData.setSubjectPublicKey(keyPair.getPublic());
		certificateData.setValidityDates(validityDates);

		return certificatesService.createIntermediateAuthorityX509Certificate(certificateData, caPrivateKey).getCertificate();
	}

	/**
	 * Creates an end-entity certificate for signing.
	 *
	 * @param keyPair       the signing key pair
	 * @param caPrivateKey  the private key of the CA issuing this certificate
	 * @param caCertificate the certificate of the CA issuing this certificate
	 * @param commonName    this certificate's common name
	 * @return the validation key certificate
	 */
	public X509Certificate createSigningLeafCertificate(KeyPair keyPair, PrivateKey caPrivateKey, X509Certificate caCertificate, String commonName)
			throws GeneralCryptoLibException, InvalidNameException {
		CertificateData certificateData = getCertificateData(keyPair, caCertificate, commonName);

		return certificatesService.createSignX509Certificate(certificateData, caPrivateKey).getCertificate();
	}

	/**
	 * Creates an end-entity certificate.
	 *
	 * @param keyPair       the signing key pair
	 * @param caPrivateKey  the private key of the CA issuing this certificate
	 * @param caCertificate the certificate of the CA issuing this certificate
	 * @param commonName    this certificate's common name
	 * @return the validation key certificate
	 */
	public X509Certificate createLeafCertificate(KeyPair keyPair, PrivateKey caPrivateKey, X509Certificate caCertificate, String commonName)
			throws GeneralCryptoLibException, InvalidNameException {
		CertificateData certificateData = getCertificateData(keyPair, caCertificate, commonName);

		return certificatesService.createEncryptionX509Certificate(certificateData, caPrivateKey).getCertificate();
	}

	/**
	 * Builds a CertificateData structure required to create certificates.
	 *
	 * @param keyPair       the signing key pair
	 * @param caCertificate the certificate of the CA issuing this certificate
	 * @param commonName    this certificate's common name
	 * @return the certificate data
	 */
	private CertificateData getCertificateData(KeyPair keyPair, X509Certificate caCertificate, String commonName)
			throws GeneralCryptoLibException, InvalidNameException {
		CertificateData certificateData = new CertificateData();
		certificateData.setIssuerDn(buildDistinguishedName(caCertificate.getSubjectDN()));
		certificateData.setSubjectDn(buildDistinguishedName(commonName));
		certificateData.setSubjectPublicKey(keyPair.getPublic());
		certificateData.setValidityDates(validityDates);

		return certificateData;
	}

	public X509Certificate getRootCertificate() {
		return rootCertificate;
	}

	public KeyPair getRootKeyPair() {
		return rootKeyPair;
	}

	public ValidityDates getValidityDates() {
		return validityDates;
	}
}
