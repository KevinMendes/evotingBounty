/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.infrastructure.votingcardset;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;
import static java.util.Collections.singletonMap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.orientechnologies.common.exception.OException;
import com.orientechnologies.orient.core.record.impl.ODocument;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.exceptions.FailedValidationException;
import ch.post.it.evoting.securedatamanager.config.commons.utils.JsonUtils;
import ch.post.it.evoting.securedatamanager.services.application.exception.DatabaseException;
import ch.post.it.evoting.securedatamanager.services.application.exception.ResourceNotFoundException;
import ch.post.it.evoting.securedatamanager.services.domain.model.votingcardset.VotingCardSet;
import ch.post.it.evoting.securedatamanager.services.infrastructure.AbstractEntityRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.DatabaseManager;
import ch.post.it.evoting.securedatamanager.services.infrastructure.JsonConstants;
import ch.post.it.evoting.securedatamanager.services.infrastructure.ballot.BallotRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.ballotbox.BallotBoxRepository;

/**
 * Implementation of operations on voting card sets.
 */
@Repository
public class VotingCardSetRepository extends AbstractEntityRepository {

	@Autowired
	BallotBoxRepository ballotBoxRepository;

	@Autowired
	BallotRepository ballotRepository;

	public VotingCardSetRepository(final DatabaseManager databaseManager) {
		super(databaseManager);
	}

	@PostConstruct
	@Override
	public void initialize() {
		super.initialize();
	}

	/**
	 * Returns the ballot box id from the voting card set identified by the given id.
	 *
	 * @param votingCardSetId identifies the voting card set where to search. Must be non-null and a valid UUID.
	 * @return the ballot box identifier.
	 * @throws FailedValidationException if {@code votingCardSetId} is null or not a valid UUID.
	 */
	public String getBallotBoxId(final String votingCardSetId) {
		validateUUID(votingCardSetId);

		final String votingCardSetAsJson = find(votingCardSetId);
		// simple check if there is a voting card set data returned
		if (JsonConstants.EMPTY_OBJECT.equals(votingCardSetAsJson)) {
			return "";
		}

		final JsonObject votingCardSet = JsonUtils.getJsonObject(votingCardSetAsJson);
		final JsonObject ballotBox = votingCardSet.getJsonObject(JsonConstants.BALLOT_BOX);

		return ballotBox.getString(JsonConstants.ID);
	}

	/**
	 * Updates the related ballot for the given voting card ids
	 *
	 * @param votingCardIds for which to update the related ballot.
	 */
	public void updateRelatedBallot(final List<String> votingCardIds) {
		try {
			for (final String id : votingCardIds) {
				final String ballotBoxId = getBallotBoxId(id);
				final JsonObject ballotBoxObject = JsonUtils.getJsonObject(ballotBoxRepository.find(ballotBoxId));
				final String ballotBoxAlias = ballotBoxObject.getString(JsonConstants.ALIAS, "");
				final String ballotId = ballotBoxObject.getJsonObject(JsonConstants.BALLOT).getString(JsonConstants.ID);
				final JsonObject ballotObject = JsonUtils.getJsonObject(ballotRepository.find(ballotId));
				final String ballotAlias = ballotObject.getString(JsonConstants.ALIAS, "");

				final ODocument set = getDocument(id);
				set.field(JsonConstants.BALLOT_ALIAS, ballotAlias);
				set.field(JsonConstants.BALLOT_BOX_ALIAS, ballotBoxAlias);
				saveDocument(set);
			}
		} catch (final OException e) {
			throw new DatabaseException("Failed to update related ballot.", e);
		}
	}

	/**
	 * Update the related verification card set id for the given voting cards id.
	 *
	 * @param votingCardSetId       for which to update the verification card set id.
	 * @param verificationCardSetId for the corresponding verification card set to be updated.
	 */
	public void updateRelatedVerificationCardSet(final String votingCardSetId, final String verificationCardSetId) {
		try {
			final ODocument set = getDocument(votingCardSetId);
			set.field(JsonConstants.VERIFICATION_CARD_SET_ID, verificationCardSetId);
			saveDocument(set);
		} catch (final OException e) {
			throw new DatabaseException("Failed to update related verification card set.", e);
		}
	}

	/**
	 * Lists the voting cards sets corresponding to a specific election event ID.
	 *
	 * @param electionEventId the election event identifier
	 * @return the voting card sets in JSON format
	 * @throws DatabaseException if the operation failed to list the voting card sets
	 */
	public String listByElectionEvent(final String electionEventId) {
		return list(singletonMap("electionEvent.id", electionEventId));
	}

