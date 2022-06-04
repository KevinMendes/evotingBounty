/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.shares.shares.keys.rsa;

import java.security.KeyPair;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.securedatamanager.config.shares.shares.keys.KeyPairGenerator;

public final class RSAKeyPairGenerator implements KeyPairGenerator {

	private final AsymmetricServiceAPI service;

	public RSAKeyPairGenerator(final AsymmetricServiceAPI asymmetricService) {
		this.service = asymmetricService;
	}

	@Override
	public KeyPair generate() {

		return service.getKeyPairForSigning();
	}
}
