/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.cryptolib.stores.keystore.configuration;

import java.io.IOException;
import java.util.Properties;

class KeystorePasswordsImpl extends AbstractKeystorePasswords {

	private KeystorePasswordsImpl(final Properties properties) throws IOException {
		super(properties);
	}

	static KeystorePasswordsImpl getInstance(final Properties properties) throws IOException {
		return new KeystorePasswordsImpl(properties);
	}

	@Override
	protected KeystorePassword getKeystorePasswordInstance(final String value) {
		return new KeystorePasswordImpl(value);
	}

}
