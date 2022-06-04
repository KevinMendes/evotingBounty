/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.service.crypto;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import ch.post.it.evoting.votingserver.commons.crypto.KeystoreForObjectOpener;
import ch.post.it.evoting.votingserver.commons.crypto.PasswordForObjectRepository;
import ch.post.it.evoting.votingserver.commons.crypto.PrivateKeyForObjectRepository;

/**
 *
 */

public class PrivateKeyRepositoryProducer {

	@Inject
	private KeystoreForObjectOpener keystoreOpener;

	@Inject
	private PasswordForObjectRepository passwordRepository;

	@Produces
	public PrivateKeyForObjectRepository getInstance() {

		return new PrivateKeyForObjectRepository(keystoreOpener, passwordRepository);
	}
}
