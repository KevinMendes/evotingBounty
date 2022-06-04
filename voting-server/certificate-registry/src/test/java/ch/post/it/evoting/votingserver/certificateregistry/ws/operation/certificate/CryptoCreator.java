/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.certificateregistry.ws.operation.certificate;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Properties;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricService;
import ch.post.it.evoting.cryptolib.certificates.bean.CSRSigningInputProperties;
import ch.post.it.evoting.cryptolib.certificates.bean.CertificateParameters;
import ch.post.it.evoting.cryptolib.certificates.bean.CredentialProperties;
import ch.post.it.evoting.cryptolib.certificates.bean.X509DistinguishedName;
import ch.post.it.evoting.cryptolib.certificates.cryptoapi.CryptoAPIX509Certificate;
import ch.post.it.evoting.cryptolib.certificates.csr.BouncyCastleCertificateRequestSigner;
import ch.post.it.evoting.cryptolib.certificates.csr.CertificateRequestSigner;
import ch.post.it.evoting.cryptolib.certificates.factory.X509CertificateGenerator;
import ch.post.it.evoting.cryptolib.certificates.factory.builders.CertificateDataBuilder;
import ch.post.it.evoting.cryptolib.certificates.service.CertificatesService;
import ch.post.it.evoting.cryptolib.certificates.utils.PemUtils;
import ch.post.it.evoting.domain.election.helpers.CertificatePropertiesConstants;
import ch.post.it.evoting.domain.election.model.platform.PlatformInstallationData;

public class CryptoCreator {

	private final CredentialPropertiesProvider credPropsProvider = new CredentialPropertiesProvider();
	private PrivateKey platformRootPrivateKey;
	private CryptoAPIX509Certificate platformRootCACert;

	public String createTenantInstallationData(int tenantId) throws OperatorCreationException, GeneralCryptoLibException {
		final CredentialProperties tenantCredentialProperties = credPropsProvider.getTenantCredentialPropertiesFromClassPath();
		KeyPair pair = generateKeyPairOfType(CertificateParameters.Type.SIGN);
		JcaPKCS10CertificationRequestBuilder p10Builder = new JcaPKCS10CertificationRequestBuilder(
				new X500Principal("CN=Tenant " + tenantId + " CA, OU=tenant" + tenantId + ", O=bddtest, C=ES"), pair.getPublic());
		JcaContentSignerBuilder csBuilder = new JcaContentSignerBuilder("SHA256withRSA");
		ContentSigner signer = csBuilder.build(pair.getPrivate());
		JcaPKCS10CertificationRequest csr = new JcaPKCS10CertificationRequest(p10Builder.build(signer));

		CertificateRequestSigner certificateRequestSigner = new BouncyCastleCertificateRequestSigner();
		CSRSigningInputProperties csrSingingPropertyInputs = certificateRequestSigner.getCsrSigningInputProperties(tenantCredentialProperties);
		CryptoAPIX509Certificate tenantCert = certificateRequestSigner
				.signCSR(platformRootCACert.getCertificate(), platformRootPrivateKey, csr, csrSingingPropertyInputs);
		return new String(tenantCert.getPemEncoded(), StandardCharsets.UTF_8);
	}

	public String createElectionCertificate(String certName) throws GeneralCryptoLibException {

		KeyPair electionCertKP = generateKeyPairOfType(CertificateParameters.Type.SIGN);
		CredentialProperties signingCredentialPropertiesFromClassPath = credPropsProvider
				.getServicesLoggingSigningCredentialPropertiesFromClassPath();
		CryptoAPIX509Certificate generateCertificate = generateCertificate(electionCertKP, platformRootPrivateKey, certName,
				getPropertiesFrom(signingCredentialPropertiesFromClassPath), CertificateParameters.Type.SIGN, platformRootCACert);
		return PemUtils.certificateToPem(generateCertificate.getCertificate());
	}

