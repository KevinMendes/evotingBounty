/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.keymanagement;

import static java.text.MessageFormat.format;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.concurrent.atomic.AtomicReference;

import javax.sql.DataSource;

import org.springframework.stereotype.Service;

import ch.post.it.evoting.controlcomponents.keymanagement.exception.InvalidKeyStoreException;
import ch.post.it.evoting.controlcomponents.keymanagement.exception.InvalidNodeCAException;
import ch.post.it.evoting.controlcomponents.keymanagement.exception.InvalidPasswordException;
import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.stores.StoresServiceAPI;
import ch.post.it.evoting.cryptolib.api.stores.bean.KeyStoreType;

/**
 * <p>
 * This implementation uses a relational database as persistence storage of the keys. Clients should respect the following rules while implementing
 * the transaction management:
 * <ul>
 * <li>The supplied {@link DataSource} must be configured with the auto-commit switched off.</li>
 * <li>All the methods like
 * {@code activateNodeKeys, createAndActivateNodeKeys, createXXX, getXXX, hasValidElectionSigningKeys}
 * must be invoked in transaction.</li>
 * <li>All the checked exceptions but the generic {@link KeyManagementException} indicates some
 * precondition check failure. They are thrown before any data is actually written to the database.
 * Thus in such a case the client can proceed with the current transaction without a risk to damage
 * the data.</li>
 * </ul>
 */
@Service
public class KeysManager {

	private final AsymmetricServiceAPI asymmetricService;
	private final StoresServiceAPI storesService;
	private final KeysRepository keysRepository;

	private AtomicReference<NodeKeys> nodeKeys;

	public KeysManager(final AsymmetricServiceAPI asymmetricService, final StoresServiceAPI storesService,
			final KeysRepository keysRepository) {
		this.asymmetricService = asymmetricService;
		this.storesService = storesService;
		this.keysRepository = keysRepository;
	}

	KeyStore loadKeyStore(final InputStream stream, final PasswordProtection password) throws IOException, KeyManagementException {
		final KeyStore keyStore;
		try {
			keyStore = storesService.loadKeyStore(KeyStoreType.PKCS12, stream, password.getPassword());
		} catch (final GeneralCryptoLibException e) {
			if (e.getCause().getCause() instanceof UnrecoverableEntryException) {
				throw new InvalidPasswordException("Password is invalid.", e);
			} else if (e.getCause() instanceof IOException) {
				throw (IOException) e.getCause();
			} else {
				throw new KeyManagementException("Failed to open keystore.", e);
			}
		}
		return keyStore;
	}

	X509Certificate[] getCertificates(final PrivateKeyEntry privateKeyEntry) throws InvalidNodeCAException {
		final Certificate[] certificateChain = privateKeyEntry.getCertificateChain();
		for (final Certificate certificate : certificateChain) {
			if (!(certificate instanceof X509Certificate)) {
				throw new InvalidNodeCAException("Certificate chain contains non-X509 certificates.");
			}
		}
		return (X509Certificate[]) certificateChain;
	}

	PrivateKeyEntry getPrivateKeyEntry(final KeyStore keyStore, final String alias, final PasswordProtection password) throws KeyManagementException {
		final PrivateKeyEntry entry;
		try {
			if (!keyStore.entryInstanceOf(alias, PrivateKeyEntry.class)) {
				throw new InvalidKeyStoreException(format("Invalid CCN CA alias ''{0}''", alias));
			}
			entry = (PrivateKeyEntry) keyStore.getEntry(alias, password);
		} catch (final UnrecoverableEntryException e) {
			throw new InvalidPasswordException("Password is invalid.", e);
		} catch (final KeyStoreException | NoSuchAlgorithmException e) {
			throw new KeyManagementException("Failed to open keystore.", e);
		}
		return entry;
	}

	public X509Certificate getPlatformCACertificate() {
		throwIfNodeKeysAbsent();

		// The existence of a non-empty certificate chain is mandatory, as the object cannot be built otherwise.
		return nodeKeys.get().caCertificateChain()[nodeKeys.get().caCertificateChain().length - 1];
	}

	public boolean hasNodeKeys() {
		return nodeKeys != null;
	}

	public X509Certificate nodeCACertificate() {
		throwIfNodeKeysAbsent();

		return nodeKeys.get().caCertificate();
	}

	public PublicKey nodeLogEncryptionPublicKey() {
		return nodeKeys.get().logEncryptionPublicKey();
	}

	public PrivateKey nodeLogSigningPrivateKey() {
		throwIfNodeKeysAbsent();

		return nodeKeys.get().logSigningPrivateKey();
	}

	void activateNodeKeys(final NodeKeys nodeKeys) {

		final PrivateKey privateKey = nodeKeys.encryptionPrivateKey();
		final PublicKey publicKey = nodeKeys.encryptionPublicKey();
		keysRepository.setEncryptionKeys(privateKey, publicKey);

		this.nodeKeys = new AtomicReference<>(nodeKeys);
	}

	public void checkNodeCAKeysMatch(final PrivateKey nodeCAPrivateKey, final PublicKey nodeCAPublicKey) throws InvalidNodeCAException {

		final byte[] bytes = nodeCAPublicKey.getEncoded();

		try {
			final byte[] signature = asymmetricService.sign(nodeCAPrivateKey, bytes);
			if (!asymmetricService.verifySignature(signature, nodeCAPublicKey, bytes)) {
				throw new InvalidNodeCAException("CCN CA keys do not match.");
			}
		} catch (final GeneralCryptoLibException e) {
			throw new InvalidNodeCAException("CCN CA keys are invalid.", e);
		}
	}

	void throwIfNodeKeysAbsent() {
		if (!hasNodeKeys()) {
			throw new IllegalStateException("Node keys are not activated.");
		}
	}

	public NodeKeys getNodeKeys() {
		return nodeKeys.get();
	}
}
