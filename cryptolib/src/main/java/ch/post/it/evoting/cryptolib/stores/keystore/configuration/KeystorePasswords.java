/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.cryptolib.stores.keystore.configuration;

import javax.security.auth.Destroyable;

public interface KeystorePasswords extends Destroyable {

	@Override
	void destroy();

}
