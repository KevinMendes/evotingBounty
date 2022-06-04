/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.keymanagement;

import static java.text.MessageFormat.format;
import static java.util.Arrays.fill;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStore.PasswordProtection;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.security.cert.X509Certificate;

import org.springframework.stereotype.Service;

import ch.post.it.evoting.controlcomponents.keymanagement.exception.InvalidPasswordException;
import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.stores.StoresServiceAPI;
import ch.post.it.evoting.cryptolib.api.stores.bean.KeyStoreType;
import ch.post.it.evoting.cryptolib.elgamal.bean.ElGamalPrivateKey;
import ch.post.it.evoting.cryptolib.elgamal.bean.ElGamalPublicKey;

@Service
public class Codec {
	private static final String NODE_CA_ALIAS = "CA";

	private static final String NODE_ENCRYPTION_ALIAS = "encryption";

	private static final String NODE_LOG_SIGNING_ALIAS = "logSigning";

	private static final String NODE_LOG_ENCRYPTION_ALIAS = "logEncryption";

	private static final String ELECTION_SIGNING_ALIAS = "signing";

	private final StoresServiceAPI storesService;

	private final AsymmetricServiceAPI asymmetricService;

	public Codec(final StoresServiceAPI storesService, final AsymmetricServiceAPI asymmetricService) {
		this.storesService = storesService;
		this.asymmetricService = asymmetricService;
	}

	private static PrivateKeyEntry getPrivateKeyEntry(final KeyStore store, final String alias, final PasswordProtection protection) throws KeyManagementException {
		try {
			if (!store.entryInstanceOf(alias, PrivateKeyEntry.class)) {
				throw new KeyManagementException(format("Key entry ''{0}'' is missing or invalid.", alias));
			}
			return (PrivateKeyEntry) store.getEntry(alias, protection);
		} catch (final UnrecoverableKeyException e) {
			throw new InvalidPasswordException("Invalid key password.", e);
		} catch (final KeyStoreException | NoSuchAlgorithmException | UnrecoverableEntryException e) {
			throw new KeyManagementException("Failed to decode private key entry.", e);
		}
	}

	public ElectionSigningKeys decodeElectionSigningKeys(final byte[] bytes, final PasswordProtection protection) throws KeyManagementException {
		final KeyStore store = decodeKeyStore(bytes, protection);
		final PrivateKeyEntry entry = getPrivateKeyEntry(store, ELECTION_SIGNING_ALIAS, protection);
		return new ElectionSigningKeys(entry.getPrivateKey(), (X509Certificate[]) entry.getCertificateChain());
	}

	public NodeKeys decodeNodeKeys(final byte[] bytes, final PasswordProtection password) throws KeyManagementException {
		final KeyStore store = decodeKeyStore(bytes, password);
		return new NodeKeys.Builder().setCAKeys(getPrivateKeyEntry(store, NODE_CA_ALIAS, password))
				.setEncryptionKeys(getPrivateKeyEntry(store, NODE_ENCRYPTION_ALIAS, password))
				.setLogSigningKeys(getPrivateKeyEntry(store, NODE_LOG_SIGNING_ALIAS, password))
				.setLogEncryptionKeys(getPrivateKeyEntry(store, NODE_LOG_ENCRYPTION_ALIAS, password)).build();
	}

	public PasswordProtection decryptPassword(final byte[] bytes, final PrivateKey encryptionKey) throws KeyManagementException {

		final byte[] decryptedBytes;
		try {
			decryptedBytes = asymmetricService.decrypt(encryptionKey, bytes);
		} catch (final GeneralCryptoLibException e) {
			throw new KeyManagementException("Failed to decode password.", e);
		}
		final ByteBuffer byteBuffer = ByteBuffer.wrap(decryptedBytes);
		try {
			final CharBuffer charBuffer = StandardCharsets.UTF_8.decode(byteBuffer);
			try {
				return new PasswordProtection(charBuffer.array());
			} finally {
				fill(charBuffer.array(), '\u0000');
			}
		} finally {
			fill(byteBuffer.array(), (byte) 0);
		}
	}

