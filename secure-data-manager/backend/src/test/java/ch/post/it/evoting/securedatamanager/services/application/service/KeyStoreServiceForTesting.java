/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.service;

import java.security.KeyPair;
import java.security.PrivateKey;

import javax.annotation.PostConstruct;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricService;

public class KeyStoreServiceForTesting implements KeyStoreService {

	private PrivateKey privateKey;

	@PostConstruct
	public void initPrivateKey() throws GeneralCryptoLibException {

		AsymmetricServiceAPI service = new AsymmetricService();
		KeyPair keyPairForSigning = service.getKeyPairForSigning();
		privateKey = keyPairForSigning.getPrivate();
	}

	@Override
	public PrivateKey getPrivateKey() {
		return privateKey;
	}
}
