/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.infrastructure.remote;

import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.post.it.evoting.votingserver.commons.beans.confirmation.ConfirmationInformation;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.confirmation.ConfirmationInformationResult;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitConsumer;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.commons.util.PropertiesFileReader;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.confirmation.ConfirmationInformationRepository;

/**
 * Implementation of the repository using a REST client
 */
@Stateless
public class ConfirmationInformationRepositoryImpl implements ConfirmationInformationRepository {

	// The properties file reader.
	private static final PropertiesFileReader PROPERTIES = PropertiesFileReader.getInstance();

	// The path to the resource authentication information.
	private static final String CONFIRMATION_PATH = PROPERTIES.getPropertyValue("CONFIRMATION_PATH");
	private final ElectionInformationClient electionInformationClient;
	// Instance of the track Id which will be written in the logs
	@Inject
	private TrackIdInstance trackId;

	@Inject
	ConfirmationInformationRepositoryImpl(final ElectionInformationClient electionInformationClient) {
		this.electionInformationClient = electionInformationClient;
	}

	/**
	 * Validates a confirmation message.
	 *
	 * @param tenantId                - the tenant identifier.
	 * @param electionEventId         - the electionEventIdentifier.
	 * @param votingCardId            - the voting card identifier.
	 * @param confirmationInformation - confirmation information to be validated
	 * @param token                   - authentication token
	 * @return Confirmation Information Result, with the result of the validation
	 */
	@Override
	public ConfirmationInformationResult validateConfirmationMessage(String tenantId, String electionEventId, String votingCardId,
			ConfirmationInformation confirmationInformation, String token) throws ResourceNotFoundException {
		return RetrofitConsumer.processResponse(electionInformationClient
				.validateConfirmationMessage(trackId.getTrackId(), CONFIRMATION_PATH, tenantId, electionEventId, votingCardId,
						confirmationInformation, token));
	}

}
