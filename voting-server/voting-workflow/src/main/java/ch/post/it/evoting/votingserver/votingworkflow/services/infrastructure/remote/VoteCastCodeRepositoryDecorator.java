/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.infrastructure.remote;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

import ch.post.it.evoting.cryptolib.certificates.utils.CryptographicOperationException;
import ch.post.it.evoting.domain.election.model.confirmation.ConfirmationMessage;
import ch.post.it.evoting.domain.returncodes.ShortVoteCastReturnCodeAndComputeResults;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.VoteCastCodeRepositoryException;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.confirmation.VoteCastCodeRepository;

/**
 * Decorator of the vote cast code repository.
 */
@Decorator
public class VoteCastCodeRepositoryDecorator implements VoteCastCodeRepository {

	@Inject
	@Delegate
	private VoteCastCodeRepository voteCastCodeRepository;

	@Override
	public ShortVoteCastReturnCodeAndComputeResults generateCastCode(String tenantId, String electionEventId, String verificationCardId,
			String votingCardId,
			String authenticationTokenSignature, ConfirmationMessage confirmationMessage) throws CryptographicOperationException {
		return voteCastCodeRepository
				.generateCastCode(tenantId, electionEventId, verificationCardId, votingCardId, authenticationTokenSignature, confirmationMessage);
	}

	@Override
	public boolean storesCastCode(String tenantId, String electionEventId, String votingCardId,
			ShortVoteCastReturnCodeAndComputeResults voteCastMessage)
			throws ResourceNotFoundException {
		return voteCastCodeRepository.storesCastCode(tenantId, electionEventId, votingCardId, voteCastMessage);
	}

	@Override
	public ShortVoteCastReturnCodeAndComputeResults getCastCode(String tenantId, String electionEventId, String votingCardId)
			throws ResourceNotFoundException {
		return voteCastCodeRepository.getCastCode(tenantId, electionEventId, votingCardId);
	}

	@Override
	public boolean voteCastCodeExists(final String tenantId, final String electionEventId, final String votingCardId)
			throws VoteCastCodeRepositoryException {
		return voteCastCodeRepository.voteCastCodeExists(tenantId, electionEventId, votingCardId);
	}

}
