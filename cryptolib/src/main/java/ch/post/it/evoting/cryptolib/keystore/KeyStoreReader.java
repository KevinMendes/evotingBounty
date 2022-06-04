/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.cryptolib.keystore;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore.PasswordProtection;
import java.security.PrivateKey;
import java.util.Base64;
import java.util.List;

import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.extendedkeystore.KeyStoreService;
import ch.post.it.evoting.cryptolib.extendedkeystore.cryptoapi.CryptoAPIExtendedKeyStore;
import ch.post.it.evoting.cryptolib.extendedkeystore.service.ExtendedKeyStoreService;

/**
 * A utility class for keystores management
 */
public class KeyStoreReader {

	private final KeyStoreService keyStoresService;

	/**
	 * Provides functionality for extracting data from a {@link KeyStoreService}.
	 */
	public KeyStoreReader() {

		keyStoresService = new ExtendedKeyStoreService();
	}

	public static String toString(final CryptoAPIExtendedKeyStore keyStore, final char[] password) {

		try {
			final byte[] keyStoreJSONBytes = keyStore.toJSON(password).getBytes(StandardCharsets.UTF_8);
			return Base64.getEncoder().encodeToString(keyStoreJSONBytes);

		} catch (final GeneralCryptoLibException e) {
			throw new CryptoLibException("Exception while obtaining string representation of keystore: " + e.getMessage(), e);
		}
	}

	/**
	 * Extracts a {@link PrivateKey} from the keystore at the path {@code pathAndFilenameOfKeystore}, using a password read from the file {@code
	 * pathAndFilenameOfPasswordFile}.
	 *
	 * @param pathAndFilenameOfKeystore     the path of the keystore.
	 * @param pathAndFilenameOfPasswordFile the path of the file containing the password that can be used to open the keystore.
	 * @param passwordTag                   the tag that is associated with the password within the file {@code pathAndFilenameOfPasswordFile}.
	 * @param alias                         the alias that is associated with the private key in the keystore.
	 * @return The extracted private key.
	 * @throws CryptoLibException
	 */
	public PrivateKey getPrivateKey(final Path pathAndFilenameOfKeystore, final Path pathAndFilenameOfPasswordFile, final String passwordTag,
			final String alias) {

		try (final InputStream in = new FileInputStream(pathAndFilenameOfKeystore.toFile())) {
			final char[] password = getPasswordFromFile(pathAndFilenameOfPasswordFile, passwordTag);
			final CryptoAPIExtendedKeyStore ks = keyStoresService.loadKeyStore(in, new PasswordProtection(password));
			return ks.getPrivateKeyEntry(alias, password);
		} catch (final IOException | GeneralCryptoLibException e) {
			throw new CryptoLibException(e);
		}

	}

	private char[] getPasswordFromFile(final Path path, final String name) throws IOException {

		final List<String> lines = Files.readAllLines(path);
		String password = null;

		for (final String line : lines) {
			final String[] splittedLine = line.split(",");

			if (splittedLine[0].equals(name)) {
				password = splittedLine[1];
			}
		}

		if (password == null) {
			throw new CryptoLibException("The passwords file does not contain a password for " + name);
		}

		return password.toCharArray();
	}
}
