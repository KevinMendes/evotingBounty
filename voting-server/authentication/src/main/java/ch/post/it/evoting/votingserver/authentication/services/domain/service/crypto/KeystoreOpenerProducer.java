/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.service.crypto;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import ch.post.it.evoting.cryptolib.api.extendedkeystore.KeyStoreService;
import ch.post.it.evoting.votingserver.commons.crypto.KeystoreForObjectOpener;
import ch.post.it.evoting.votingserver.commons.crypto.KeystoreForObjectRepository;
import ch.post.it.evoting.votingserver.commons.crypto.PasswordForObjectRepository;

/**
 *
 */

public class KeystoreOpenerProducer {

	@Inject
	private KeyStoreService storesService;

	@Inject
	private KeystoreForObjectRepository keystoreRepository;

	@Inject
	private PasswordForObjectRepository passwordRepository;

	@Produces
	public KeystoreForObjectOpener getInstance() {

		return new KeystoreForObjectOpener(storesService, keystoreRepository, passwordRepository);
	}
}
