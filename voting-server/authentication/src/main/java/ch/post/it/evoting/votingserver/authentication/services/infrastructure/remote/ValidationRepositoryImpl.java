/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.infrastructure.remote;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitConsumer;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.commons.util.PropertiesFileReader;

/**
 * The validation repository implementation.
 */
public class ValidationRepositoryImpl {

	// The name of the property path.
	private static final String PROPERTY_PATH_VALIDATIONS = "path.validations";

	// The value of the property validations.
	private static final String PATH_VALIDATIONS = PropertiesFileReader.getInstance().getPropertyValue(PROPERTY_PATH_VALIDATIONS);
	private static final Logger LOGGER = LoggerFactory.getLogger(ValidationRepositoryImpl.class);
	@Inject
	private TrackIdInstance trackId;
	private ElectionInformationClient electionInformationClient;

	@Inject
	ValidationRepositoryImpl(final ElectionInformationClient electionInformationClient) {
		this.electionInformationClient = electionInformationClient;
	}

	/**
	 * Validates if an election is in dates.
	 *
	 * @param tenantId        - the tenant id
	 * @param electionEventId - the election event id.
	 * @param ballotBoxId     - the ballot box id.
	 * @return the result of the validation.
	 * @throws ResourceNotFoundException if a resource that is needed for the validation is missing
	 */
	public ValidationResult validateElectionInDates(String tenantId, String electionEventId, String ballotBoxId) throws ResourceNotFoundException {
		LOGGER.info("Validating election dates in authentication");
		ValidationResult ballotBoxStatus = RetrofitConsumer.processResponse(getElectionInformationClient()
				.validateElectionInDates(trackId.getTrackId(), PATH_VALIDATIONS, tenantId, electionEventId, ballotBoxId));
		LOGGER.info("Election is: {}", ballotBoxStatus.getValidationError().getValidationErrorType().name());
		return ballotBoxStatus;
	}

	/**
	 * Gets electionInformationClient.
	 *
	 * @return Value of electionInformationClient.
	 */
	public ElectionInformationClient getElectionInformationClient() {
		return electionInformationClient;
	}
}
