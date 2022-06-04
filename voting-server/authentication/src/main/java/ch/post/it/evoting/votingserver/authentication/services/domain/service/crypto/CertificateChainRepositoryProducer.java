/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.service.crypto;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import ch.post.it.evoting.votingserver.commons.crypto.CertificateChainRepository;
import ch.post.it.evoting.votingserver.commons.crypto.KeystoreForObjectOpener;

/**
 *
 */
public class CertificateChainRepositoryProducer {

	@Inject
	private KeystoreForObjectOpener keystoreOpener;

	@Produces
	public CertificateChainRepository getInstance() {
		return new CertificateChainRepository(keystoreOpener);
	}
}
