/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.infrastructure.persistence;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.infrastructure.persistence.BaseRepositoryImpl;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.validation.VoteValidation;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.validation.VoteValidationRepository;

/**
 * Implementation of vote validation repository.
 */
@Stateless
public class VoteValidationRepositoryImpl extends BaseRepositoryImpl<VoteValidation, Integer> implements VoteValidationRepository {

	private static final String PARAMETER_TENANT_ID = "tenantId";

	private static final String PARAMETER_ELECTION_EVENT_ID = "electionEventId";

	private static final String PARAMETER_VOTING_CARD_ID = "votingCardId";

	private static final String PARAMETER_VOTE_HASH = "voteHash";

	/**
	 * Returns the result of finding the vote validation by the input parameters.
	 *
	 * @param tenantId        the tenant id.
	 * @param electionEventId the election event id.
	 * @param votingCardId    the voting card id.
	 * @param voteHash        the hash of the vote.
	 * @return the vote validation if found.
	 * @throws ResourceNotFoundException
	 */
	@Override
	public VoteValidation findByTenantIdElectionEventIdVotingCardId(final String tenantId, final String electionEventId, final String votingCardId, final String voteHash)
			throws ResourceNotFoundException {
		final TypedQuery<VoteValidation> query = entityManager.createQuery(
				"SELECT v FROM VoteValidation v WHERE v.tenantId = :tenantId AND v.electionEventId = :electionEventId AND v.votingCardId = :votingCardId AND v.voteHash = :voteHash",
				VoteValidation.class);
		query.setParameter(PARAMETER_TENANT_ID, tenantId);
		query.setParameter(PARAMETER_ELECTION_EVENT_ID, electionEventId);
		query.setParameter(PARAMETER_VOTING_CARD_ID, votingCardId);
		query.setParameter(PARAMETER_VOTE_HASH, voteHash);
		try {
			return query.getSingleResult();
		} catch (final NoResultException e) {
			throw new ResourceNotFoundException("", e);
		}
	}

}
