/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.keymanagement;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.KeyManagementException;
import java.security.KeyStore.PasswordProtection;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.controlcomponents.keymanagement.exception.KeyAlreadyExistsException;
import ch.post.it.evoting.controlcomponents.keymanagement.exception.KeyNotFoundException;
import ch.post.it.evoting.controlcomponents.keymanagement.persistence.ElectionSigningKeysEntity;
import ch.post.it.evoting.controlcomponents.keymanagement.persistence.ElectionSigningKeysEntityPrimaryKey;
import ch.post.it.evoting.controlcomponents.keymanagement.persistence.ElectionSigningKeysEntityRepository;
import ch.post.it.evoting.controlcomponents.keymanagement.persistence.NodeKeysEntity;
import ch.post.it.evoting.controlcomponents.keymanagement.persistence.NodeKeysEntityRepository;

class KeysRepositoryTest {

	private static final String NODE_ID = "nodeId";
	private static final String ELECTION_EVENT_ID = "0b88257ec32142bb8ee0ed1bb70f362e";

	private final PasswordProtection password = new PasswordProtection("password".toCharArray());
	private final Codec codec = mock(Codec.class);
	private final KeysAndCertificateGenerator keysAndCertificateGenerator = mock(KeysAndCertificateGenerator.class);
	private final PrivateKey encryptionPrivateKey = mock(PrivateKey.class);
	private final PublicKey encryptionPublicKey = mock(PublicKey.class);
	private final NodeKeysEntityRepository nodeKeysEntityRepository = mock(NodeKeysEntityRepository.class);
	private final ElectionSigningKeysEntityRepository electionSigningKeysEntityRepository = mock(ElectionSigningKeysEntityRepository.class);

	private KeysRepository keysRepository;

	@BeforeEach
	public void setUp() throws KeyManagementException {
		when(keysAndCertificateGenerator.generatePassword()).thenReturn(password);

		keysRepository = new KeysRepository(codec, keysAndCertificateGenerator, nodeKeysEntityRepository, electionSigningKeysEntityRepository,
				NODE_ID);
		keysRepository.setEncryptionKeys(encryptionPrivateKey, encryptionPublicKey);
	}

	@Nested
	class NodeKeysTests {

		@Test
		void testSaveNodeKeys() throws KeyManagementException {
			final PrivateKey privateKey = mock(PrivateKey.class);
			final X509Certificate[] certificateChain = { mock(X509Certificate.class) };
			final NodeKeys nodeKeys = new NodeKeys.Builder().setCAKeys(privateKey, certificateChain).setEncryptionKeys(privateKey, certificateChain)
					.setLogSigningKeys(privateKey, certificateChain).setLogEncryptionKeys(privateKey, certificateChain).build();

			final byte[] keysBytes = { 1, 2, 3 };

			when(codec.encodeNodeKeysAsKeystore(nodeKeys, password)).thenReturn(keysBytes);
			when(nodeKeysEntityRepository.existsById(NODE_ID)).thenReturn(false);

			Assertions.assertDoesNotThrow(() -> keysRepository.saveNodeKeys(nodeKeys, password));
		}

		@Test
		void testSaveNodeKeysAlreadyExist() {
			final PrivateKey privateKey = mock(PrivateKey.class);
			final X509Certificate[] certificateChain = { mock(X509Certificate.class) };
			final NodeKeys nodeKeys = new NodeKeys.Builder().setCAKeys(privateKey, certificateChain).setEncryptionKeys(privateKey, certificateChain)
					.setLogSigningKeys(privateKey, certificateChain).setLogEncryptionKeys(privateKey, certificateChain).build();

			when(nodeKeysEntityRepository.existsById(NODE_ID)).thenReturn(true);

			final KeyAlreadyExistsException keyAlreadyExistsException = Assertions.assertThrows(KeyAlreadyExistsException.class,
					() -> keysRepository.saveNodeKeys(nodeKeys, password));
			Assertions.assertEquals(String.format("Node keys already exist for node id %s.", NODE_ID), keyAlreadyExistsException.getMessage());
		}

		@Test
		void testSaveNodeKeysCodecException() throws KeyManagementException {
			final PrivateKey privateKey = mock(PrivateKey.class);
			final X509Certificate[] certificateChain = { mock(X509Certificate.class) };
			final NodeKeys nodeKeys = new NodeKeys.Builder().setCAKeys(privateKey, certificateChain).setEncryptionKeys(privateKey, certificateChain)
					.setLogSigningKeys(privateKey, certificateChain).setLogEncryptionKeys(privateKey, certificateChain).build();

			final String exceptionMessage = "exceptionMessage";

			when(codec.encodeNodeKeysAsKeystore(nodeKeys, password)).thenThrow(new KeyManagementException(exceptionMessage));
			when(nodeKeysEntityRepository.existsById(NODE_ID)).thenReturn(false);

			final KeyManagementException keyManagementException = Assertions.assertThrows(KeyManagementException.class,
					() -> keysRepository.saveNodeKeys(nodeKeys, password));
			Assertions.assertEquals(exceptionMessage, keyManagementException.getMessage());
		}

