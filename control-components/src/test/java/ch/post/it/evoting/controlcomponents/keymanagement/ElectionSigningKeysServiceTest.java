/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.keymanagement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.KeyStoreSpi;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.Optional;

import javax.security.auth.x500.X500Principal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.controlcomponents.keymanagement.exception.KeyAlreadyExistsException;
import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.stores.StoresServiceAPI;
import ch.post.it.evoting.cryptolib.api.stores.bean.KeyStoreType;

class ElectionSigningKeysServiceTest {

	private static final KeyStore.PasswordProtection PASSWORD_PROTECTION = new KeyStore.PasswordProtection("password".toCharArray());
	private static final String ELECTION_EVENT_ID = "electionEventId";
	private static final String ALIAS = "alias";

	private NodeKeys nodeKeys;
	private ElectionSigningKeys electionSigningKeys;
	private KeysAndCertificateGenerator keysAndCertificateGenerator;
	private KeysRepository keysRepository;
	private ElectionSigningKeysService electionSigningKeysService;

	@BeforeEach
	public void setUp()
			throws CertificateException, GeneralCryptoLibException, KeyManagementException, UnrecoverableEntryException, KeyStoreException,
			NoSuchAlgorithmException, IOException {
		PrivateKey caPrivateKey = mock(PrivateKey.class);
		when(caPrivateKey.getAlgorithm()).thenReturn("RSA");
		PublicKey caPublicKey = mock(PublicKey.class);
		when(caPublicKey.getAlgorithm()).thenReturn("RSA");
		when(caPublicKey.getEncoded()).thenReturn(new byte[0]);
		X509Certificate caCertificate = mock(X509Certificate.class);
		when(caCertificate.getPublicKey()).thenReturn(caPublicKey);
		X509Certificate[] caCertificateChain = new X509Certificate[] { caCertificate };

		PrivateKey encryptionPrivateKey = mock(PrivateKey.class);
		PublicKey encryptionPublicKey = mock(PublicKey.class);
		X509Certificate encryptionCertificate = mock(X509Certificate.class);
		when(encryptionCertificate.getPublicKey()).thenReturn(encryptionPublicKey);
		X509Certificate[] encryptionCertificateChain = { encryptionCertificate, caCertificate };

		PrivateKey logSigningPrivateKey = mock(PrivateKey.class);
		PublicKey logSigningPublicKey = mock(PublicKey.class);
		X509Certificate logSigningCertificate = mock(X509Certificate.class);
		when(logSigningCertificate.getPublicKey()).thenReturn(logSigningPublicKey);
		X509Certificate[] logSigningCertificateChain = { logSigningCertificate, caCertificate };

		PrivateKey logEncryptionPrivateKey = mock(PrivateKey.class);
		PublicKey logEncryptionPublicKey = mock(PublicKey.class);
		X509Certificate logEncryptionCertificate = mock(X509Certificate.class);
		when(logEncryptionCertificate.getPublicKey()).thenReturn(logEncryptionPublicKey);
		X509Certificate[] logEncryptionCertificateChain = { logEncryptionCertificate, caCertificate };

		nodeKeys = new NodeKeys.Builder().setCAKeys(caPrivateKey, caCertificateChain)
				.setEncryptionKeys(encryptionPrivateKey, encryptionCertificateChain)
				.setLogSigningKeys(logSigningPrivateKey, logSigningCertificateChain)
				.setLogEncryptionKeys(logEncryptionPrivateKey, logEncryptionCertificateChain).build();

		PrivateKey electionSigningPrivateKey = mock(PrivateKey.class);
		PublicKey electionSigningPublicKey = mock(PublicKey.class);
		X509Certificate electionSigningCertificate = mock(X509Certificate.class);
		when(electionSigningCertificate.getPublicKey()).thenReturn(electionSigningPublicKey);
		X500Principal subjectPrincipal = new X500Principal("");
		when(electionSigningCertificate.getSubjectX500Principal()).thenReturn(subjectPrincipal);
		X500Principal issuerPrincipal = new X500Principal("");
		when(electionSigningCertificate.getIssuerX500Principal()).thenReturn(issuerPrincipal);
		when(electionSigningCertificate.getIssuerDN()).thenReturn(issuerPrincipal);
		when(electionSigningCertificate.getSerialNumber()).thenReturn(BigInteger.ONE);
		when(electionSigningCertificate.getEncoded()).thenReturn("encoded".getBytes(StandardCharsets.UTF_8));

		X509Certificate[] electionSigningCertificateChain = { electionSigningCertificate, caCertificate };
		electionSigningKeys = new ElectionSigningKeys(electionSigningPrivateKey, electionSigningCertificateChain);

		AsymmetricServiceAPI asymmetricService = mock(AsymmetricServiceAPI.class);
		when(asymmetricService.sign(eq(caPrivateKey), any(byte[].class))).thenReturn(new byte[0]);
		when(asymmetricService.verifySignature(any(byte[].class), eq(caPublicKey), any(byte[].class))).thenReturn(true);

		KeyStoreSpi keyStoreSpi = mock(KeyStoreSpi.class);
		doReturn(new KeyStore.PrivateKeyEntry(caPrivateKey, caCertificateChain)).when(keyStoreSpi).engineGetEntry(ALIAS, PASSWORD_PROTECTION);
		doReturn(true).when(keyStoreSpi).engineEntryInstanceOf(ALIAS, KeyStore.PrivateKeyEntry.class);

		KeyStoreDouble store = new KeyStoreDouble(keyStoreSpi);
		store.load(null, null);

		StoresServiceAPI storesService = mock(StoresServiceAPI.class);
		when(storesService.loadKeyStore(eq(KeyStoreType.PKCS12), any(InputStream.class), eq(PASSWORD_PROTECTION.getPassword()))).thenReturn(store);

		keysAndCertificateGenerator = mock(KeysAndCertificateGenerator.class);
		when(keysAndCertificateGenerator.generateNodeKeys(caPrivateKey, caCertificateChain)).thenReturn(nodeKeys);
		when(keysAndCertificateGenerator.generateElectionSigningKeys(eq(ELECTION_EVENT_ID), any(Date.class), any(Date.class), eq(nodeKeys)))
				.thenReturn(electionSigningKeys);

		keysRepository = mock(KeysRepository.class);
		when(keysRepository.loadNodeKeys(PASSWORD_PROTECTION)).thenReturn(nodeKeys);

		KeysManager manager = new KeysManager(asymmetricService, storesService, keysRepository);
		electionSigningKeysService = new ElectionSigningKeysService(manager, keysAndCertificateGenerator, keysRepository, "nodeId");
		manager.activateNodeKeys(nodeKeys);
	}

