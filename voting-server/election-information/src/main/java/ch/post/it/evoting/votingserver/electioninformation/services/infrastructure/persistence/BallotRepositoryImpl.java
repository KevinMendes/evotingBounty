/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.infrastructure.persistence;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.infrastructure.persistence.BaseRepositoryImpl;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballot.Ballot;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballot.BallotRepository;

/**
 * The implementation for the ballot repository with jpa.
 */
@Stateless
public class BallotRepositoryImpl extends BaseRepositoryImpl<Ballot, Integer> implements BallotRepository {

	// The name of the parameter which identifies the ballotId
	private static final String PARAMETER_BALLOT_ID = "ballotId";

	// The name of the parameter which identifies the tenantId
	private static final String PARAMETER_TENANT_ID = "tenantId";

	// The name of the parameter which identifies the electionEventId
	private static final String PARAMETER_ELECTION_EVENT_ID = "electionEventId";

	/**
	 * Searches for a ballot with the given id and tenant. This implementation uses database access by
	 * executing a SQL-query to select the data to be retrieved.
	 *
	 * @param tenantId        - the identifier of the tenant.
	 * @param electionEventId - the identifier of the election event.
	 * @param ballotId        - the external identifier of the ballot.
	 * @return a entity representing the ballot.
	 * @throws ResourceNotFoundException if ballot is not found.
	 * @see BallotRepository#findByTenantIdElectionEventIdBallotId(String,
	 * String, String)
	 */
	@Override
	public Ballot findByTenantIdElectionEventIdBallotId(String tenantId, String electionEventId, String ballotId) throws ResourceNotFoundException {
		TypedQuery<Ballot> query = entityManager.createQuery(
				"SELECT b FROM Ballot b WHERE b.tenantId = :tenantId AND b.electionEventId = :electionEventId AND b.ballotId = :ballotId",
				Ballot.class);
		query.setParameter(PARAMETER_TENANT_ID, tenantId);
		query.setParameter(PARAMETER_ELECTION_EVENT_ID, electionEventId);
		query.setParameter(PARAMETER_BALLOT_ID, ballotId);
		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			throw new ResourceNotFoundException("", e);
		}
	}
}
