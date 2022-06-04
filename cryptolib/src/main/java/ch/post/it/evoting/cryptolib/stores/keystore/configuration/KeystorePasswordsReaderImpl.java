/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.cryptolib.stores.keystore.configuration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.security.auth.Destroyable;

class KeystorePasswordsReaderImpl extends AbstractKeystorePasswordsReader implements Destroyable {

	private static final String DEFAULT_FILENAME_PREFIX = "keystore-passwords";

	protected KeystorePasswordsReaderImpl(final KeystoreResources keystoreResources, final NodeIdentifier nodeIdentifier) {
		super(keystoreResources, DEFAULT_FILENAME_PREFIX, nodeIdentifier);
	}

	static KeystorePasswordsReaderImpl getInstance(final KeystoreResources keystoreResources, final NodeIdentifier nodeIdentifier) {
		return new KeystorePasswordsReaderImpl(keystoreResources, nodeIdentifier);
	}

	@Override
	public void destroy() {
		if (this.destroyed) {
			final String errorMsg = "Object already destroyed";
			LOGGER.debug(errorMsg);
		} else {
			final Path passwordsPath = this.resources.getResourcePath(this.passwordsPath);
			if (passwordsPath == null) {
				LOGGER.info("No destroyable file at {}", this.passwordsPath);
			} else {
				try {
					Files.delete(passwordsPath);
					this.destroyed = true;
				} catch (IOException ioE) {
					LOGGER.info("Could not destroy file {}: {}", passwordsPath, ioE.getMessage(), ioE);
				}
			}
		}
	}

}