	@Test
	void testCreateElectionSigningKeysStringDateDate() throws KeyManagementException {
		when(keysRepository.loadElectionSigningKeys(ELECTION_EVENT_ID)).thenReturn(Optional.empty());

		ZonedDateTime validFrom = ZonedDateTime.now();
		ZonedDateTime validTo = validFrom.plusSeconds(1);
		electionSigningKeysService.createElectionSigningKeys(ELECTION_EVENT_ID, validFrom, validTo);
		verify(keysAndCertificateGenerator).generateElectionSigningKeys(ELECTION_EVENT_ID, Date.from(validFrom.toInstant()),
				Date.from(validTo.toInstant()), nodeKeys);
		verify(keysRepository).saveElectionSigningKeys(ELECTION_EVENT_ID, electionSigningKeys);
	}

	@Test
	void testCreateElectionSigningKeysStringDateDateAlreadyExist() throws KeyManagementException {
		doThrow(new KeyAlreadyExistsException("test")).when(keysRepository).saveElectionSigningKeys(ELECTION_EVENT_ID, electionSigningKeys);

		ZonedDateTime validFrom = ZonedDateTime.now();
		ZonedDateTime validTo = validFrom.plusSeconds(1);
		assertThrows(KeyAlreadyExistsException.class,
				() -> electionSigningKeysService.createElectionSigningKeys(ELECTION_EVENT_ID, validFrom, validTo));
	}

