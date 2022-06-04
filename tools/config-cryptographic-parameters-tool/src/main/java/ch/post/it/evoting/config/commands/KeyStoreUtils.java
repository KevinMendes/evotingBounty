/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.config.commands;

import static java.text.MessageFormat.format;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.UnrecoverableEntryException;
import java.security.UnrecoverableKeyException;
import java.util.Enumeration;

import org.springframework.stereotype.Component;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.stores.StoresServiceAPI;
import ch.post.it.evoting.cryptolib.api.stores.bean.KeyStoreType;

@Component
public class KeyStoreUtils {

	private static final String MALFORMED_KEYSTORE = "Malformed Keystore";

	private final StoresServiceAPI storesService;

	public KeyStoreUtils(StoresServiceAPI storesService) {
		this.storesService = storesService;
	}

	public KeyStore decodeKeyStore(final String p12Path, char[] protection) {
		try (InputStream stream = Files.newInputStream(Paths.get(p12Path))) {
			return storesService.loadKeyStore(KeyStoreType.PKCS12, stream, protection);
		} catch (GeneralCryptoLibException e) {
			if (e.getCause() instanceof IOException && e.getCause().getCause() instanceof UnrecoverableKeyException) {
				throw new IllegalArgumentException("Key store password is invalid.", e);
			} else {
				throw new IllegalArgumentException("Failed to decode the key store.", e);
			}
		} catch (IOException e) {
			throw new IllegalArgumentException("Failed to decode the key store.", e);
		}
	}

	public PrivateKey loadPrivateKeyFromKeyStore(final KeyStore keyStore, final char[] protection) {
		try {
			KeyStore.PrivateKeyEntry privateKeyEntry = decodePrivateKeyEntry(keyStore, getAlias(keyStore),
					new KeyStore.PasswordProtection(protection));
			return privateKeyEntry.getPrivateKey();
		} catch (KeyManagementException | KeyStoreException e) {
			throw new IllegalArgumentException(e.getMessage(), e);
		}
	}

	public KeyStore.PrivateKeyEntry decodePrivateKeyEntry(KeyStore store, String alias, KeyStore.PasswordProtection protection)
			throws KeyManagementException {
		try {
			if (!store.entryInstanceOf(alias, KeyStore.PrivateKeyEntry.class)) {
				throw new KeyManagementException(format("Key entry ''{0}'' is missing or invalid.", alias));
			}
			return (KeyStore.PrivateKeyEntry) store.getEntry(alias, protection);
		} catch (UnrecoverableKeyException e) {
			throw new InvalidPasswordException("Invalid key password.", e);
		} catch (KeyStoreException | NoSuchAlgorithmException | UnrecoverableEntryException e) {
			throw new KeyManagementException("Failed to decode private key entry.", e);
		}
	}

	/**
	 * The keyStore must contain only one {@link KeyStore.PrivateKeyEntry}. Discover what is the alias for that entry.
	 */
	public String getAlias(final KeyStore keyStore) throws KeyStoreException {
		String privateKeyEntryAlias = null;
		Enumeration<String> aliases = keyStore.aliases();
		while (aliases.hasMoreElements()) {
			String currentAlias = aliases.nextElement();
			if (privateKeyEntryAlias == null) {
				if (aliasPKEntry(keyStore, currentAlias)) {
					privateKeyEntryAlias = currentAlias;
				}
			} else {
				if (aliasPKEntry(keyStore, currentAlias)) {
					throw new KeyStoreException(MALFORMED_KEYSTORE);
				}
			}
		}
		return privateKeyEntryAlias;
	}

	private static boolean aliasPKEntry(final KeyStore keyStore, final String alias) throws KeyStoreException {
		return keyStore.entryInstanceOf(alias, KeyStore.PrivateKeyEntry.class);
	}
}
