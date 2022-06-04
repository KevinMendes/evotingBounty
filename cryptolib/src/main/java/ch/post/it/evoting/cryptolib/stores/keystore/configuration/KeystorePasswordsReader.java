/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.cryptolib.stores.keystore.configuration;

import javax.security.auth.Destroyable;

public interface KeystorePasswordsReader extends Destroyable {

	/**
	 * Reads the passwords associated with the node this object was created for. Implementations may or may not also destroy the object in the process
	 * (and possibly its underlying resource).
	 *
	 * @return Passwords for the node this object was created for, if found; null if not found.
	 */
	KeystorePasswords read();

	/**
	 * Destroys the object, together with its underlying resource if possible.
	 */
	@Override
	void destroy();

}
