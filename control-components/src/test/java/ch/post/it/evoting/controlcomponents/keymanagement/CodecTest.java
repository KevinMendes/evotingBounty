/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.keymanagement;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyStore.PasswordProtection;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Date;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.controlcomponents.keymanagement.exception.InvalidPasswordException;
import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.api.certificates.CertificatesServiceAPI;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.primitives.PrimitivesServiceAPI;
import ch.post.it.evoting.cryptolib.api.stores.StoresServiceAPI;
import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricService;
import ch.post.it.evoting.cryptolib.certificates.bean.RootCertificateData;
import ch.post.it.evoting.cryptolib.certificates.bean.ValidityDates;
import ch.post.it.evoting.cryptolib.certificates.bean.X509DistinguishedName;
import ch.post.it.evoting.cryptolib.certificates.service.CertificatesService;
import ch.post.it.evoting.cryptolib.primitives.service.PrimitivesService;
import ch.post.it.evoting.cryptolib.stores.service.StoresService;

/**
 * Tests of {@link Codec}.
 */
class CodecTest {
	private static KeysAndCertificateGenerator keysAndCertificateGenerator;
	private static Codec codec;
	private static NodeKeys nodeKeys;
	private PasswordProtection pwd = new PasswordProtection("pwdpwd".toCharArray());

	@BeforeAll
	public static void setUpAll() throws GeneralCryptoLibException, KeyManagementException {

		StoresServiceAPI storesService = new StoresService();
		AsymmetricServiceAPI asymmetricService = new AsymmetricService();
		CertificatesServiceAPI certificatesService = new CertificatesService();
		PrimitivesServiceAPI primitivesService = new PrimitivesService();

		KeyPair pair = asymmetricService.getKeyPairForSigning();

		PrivateKey caPrivateKey = pair.getPrivate();

		RootCertificateData certificateData = new RootCertificateData();
		certificateData.setSubjectDn(new X509DistinguishedName.Builder("CA", "ES").build());
		certificateData.setSubjectPublicKey(pair.getPublic());
		certificateData.setValidityDates(new ValidityDates(new Date(), new Date(System.currentTimeMillis() + 1000)));
		X509Certificate certificate = certificatesService.createRootAuthorityX509Certificate(certificateData, caPrivateKey).getCertificate();

		X509Certificate[] caCertificateChain = new X509Certificate[] { certificate };

		codec = new Codec(storesService, asymmetricService);

		keysAndCertificateGenerator = new KeysAndCertificateGenerator(asymmetricService, certificatesService, primitivesService, "nodeId");
		nodeKeys = keysAndCertificateGenerator.generateNodeKeys(caPrivateKey, caCertificateChain);
	}

	@BeforeEach
	public void initiatePassword() {
		pwd = new PasswordProtection("password".toCharArray());
	}

	@Test
	void testDecodeElectionSigningKeys() throws KeyManagementException {
		ElectionSigningKeys electionSigningKeys = keysAndCertificateGenerator
				.generateElectionSigningKeys("electionEventId", new Date(), new Date(System.currentTimeMillis() + 1000), nodeKeys);

		byte[] bytes = codec.encodeElectionSigningKeysAsKeystore(electionSigningKeys, pwd);
		ElectionSigningKeys electionSigningKeys2 = codec.decodeElectionSigningKeys(bytes, pwd);

		assertEquals(electionSigningKeys.privateKey(), electionSigningKeys2.privateKey());
		assertArrayEquals(electionSigningKeys.certificateChain(), electionSigningKeys2.certificateChain());
	}

	@Test
	void testDecodeElectionSigningKeysInvalidBytes() {
		byte[] bytes = { 1, 2, 3 };

		assertThrows(KeyManagementException.class, () -> codec.decodeElectionSigningKeys(bytes, pwd));
	}

	@Test
	void testDecodeElectionSigningKeysInvalidPassword() throws KeyManagementException {
		ElectionSigningKeys electionSigningKeys = keysAndCertificateGenerator
				.generateElectionSigningKeys("electionEventId", new Date(), new Date(System.currentTimeMillis() + 1000), nodeKeys);

		byte[] bytes = codec.encodeElectionSigningKeysAsKeystore(electionSigningKeys, pwd);

		assertThrows(InvalidPasswordException.class,
				() -> codec.decodeElectionSigningKeys(bytes, new PasswordProtection("pwdpwd2".toCharArray())));
	}

	@Test
	void testDecodeNodeKeys() throws KeyManagementException {
		byte[] bytes = codec.encodeNodeKeysAsKeystore(nodeKeys, pwd);
		NodeKeys nodeKeys2 = codec.decodeNodeKeys(bytes, pwd);

		assertEquals(nodeKeys.caPrivateKey(), nodeKeys2.caPrivateKey());
		assertArrayEquals(nodeKeys.caCertificateChain(), nodeKeys2.caCertificateChain());
		assertEquals(nodeKeys.encryptionPrivateKey(), nodeKeys2.encryptionPrivateKey());
		assertArrayEquals(nodeKeys.encryptionCertificateChain(), nodeKeys2.encryptionCertificateChain());
		assertEquals(nodeKeys.logEncryptionPrivateKey(), nodeKeys2.logEncryptionPrivateKey());
		assertArrayEquals(nodeKeys.logEncryptionCertificateChain(), nodeKeys2.logEncryptionCertificateChain());
		assertEquals(nodeKeys.logSigningPrivateKey(), nodeKeys2.logSigningPrivateKey());
		assertArrayEquals(nodeKeys.logSigningCertificateChain(), nodeKeys2.logSigningCertificateChain());
	}

	@Test
	void testDecodeNodeKeysInvalidBytes() {
		byte[] bytes = { 1, 2, 3 };

		assertThrows(KeyManagementException.class, () -> codec.decodeNodeKeys(bytes, pwd));
	}

	@Test
	void testDecodeNodeKeysInvalidPassword() throws KeyManagementException {
		byte[] bytes = codec.encodeNodeKeysAsKeystore(nodeKeys, pwd);

		assertThrows(InvalidPasswordException.class, () -> codec.decodeNodeKeys(bytes, new PasswordProtection("pwdpwd2".toCharArray())));
	}

	@Test
	void testDecodePassword() throws KeyManagementException {
		char[] password = pwd.getPassword();
		char[] guardedPassword = new char[password.length];
		System.arraycopy(password, 0, guardedPassword, 0, password.length);
		byte[] bytes = codec.encryptAndEncodePassword(pwd, nodeKeys.encryptionPublicKey());
		PasswordProtection password2 = codec.decryptPassword(bytes, nodeKeys.encryptionPrivateKey());

		assertArrayEquals(guardedPassword, password2.getPassword());
	}

	@Test
	void testDecodePasswordInvalidBytes() {
		byte[] bytes = new byte[257];

		assertThrows(KeyManagementException.class, () -> codec.decryptPassword(bytes, nodeKeys.encryptionPrivateKey()));
	}
}
