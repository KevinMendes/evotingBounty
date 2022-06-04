/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.keymanagement;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyStore.PasswordProtection;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.api.certificates.CertificatesServiceAPI;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.primitives.PrimitivesServiceAPI;
import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricService;
import ch.post.it.evoting.cryptolib.certificates.bean.RootCertificateData;
import ch.post.it.evoting.cryptolib.certificates.bean.ValidityDates;
import ch.post.it.evoting.cryptolib.certificates.bean.X509DistinguishedName;
import ch.post.it.evoting.cryptolib.certificates.service.CertificatesService;
import ch.post.it.evoting.cryptolib.primitives.service.PrimitivesService;

/**
 * Tests of {@link KeysAndCertificateGenerator}.
 */
class KeysAndCertificateGeneratorTest {

	private static final String ELECTION_EVENT_ID = "electionEventId";
	private static final String NODE_ID = "nodeId";
	private static final Date VALID_FROM;
	private static final Date VALID_TO;
	private static AsymmetricServiceAPI asymmetricService;
	private static CertificatesServiceAPI certificatesService;
	private static PrivateKey nodeCAPrivateKey;
	private static X509Certificate[] nodeCACertificateChain;

	static {
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.set(Calendar.MILLISECOND, 0);
		VALID_FROM = calendar.getTime();
		VALID_TO = new Date(VALID_FROM.getTime() + 1000);
	}

	private KeysAndCertificateGenerator keysAndCertificateGenerator;

	@BeforeAll
	public static void setUpAll() throws GeneralCryptoLibException {
		asymmetricService = new AsymmetricService();
		certificatesService = new CertificatesService();

		KeyPair pair = asymmetricService.getKeyPairForSigning();
		nodeCAPrivateKey = pair.getPrivate();

		RootCertificateData data = new RootCertificateData();
		data.setSubjectDn(
				new X509DistinguishedName.Builder("AdministrationBoard electionEventId", "CH").addLocality("Bern").addOrganization("Swiss Post")
						.addOrganizationalUnit("Online Voting").build());
		data.setSubjectPublicKey(pair.getPublic());
		data.setValidityDates(new ValidityDates(VALID_FROM, VALID_TO));
		X509Certificate nodeCACertificate = certificatesService.createRootAuthorityX509Certificate(data, nodeCAPrivateKey).getCertificate();
		nodeCACertificateChain = new X509Certificate[] { nodeCACertificate };

	}

	@BeforeEach
	public void setUp() {
		PrimitivesServiceAPI primitivesService = new PrimitivesService();
		keysAndCertificateGenerator = new KeysAndCertificateGenerator(asymmetricService, certificatesService, primitivesService, NODE_ID);
	}

	@Test
	void testGenerateElectionSigningKeys()
			throws KeyManagementException, GeneralCryptoLibException, InvalidKeyException, CertificateException, NoSuchAlgorithmException,
			NoSuchProviderException, SignatureException {
		NodeKeys nodeKeys = keysAndCertificateGenerator.generateNodeKeys(nodeCAPrivateKey, nodeCACertificateChain);

		ElectionSigningKeys electionSigningKeys = keysAndCertificateGenerator.generateElectionSigningKeys(ELECTION_EVENT_ID, VALID_FROM, VALID_TO,
				nodeKeys);

		PrivateKey privateKey = electionSigningKeys.privateKey();
		PublicKey publicKey = electionSigningKeys.publicKey();
		byte[] bytes = publicKey.getEncoded();
		byte[] signature = asymmetricService.sign(privateKey, bytes);
		assertTrue(asymmetricService.verifySignature(signature, publicKey, bytes));

		X509Certificate[] certificateChain = electionSigningKeys.certificateChain();
		assertEquals(2, certificateChain.length);
		assertEquals(nodeKeys.caCertificate(), certificateChain[1]);
		X509Certificate certificate = certificateChain[0];
		assertEquals(VALID_FROM, certificate.getNotBefore());
		assertEquals(VALID_TO, certificate.getNotAfter());
		certificate.verify(nodeKeys.caPublicKey());
	}

