/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.service;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.utils.PemUtils;
import ch.post.it.evoting.cryptolib.commons.serialization.JsonSignatureService;
import ch.post.it.evoting.cryptoprimitives.domain.election.Ballot;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.commons.PathResolver;
import ch.post.it.evoting.securedatamanager.config.commons.utils.JsonUtils;
import ch.post.it.evoting.securedatamanager.services.application.exception.ResourceNotFoundException;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.Status;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.SynchronizeStatus;
import ch.post.it.evoting.securedatamanager.services.infrastructure.JsonConstants;
import ch.post.it.evoting.securedatamanager.services.infrastructure.ballot.BallotRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.ballottext.BallotTextRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.service.ConfigurationEntityStatusService;

/**
 * Service for operates with ballots.
 */
@Service
public class BallotService {

	private static final Logger LOGGER = LoggerFactory.getLogger(BallotService.class);

	private static final String BALLOT_ID_FIELD = "ballot.id";

	@Autowired
	private ConfigurationEntityStatusService statusService;

	@Autowired
	private BallotRepository ballotRepository;

	@Autowired
	private BallotTextRepository ballotTextRepository;

	@Autowired
	private ConsistencyCheckService consistencyCheckService;

	@Autowired
	private PathResolver pathResolver;

	@Autowired
	private ObjectMapper objectMapper;

	/**
	 * Sign the given ballot and related ballot texts, and change the state of the ballot from locked to SIGNED for a given election event and ballot
	 * id.
	 *
	 * @param electionEventId the election event id.
	 * @param ballotId        the ballot id.
	 * @param privateKeyPEM   the administration board private key in PEM format.
	 * @throws ResourceNotFoundException if the ballot is not found.
	 * @throws GeneralCryptoLibException if the private key cannot be read.
	 */
	public void sign(final String electionEventId, final String ballotId, final String privateKeyPEM)
			throws ResourceNotFoundException, GeneralCryptoLibException {

		final PrivateKey privateKey = PemUtils.privateKeyFromPem(privateKeyPEM);

		final JsonObject ballot = getValidBallot(electionEventId, ballotId);
		final JsonObject modifiedBallot = removeBallotMetaData(ballot);

		if (!validateElectionEventRepresentations(electionEventId, ballot)) {
			throw new GeneralCryptoLibException("Validation of the representations used on the ballot options failed.");
		}

		LOGGER.info("Signing ballot {}.", ballotId);
		final String signedBallot = JsonSignatureService.sign(privateKey, modifiedBallot.toString());

		ballotRepository.updateSignedBallot(ballotId, signedBallot);
		final JsonArray ballotTexts = getBallotTexts(ballotId);

		LOGGER.info("Signing ballot texts");
		for (int i = 0; i < ballotTexts.size(); i++) {

			final JsonObject ballotText = ballotTexts.getJsonObject(i);
			final String ballotTextId = ballotText.getString(JsonConstants.ID);
			final JsonObject modifiedBallotText = removeBallotTextMetaData(ballotText);
			final String signedBallotText = JsonSignatureService.sign(privateKey, modifiedBallotText.toString());
			ballotTextRepository.updateSignedBallotText(ballotTextId, signedBallotText);
		}

		LOGGER.info("Changing the ballot status");
		statusService.updateWithSynchronizedStatus(Status.SIGNED.name(), ballotId, ballotRepository, SynchronizeStatus.PENDING);

		LOGGER.info("The ballot was successfully signed");
	}

	/**
	 * Validate the representations used on the ballot against the assigned to the election event Validate the representations file signature
	 */
	private boolean validateElectionEventRepresentations(final String electionEventId, final JsonObject ballot) throws GeneralCryptoLibException {

		// Get the representations file for the Election Event
		final Path representationsFile = pathResolver
				.resolve(Constants.SDM_DIR_NAME, Constants.CONFIG_DIR_NAME, electionEventId, Constants.CONFIG_DIR_NAME_CUSTOMER,
						Constants.CONFIG_DIR_NAME_OUTPUT, Constants.CONFIG_FILE_NAME_REPRESENTATIONS_CSV);

		try {
			// Check consistency of db stored representations of the options in the ballot
			return consistencyCheckService.representationsConsistent(ballot.toString(), representationsFile);
		} catch (final IOException e) {
			throw new GeneralCryptoLibException("CSV file with representations prime numbers not found.", e);
		}
	}

