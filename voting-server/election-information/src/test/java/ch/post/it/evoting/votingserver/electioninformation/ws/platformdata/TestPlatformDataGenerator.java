/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.ws.platformdata;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.Properties;

import javax.security.auth.x500.X500Principal;

import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequest;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.bean.CSRSigningInputProperties;
import ch.post.it.evoting.cryptolib.certificates.bean.CertificateParameters;
import ch.post.it.evoting.cryptolib.certificates.bean.CredentialProperties;
import ch.post.it.evoting.cryptolib.certificates.cryptoapi.CryptoAPIX509Certificate;
import ch.post.it.evoting.cryptolib.certificates.csr.BouncyCastleCertificateRequestSigner;
import ch.post.it.evoting.cryptolib.certificates.csr.CertificateRequestSigner;
import ch.post.it.evoting.cryptolib.certificates.factory.X509CertificateGenerator;
import ch.post.it.evoting.cryptolib.certificates.factory.builders.CertificateDataBuilder;
import ch.post.it.evoting.cryptolib.certificates.service.CertificatesService;
import ch.post.it.evoting.cryptolib.certificates.utils.PemUtils;
import ch.post.it.evoting.cryptolib.extendedkeystore.cryptoapi.CryptoAPIExtendedKeyStore;
import ch.post.it.evoting.cryptolib.extendedkeystore.factory.CryptoExtendedKeyStoreWithPBKDF;
import ch.post.it.evoting.cryptolib.extendedkeystore.service.ExtendedKeyStoreService;
import ch.post.it.evoting.domain.election.helpers.CertificatePropertiesConstants;
import ch.post.it.evoting.domain.election.model.platform.PlatformInstallationData;
import ch.post.it.evoting.domain.election.model.tenant.TenantInstallationData;
import ch.post.it.evoting.votingserver.commons.beans.utils.PasswordEncrypter;
import ch.post.it.evoting.votingserver.commons.util.CryptoUtils;
import ch.post.it.evoting.votingserver.electioninformation.ws.config.SystemPropertiesLoader;

public class TestPlatformDataGenerator {

	private final String platformName;
	private final String tenantId;
	private final char[] tenantPassword;
	private final String keystorePassphrase;
	private KeyPair tenantKeyPair;
	private PrivateKey platformRootPrivateKey;
	private CryptoAPIX509Certificate platformRootCACert;
	private CryptoAPIX509Certificate tenantCert;
	private CredentialPropertiesProvider credPropsProvider;
	private CryptoAPIExtendedKeyStore tenantKeystore;
	private boolean dataGenerated;

	public TestPlatformDataGenerator(String tenantId, String platformName, String tenantPasswordString) {
		this.platformName = platformName;
		this.tenantId = tenantId;
		this.tenantPassword = tenantPasswordString.toCharArray();
		this.keystorePassphrase = tenantPasswordString;

	}

	/**
	 * Generates the platform certificates for the corresponding context.
	 */
	public void generate() throws CertificateException, NoSuchAlgorithmException, KeyStoreException, IOException, GeneralCryptoLibException,
			OperatorCreationException {
		createPlatformRootData();
		createTenantKeystore();
		dataGenerated = true;
	}

