/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.keymanagement;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.Files.newInputStream;
import static java.nio.file.Files.readAllBytes;
import static java.security.KeyStore.PrivateKeyEntry;
import static java.text.MessageFormat.format;
import static java.util.Arrays.fill;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.Lock;

import javax.security.auth.DestroyFailedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.support.locks.LockRegistry;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import ch.post.it.evoting.controlcomponents.keymanagement.exception.InvalidKeyStoreException;
import ch.post.it.evoting.controlcomponents.keymanagement.exception.InvalidNodeCAException;
import ch.post.it.evoting.controlcomponents.keymanagement.exception.InvalidPasswordException;
import ch.post.it.evoting.controlcomponents.keymanagement.persistence.NodeKeysEntity;
import ch.post.it.evoting.controlcomponents.keymanagement.persistence.NodeKeysEntityRepository;

@Transactional(noRollbackFor = { InvalidKeyStoreException.class, InvalidPasswordException.class, InvalidNodeCAException.class,
		NoSuchFileException.class, IOException.class }, rollbackFor = KeyManagementException.class)
@Service
public class NodeKeysActivator {

	private static final Logger LOGGER = LoggerFactory.getLogger(NodeKeysActivator.class);

	private final LockRegistry nodeLockRegistry;
	private final KeysAndCertificateGenerator keysAndCertificateGenerator;
	private final NodeKeysEntityRepository nodeKeysEntityRepository;
	private final KeysManager keysManager;
	private final Codec codec;
	@Value("${key.node.id}")
	String nodeId;

	public NodeKeysActivator(final LockRegistry registry,
			final KeysAndCertificateGenerator keysAndCertificateGenerator, final NodeKeysEntityRepository nodeKeysEntityRepository,
			final KeysManager manager, final Codec codec) {
		this.nodeLockRegistry = registry;
		this.keysAndCertificateGenerator = keysAndCertificateGenerator;
		this.nodeKeysEntityRepository = nodeKeysEntityRepository;
		this.keysManager = manager;
		this.codec = codec;
	}

	private static void deletePasswordFile(final Path passwordFile) throws IOException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(format("Deleting password file ''{0}''...", passwordFile));
		}
		deleteIfExists(passwordFile);
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(format("Password file ''{0}'' has been deleted.", passwordFile));
		}
	}

	private static void destroyPassword(final PasswordProtection password) {
		try {
			password.destroy();
		} catch (final DestroyFailedException e) {
			LOGGER.warn("Failed to destroy password.", e);
		}
	}

	private static PasswordProtection loadPassword(final Path file) throws IOException {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(format("Loading password from file ''{0}''...", file));
		}
		final ByteBuffer bytes = ByteBuffer.wrap(readAllBytes(file));
		try {
			final CharBuffer chars = StandardCharsets.UTF_8.decode(bytes);
			try {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug(format("Password has been loaded from file ''{0}''.", file));
				}
				return new PasswordProtection(chars.array());
			} finally {
				fill(chars.array(), '\u0000');
			}
		} finally {
			fill(bytes.array(), (byte) 0);
		}
	}

	@SuppressWarnings("squid:S2222")
	@Retryable(value = TimeoutException.class, maxAttempts = 5, backoff = @Backoff(delay = 1000, multiplier = 2, random = true))
	public void activateNodeKeys(final Path keyStoreFile, final String nodeAlias, final Path passwordFile)
			throws KeyManagementException, IOException, InterruptedException, TimeoutException {

		checkNotNull(keysManager);
		checkNotNull(keyStoreFile);
		checkNotNull(passwordFile);

		//A node level lock across all instances.
		final Lock nodeLock = nodeLockRegistry.obtain(LockKey.NODE_KEY.getKey());

		final PasswordProtection password = loadPassword(passwordFile);
		checkNotNull(password, "Password cannot be empty {} see file : ", passwordFile);

		//The lock is released in the final section
		if (nodeLock.tryLock(5, TimeUnit.SECONDS)) {
			LOGGER.info("Acquired lock for node {}", nodeId);
			try {
				final Optional<NodeKeysEntity> optionalNodeKeysEntity = nodeKeysEntityRepository.findById(nodeId);

				final NodeKeys nodeKeys;

				if (optionalNodeKeysEntity.isPresent()) {
					final byte[] keys = optionalNodeKeysEntity.get().getKeys();
					nodeKeys = codec.decodeNodeKeys(keys, password);
					LOGGER.info("Found node keys in database and successfully decrypted them");
				} else {

					final KeyStore loadKeyStore = keysManager.loadKeyStore(newInputStream(keyStoreFile), password);

					final PrivateKeyEntry privateKeyEntry = keysManager.getPrivateKeyEntry(loadKeyStore, nodeAlias, password);
					final X509Certificate[] certificates = keysManager.getCertificates(privateKeyEntry);

					validateNodeCAX509Elements(privateKeyEntry.getPrivateKey(), certificates);

					nodeKeys = keysAndCertificateGenerator.generateNodeKeys(privateKeyEntry.getPrivateKey(), certificates);
					final byte[] keys = codec.encodeNodeKeysAsKeystore(nodeKeys, password);
					final NodeKeysEntity nodeKeysEntity = new NodeKeysEntity(nodeId, keys);
					nodeKeysEntityRepository.save(nodeKeysEntity);
					LOGGER.info("Did not find node keys in database, successfully created them");
				}
				keysManager.activateNodeKeys(nodeKeys);
				LOGGER.info("Successfully placed keys in memory");
			} finally {
				nodeLock.unlock();
				destroyPassword(password);
				deletePasswordFile(passwordFile);
				LOGGER.info("Destroyed instance password file and removed password from memory. Released node lock");
			}
		} else {
			LOGGER.info("Failed to acquire lock");
			throw new TimeoutException("Failed to acquire node lock");
		}
	}

	private void validateNodeCAX509Elements(final PrivateKey nodeCAPrivateKey, final X509Certificate[] nodeCACertificateChain) throws InvalidNodeCAException {
		checkNotNull(nodeCAPrivateKey, "Node CA private key cannot be null.");
		checkNotNull(nodeCACertificateChain, "Node CA Certificate chain cannot be null.");
		checkArgument(nodeCACertificateChain.length != 0, "Node CA certificate chain is empty.");
		keysManager.checkNodeCAKeysMatch(nodeCAPrivateKey, nodeCACertificateChain[0].getPublicKey());
	}
}
