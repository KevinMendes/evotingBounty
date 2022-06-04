/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.infrastructure.persistence;

import java.util.List;
import java.util.stream.Stream;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;

import ch.post.it.evoting.domain.election.model.ballotbox.BallotBoxId;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.CleansedBallotBoxRepositoryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.infrastructure.persistence.BaseRepositoryImpl;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.CleansedBallotBox;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.CleansedBallotBoxRepository;

/**
 * Implementation of CleansedBallotBoxRepository.
 */
@Stateless
public class CleansedBallotBoxRepositoryImpl extends BaseRepositoryImpl<CleansedBallotBox, Integer> implements CleansedBallotBoxRepository {

	private static final String PARAMETER_VOTING_CARD_ID = "votingCardId";
	private static final String PARAMETER_TENANT_ID = "tenantId";
	private static final String PARAMETER_ELECTION_EVENT_ID = "electionEventId";
	private static final String PARAMETER_BALLOT_BOX_ID = "ballotBoxId";

	/**
	 * Searches for a vote with the given tenant, election event and voting card id. This implementation uses database access by executing a SQL-query
	 * to select the data to be retrieved.
	 *
	 * @param tenantId        - the identifier of the tenant.
	 * @param electionEventId - the identifier of the election event.
	 * @param votingCardId    - the identifier of the voting card.
	 * @return a entity representing the vote stored in the ballot box.
	 * @throws ResourceNotFoundException if the vote is not found.
	 */
	@Override
	public CleansedBallotBox findByTenantIdElectionEventIdVotingCardId(final String tenantId, final String electionEventId, final String votingCardId)
			throws ResourceNotFoundException {
		final TypedQuery<CleansedBallotBox> query = entityManager.createQuery(
				"SELECT b FROM CleansedBallotBox b WHERE b.tenantId = :tenantId AND b.electionEventId = :electionEventId AND b.votingCardId = :votingCardId ORDER BY b.id DESC",
				CleansedBallotBox.class);
		query.setParameter(PARAMETER_TENANT_ID, tenantId);
		query.setParameter(PARAMETER_ELECTION_EVENT_ID, electionEventId);
		query.setParameter(PARAMETER_VOTING_CARD_ID, votingCardId);
		final List<CleansedBallotBox> listBallotBox = query.getResultList();
		if (!listBallotBox.isEmpty()) {
			return listBallotBox.get(0);
		}
		throw new ResourceNotFoundException("");
	}

	@Override
	public List<CleansedBallotBox> findByTenantIdElectionEventIdBallotBoxId(final String tenantId, final String electionEventId,
			final String ballotBoxId) {
		final TypedQuery<CleansedBallotBox> query = entityManager.createQuery(
				"SELECT b FROM CleansedBallotBox b WHERE b.tenantId = :tenantId AND b.electionEventId = :electionEventId AND b.ballotBoxId = :ballotBoxId",
				CleansedBallotBox.class);
		query.setParameter(PARAMETER_TENANT_ID, tenantId);
		query.setParameter(PARAMETER_ELECTION_EVENT_ID, electionEventId);
		query.setParameter(PARAMETER_BALLOT_BOX_ID, ballotBoxId);
		return query.getResultList();
	}

	@Override
	public void with(final EntityManager entityManager) {
		this.entityManager = entityManager;
	}

	@Override
	public int count(BallotBoxId ballotBoxId) throws CleansedBallotBoxRepositoryException {
		TypedQuery<Long> query = entityManager.createQuery(
				"SELECT count(cbb.tenantId) FROM CleansedBallotBox cbb " + "WHERE cbb.tenantId = :tenantId "
						+ "AND cbb.electionEventId = :electionEventId " + "AND cbb.ballotBoxId = :ballotBoxId", Long.class);
		query.setParameter(PARAMETER_TENANT_ID, ballotBoxId.getTenantId());
		query.setParameter(PARAMETER_ELECTION_EVENT_ID, ballotBoxId.getElectionEventId());
		query.setParameter(PARAMETER_BALLOT_BOX_ID, ballotBoxId.getId());

		long result = query.getSingleResult();
		if (result > Integer.MAX_VALUE) {
			throw new CleansedBallotBoxRepositoryException("Too many votes! The application-imposed limit is Integer.MAX_VALUE");
		}

		return query.getSingleResult().intValue();
	}

	@Override
	public boolean exists(final String electionEventId, final String ballotBoxId) {
		TypedQuery<Long> query = entityManager.createQuery(
				"SELECT count(*) FROM CleansedBallotBox cbb " + "WHERE cbb.electionEventId = :electionEventId "
						+ "AND cbb.ballotBoxId = :ballotBoxId " + "AND rownum = 1", Long.class);
		query.setParameter(PARAMETER_ELECTION_EVENT_ID, electionEventId);
		query.setParameter(PARAMETER_BALLOT_BOX_ID, ballotBoxId);

		return query.getSingleResult() == 1;
	}

	@Override
	public Stream<String> getVoteSet(BallotBoxId ballotBoxId, int offset, int pageSize) {
		String voteSetSQL = "SELECT cbb.encryptedVote FROM CleansedBallotBox cbb " + "WHERE cbb.tenantId = :tenantId "
				+ "AND cbb.electionEventId = :electionEventId " + "AND cbb.ballotBoxId = :ballotBoxId " + "ORDER BY cbb.votingCardId ";
		return entityManager.createQuery(voteSetSQL, String.class)
				.setParameter(PARAMETER_TENANT_ID, ballotBoxId.getTenantId())
				.setParameter(PARAMETER_ELECTION_EVENT_ID, ballotBoxId.getElectionEventId())
				.setParameter(PARAMETER_BALLOT_BOX_ID, ballotBoxId.getId())
				.setFirstResult(offset)
				.setMaxResults(pageSize)
				.getResultList().stream();
	}
}