	/**
	 * Finds all voting card set ids related to the given election event id.
	 *
	 * @param electionEventId the election event id. Must be non-null and a valid UUID.
	 * @return a list of all voting card set ids related to the given election event id.
	 * @throws FailedValidationException if {@code electionEventId} is null or not a valid UUID.
	 */
	public List<String> findAllVotingCardSetIds(final String electionEventId) {
		validateUUID(electionEventId);

		final String json = listByElectionEvent(electionEventId);
		final JsonArray array = JsonUtils.getJsonObject(json).getJsonArray(JsonConstants.RESULT);

		return array.stream()
				.map(JsonObject.class::cast)
				.map(votingCardSet -> votingCardSet.getJsonString(JsonConstants.ID))
				.map(JsonString::getString)
				.collect(Collectors.toList());
	}

	/**
	 * @see AbstractEntityRepository#entityName()
	 */
	@Override
	protected String entityName() {
		return VotingCardSet.class.getSimpleName();
	}

	/**
	 * Returns the verification card set id related to the given voting card set id
	 *
	 * @param votingCardSetId identifies the voting card set where to search. Must be non-null and a valid UUID.
	 * @return the verification card set id
	 * @throws FailedValidationException if {@code votingCardSetId} is null or not a valid UUID.
	 */
	public String getVerificationCardSetId(final String votingCardSetId) {
		validateUUID(votingCardSetId);

		final String votingCardSetAsJson = find(votingCardSetId);
		if (JsonConstants.EMPTY_OBJECT.equals(votingCardSetAsJson)) {
			return "";
		}

		final JsonObject votingCardSet = JsonUtils.getJsonObject(votingCardSetAsJson);

		return votingCardSet.getString(JsonConstants.VERIFICATION_CARD_SET_ID);
	}

	/**
	 * Returns the specified voting card set in JSON form
	 *
	 * @param electionEventId the election event identifier
	 * @param votingCardSetId identifies the voting card set where to search.
	 * @return the verification card set id
	 */
	public JsonObject getVotingCardSetJson(final String electionEventId, final String votingCardSetId) throws ResourceNotFoundException {
		final JsonObject votingCardSet;
		final Map<String, Object> attributeValueMap = new HashMap<>();
		attributeValueMap.put(JsonConstants.ELECTION_EVENT_DOT_ID, electionEventId);
		attributeValueMap.put(JsonConstants.ID, votingCardSetId);
		final String votingCardSetResultListAsJson = list(attributeValueMap);
		if (StringUtils.isEmpty(votingCardSetResultListAsJson)) {
			throw new ResourceNotFoundException("Voting card set not found");
		} else {
			final JsonArray votingCardSetResultList = JsonUtils.getJsonObject(votingCardSetResultListAsJson).getJsonArray(JsonConstants.RESULT);

			// Searching for a specific electionEventId and votingCardSetId returns just one element in the JsonArray
			if (votingCardSetResultList != null && !votingCardSetResultList.isEmpty()) {
				votingCardSet = votingCardSetResultList.getJsonObject(0);
			} else {
				throw new ResourceNotFoundException("Voting card set not found");
			}
		}

		return votingCardSet;
	}

	/**
	 * Returns the voting card set alias related to the given voting card set id.
	 *
	 * @param votingCardSetId identifies the voting card set where to search. Must be non-null and a valid UUID.
	 * @return the voting card set alias
	 * @throws FailedValidationException if {@code votingCardSetId} is null or not a valid UUID.
	 */
	public String getVotingCardSetAlias(final String votingCardSetId) {
		validateUUID(votingCardSetId);

		final String sql = "select alias from " + entityName() + " where id = :id";
		final Map<String, Object> parameters = singletonMap(JsonConstants.ID, votingCardSetId);
		final List<ODocument> documents;
		try {
			documents = selectDocuments(sql, parameters, 1);
		} catch (final OException e) {
			throw new DatabaseException("Failed to get voting card set alias.", e);
		}
		return documents.isEmpty() ? "" : documents.get(0).field("alias", String.class);
	}

	/**
	 * Returns the number of voting cards in the specified voting card set for an election event id.
	 *
	 * @param electionEventId the election event identifier
	 * @param votingCardSetId identifies the voting card set where to search.
	 * @return the number of voting cards in the set
	 */
	public int getNumberOfVotingCards(final String electionEventId, final String votingCardSetId) throws ResourceNotFoundException {
		final JsonObject votingCardSet = this.getVotingCardSetJson(electionEventId, votingCardSetId);
		// Extract the number of voting cards in the set, which corresponds to the number of eligible voters for that voting card set.
		return votingCardSet.getInt(JsonConstants.NUMBER_OF_VOTING_CARDS_TO_GENERATE);
	}

}