	/**
	 * Gets the tenant keystore encoded B64.
	 *
	 * @return the encoded tenant keystore
	 * @throws GeneralCryptoLibException if something fails when exporting the keystore
	 */
	public String getTenantKeystoreAsB64String() throws GeneralCryptoLibException {
		if (!dataGenerated) {
			throw new IllegalStateException("Tenant data not generated. Call generate() method first");
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		tenantKeystore.store(out, tenantPassword);
		byte[] keystoreBytes = out.toByteArray();
		return Base64.getEncoder().encodeToString(keystoreBytes);
	}

	/**
	 * Gets the tenant keystore in JSON format.
	 *
	 * @return the tenant keystore as JSON.
	 * @throws GeneralCryptoLibException if something fails when exporting the keystore.
	 */
	public String getTenantKeystoreAsJSON() throws GeneralCryptoLibException {
		if (!dataGenerated) {
			throw new IllegalStateException("Tenant data not generated. Call generate() method first");
		}
		final ExtendedKeyStoreService storesService = new ExtendedKeyStoreService();
		final CryptoExtendedKeyStoreWithPBKDF keyStore = (CryptoExtendedKeyStoreWithPBKDF) storesService.createKeyStore();
		final Certificate[] chainEncryption = { tenantCert.getCertificate(), platformRootCACert.getCertificate() };
		keyStore.setPrivateKeyEntry("privatekey", tenantKeyPair.getPrivate(), tenantPassword, chainEncryption);
		byte[] keyStoreJSONBytes = keyStore.toJSON(tenantPassword).getBytes(StandardCharsets.UTF_8);

		return Base64.getEncoder().encodeToString(keyStoreJSONBytes);
	}

	/**
	 * Encrypts the tenant password with the tenant public key.
	 *
	 * @return the encrypted password
	 * @throws GeneralCryptoLibException if something fails when encrypting.
	 */
	public String getEncryptedTenantKeystorePassword() throws GeneralCryptoLibException {
		if (!dataGenerated) {
			throw new IllegalStateException("Tenant data not generated. Call generate() method first");
		}
		PasswordEncrypter passwordEncrypter = new PasswordEncrypter(CryptoUtils.getAsymmetricService());
		String publicKey = PemUtils.publicKeyToPem(tenantKeyPair.getPublic());
		return passwordEncrypter.encryptPasswordIfEncryptionKeyAvailable(keystorePassphrase, publicKey);
	}

	private void createPlatformRootData() throws IOException {

		KeyPair keyPairForSign = generateKeyPairOfType(CertificateParameters.Type.SIGN);

		credPropsProvider = new CredentialPropertiesProvider();

		final CredentialProperties platformRoomCredentialProperties = credPropsProvider.getPlatformRootCredentialPropertiesFromClassPath();

		platformRootCACert = generateRootCertificate(keyPairForSign, platformRoomCredentialProperties, platformName);

		String platformRootCACertPem = new String(platformRootCACert.getPemEncoded(), StandardCharsets.UTF_8);

		platformRootPrivateKey = keyPairForSign.getPrivate();

		File tenantFile = Paths.get(SystemPropertiesLoader.ABSOLUTEPATH).resolve("tenant_" + platformName + "_" + tenantId + ".properties").toFile();
		Properties p = new Properties();
		p.setProperty(tenantId + "_" + platformName, keystorePassphrase);
		try (FileOutputStream fos = new FileOutputStream(tenantFile)) {
			p.store(fos, "");
		}

		PlatformInstallationData platformInstallationData = new PlatformInstallationData();
		platformInstallationData.setPlatformRootCaPEM(platformRootCACertPem);
	}

	private void createTenantKeystore() throws OperatorCreationException, GeneralCryptoLibException {

		// load credentialproperties
		final CredentialProperties tenantCredentialProperties = credPropsProvider.getTenantCredentialPropertiesFromClassPath();

		// Generate and sign CSR (#PKCS10)
		tenantKeyPair = generateKeyPairOfType(CertificateParameters.Type.ENCRYPTION);
		JcaPKCS10CertificationRequestBuilder p10Builder = new JcaPKCS10CertificationRequestBuilder(
				new X500Principal("CN=Tenant " + tenantId + " CA, OU=tenant" + tenantId + ", O=test, C=ES"), tenantKeyPair.getPublic());
		JcaContentSignerBuilder csBuilder = new JcaContentSignerBuilder("SHA256withRSA");
		ContentSigner signer = csBuilder.build(tenantKeyPair.getPrivate());
		JcaPKCS10CertificationRequest csr = new JcaPKCS10CertificationRequest(p10Builder.build(signer));

		// Issue tenant certificate
		CertificateRequestSigner certificateRequestSigner = new BouncyCastleCertificateRequestSigner();
		CSRSigningInputProperties csrSigningInputProperties = certificateRequestSigner.getCsrSigningInputProperties(tenantCredentialProperties);
		tenantCert = certificateRequestSigner.signCSR(platformRootCACert.getCertificate(), platformRootPrivateKey, csr, csrSigningInputProperties);

		// Build tenant keystore
		final ExtendedKeyStoreService storesService = new ExtendedKeyStoreService();
		tenantKeystore = storesService.createKeyStore();
		final Certificate[] chainEncryption = { tenantCert.getCertificate(), platformRootCACert.getCertificate() };
		String privateKeyAlias = tenantCredentialProperties.getAlias().get(CertificatePropertiesConstants.PRIVATE_KEY_ALIAS);

		tenantKeystore.setPrivateKeyEntry(privateKeyAlias, tenantKeyPair.getPrivate(), tenantPassword, chainEncryption);

		TenantInstallationData tenantInstallationData = new TenantInstallationData();
		tenantInstallationData.setEncodedData(new String(tenantCert.getPemEncoded(), StandardCharsets.UTF_8));
	}

	private KeyPair generateKeyPairOfType(CertificateParameters.Type certificateType) {

		switch (certificateType) {
		case SIGN:
			return CryptoUtils.getKeyPairForSigning();
		case ENCRYPTION:
			return CryptoUtils.getKeyPairForEncryption();
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
		CertificatesService certificatesService;
		certificatesService = new CertificatesService();

		CertificateDataBuilder certificateDataBuilder = new CertificateDataBuilder();
		return new X509CertificateGenerator(certificatesService, certificateDataBuilder);
	}
}
