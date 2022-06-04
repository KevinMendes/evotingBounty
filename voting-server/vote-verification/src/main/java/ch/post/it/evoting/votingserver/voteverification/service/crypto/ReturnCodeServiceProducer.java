/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.voteverification.service.crypto;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import ch.post.it.evoting.cryptolib.api.primitives.PrimitivesServiceAPI;
import ch.post.it.evoting.cryptolib.api.symmetric.SymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.returncode.VoterCodesService;
import ch.post.it.evoting.cryptolib.returncode.VoterCodesServiceImpl;

public class ReturnCodeServiceProducer {

	@Inject
	private PrimitivesServiceAPI primitivesService;

	@Inject
	private SymmetricServiceAPI symmetricService;

	@Produces
	@ApplicationScoped
	public VoterCodesService getInstance() {
		return new VoterCodesServiceImpl(primitivesService, symmetricService);
	}
}