	@Test
	void testCreateElectionSigningKeysStringDateDateDatabaseError() throws KeyManagementException {
		doThrow(new KeyManagementException("test")).when(keysRepository).saveElectionSigningKeys(ELECTION_EVENT_ID, electionSigningKeys);

		ZonedDateTime validFrom = ZonedDateTime.now();
		ZonedDateTime validTo = validFrom.plusSeconds(1);
		assertThrows(KeyManagementException.class,
				() -> electionSigningKeysService.createElectionSigningKeys(ELECTION_EVENT_ID, validFrom, validTo));
	}

	@Test
	void testCreateElectionSigningKeysStringDateDateGeneratorError() throws KeyManagementException {
		ZonedDateTime validFrom = ZonedDateTime.now();
		ZonedDateTime validTo = validFrom.plusSeconds(1);
		when(keysAndCertificateGenerator.generateElectionSigningKeys(ELECTION_EVENT_ID, Date.from(validFrom.toInstant()),
				Date.from(validTo.toInstant()),
				nodeKeys)).thenThrow(new KeyManagementException("test"));
		assertThrows(KeyManagementException.class,
				() -> electionSigningKeysService.createElectionSigningKeys(ELECTION_EVENT_ID, validFrom, validTo));
	}

	@Test
	void testCreateElectionSigningKeysStringZonedDateTimeZonedDateTime() throws KeyManagementException {
		ZonedDateTime validFrom = ZonedDateTime.now();
		ZonedDateTime validTo = validFrom.plusSeconds(1);
		electionSigningKeysService.createElectionSigningKeys(ELECTION_EVENT_ID, validFrom, validTo);
		verify(keysAndCertificateGenerator).generateElectionSigningKeys(ELECTION_EVENT_ID, Date.from(validFrom.toInstant()),
				Date.from(validTo.toInstant()), nodeKeys);
	}

	@Test
	void testGetElectionSigningCertificate() throws KeyManagementException {
		when(keysRepository.loadElectionSigningKeys(ELECTION_EVENT_ID)).thenReturn(Optional.of(electionSigningKeys));

		assertEquals(electionSigningKeys.certificate(), electionSigningKeysService.getElectionSigningKeys(ELECTION_EVENT_ID).certificate());
	}

	@Test
	void testGetElectionSigningPrivateKey() throws KeyManagementException {
		when(keysRepository.loadElectionSigningKeys(ELECTION_EVENT_ID)).thenReturn(Optional.of(electionSigningKeys));

		assertEquals(electionSigningKeys.privateKey(), electionSigningKeysService.getElectionSigningKeys(ELECTION_EVENT_ID).privateKey());
	}

	@Test
	void testGetElectionSigningPublicKey() throws KeyManagementException {
		when(keysRepository.loadElectionSigningKeys(ELECTION_EVENT_ID)).thenReturn(Optional.of(electionSigningKeys));

		assertEquals(electionSigningKeys.publicKey(), electionSigningKeysService.getElectionSigningKeys(ELECTION_EVENT_ID).publicKey());
	}

	@Test
	void testIsValidElectionSigningKeysStringDateDate() {
		Date notBefore = new Date();
		Date notAfter = new Date(notBefore.getTime() + 1000);
		X509Certificate certificate = electionSigningKeys.certificate();
		when(certificate.getNotBefore()).thenReturn(notBefore);
		when(certificate.getNotAfter()).thenReturn(notAfter);

		Date validFrom = notBefore;
		Date validTo = notAfter;
		assertTrue(electionSigningKeysService.isValidInPeriod(electionSigningKeys, validFrom, validTo));

		validFrom = new Date(notBefore.getTime() - 1);
		// validTo = notAfter;
		assertFalse(electionSigningKeysService.isValidInPeriod(electionSigningKeys, validFrom, validTo));

		validFrom = notBefore;
		validTo = new Date(notAfter.getTime() + 1);
		assertFalse(electionSigningKeysService.isValidInPeriod(electionSigningKeys, validFrom, validTo));
	}

	private static class KeyStoreDouble extends KeyStore {
		public KeyStoreDouble(KeyStoreSpi keyStoreSpi) {
			super(keyStoreSpi, null, "PkCSS12");
		}
	}

}