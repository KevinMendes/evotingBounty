/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.infrastructure.ballotbox;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;
import static java.util.Collections.singletonMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.json.JsonObject;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Repository;

import com.orientechnologies.common.exception.OException;
import com.orientechnologies.orient.core.record.impl.ODocument;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.exceptions.FailedValidationException;
import ch.post.it.evoting.domain.election.BallotBox;
import ch.post.it.evoting.securedatamanager.config.commons.utils.JsonUtils;
import ch.post.it.evoting.securedatamanager.services.application.exception.DatabaseException;
import ch.post.it.evoting.securedatamanager.services.infrastructure.AbstractEntityRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.DatabaseManager;
import ch.post.it.evoting.securedatamanager.services.infrastructure.JsonConstants;
import ch.post.it.evoting.securedatamanager.services.infrastructure.ballot.BallotRepository;

/**
 * Implementation of operations on ballot boxes.
 */
@Repository
public class BallotBoxRepository extends AbstractEntityRepository {

	// The name of the json parameter ballot.
	private static final String JSON_NAME_PARAM_BALLOT = "ballot";

	// the name of the ballot alias
	private static final String JSON_NAME_PARAM_BALLOT_ALIAS = "ballotAlias";

	@Autowired
	@Lazy
	BallotRepository ballotRepository;

	/**
	 * Constructor
	 *
	 * @param databaseManager the injected database manager
	 */
	@Autowired
	public BallotBoxRepository(final DatabaseManager databaseManager) {
		super(databaseManager);
	}

	@PostConstruct
	@Override
	public void initialize() {
		super.initialize();
	}

	/**
	 * Returns the ballot id from the ballot box identified by the given ballotBoxId.
	 *
	 * @param ballotBoxId identifies the ballot box where to search.
	 * @return the ballot identifier.
	 */
	public String getBallotId(final String ballotBoxId) {
		final String ballotBoxAsJson = find(ballotBoxId);
		// simple check if there is a voting card set data returned
		if (JsonConstants.EMPTY_OBJECT.equals(ballotBoxAsJson)) {
			return "";
		}

		final JsonObject ballotBox = JsonUtils.getJsonObject(ballotBoxAsJson);
		return ballotBox.getJsonObject(JSON_NAME_PARAM_BALLOT).getString(JsonConstants.ID);
	}

	/**
	 * Indicates if the ballot box corresponding to the given ballot box id is a test ballot box.
	 *
	 * @param ballotBoxId the ballot box id. Must be non-null and a valid UUID.
	 * @return true if the corresponding ballot box is a test ballot box, false otherwise.
	 * @throws FailedValidationException if the given ballot box is null or not a valid UUID.
	 * @throws IllegalArgumentException  if the found ballot box is a {@link JsonConstants#EMPTY_OBJECT}.
	 * @throws DatabaseException         if no ballot box is found.
	 */
	public boolean isTestBallotBox(final String ballotBoxId) {
		validateUUID(ballotBoxId);

		final String ballotBoxAsJson = find(ballotBoxId);

		if (JsonConstants.EMPTY_OBJECT.equals(ballotBoxAsJson)) {
			throw new IllegalArgumentException(String.format("Found an empty ballot box object. [ballotBoxId: %s]", ballotBoxId));
		}

		final JsonObject ballotBox = JsonUtils.getJsonObject(ballotBoxAsJson);
		return Boolean.parseBoolean(ballotBox.getString(JsonConstants.TEST));
	}

	/**
	 * Lists the aliases of the ballot boxes which belongs to the specified ballot.
	 *
	 * @param ballotId the ballot identifier
	 * @return the aliases
	 * @throws DatabaseException failed to list aliases.
	 */
	public List<String> listAliases(final String ballotId) {
		final String sql = "select alias from " + entityName() + " where ballot.id = :ballotId";
		final Map<String, Object> parameters = singletonMap("ballotId", ballotId);
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
	 * Updates the content of a ballotBox with the alias of its related ballot
	 *
	 * @param id - the id of the ballot box to update
	 */
	public void updateRelatedBallotAlias(final List<String> ballotBoxIds) {
		try {
			for (final String id : ballotBoxIds) {
				final ODocument ballotBox = getDocument(id);
				final String ballotId = ballotBox.field("ballot.id", String.class);
				final List<String> aliases = ballotRepository.listAliases(ballotId);
				// should be only one alias. to maintain compatibility with FE,
				// save
				// as comma-separated string
				ballotBox.field(JSON_NAME_PARAM_BALLOT_ALIAS, StringUtils.join(aliases, ","));
				saveDocument(ballotBox);
			}
		} catch (final OException e) {
			throw new DatabaseException("Failed to update related ballot aliases.", e);
		}
	}

	/**
	 * Returns the ballot boxes corresponding to a specific electoral authority ID
	 *
	 * @param id the electoralAuthorityId
	 * @return the ballot boxes in JSON format
	 */
	public String findByElectoralAuthority(final String id) {
		final Map<String, Object> attributes = singletonMap(JsonConstants.ELECTORAL_AUTHORITY_DOT_ID, id);
		return list(attributes);
	}

	/**
	 * Lists the ballot boxes corresponding to a specific election event ID
	 *
	 * @param electionEventId the election event identifier
	 * @return the ballot boxes in JSON format
	 * @throws DatabaseException failed to list the ballot boxes
	 */
	public String listByElectionEvent(final String electionEventId) {
		return list(singletonMap("electionEvent.id", electionEventId));
	}

	@Override
	protected String entityName() {
		return BallotBox.class.getSimpleName();
	}
}