	public byte[] encodeElectionSigningKeysAsKeystore(final ElectionSigningKeys electionSigningKeys, final PasswordProtection protection)
			throws KeyManagementException {
		try {
			final KeyStore store = storesService.createKeyStore(KeyStoreType.PKCS12);
			store.setKeyEntry(ELECTION_SIGNING_ALIAS, electionSigningKeys.privateKey(), protection.getPassword(),
					electionSigningKeys.certificateChain());
			try (final ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
				store.store(stream, protection.getPassword());
				return stream.toByteArray();
			}
		} catch (final IOException | GeneralCryptoLibException | GeneralSecurityException e) {
			throw new KeyManagementException("Failed to encode election signing keys.", e);
		}
	}

	public byte[] encryptAndEncodeElGamalPrivateKey(final ElGamalPrivateKey key, final PublicKey encryptionKey) throws KeyManagementException {
		try {
			final byte[] bytes = key.toJson().getBytes(StandardCharsets.UTF_8);
			return asymmetricService.encrypt(encryptionKey, bytes);
		} catch (final GeneralCryptoLibException e) {
			throw new KeyManagementException("Failed to encode ElGamal private key.", e);
		}
	}

	public byte[] encodeElGamalPublicKey(final ElGamalPublicKey key) throws KeyManagementException {
		try {
			return key.toJson().getBytes(StandardCharsets.UTF_8);
		} catch (final GeneralCryptoLibException e) {
			throw new KeyManagementException("Failed to encode ElGamal public key.", e);
		}
	}

	public byte[] encodeNodeKeysAsKeystore(final NodeKeys nodeKeys, final PasswordProtection protection) throws KeyManagementException {
		try {
			final KeyStore store = storesService.createKeyStore(KeyStoreType.PKCS12);
			store.setKeyEntry(NODE_CA_ALIAS, nodeKeys.caPrivateKey(), protection.getPassword(), nodeKeys.caCertificateChain());
			store.setKeyEntry(NODE_ENCRYPTION_ALIAS, nodeKeys.encryptionPrivateKey(), protection.getPassword(),
					nodeKeys.encryptionCertificateChain());
			store.setKeyEntry(NODE_LOG_SIGNING_ALIAS, nodeKeys.logSigningPrivateKey(), protection.getPassword(),
					nodeKeys.logSigningCertificateChain());
			store.setKeyEntry(NODE_LOG_ENCRYPTION_ALIAS, nodeKeys.logEncryptionPrivateKey(), protection.getPassword(),
					nodeKeys.logEncryptionCertificateChain());
			try (final ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
				store.store(stream, protection.getPassword());
				return stream.toByteArray();
			}
		} catch (final IOException | GeneralCryptoLibException | GeneralSecurityException e) {
			throw new KeyManagementException("Failed to encode election signing keys.", e);
		}
	}

	public byte[] encryptAndEncodePassword(final PasswordProtection protection, final PublicKey encryptionKey) throws KeyManagementException {
		final CharBuffer charBuffer = CharBuffer.wrap(protection.getPassword());
		final ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(charBuffer);
		final byte[] bytes = new byte[byteBuffer.limit()];
		byteBuffer.get(bytes);
		try {
			return asymmetricService.encrypt(encryptionKey, bytes);
		} catch (final GeneralCryptoLibException e) {
			throw new KeyManagementException("Failed to encode password.", e);
		} finally {
			fill(bytes, (byte) 0);
			fill(byteBuffer.array(), (byte) 0);
			fill(charBuffer.array(), '\u0000');
		}
	}

	private KeyStore decodeKeyStore(final byte[] bytes, final PasswordProtection protection) throws KeyManagementException {
		try (final InputStream stream = new ByteArrayInputStream(bytes)) {
			return storesService.loadKeyStore(KeyStoreType.PKCS12, stream, protection.getPassword());
		} catch (final GeneralCryptoLibException e) {
			if (e.getCause() instanceof IOException && e.getCause().getCause() instanceof UnrecoverableKeyException) {
				throw new InvalidPasswordException("Key store password is invalid.", e);
			} else {
				throw new KeyManagementException("Failed to decode the key store.", e);
			}
		} catch (final IOException e) {
			throw new KeyManagementException("Failed to decode the key store.", e);
		}
	}
}
