/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.infrastructure.persistence;

import java.util.Optional;

import javax.ejb.Stateless;
import javax.persistence.LockModeType;
import javax.persistence.LockTimeoutException;
import javax.persistence.PersistenceException;
import javax.persistence.PessimisticLockException;

import org.hibernate.LockMode;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.infrastructure.persistence.BaseRepositoryImpl;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.common.csv.ExportedPartialVotingCardStateItemWriter;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.state.ExportedPartialVotingCardStateItem;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.state.VotingCardState;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.state.VotingCardStatePK;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.state.VotingCardStateRepository;

/**
 * The implementation of the operations on the credential repository. The implementation uses JPA as
 * data layer to access database.
 */
@Stateless
public class VotingCardStateRepositoryImpl extends BaseRepositoryImpl<VotingCardState, Long> implements VotingCardStateRepository {

	private static final Logger LOGGER = LoggerFactory.getLogger(VotingCardStateRepositoryImpl.class);

	/**
	 * Returns a voting card state for a given tenant, election event and voting card. In this
	 * implementation, we use a database query to obtain credential data taking into account the input
	 * parameters.
	 *
	 * @param tenantId        - the tenant identifier.
	 * @param electionEventId - the election event identifier.
	 * @param votingCardId    - the voting card identifier.
	 * @return The voting card state.
	 * @throws ResourceNotFoundException if voting card state is not found.
	 */
	@Override
	public Optional<VotingCardState> acquire(String tenantId, String electionEventId, String votingCardId) {

		VotingCardStatePK pk = new VotingCardStatePK(tenantId, electionEventId, votingCardId);

		try {
			final VotingCardState votingCardState = entityManager.find(VotingCardState.class, pk, LockModeType.PESSIMISTIC_WRITE);
			return Optional.ofNullable(votingCardState);
		} catch (PessimisticLockException e) {
			LOGGER.error("Cannot create lock on entity ", e);
			throw e;
		} catch (LockTimeoutException e) {
			LOGGER.error("Cannot create lock on entity due to timeout", e);
			throw e;
		} catch (PersistenceException e) {
			LOGGER.info("Cannot create lock on entity due to persistence", e);
			return Optional.empty();
		}
	}

	/**
	 * Find and write voting cards with inactive state.
	 *
	 * @param tenantId        the tenant id
	 * @param electionEventId the election event id
	 * @param writer          the writer
	 * @throws ResourceNotFoundException the resource not found exception
	 */
	@Override
	public void findAndWriteVotingCardsWithInactiveState(final String tenantId, final String electionEventId,
			final ExportedPartialVotingCardStateItemWriter writer) {

		final Session session = entityManager.unwrap(Session.class);

		final  Query  query = session.createQuery("SELECT v FROM VotingCardState v " + "WHERE v.tenantId = ?1 " + "AND v.electionEventId = ?2 "
				+ "AND v.state IN ('BLOCKED','CAST','CHOICE_CODES_FAILED','WRONG_BALLOT_CASTING_KEY') " + "ORDER BY v.state, v.votingCardId");
		query.setParameter(1, tenantId);
		query.setParameter(2, electionEventId);
		query.setFetchSize(1000);
		query.setReadOnly(true);
		query.setLockMode("v", LockMode.NONE);
		ScrollableResults results = query.scroll(ScrollMode.FORWARD_ONLY);
		try {
			while (results.next()) {
				VotingCardState state = (VotingCardState) results.get(0);
				ExportedPartialVotingCardStateItem item = new ExportedPartialVotingCardStateItem();
				item.setVotingCardId(state.getVotingCardId());
				item.setState(state.getState().name());
				writer.write(item);
			}
		} finally {
			results.close();
		}
	}
}
