/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.keystore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.cryptolib.stores.keystore.configuration.AbstractKeystoreResources;

class TestKeystoreResources extends AbstractKeystoreResources {

	private static final Logger LOGGER = LoggerFactory.getLogger(TestKeystoreResources.class);

	private static final String TEST_RESOURCES_PATH = "/src/test/resources/keystore";
	private static final String TEST_KEYSTORE_PROPERTIES_PATH = "test-keystore.properties";

	private TestKeystoreResources(final String resourcesPath, final String keystorePropertiesPath) {
		super(resourcesPath, keystorePropertiesPath);
	}

	static TestKeystoreResources getInstance() {
		String keystoreLocation = System.getenv("keystore_location");
		if (keystoreLocation == null) {
			LOGGER.info("Test Keystore location NOT set in environment, defaulting to {}.", TEST_RESOURCES_PATH);
			keystoreLocation = TEST_RESOURCES_PATH;
		} else {
			LOGGER.info("Test Keystore location: {}.", keystoreLocation);
		}

		return new TestKeystoreResources(keystoreLocation, TEST_KEYSTORE_PROPERTIES_PATH);
	}

}