		@Test
		void testLoadNodeKeys() throws KeyManagementException {
			final PrivateKey privateKey = mock(PrivateKey.class);
			final X509Certificate[] certificateChain = { mock(X509Certificate.class) };
			final NodeKeys nodeKeys = new NodeKeys.Builder().setCAKeys(privateKey, certificateChain).setEncryptionKeys(privateKey, certificateChain)
					.setLogSigningKeys(privateKey, certificateChain).setLogEncryptionKeys(privateKey, certificateChain).build();

			final byte[] keysBytes = { 1, 2, 3 };
			when(nodeKeysEntityRepository.findById(NODE_ID)).thenReturn(Optional.of(new NodeKeysEntity(NODE_ID, keysBytes)));
			when(codec.decodeNodeKeys(keysBytes, password)).thenReturn(nodeKeys);

			Assertions.assertEquals(nodeKeys, keysRepository.loadNodeKeys(password));
		}

		@Test
		void testLoadNodeKeysCodecException() throws KeyManagementException {
			when(nodeKeysEntityRepository.findById(NODE_ID)).thenReturn(Optional.of(new NodeKeysEntity(NODE_ID, new byte[0])));

			final String exceptionMessage = "exceptionMessage";
			when(codec.decodeNodeKeys(any(byte[].class), eq(password))).thenThrow(new KeyManagementException(exceptionMessage));

			final KeyManagementException keyManagementException = Assertions.assertThrows(KeyManagementException.class,
					() -> keysRepository.loadNodeKeys(password));
			Assertions.assertEquals(exceptionMessage, keyManagementException.getMessage());
		}

		@Test
		void testLoadNodeKeysNotFound() {
			when(nodeKeysEntityRepository.findById(NODE_ID)).thenReturn(Optional.empty());

			final KeyNotFoundException keyNotFoundException = Assertions.assertThrows(KeyNotFoundException.class,
					() -> keysRepository.loadNodeKeys(password));
			Assertions.assertEquals(String.format("Node keys not found for node id %s.", NODE_ID), keyNotFoundException.getMessage());
		}
	}

	@Nested
	class ElectionSigningKeysTests {
		@Test
		void testLoadElectionSigningKeys() throws KeyManagementException {
			final PrivateKey privateKey = mock(PrivateKey.class);
			final X509Certificate[] certificateChain = { mock(X509Certificate.class) };

			final ElectionSigningKeys electionSigningKeys = new ElectionSigningKeys(privateKey, certificateChain);

			final byte[] keysBytes = { 1, 2, 3 };
			final byte[] passwordBytes = { 4, 5, 6 };

			when(codec.decryptPassword(passwordBytes, encryptionPrivateKey)).thenReturn(password);
			when(codec.decodeElectionSigningKeys(keysBytes, password)).thenReturn(electionSigningKeys);

			when(electionSigningKeysEntityRepository.findById(new ElectionSigningKeysEntityPrimaryKey(NODE_ID, ELECTION_EVENT_ID)))
					.thenReturn(Optional.of(new ElectionSigningKeysEntity(NODE_ID, ELECTION_EVENT_ID, keysBytes, passwordBytes)));

			Assertions.assertEquals(electionSigningKeys, keysRepository.loadElectionSigningKeys(ELECTION_EVENT_ID).get());
			Assertions.assertTrue(password.isDestroyed());
		}

		@Test
		void testLoadElectionSigningKeysCodecException() throws KeyManagementException {
			final String exceptionMessage = "exceptionMessage";
			when(codec.decryptPassword(any(byte[].class), eq(encryptionPrivateKey))).thenThrow(new KeyManagementException(exceptionMessage));

			when(electionSigningKeysEntityRepository.findById(new ElectionSigningKeysEntityPrimaryKey(NODE_ID, ELECTION_EVENT_ID)))
					.thenReturn(Optional.of(new ElectionSigningKeysEntity(NODE_ID, ELECTION_EVENT_ID, new byte[0], new byte[0])));

			final KeyManagementException keyManagementException = Assertions.assertThrows(KeyManagementException.class,
					() -> keysRepository.loadElectionSigningKeys(ELECTION_EVENT_ID));

			Assertions.assertEquals(exceptionMessage, keyManagementException.getMessage());
		}

		@Test
		void testLoadElectionSigningKeysNotFound() throws KeyManagementException {
			when(electionSigningKeysEntityRepository.findById(new ElectionSigningKeysEntityPrimaryKey(NODE_ID, ELECTION_EVENT_ID)))
					.thenReturn(Optional.empty());

			Assertions.assertFalse(keysRepository.loadElectionSigningKeys(ELECTION_EVENT_ID).isPresent());
		}