	private JsonObject removeBallotTextMetaData(final JsonObject ballotText) {
		return removeField(JsonConstants.SIGNED_OBJECT, ballotText);
	}

	private JsonObject removeBallotMetaData(final JsonObject ballot) {

		JsonObject modifiedBallot = removeField(JsonConstants.STATUS, ballot);
		modifiedBallot = removeField(JsonConstants.DETAILS, modifiedBallot);
		modifiedBallot = removeField(JsonConstants.SYNCHRONIZED, modifiedBallot);
		return removeField(JsonConstants.SIGNED_OBJECT, modifiedBallot);
	}

	private JsonObject removeField(final String field, final JsonObject obj) {

		final JsonObjectBuilder builder = Json.createObjectBuilder();

		for (final Map.Entry<String, JsonValue> e : obj.entrySet()) {
			final String key = e.getKey();
			final JsonValue value = e.getValue();
			if (!key.equals(field)) {
				builder.add(key, value);
			}

		}
		return builder.build();
	}

	private JsonArray getBallotTexts(final String ballotId) {
		final Map<String, Object> ballotTextParams = new HashMap<>();
		ballotTextParams.put(BALLOT_ID_FIELD, ballotId);
		return JsonUtils.getJsonObject(ballotTextRepository.list(ballotTextParams)).getJsonArray(JsonConstants.RESULT);
	}

	private JsonObject getValidBallot(final String electionEventId, final String ballotId) throws ResourceNotFoundException {

		final Optional<JsonObject> possibleBallot = getPossibleValidBallot(electionEventId, ballotId);

		if (!possibleBallot.isPresent()) {
			throw new ResourceNotFoundException("Ballot not found");
		}

		return possibleBallot.get();
	}

	/**
	 * Pre: there is just one matching element
	 *
	 * @return single {@link Status.LOCKED} ballot in json object format
	 */
	private Optional<JsonObject> getPossibleValidBallot(final String electionEventId, final String ballotId) {

		Optional<JsonObject> ballot = Optional.empty();
		final Map<String, Object> attributeValueMap = new HashMap<>();

		attributeValueMap.put(JsonConstants.ELECTION_EVENT_DOT_ID, electionEventId);
		attributeValueMap.put(JsonConstants.ID, ballotId);
		attributeValueMap.put(JsonConstants.STATUS, Status.LOCKED.name());
		final String ballotResultListAsJson = ballotRepository.list(attributeValueMap);

		if (StringUtils.isEmpty(ballotResultListAsJson)) {
			return ballot;
		} else {
			final JsonArray ballotResultList = JsonUtils.getJsonObject(ballotResultListAsJson).getJsonArray(JsonConstants.RESULT);

			if (ballotResultList != null && !ballotResultList.isEmpty()) {
				ballot = Optional.of(ballotResultList.getJsonObject(0));
			} else {
				return ballot;
			}
		}
		return ballot;
	}

	public Ballot getBallot(final String electionEventId, final String ballotId) {
		final Map<String, Object> attributeValueMap = new HashMap<>();
		attributeValueMap.put(JsonConstants.ELECTION_EVENT_DOT_ID, electionEventId);
		attributeValueMap.put(JsonConstants.ID, ballotId);
		final String ballotAsJson = ballotRepository.find(attributeValueMap);
		try {
			return objectMapper.readValue(ballotAsJson, Ballot.class);
		} catch (final IOException e) {
			throw new UncheckedIOException("Cannot deserialize the ballot box json string to a valid Ballot object.", e);
		}
	}
}
