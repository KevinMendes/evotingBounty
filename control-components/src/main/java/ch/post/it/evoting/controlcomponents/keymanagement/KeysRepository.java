/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.keymanagement;

import java.security.KeyManagementException;
import java.security.KeyPair;
import java.security.KeyStore.PasswordProtection;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import javax.security.auth.DestroyFailedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;

import ch.post.it.evoting.controlcomponents.keymanagement.exception.KeyAlreadyExistsException;
import ch.post.it.evoting.controlcomponents.keymanagement.exception.KeyNotFoundException;
import ch.post.it.evoting.controlcomponents.keymanagement.persistence.ElectionSigningKeysEntity;
import ch.post.it.evoting.controlcomponents.keymanagement.persistence.ElectionSigningKeysEntityPrimaryKey;
import ch.post.it.evoting.controlcomponents.keymanagement.persistence.ElectionSigningKeysEntityRepository;
import ch.post.it.evoting.controlcomponents.keymanagement.persistence.NodeKeysEntity;
import ch.post.it.evoting.controlcomponents.keymanagement.persistence.NodeKeysEntityRepository;

@Repository
public class KeysRepository {
	private static final Logger LOGGER = LoggerFactory.getLogger(KeysRepository.class);

	private final Codec codec;
	private final KeysAndCertificateGenerator keysAndCertificateGenerator;
	private final String nodeId;
	private final NodeKeysEntityRepository nodeKeysEntityRepository;
	private final ElectionSigningKeysEntityRepository electionSigningKeysEntityRepository;
	//In memory reference to keys
	private AtomicReference<KeyPair> encryptionKeys;

	public KeysRepository(final Codec codec, final KeysAndCertificateGenerator keysAndCertificateGenerator,
			final NodeKeysEntityRepository nodeKeysEntityRepository,
			final ElectionSigningKeysEntityRepository electionSigningKeysEntityRepository,
			@Value("${key.node.id}")
			final String nodeId) {

		this.codec = codec;
		this.keysAndCertificateGenerator = keysAndCertificateGenerator;
		this.nodeId = nodeId;
		this.nodeKeysEntityRepository = nodeKeysEntityRepository;
		this.electionSigningKeysEntityRepository = electionSigningKeysEntityRepository;
	}

	/**
	 * Loads the election signing keys from the db.
	 *
	 * @param electionEventId the election id for which to retrieve the election signing keys.
	 * @return the election signing keys
	 * @throws KeyManagementException if the keys can't be decrypted.
	 */
	public Optional<ElectionSigningKeys> loadElectionSigningKeys(final String electionEventId) throws KeyManagementException {

		final Optional<ElectionSigningKeysEntity> optionalElectionSigningKeysEntity = electionSigningKeysEntityRepository
				.findById(new ElectionSigningKeysEntityPrimaryKey(nodeId, electionEventId));

		if (!optionalElectionSigningKeysEntity.isPresent()) {
			return Optional.empty();
		}

		final ElectionSigningKeysEntity electionSigningKeysEntity = optionalElectionSigningKeysEntity.get();
		final PasswordProtection password = codec.decryptPassword(electionSigningKeysEntity.getPassword(), encryptionKeys.get().getPrivate());

		try {
			final ElectionSigningKeys value = codec.decodeElectionSigningKeys(electionSigningKeysEntity.getKeys(), password);
			return Optional.of(value);
		} finally {
			try {
				password.destroy();
			} catch (final DestroyFailedException e) {
				LOGGER.warn(String.format("Failed to destroy the password for node id %s and election event id %s.", nodeId, electionEventId), e);
			}
		}
	}

	public NodeKeys loadNodeKeys(final PasswordProtection passwordProtection) throws KeyManagementException {
		final Optional<NodeKeysEntity> optionalNodeKeysEntity = nodeKeysEntityRepository.findById(nodeId);

		if (!optionalNodeKeysEntity.isPresent()) {
			throw new KeyNotFoundException(String.format("Node keys not found for node id %s.", nodeId));
		}

		final byte[] keys = optionalNodeKeysEntity.get().getKeys();

		return codec.decodeNodeKeys(keys, passwordProtection);
	}

	public void saveElectionSigningKeys(final String electionEventId, final ElectionSigningKeys electionSigningKeys) throws KeyManagementException {

		if (electionSigningKeysEntityRepository.existsById(new ElectionSigningKeysEntityPrimaryKey(nodeId, electionEventId))) {
			throw new KeyAlreadyExistsException(
					String.format("Election signing keys already exist for node id %s and election event id %s.", nodeId, electionEventId));
		}

		final PasswordProtection passwordProtection = keysAndCertificateGenerator.generatePassword();
		try {

			final byte[] keys = codec.encodeElectionSigningKeysAsKeystore(electionSigningKeys, passwordProtection);
			final byte[] password = codec.encryptAndEncodePassword(passwordProtection, encryptionKeys.get().getPublic());

			electionSigningKeysEntityRepository.save(new ElectionSigningKeysEntity(nodeId, electionEventId, keys, password));

		} finally {
			try {
				passwordProtection.destroy();
			} catch (final DestroyFailedException e) {
				LOGGER.warn(String.format("Failed to destroy the password for node id %s and election event id %s.", nodeId, electionEventId), e);
			}
		}
	}

	public void saveNodeKeys(final NodeKeys nodeKeys, final PasswordProtection passwordProtection) throws KeyManagementException {
		if (nodeKeysEntityRepository.existsById(nodeId)) {
			throw new KeyAlreadyExistsException(String.format("Node keys already exist for node id %s.", nodeId));
		}

		final byte[] keys = codec.encodeNodeKeysAsKeystore(nodeKeys, passwordProtection);
		nodeKeysEntityRepository.save(new NodeKeysEntity(nodeId, keys));
	}

	public void setEncryptionKeys(final PrivateKey privateKey, final PublicKey publicKey) {
		this.encryptionKeys = new AtomicReference<>(new KeyPair(publicKey, privateKey));
	}

}