		@Test
		void testSaveElectionSigningKeys() throws KeyManagementException {
			final PrivateKey privateKey = mock(PrivateKey.class);
			final X509Certificate[] certificateChain = { mock(X509Certificate.class) };
			final ElectionSigningKeys electionSigningKeys = new ElectionSigningKeys(privateKey, certificateChain);

			final byte[] passwordBytes = { 1 };
			final byte[] keysBytes = { 2 };
			when(codec.encryptAndEncodePassword(password, encryptionPublicKey)).thenReturn(passwordBytes);
			when(codec.encodeElectionSigningKeysAsKeystore(electionSigningKeys, password)).thenReturn(keysBytes);

			when(electionSigningKeysEntityRepository.existsById(new ElectionSigningKeysEntityPrimaryKey(NODE_ID, ELECTION_EVENT_ID)))
					.thenReturn(false);

			Assertions.assertDoesNotThrow(() -> keysRepository.saveElectionSigningKeys(ELECTION_EVENT_ID, electionSigningKeys));
			Assertions.assertTrue(password.isDestroyed());
		}

		@Test
		void testSaveElectionSigningKeysAlreadyExists() {
			final PrivateKey privateKey = mock(PrivateKey.class);
			final X509Certificate[] certificateChain = { mock(X509Certificate.class) };
			final ElectionSigningKeys electionSigningKeys = new ElectionSigningKeys(privateKey, certificateChain);

			when(electionSigningKeysEntityRepository.existsById(new ElectionSigningKeysEntityPrimaryKey(NODE_ID, ELECTION_EVENT_ID)))
					.thenReturn(true);

			final KeyAlreadyExistsException keyAlreadyExistsException = Assertions.assertThrows(KeyAlreadyExistsException.class,
					() -> keysRepository.saveElectionSigningKeys(ELECTION_EVENT_ID, electionSigningKeys));

			Assertions.assertEquals(
					String.format("Election signing keys already exist for node id %s and election event id %s.", NODE_ID, ELECTION_EVENT_ID),
					keyAlreadyExistsException.getMessage());
		}

		@Test
		void testSaveElectionSigningKeysCodecException() throws KeyManagementException {
			final PrivateKey privateKey = mock(PrivateKey.class);
			final X509Certificate[] certificateChain = { mock(X509Certificate.class) };
			final ElectionSigningKeys electionSigningKeys = new ElectionSigningKeys(privateKey, certificateChain);

			final byte[] passwordBytes = { 1 };
			when(codec.encryptAndEncodePassword(password, encryptionPublicKey)).thenReturn(passwordBytes);

			final String exceptionMessage = "exceptionMessage";
			when(codec.encodeElectionSigningKeysAsKeystore(electionSigningKeys, password)).thenThrow(new KeyManagementException(exceptionMessage));

			when(electionSigningKeysEntityRepository.existsById(new ElectionSigningKeysEntityPrimaryKey(NODE_ID, ELECTION_EVENT_ID)))
					.thenReturn(false);

			final KeyManagementException keyManagementException = Assertions.assertThrows(KeyManagementException.class,
					() -> keysRepository.saveElectionSigningKeys(ELECTION_EVENT_ID, electionSigningKeys));

			Assertions.assertEquals(exceptionMessage, keyManagementException.getMessage());
			Assertions.assertTrue(password.isDestroyed());
		}

		@Test
		void testSaveElectionSigningKeysDuplicates() {
			final PrivateKey privateKey = mock(PrivateKey.class);
			final X509Certificate[] certificateChain = { mock(X509Certificate.class) };
			final ElectionSigningKeys electionSigningKeys = new ElectionSigningKeys(privateKey, certificateChain);

			when(electionSigningKeysEntityRepository.existsById(new ElectionSigningKeysEntityPrimaryKey(NODE_ID, ELECTION_EVENT_ID)))
					.thenReturn(true);

			final KeyAlreadyExistsException keyAlreadyExistsException = Assertions.assertThrows(KeyAlreadyExistsException.class,
					() -> keysRepository.saveElectionSigningKeys(ELECTION_EVENT_ID, electionSigningKeys));
			Assertions.assertEquals(
					String.format("Election signing keys already exist for node id %s and election event id %s.", NODE_ID, ELECTION_EVENT_ID),
					keyAlreadyExistsException.getMessage());
		}

		@Test
		void testSaveElectionSigningKeysGeneratorException() throws KeyManagementException {
			final PrivateKey privateKey = mock(PrivateKey.class);
			final X509Certificate[] certificateChain = { mock(X509Certificate.class) };
			final ElectionSigningKeys electionSigningKeys = new ElectionSigningKeys(privateKey, certificateChain);

			final String exceptionMessage = "exceptionMessage";
			when(keysAndCertificateGenerator.generatePassword()).thenThrow(new KeyManagementException(exceptionMessage));

			final KeyManagementException keyManagementException = Assertions.assertThrows(KeyManagementException.class,
					() -> keysRepository.saveElectionSigningKeys(ELECTION_EVENT_ID, electionSigningKeys));
			Assertions.assertEquals(exceptionMessage, keyManagementException.getMessage());
		}
	}

}
