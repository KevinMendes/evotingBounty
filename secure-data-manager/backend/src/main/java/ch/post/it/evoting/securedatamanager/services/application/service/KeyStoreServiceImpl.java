/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.service;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ch.post.it.evoting.cryptolib.stores.keystore.configuration.KeystoreReader;
import ch.post.it.evoting.cryptolib.stores.keystore.configuration.KeystoreReaderFactory;
import ch.post.it.evoting.cryptolib.stores.keystore.configuration.NodeIdentifier;
import ch.post.it.evoting.securedatamanager.services.application.exception.KeyStoreServiceException;

@Service
public class KeyStoreServiceImpl implements KeyStoreService {

	private static final String PRIVATE_KEYSTORE_PW = "private-keystore.password";
	private static final Logger LOGGER = LoggerFactory.getLogger(KeyStoreServiceImpl.class);
	private static final NodeIdentifier THIS_NODE = NodeIdentifier.SECURE_DATA_MANAGER;
	private final KeystoreReader keystoreReader = new KeystoreReaderFactory().getInstance();
	@Value("${keystore.password.file}")
	private String passwordFile;

	private PrivateKey key;

	@PostConstruct
	public void readPrivateKey() {
		LOGGER.info("Trying to get Private key from proper keystore to sign requests...");

		try (final Reader reader = new FileReader(Paths.get(passwordFile).toFile())) {
			final Properties prop = new Properties();
			prop.load(reader);
			final String password = prop.getProperty(PRIVATE_KEYSTORE_PW);
			key = this.keystoreReader.readSigningPrivateKey(THIS_NODE, password);
			if (key == null) {
				LOGGER.error("Unable to create request signing key with the password stored at: {}", passwordFile);
				throw new KeyStoreServiceException("Unable to create request signing key with the password stored at: " + passwordFile);
			}
		} catch (final IOException e) {
			LOGGER.error("Failed to read keystore password from file: {}", passwordFile);
			throw new KeyStoreServiceException("Failed to read keystore password from file: " + passwordFile, e);
		}
	}

	public PrivateKey getPrivateKey() {
		return key;
	}
}
