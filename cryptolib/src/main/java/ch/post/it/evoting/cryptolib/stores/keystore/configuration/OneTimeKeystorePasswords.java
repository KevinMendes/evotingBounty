/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.cryptolib.stores.keystore.configuration;

import java.io.IOException;
import java.util.Properties;

class OneTimeKeystorePasswords extends AbstractKeystorePasswords {

	protected OneTimeKeystorePasswords(final Properties properties) throws IOException {
		super(properties);
	}

	static OneTimeKeystorePasswords getInstance(final Properties properties) throws IOException {
		return new OneTimeKeystorePasswords(properties);
	}

	@Override
	protected KeystorePassword getKeystorePasswordInstance(final String value) {
		return new OneTimeKeystorePassword(value);
	}

}
