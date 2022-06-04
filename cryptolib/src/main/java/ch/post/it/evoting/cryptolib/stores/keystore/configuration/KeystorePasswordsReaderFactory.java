/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.cryptolib.stores.keystore.configuration;

public class KeystorePasswordsReaderFactory {

	public KeystorePasswordsReader getInstance(final NodeIdentifier nodeIdentifier) {
		final KeystoreResources keystoreResources = KeystoreResourcesImpl.getInstance();
		if (keystoreResources.getKeystoreProperties().isOneTimePasswordsFile()) {
			return OneTimeKeystorePasswordsReader.getInstance(keystoreResources, nodeIdentifier);
		} else {
			return KeystorePasswordsReaderImpl.getInstance(keystoreResources, nodeIdentifier);
		}
	}

}
