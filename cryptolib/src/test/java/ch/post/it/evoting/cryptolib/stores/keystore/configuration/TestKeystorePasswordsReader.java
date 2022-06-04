/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.cryptolib.stores.keystore.configuration;

public class TestKeystorePasswordsReader extends AbstractKeystorePasswordsReader implements KeystorePasswordsReader {

	private static final String TEST_FILENAME_PREFIX = "test-keystore-passwords";

	private TestKeystorePasswordsReader(final NodeIdentifier nodeIdentifier) {
		super(TestKeystoreResources.getInstance(), TEST_FILENAME_PREFIX, nodeIdentifier);
	}

	public static TestKeystorePasswordsReader getInstance(final NodeIdentifier nodeIdentifier) {
		return new TestKeystorePasswordsReader(nodeIdentifier);
	}

	/**
	 * Only the object is destroyed. The underlying resource will not be physically destroyed.
	 */
	@Override
	public void destroy() {
		if (this.destroyed) {
			final String errorMsg = "Object already destroyed";
			LOGGER.debug(errorMsg);
		} else {
			this.destroyed = true;
		}
	}

}