	public PlatformInstallationData createPlatformInstallationData() {
		KeyPair keyPairForSign = generateKeyPairOfType(CertificateParameters.Type.SIGN);

		final CredentialProperties platformRoomCredentialProperties = credPropsProvider.getPlatformRootCredentialPropertiesFromClassPath();

		platformRootCACert = generateRootCertificate(keyPairForSign, platformRoomCredentialProperties, "CR");

		String platformRootCACertPem = new String(platformRootCACert.getPemEncoded(), StandardCharsets.UTF_8);

		platformRootPrivateKey = keyPairForSign.getPrivate();

		PlatformInstallationData pid = new PlatformInstallationData();
		pid.setPlatformRootCaPEM(platformRootCACertPem);
		return pid;
	}

	private Properties getPropertiesFrom(final CredentialProperties credentialPropertiesServicesSigner) {
		final Properties subjectPropertiesSigner = new Properties();
		try (InputStream input = Thread.currentThread().getContextClassLoader()
				.getResourceAsStream(credentialPropertiesServicesSigner.getPropertiesFile())) {
			subjectPropertiesSigner.load(input);
		} catch (IOException e) {
			throw new RuntimeException("An error occurred while loading the certificate properties", e);
		}
		return subjectPropertiesSigner;
	}

	private CryptoAPIX509Certificate generateCertificate(final KeyPair keyPair, final PrivateKey issuerPrivateKey, final String serviceName,
			final Properties properties, final CertificateParameters.Type credentialType, final CryptoAPIX509Certificate issuerCertificate) {

		final PublicKey publicKey = keyPair.getPublic();

		X509CertificateGenerator certificateGenerator = createCertificateGenerator();

		final CertificateParameters certificateParameters = new CertificateParameters();

		certificateParameters.setType(credentialType);

		certificateParameters.setUserSubjectCn(
				properties.getProperty(CertificatePropertiesConstants.SUBJECT_COMMON_NAME_PROPERTY_NAME).replace("${serviceName}", serviceName));
		certificateParameters.setUserSubjectOrgUnit(properties.getProperty(CertificatePropertiesConstants.SUBJECT_ORGANIZATIONAL_UNIT_PROPERTY_NAME));
		certificateParameters.setUserSubjectOrg(properties.getProperty(CertificatePropertiesConstants.SUBJECT_ORGANIZATION_PROPERTY_NAME));
		certificateParameters.setUserSubjectCountry(properties.getProperty(CertificatePropertiesConstants.SUBJECT_COUNTRY_PROPERTY_NAME));

		X509DistinguishedName issuerDn = issuerCertificate.getIssuerDn();
		certificateParameters.setUserIssuerCn(issuerDn.getCommonName());
		certificateParameters.setUserIssuerOrgUnit(issuerDn.getOrganizationalUnit());
		certificateParameters.setUserIssuerOrg(issuerDn.getOrganization());
		certificateParameters.setUserIssuerCountry(issuerDn.getCountry());

		String start = properties.getProperty(CertificatePropertiesConstants.START_DATE);
		String end = properties.getProperty(CertificatePropertiesConstants.END_DATE);
		ZonedDateTime notBefore = ZonedDateTime.ofInstant(Instant.parse(start), ZoneOffset.UTC);
		ZonedDateTime notAfter = ZonedDateTime.ofInstant(Instant.parse(end), ZoneOffset.UTC);

		if (notAfter.isBefore(notBefore)) {
			throw new RuntimeException("The validity period of the certificate is empty");
		}

		if (Date.from(notBefore.toInstant()).before(issuerCertificate.getNotBefore())) {
			throw new RuntimeException("The tenant \"start\" time should be strictly after the root \"start\" time");
		}
		if (Date.from(notAfter.toInstant()).after(issuerCertificate.getNotAfter())) {
			throw new RuntimeException("The tenant \"end\" time should be strictly before the root \"end\" time");
		}

		certificateParameters.setUserNotBefore(notBefore);
		certificateParameters.setUserNotAfter(notAfter);

		CryptoAPIX509Certificate platformRootCACert;
		try {
			platformRootCACert = certificateGenerator.generate(certificateParameters, publicKey, issuerPrivateKey);
		} catch (GeneralCryptoLibException e) {
			throw new RuntimeException("An error occurred while creating the certificate of service " + serviceName, e);
		}

		return platformRootCACert;
	}

