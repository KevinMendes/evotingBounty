/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.cryptolib.stores.keystore.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractKeystorePasswordsReader implements KeystorePasswordsReader {

	protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractKeystorePasswordsReader.class);

	private static final String EXTERNAL_PW_PATH = Paths.get(System.getProperty("user.home"), "certificates").toString();
	private static final String FILENAME_SUFFIX = ".properties";

	protected final KeystoreResources resources;
	protected final String passwordsPath;
	protected boolean destroyed;

	protected AbstractKeystorePasswordsReader(final KeystoreResources resources, final String filenamePrefix, final NodeIdentifier nodeIdentifier) {
		this.resources = resources;
		String tempPath = "";
		if (this.resources.getKeystoreProperties().isExternalPasswordsFile()) {
			tempPath = EXTERNAL_PW_PATH;
		}
		passwordsPath = tempPath + filenamePrefix + (nodeIdentifier == null ? "" : "." + nodeIdentifier.getAlias()) + FILENAME_SUFFIX;
	}

	protected KeystorePasswords getKeystorePasswordsInstance(final Properties properties) throws IOException {
		return KeystorePasswordsImpl.getInstance(properties);
	}

	/**
	 * Reads the passwords associated with the node this object was created for, without destroying the object.
	 *
	 * @return Passwords for the node this object was created for, if found; null if read error, not found, or destroyed.
	 */
	@Override
	public KeystorePasswords read() {
		if (this.destroyed) {
			final String errorMsg = "Object is destroyed";
			LOGGER.info(errorMsg);
			return null;
		} else {
			final String resourcePath = this.passwordsPath;
			try (final InputStream inputStream = this.resources.getResourceAsStream(resourcePath)) {
				if (inputStream == null) {
					final String errorMsg = "Could not locate keystore passwords at " + resourcePath;
					LOGGER.info(errorMsg);
					return null;
				} else {
					final Properties properties = new Properties();
					properties.load(inputStream);
					return this.getKeystorePasswordsInstance(properties);
				}
			} catch (final IOException ioE) {
				LOGGER.error("Error while trying to read keystore passwords from {}: {}", resourcePath, ioE.getMessage(), ioE);
				return null;
			}
		}
	}

	@Override
	public boolean isDestroyed() {
		return this.destroyed;
	}

}
