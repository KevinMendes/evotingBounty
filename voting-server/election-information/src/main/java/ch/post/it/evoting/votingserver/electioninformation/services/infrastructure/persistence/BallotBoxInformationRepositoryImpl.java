/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.infrastructure.persistence;

import java.io.IOException;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.infrastructure.persistence.BaseRepositoryImpl;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballot.BallotRepository;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.BallotBoxInformation;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.BallotBoxInformationRepository;

/**
 * Implementation of the ballot box info repository with jpa
 */
@Stateless
public class BallotBoxInformationRepositoryImpl extends BaseRepositoryImpl<BallotBoxInformation, Integer> implements BallotBoxInformationRepository {

	// The name of the parameter which identifies the externalId
	private static final String PARAMETER_BALLOT_BOX_ID = "ballotBoxId";

	// The name of the parameter which identifies the tenantId
	private static final String PARAMETER_TENANT_ID = "tenantId";

	// The name of the parameter which identifies the electionEventId
	private static final String PARAMETER_ELECTION_EVENT_ID = "electionEventId";

	/**
	 * Searches for a ballot box information with the given id and tenant. This implementation uses
	 * database access by executing a SQL-query to select the data to be retrieved.
	 *
	 * @param tenantId        - the identifier of the tenant.
	 * @param electionEventId - the identifier of the election event.
	 * @param ballotBoxId     - the external identifier of the ballot box.
	 * @return a entity representing the ballot.
	 * @see BallotRepository#findByTenantIdElectionEventIdBallotId(String,
	 * String, String)
	 */
	@Override
	public BallotBoxInformation findByTenantIdElectionEventIdBallotBoxId(String tenantId, String electionEventId, String ballotBoxId)
			throws ResourceNotFoundException {
		TypedQuery<BallotBoxInformation> query = entityManager.createQuery(
				"SELECT b FROM BallotBoxInformation b WHERE b.tenantId = :tenantId AND b.electionEventId = :electionEventId AND b.ballotBoxId = :ballotBoxId",
				BallotBoxInformation.class);
		query.setParameter(PARAMETER_TENANT_ID, tenantId);
		query.setParameter(PARAMETER_ELECTION_EVENT_ID, electionEventId);
		query.setParameter(PARAMETER_BALLOT_BOX_ID, ballotBoxId);
		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			throw new ResourceNotFoundException("", e);
		}
	}

	/**
	 * @see BallotBoxInformationRepository#addBallotBoxInformation(String,
	 * String, String, String)
	 */
	@Override
	public void addBallotBoxInformation(String tenantId, String electionEventId, String ballotBoxId, String jsonContent)
			throws DuplicateEntryException, IOException {
		BallotBoxInformation ballotBoxInformation = new BallotBoxInformation();
		ballotBoxInformation.setTenantId(tenantId);
		ballotBoxInformation.setElectionEventId(electionEventId);
		ballotBoxInformation.setBallotBoxId(ballotBoxId);
		ballotBoxInformation.setJson(jsonContent);
		save(ballotBoxInformation);
	}
}
