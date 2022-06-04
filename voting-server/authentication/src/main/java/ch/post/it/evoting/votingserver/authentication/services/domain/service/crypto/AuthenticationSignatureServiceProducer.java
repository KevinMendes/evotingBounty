/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.service.crypto;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.votingserver.commons.crypto.PrivateKeyForObjectRepository;
import ch.post.it.evoting.votingserver.commons.crypto.SignatureForObjectService;

/**
 *
 */

public class AuthenticationSignatureServiceProducer {

	@Inject
	private PrivateKeyForObjectRepository privateKeyRepository;

	@Inject
	private AsymmetricServiceAPI asymmetricService;

	@Produces
	public SignatureForObjectService getInstance() {

		return new SignatureForObjectService(asymmetricService, privateKeyRepository);
	}
}
