/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.infrastructure.persistence;

import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.TypedQuery;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.infrastructure.persistence.BaseRepositoryImpl;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.BallotBox;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.BallotBoxRepository;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.ExportedBallotBoxItem;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.votingcard.VotingCard;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.votingcard.VotingCardWriter;

/**
 * Implementation of VoteRepository.
 */
@Stateless
public class BallotBoxRepositoryImpl extends BaseRepositoryImpl<BallotBox, Integer> implements BallotBoxRepository {

	private static final String PARAMETER_VOTING_CARD_ID = "votingCardId";

	private static final String PARAMETER_TENANT_ID = "tenantId";

	private static final String PARAMETER_ELECTION_EVENT_ID = "electionEventId";

	private static final String PARAMETER_BALLOT_BOX_ID = "ballotBoxId";

	private static final String PARAMETER_BALLOT_ID = "ballotId";

	private static final String MAX_ROW_TO_FETCH = "maxRowToFetch";

	private static final String MIN_ROW_TO_FETCH = "minRowToFetch";

	private static final int FETCH_SIZE = 1000;

	private static final Logger LOGGER = LoggerFactory.getLogger(BallotBoxRepositoryImpl.class);

	/**
	 * Searches for a vote with the given tenant, election event and voting card id. This
	 * implementation uses database access by executing a SQL-query to select the data to be
	 * retrieved.
	 *
	 * @param tenantId        - the identifier of the tenant.
	 * @param electionEventId - the identifier of the election event.
	 * @param votingCardId    - the identifier of the voting card.
	 * @return a entity representing the vote stored in the ballot box.
	 * @throws ResourceNotFoundException if the vote is not found.
	 */
	@Override
	public BallotBox findByTenantIdElectionEventIdVotingCardId(final String tenantId, final String electionEventId, final String votingCardId)
			throws ResourceNotFoundException {
		final TypedQuery<BallotBox> query = entityManager.createQuery(
				"SELECT b FROM BallotBox b WHERE b.tenantId = :tenantId AND b.electionEventId = :electionEventId AND b.votingCardId = :votingCardId ORDER BY b.id DESC",
				BallotBox.class);
		query.setParameter(PARAMETER_TENANT_ID, tenantId);
		query.setParameter(PARAMETER_ELECTION_EVENT_ID, electionEventId);
		query.setParameter(PARAMETER_VOTING_CARD_ID, votingCardId);
		final List<BallotBox> listBallotBox = query.getResultList();
		if (!listBallotBox.isEmpty()) {
			return listBallotBox.get(0);
		}
		throw new ResourceNotFoundException("");
	}

	@Override
	public List<ExportedBallotBoxItem> getEncryptedVotesByTenantIdElectionEventIdBallotBoxId(final String tenantId, final String electionEventId,
			final String ballotBoxId, final int firstElement, final int lastElement) {

		final List<ExportedBallotBoxItem> exportedBallotBoxList = new ArrayList<>();

		final Session session = entityManager.unwrap(Session.class);

		final String exportedBallotBoxSQL =
				"select fr.tenant_id, fr.election_event_id, fr.voting_card_id, " + "fr.ballot_id, fr.ballot_box_id, fr.vote, "
						+ "fr.bb_computation_results, fr.vote_cast_code, fr.vc_computation_results "
						+ "from ( select /*+ FIRST_ROWS(n) */ ft.*, ROWNUM rnum "
						+ "from (select rownum, B.TENANT_ID,B.ELECTION_EVENT_ID,B.VOTING_CARD_ID,B.BALLOT_ID,B.BALLOT_BOX_ID,B.VOTE, "
						+ "B.COMPUTATION_RESULTS AS BB_COMPUTATION_RESULTS, "
						+ "VC.VOTE_CAST_CODE, VC.COMPUTATION_RESULTS AS VC_COMPUTATION_RESULTS " + "FROM  BALLOT_BOX B "
						+ "LEFT OUTER JOIN VOTE_CAST_CODE VC "
						+ "ON B.VOTING_CARD_ID = VC.VOTING_CARD_ID AND B.TENANT_ID = VC.TENANT_ID AND B.ELECTION_EVENT_ID = VC.ELECTION_EVENT_ID "
						+ "LEFT OUTER JOIN BALLOT_BOX_INFORMATION BB " + "ON B.BALLOT_BOX_ID = BB.BALLOT_BOX_ID "
						+ "WHERE B.TENANT_ID = :tenantId AND B.ELECTION_EVENT_ID = :electionEventId AND B.BALLOT_BOX_ID = :ballotBoxId order by B.ID "
						+ ") ft where ROWNUM <= :maxRowToFetch " + ") fr where rnum  >= :minRowToFetch";

		final SQLQuery createSQLQuery = session.createSQLQuery(exportedBallotBoxSQL);
		createSQLQuery.setFetchSize(500);
		createSQLQuery.setParameter(PARAMETER_TENANT_ID, tenantId);
		createSQLQuery.setParameter(PARAMETER_ELECTION_EVENT_ID, electionEventId);
		createSQLQuery.setParameter(PARAMETER_BALLOT_BOX_ID, ballotBoxId);
		createSQLQuery.setParameter(MIN_ROW_TO_FETCH, firstElement);
		createSQLQuery.setParameter(MAX_ROW_TO_FETCH, lastElement);

		@SuppressWarnings("unchecked")
		final List<Object[]> resultSet = createSQLQuery.list();

		resultSet.forEach(record -> {

			final String expTenantId = (String) record[0];
			final String expElectionEventId = (String) record[1];
			final String votingCardId = (String) record[2];
			final String ballotId = (String) record[3];
			final String expBallotBoxId = (String) record[4];
			final String vote = clobToString((Clob) record[5]);
			final String voteComputationResults = blobToBase64((Blob) record[6]);
			// They can be null. In this case we insert them as Empty
			final String voteCastCode = record[7] != null ? (String) record[7] : StringUtils.EMPTY;
			final String castCodeComputationResults = record[8] != null ? blobToBase64((Blob) record[8]) : StringUtils.EMPTY;

			exportedBallotBoxList
					.add(new ExportedBallotBoxItem.ExportedBallotBoxItemBuilder().setTenantId(expTenantId).setElectionEventId(expElectionEventId)
							.setVotingCardId(votingCardId).setBallotId(ballotId).setBallotBoxId(expBallotBoxId).setVote(vote)
							.setVoteComputationResults(voteComputationResults).setVoteCastCode(voteCastCode)
							.setCastCodeComputationResults(castCodeComputationResults).build());
		});

		return exportedBallotBoxList;

	}

