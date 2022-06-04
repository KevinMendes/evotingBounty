/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.mixdec.infrastructure.persistence;

import java.util.List;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.EntryPersistenceException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.infrastructure.persistence.BaseRepositoryImpl;
import ch.post.it.evoting.votingserver.orchestrator.mixdec.domain.model.MixDecBallotBoxStatus;
import ch.post.it.evoting.votingserver.orchestrator.mixdec.domain.model.MixDecBallotBoxStatusId;
import ch.post.it.evoting.votingserver.orchestrator.mixdec.domain.model.MixDecStatus;

@Stateless
public class MixDecBallotBoxStatusRepositoryImpl extends BaseRepositoryImpl<MixDecBallotBoxStatus, Integer>
		implements MixDecBallotBoxStatusRepository {

	private static final String PARAMETER_ELECTION_EVENT_ID = "electionEventId";
	private static final String PARAMETER_BALLOT_BOX_ID = "ballotBoxId";
	private static final String PARAMETER_STATUS = "status";
	private static final String PARAMETER_ERROR_MESSAGE = "errorMessage";
	private static final String PARAMETER_MIXED_STATUS = "mixedStatus";
	private static final String PARAMETER_PROCESSING_STATUS = "processingStatus";

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void save(final String electionEventId, final String ballotBoxId, final String status, final String errorMessage)
			throws EntryPersistenceException {

		final MixDecBallotBoxStatus mixDecBallotBoxStatus = new MixDecBallotBoxStatus();
		mixDecBallotBoxStatus.setId(new MixDecBallotBoxStatusId(electionEventId, ballotBoxId));
		mixDecBallotBoxStatus.setStatus(status);
		mixDecBallotBoxStatus.setErrorMessage(cutErrorMessage(errorMessage));

		update(mixDecBallotBoxStatus);
	}

	@Override
	public List<MixDecBallotBoxStatus> getMixDecBallotBoxStatus(final String electionEventId, final String ballotBoxId)
			throws ResourceNotFoundException {

		try {
			final TypedQuery<MixDecBallotBoxStatus> query = entityManager
					.createQuery("from MixDecBallotBoxStatus where id.electionEventId = :electionEventId and id.ballotBoxId = :ballotBoxId ",
							MixDecBallotBoxStatus.class);

			query.setParameter(PARAMETER_ELECTION_EVENT_ID, electionEventId);
			query.setParameter(PARAMETER_BALLOT_BOX_ID, ballotBoxId);

			return query.getResultList();
		} catch (NoResultException ex) {
			throw new ResourceNotFoundException("Unable to find status in the database of the ballot box " + ballotBoxId, ex);
		}
	}

	@Override
	public boolean isVoteSetMixable(final String electionEventId, final String ballotBoxId) {
		final TypedQuery<Long> query = entityManager.createQuery(
				"select count(*) from MixDecBallotBoxStatus where id.electionEventId = :electionEventId and id.ballotBoxId = :ballotBoxId and (status = :mixedStatus OR status = :processingStatus)",
				Long.class);

		query.setParameter(PARAMETER_ELECTION_EVENT_ID, electionEventId);
		query.setParameter(PARAMETER_BALLOT_BOX_ID, ballotBoxId);
		query.setParameter(PARAMETER_MIXED_STATUS, MixDecStatus.MIXED.toString());
		query.setParameter(PARAMETER_PROCESSING_STATUS, MixDecStatus.PROCESSING.toString());

		return 0 <= query.getSingleResult();
	}

	@Override
	public long countByMixDecBallotBoxStatus(final String electionEventId, final String ballotBoxId, final String status) {
		final TypedQuery<Long> query = entityManager.createQuery(
				"select count(*) from MixDecBallotBoxStatus where id.electionEventId = :electionEventId and id.ballotBoxId = :ballotBoxId and status = :status ",
				Long.class);

		query.setParameter(PARAMETER_ELECTION_EVENT_ID, electionEventId);
		query.setParameter(PARAMETER_BALLOT_BOX_ID, ballotBoxId);
		query.setParameter(PARAMETER_STATUS, status);

		return query.getSingleResult();
	}

	@Override
	public long countByMixDecBallotBoxStatus(final String electionEventId, final String ballotBoxId) {
		final TypedQuery<Long> query = entityManager.createQuery(
				"select count(*) from MixDecBallotBoxStatus where id.electionEventId = :electionEventId and id.ballotBoxId = :ballotBoxId ",
				Long.class);

		query.setParameter(PARAMETER_ELECTION_EVENT_ID, electionEventId);
		query.setParameter(PARAMETER_BALLOT_BOX_ID, ballotBoxId);

		return query.getSingleResult();
	}

	@Override
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void update(final String electionEventId, final String ballotBoxId, final String status, final String errorMessage)
			throws ResourceNotFoundException {
		final Query query = entityManager.createQuery(
				"update MixDecBallotBoxStatus set status = :status, errorMessage = :errorMessage where id.electionEventId = :electionEventId and id.ballotBoxId = :ballotBoxId");

		query.setParameter(PARAMETER_STATUS, status);
		query.setParameter(PARAMETER_ERROR_MESSAGE, errorMessage);
		query.setParameter(PARAMETER_ELECTION_EVENT_ID, electionEventId);
		query.setParameter(PARAMETER_BALLOT_BOX_ID, ballotBoxId);

		if (query.executeUpdate() == 0) {
			throw new ResourceNotFoundException("Unable to find status in the database of the ballot box " + ballotBoxId);
		}
	}

	private String cutErrorMessage(String errorMessage) {
		if (errorMessage != null && errorMessage.length() > 150) {
			return errorMessage.substring(0, 150);
		}
		return errorMessage;
	}

}