	@Test
	void testGenerateNodeKeys()
			throws KeyManagementException, GeneralCryptoLibException, InvalidKeyException, CertificateException, NoSuchAlgorithmException,
			NoSuchProviderException, SignatureException {
		NodeKeys nodeKeys = keysAndCertificateGenerator.generateNodeKeys(nodeCAPrivateKey, nodeCACertificateChain);

		assertEquals(nodeCAPrivateKey, nodeKeys.caPrivateKey());
		assertArrayEquals(nodeCACertificateChain, nodeKeys.caCertificateChain());

		X509Certificate caCertificate = nodeKeys.caCertificate();

		PrivateKey encryptionPrivateKey = nodeKeys.encryptionPrivateKey();
		PublicKey encryptionPublicKey = nodeKeys.encryptionPublicKey();
		byte[] bytes = encryptionPublicKey.getEncoded();
		byte[] encrypted = asymmetricService.encrypt(encryptionPublicKey, bytes);
		assertArrayEquals(bytes, asymmetricService.decrypt(encryptionPrivateKey, encrypted));

		X509Certificate[] encryptionCertificateChain = nodeKeys.encryptionCertificateChain();
		assertEquals(2, encryptionCertificateChain.length);
		assertEquals(caCertificate, encryptionCertificateChain[1]);
		X509Certificate encryptionCertificate = encryptionCertificateChain[0];
		assertEquals(VALID_FROM, caCertificate.getNotBefore());
		assertEquals(VALID_TO, caCertificate.getNotAfter());
		encryptionCertificate.verify(caCertificate.getPublicKey());

		PrivateKey logSigningPrivateKey = nodeKeys.logSigningPrivateKey();
		PublicKey logSigningPublicKey = nodeKeys.logSigningPublicKey();
		bytes = logSigningPublicKey.getEncoded();
		encrypted = asymmetricService.encrypt(logSigningPublicKey, bytes);
		assertArrayEquals(bytes, asymmetricService.decrypt(logSigningPrivateKey, encrypted));

		X509Certificate[] logSigningCertificateChain1 = nodeKeys.logSigningCertificateChain();
		assertEquals(2, logSigningCertificateChain1.length);
		assertEquals(caCertificate, logSigningCertificateChain1[1]);
		X509Certificate logSigningCertificate = logSigningCertificateChain1[0];
		assertEquals(VALID_FROM, caCertificate.getNotBefore());
		assertEquals(VALID_TO, caCertificate.getNotAfter());
		logSigningCertificate.verify(caCertificate.getPublicKey());

		PrivateKey logEncryptionPrivateKey = nodeKeys.logEncryptionPrivateKey();
		PublicKey logEncryptionPublicKey = nodeKeys.logEncryptionPublicKey();
		bytes = logEncryptionPublicKey.getEncoded();
		encrypted = asymmetricService.encrypt(logEncryptionPublicKey, bytes);
		assertArrayEquals(bytes, asymmetricService.decrypt(logEncryptionPrivateKey, encrypted));

		X509Certificate[] logEncryptionCertificateChain = nodeKeys.logEncryptionCertificateChain();
		assertEquals(2, logEncryptionCertificateChain.length);
		assertEquals(caCertificate, logEncryptionCertificateChain[1]);
		X509Certificate logEncryptionCertificate = logEncryptionCertificateChain[0];
		assertEquals(VALID_FROM, caCertificate.getNotBefore());
		assertEquals(VALID_TO, caCertificate.getNotAfter());
		logEncryptionCertificate.verify(caCertificate.getPublicKey());
	}

	@Test
	void testGeneratePassword() throws KeyManagementException {
		PasswordProtection protection = keysAndCertificateGenerator.generatePassword();
		assertTrue(protection.getPassword().length >= 16);
	}
}
