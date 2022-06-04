/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.cryptolib.stores.keystore.configuration;

import java.io.IOException;
import java.util.Properties;

class OneTimeKeystorePasswordsReader extends KeystorePasswordsReaderImpl {

	protected OneTimeKeystorePasswordsReader(final KeystoreResources keystoreResources, final NodeIdentifier nodeIdentifier) {
		super(keystoreResources, nodeIdentifier);
	}

	static OneTimeKeystorePasswordsReader getInstance(final KeystoreResources keystoreResources, final NodeIdentifier nodeIdentifier) {
		return new OneTimeKeystorePasswordsReader(keystoreResources, nodeIdentifier);
	}

	@Override
	protected KeystorePasswords getKeystorePasswordsInstance(final Properties passwords) throws IOException {
		return OneTimeKeystorePasswords.getInstance(passwords);
	}

	/**
	 * If first-time read, the passwords will be read and the object with self-destroy immediately afterwards, together with its underlying resource.
	 * All subsequent read attempts will raise an IllegalStateException.
	 *
	 * @return Passwords for the node this object was created for, if found; null if not found.
	 * @throws IOException            When reading the passwords, or if the underlying resource could not be destroyed afterwards.
	 * @throws IllegalAccessException If this object has already been destroyed.
	 */
	@Override
	public KeystorePasswords read() {
		final KeystorePasswords keystorePasswords = super.read();
		this.destroy();
		return keystorePasswords;
	}

}