	private KeyPair generateKeyPairOfType(CertificateParameters.Type certificateType) {
		AsymmetricService asymmetricService = new AsymmetricService();
		switch (certificateType) {
		case SIGN:
			return asymmetricService.getKeyPairForSigning();
		case ENCRYPTION:
			return asymmetricService.getKeyPairForEncryption();
		default:
			throw new RuntimeException("The provided certificate type is invalid");
		}
	}

	private CryptoAPIX509Certificate generateRootCertificate(final KeyPair keyPairForSigning, final CredentialProperties credentialProperties,
			final String platformName) {
		final PublicKey publicKey = keyPairForSigning.getPublic();
		PrivateKey parentPrivateKey = keyPairForSigning.getPrivate();

		X509CertificateGenerator certificateGenerator = createCertificateGenerator();

		final Properties props = readProperties(credentialProperties);

		final CertificateParameters certificateParameters = new CertificateParameters();

		certificateParameters.setType(credentialProperties.getCredentialType());

		certificateParameters.setUserSubjectCn(
				props.getProperty(CertificatePropertiesConstants.SUBJECT_COMMON_NAME_PROPERTY_NAME).replace("${platformName}", platformName));
		certificateParameters.setUserSubjectOrgUnit(props.getProperty(CertificatePropertiesConstants.SUBJECT_ORGANIZATIONAL_UNIT_PROPERTY_NAME));
		certificateParameters.setUserSubjectOrg(props.getProperty(CertificatePropertiesConstants.SUBJECT_ORGANIZATION_PROPERTY_NAME));
		certificateParameters.setUserSubjectCountry(props.getProperty(CertificatePropertiesConstants.SUBJECT_COUNTRY_PROPERTY_NAME));

		certificateParameters.setUserIssuerCn(
				props.getProperty(CertificatePropertiesConstants.SUBJECT_COMMON_NAME_PROPERTY_NAME).replace("${platformName}", platformName));
		certificateParameters.setUserIssuerOrgUnit(props.getProperty(CertificatePropertiesConstants.SUBJECT_ORGANIZATIONAL_UNIT_PROPERTY_NAME));
		certificateParameters.setUserIssuerOrg(props.getProperty(CertificatePropertiesConstants.SUBJECT_ORGANIZATION_PROPERTY_NAME));
		certificateParameters.setUserIssuerCountry(props.getProperty(CertificatePropertiesConstants.SUBJECT_COUNTRY_PROPERTY_NAME));

		String start = props.getProperty(CertificatePropertiesConstants.START_DATE);
		String end = props.getProperty(CertificatePropertiesConstants.END_DATE);
		ZonedDateTime notBefore = ZonedDateTime.ofInstant(Instant.parse(start), ZoneOffset.UTC);
		ZonedDateTime notAfter = ZonedDateTime.ofInstant(Instant.parse(end), ZoneOffset.UTC);
		if (!notBefore.isBefore(notAfter)) {
			throw new RuntimeException("The given \"start\" date should be strictly before than the \"end\" date");
		}
		certificateParameters.setUserNotBefore(notBefore);
		certificateParameters.setUserNotAfter(notAfter);

		CryptoAPIX509Certificate platformRootCACert;
		try {
			platformRootCACert = certificateGenerator.generate(certificateParameters, publicKey, parentPrivateKey);
		} catch (GeneralCryptoLibException e) {
			throw new RuntimeException("An error occurred while creating the platform root certificate", e);
		}

		return platformRootCACert;
	}

	private Properties readProperties(final CredentialProperties credentialProperties) {
		final Properties props = new Properties();
		try (InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(credentialProperties.getPropertiesFile())) {
			props.load(input);
		} catch (IOException e) {
			throw new RuntimeException("An error occurred while loading the certificate properties", e);
		}
		return props;
	}

	private X509CertificateGenerator createCertificateGenerator() {
		CertificatesService certificatesService = new CertificatesService();

		CertificateDataBuilder certificateDataBuilder = new CertificateDataBuilder();

		return new X509CertificateGenerator(certificatesService, certificateDataBuilder);
	}

}
