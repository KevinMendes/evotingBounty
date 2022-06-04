/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.cryptolib.stores.keystore.configuration;

class KeystoreReaderImpl extends AbstractKeystoreReader {

	private KeystoreReaderImpl() {
		super(KeystoreResourcesImpl.getInstance());
	}

	static KeystoreReaderImpl getInstance() {
		return new KeystoreReaderImpl();
	}

}
