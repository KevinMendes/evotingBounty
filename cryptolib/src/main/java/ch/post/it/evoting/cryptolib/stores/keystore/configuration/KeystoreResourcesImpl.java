/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.cryptolib.stores.keystore.configuration;

import java.nio.file.FileSystems;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class KeystoreResourcesImpl extends AbstractKeystoreResources {

	private static final Logger LOGGER = LoggerFactory.getLogger(KeystoreResourcesImpl.class);

	private static final String DEFAULT_RESOURCES_PATH = System.getProperty("user.home") + FileSystems.getDefault().getSeparator() + "keystore";
	private static final String DEFAULT_KEYSTORE_PROPERTIES_PATH = "keystore.properties";

	private KeystoreResourcesImpl(final String resourcesPath, final String keystorePropertiesPath) {
		super(resourcesPath, keystorePropertiesPath);
	}

	static KeystoreResourcesImpl getInstance() {
		String keystoreLocation = System.getenv("keystore_location");
		if (keystoreLocation == null) {
			LOGGER.info("Keystore location NOT set in environment, defaulting to {}.", DEFAULT_RESOURCES_PATH);
			keystoreLocation = DEFAULT_RESOURCES_PATH;
		} else {
			LOGGER.info("Keystore location: {}.", keystoreLocation);
		}

		return new KeystoreResourcesImpl(keystoreLocation, DEFAULT_KEYSTORE_PROPERTIES_PATH);
	}

}
