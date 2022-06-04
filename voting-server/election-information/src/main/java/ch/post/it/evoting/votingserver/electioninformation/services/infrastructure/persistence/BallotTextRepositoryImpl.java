/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.infrastructure.persistence;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.infrastructure.persistence.BaseRepositoryImpl;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballot.BallotText;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballot.BallotTextRepository;

/**
 * The implementation for the ballot text repository with jpa.
 */
@Stateless
public class BallotTextRepositoryImpl extends BaseRepositoryImpl<BallotText, Integer> implements BallotTextRepository {

	// The name of the parameter which identifies the externalId
	private static final String PARAMETER_BALLOT_ID = "ballotId";

	// The name of the parameter which identifies the tenantId
	private static final String PARAMETER_ELEVTION_EVENT_ID = "electionEventId";

	// The name of the parameter which identifies the tenantId
	private static final String PARAMETER_TENANT_ID = "tenantId";

	/**
	 * Searches for a ballot texts with the given ballotId, election event and tenant. This implementation uses database access by executing a
	 * SQL-query to select the data to be retrieved.
	 *
	 * @param tenantId        - the identifier of the tenant.
	 * @param electionEventId - the identifier of the election event.
	 * @param ballotId        - the identifier of the ballot.
	 * @return a entity representing the ballot text.
	 * @throws ResourceNotFoundException if ballot text is not found.
	 */
	@Override
	public BallotText findByTenantIdElectionEventIdBallotId(final String tenantId, final String electionEventId, final String ballotId)
			throws ResourceNotFoundException {
		final TypedQuery<BallotText> query = entityManager.createQuery(
				"SELECT b FROM BallotText b WHERE b.ballotId = :ballotId AND b.electionEventId = :electionEventId AND b.tenantId = :tenantId",
				BallotText.class);
		query.setParameter(PARAMETER_BALLOT_ID, ballotId);
		query.setParameter(PARAMETER_ELEVTION_EVENT_ID, electionEventId);
		query.setParameter(PARAMETER_TENANT_ID, tenantId);
		try {
			return query.getSingleResult();
		} catch (final NoResultException e) {
			throw new ResourceNotFoundException("", e);
		}
	}
}