	private String blobToBase64(final Blob blob) {
		try {
			return new String(Base64.getEncoder().encode(blob.getBytes(1L, (int) blob.length())), StandardCharsets.UTF_8);
		} catch (SQLException e) {
			throw new IllegalStateException("Error trying to convert blob to base64.", e);
		} finally {
			try {
				blob.free();
			} catch (SQLException e) {
				LOGGER.warn("Error trying to free blob.", e);
			}
		}
	}

	private String clobToString(final Clob clob) {
		try {
			return clob.getSubString(1, (int) clob.length());
		} catch (SQLException e) {
			throw new IllegalStateException("Error trying to convert clob to String.", e);
		} finally {
			try {
				clob.free();
			} catch (SQLException e) {
				LOGGER.warn("Error trying to free clob.", e);
			}
		}
	}

	@Override
	public List<BallotBox> findByTenantIdElectionEventIdBallotBoxId(final String tenantId, final String electionEventId, final String ballotBoxId) {
		final TypedQuery<BallotBox> query = entityManager.createQuery(
				"SELECT b FROM BallotBox b WHERE b.tenantId = :tenantId AND b.electionEventId = :electionEventId AND b.ballotBoxId = :ballotBoxId",
				BallotBox.class);
		query.setParameter(PARAMETER_TENANT_ID, tenantId);
		query.setParameter(PARAMETER_ELECTION_EVENT_ID, electionEventId);
		query.setParameter(PARAMETER_BALLOT_BOX_ID, ballotBoxId);
		return query.getResultList();
	}

	/**
	 * @see BallotBoxRepository#findByTenantIdElectionEventIdVotingCardIdBallotBoxIdBallotId(String,
	 * String, String, String, String)
	 */
	@Override
	public List<BallotBox> findByTenantIdElectionEventIdVotingCardIdBallotBoxIdBallotId(final String tenantId, final String electionEventId,
			final String votingCardId, final String ballotBoxId, final String ballotId) {
		final TypedQuery<BallotBox> query = entityManager.createQuery(
				"SELECT b FROM BallotBox b WHERE b.tenantId = :tenantId AND b.electionEventId = :electionEventId "
						+ "AND b.votingCardId = :votingCardId AND b.ballotId = :ballotId AND b.ballotBoxId = :ballotBoxId", BallotBox.class);
		query.setParameter(PARAMETER_TENANT_ID, tenantId);
		query.setParameter(PARAMETER_ELECTION_EVENT_ID, electionEventId);
		query.setParameter(PARAMETER_VOTING_CARD_ID, votingCardId);
		query.setParameter(PARAMETER_BALLOT_ID, ballotId);
		query.setParameter(PARAMETER_BALLOT_BOX_ID, ballotBoxId);
		return query.getResultList();
	}

	/**
	 * Find and write voting cards that have votes in ballot boxes (used voting cards).
	 *
	 * @param tenantId        the tenant id
	 * @param electionEventId the election event id
	 * @param writer          the writer
	 */
	@Override
	public void findAndWriteUsedVotingCards(final String tenantId, final String electionEventId, final VotingCardWriter writer) {
		final Session session = entityManager.unwrap(Session.class);
		final Query query = session.createQuery("SELECT bb.votingCardId FROM BallotBox bb WHERE bb.tenantId = ?1 AND bb.electionEventId = ?2");
		query.setParameter(1, tenantId);
		query.setParameter(2, electionEventId);
		query.setFetchSize(FETCH_SIZE);
		query.setReadOnly(true);
		query.setLockMode("bb", LockMode.NONE);

		try {
			ScrollableResults results = query.scroll(ScrollMode.FORWARD_ONLY);
			while (results.next()) {
				writer.write(new VotingCard((String) results.get(0)));
			}
			results.close();
		} catch (HibernateException e) {
			throw new CryptoLibException("Error getting used voting cards", e);
		}
	}

}
