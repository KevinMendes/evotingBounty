/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.cryptolib.stores.keystore.configuration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractKeystoreResources implements KeystoreResources {

	protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractKeystoreResources.class);

	private final Path resourcesPath;
	private final KeystoreProperties keystoreProperties;

	protected AbstractKeystoreResources(final String resourcesPath, final String keystorePropertiesPath) {
		this.resourcesPath = Paths.get(resourcesPath);
		this.keystoreProperties = this.readKeystoreProperties(keystorePropertiesPath);
	}

	private KeystoreProperties readKeystoreProperties(final String keystorePropertiesPath) {
		final Properties properties = new Properties();
		try (final InputStream inputStream = this.getResourceAsStream(keystorePropertiesPath)) {
			if (inputStream == null) {
				LOGGER.info("Keystore properties not found at {}.", this.resourcesPath.resolve(keystorePropertiesPath));
			} else {
				properties.load(inputStream);
			}
		} catch (final IOException e) {
			LOGGER.error("Error while trying to initialize keystore properties from {}.", this.resourcesPath.resolve(keystorePropertiesPath), e);
		}
		return KeystoreProperties.read(properties);
	}

	@Override
	public Path getResourcePath(final String resourceName) {
		return resourcesPath.resolve(resourceName);
	}

	@Override
	public InputStream getResourceAsStream(final String resourceName) {
		final Path resourcePath = this.getResourcePath(resourceName);
		try {
			return Files.newInputStream(resourcePath);
		} catch (IOException e) {
			final String errorMsg = "Resource not found at " + resourcePath;
			LOGGER.info(errorMsg);
			return null;
		}
	}

	@Override
	public KeystoreProperties getKeystoreProperties() {
		return this.keystoreProperties;
	}

}
