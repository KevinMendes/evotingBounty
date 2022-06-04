/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.infrastructure.ballot;

import static java.util.Collections.singletonMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import com.orientechnologies.common.exception.OException;
import com.orientechnologies.orient.core.record.impl.ODocument;

import ch.post.it.evoting.cryptoprimitives.domain.election.Ballot;
import ch.post.it.evoting.securedatamanager.services.application.exception.DatabaseException;
import ch.post.it.evoting.securedatamanager.services.infrastructure.AbstractEntityRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.DatabaseManager;
import ch.post.it.evoting.securedatamanager.services.infrastructure.JsonConstants;
import ch.post.it.evoting.securedatamanager.services.infrastructure.ballotbox.BallotBoxRepository;

/**
 * Implementation of the Ballot Repository
 */
@Repository
public class BallotRepository extends AbstractEntityRepository {

	@Autowired
	@Lazy
	BallotBoxRepository ballotBoxRepository;

	/**
	 * Constructor
	 *
	 * @param databaseManager the injected database manager
	 */
	@Autowired
	public BallotRepository(final DatabaseManager databaseManager) {
		super(databaseManager);
	}

	@PostConstruct
	@Override
	public void initialize() {
		super.initialize();
	}

	/**
	 * Lists aliases of the specified ballot.
	 *
	 * @param id the ballot identifier
	 * @return the aliases
	 * @throws DatabaseException failed to list aliases.
	 */
	public List<String> listAliases(final String id) {
		final String sql = "select alias from " + entityName() + " where id=:id";
		final Map<String, Object> parameters = singletonMap(JsonConstants.ID, id);
		final List<ODocument> documents;
		try {
			documents = selectDocuments(sql, parameters, -1);
		} catch (final OException e) {
			throw new DatabaseException("Failed to list aliases.", e);
		}
		final List<String> aliases = new ArrayList<>(documents.size());
		for (final ODocument document : documents) {
			aliases.add(document.field("alias", String.class));
		}
		return aliases;
	}

	/**
	 * Update the related ballot box to a given list of ballots
	 *
	 * @param ids - the list of ballots ids to be updated
	 */
	public void updateRelatedBallotBox(final List<String> ballotIds) {
		try {
			for (final String id : ballotIds) {
				final ODocument ballot = getDocument(id);
				final List<String> relatedIds = ballotBoxRepository.listAliases(id);
				// to maintain compatibility with FE, save as comma-separated
				// string
				ballot.field(JsonConstants.BALLOT_BOXES, StringUtils.join(relatedIds, ","));
				saveDocument(ballot);
			}
		} catch (final OException e) {
			throw new DatabaseException("Failed to update related ballot box.", e);
		}
	}

	/**
	 * Updates a ballot adding its signature
	 *
	 * @param ballotId     - identifier of the ballot to be updated
	 * @param signedBallot - signature of the ballot
	 */
	public void updateSignedBallot(final String ballotId, final String signedBallot) {
		try {
			final ODocument ballot = getDocument(ballotId);
			ballot.field(JsonConstants.SIGNED_OBJECT, signedBallot);
			saveDocument(ballot);
		} catch (final OException e) {
			throw new DatabaseException("Failed to update signed ballot.", e);
		}
	}

	/**
	 * Lists ballots which belong to a given election event in JSON format.
	 *
	 * @param electionEventId the election event identifier
	 * @return the ballots.
	 */
	public String listByElectionEvent(final String electionEventId) {
		return list(singletonMap("electionEvent.id", electionEventId));
	}

	@Override
	protected String entityName() {
		return Ballot.class.getSimpleName();
	}
}
