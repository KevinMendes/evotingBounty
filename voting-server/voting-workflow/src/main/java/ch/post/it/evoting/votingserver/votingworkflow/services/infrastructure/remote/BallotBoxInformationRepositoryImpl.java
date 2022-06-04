/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.infrastructure.remote;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitConsumer;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.commons.util.PropertiesFileReader;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.ballotbox.BallotBoxInformationRepository;

/**
 * Implementation of the repository for handling BallotInformation Objects through a REST client.
 */
@Stateless(name = "vw-BallotBoxInformationRepositoryImpl")
public class BallotBoxInformationRepositoryImpl implements BallotBoxInformationRepository {

	// The properties file reader.
	private static final PropertiesFileReader PROPERTIES = PropertiesFileReader.getInstance();

	// The path to the resource authentication information.
	private static final String BALLOT_BOX_PATH = PROPERTIES.getPropertyValue("BALLOT_BOX_PATH");

	private static final String ACCEPT_HEADER = MediaType.APPLICATION_JSON;

	private static final Logger LOGGER = LoggerFactory.getLogger(BallotBoxInformationRepositoryImpl.class);

	private final ElectionInformationClient electionInformationClient;
	// Instance of the track Id which will be written in the logs
	@Inject
	private TrackIdInstance trackId;

	@Inject
	BallotBoxInformationRepositoryImpl(final ElectionInformationClient electionInformationClient) {
		this.electionInformationClient = electionInformationClient;
	}

	/**
	 * Gets the BallotBox information for a given tenant, election event and ballot box identifier.
	 *
	 * @param tenantId        - identifier of the tenant
	 * @param electionEventId - the identifier of the election event.
	 * @param ballotBoxId     - identifier of the ballot.
	 * @return a BallotBoxInformationObject.
	 */
	@Override
	public String getBallotBoxInfoByTenantIdElectionEventIdBallotBoxId(String tenantId, String electionEventId, String ballotBoxId)
			throws ResourceNotFoundException {

		LOGGER.info("Getting ballot box information. [tenantId: {}, ballotBoxId: {}]", tenantId, ballotBoxId);
		try {
			String result = RetrofitConsumer.processResponse(electionInformationClient
					.getBallotBoxInformation(ACCEPT_HEADER, trackId.getTrackId(), BALLOT_BOX_PATH, tenantId, electionEventId, ballotBoxId));
			LOGGER.info("Ballot box information found. [tenantId: {}, ballotBoxId: {}]", tenantId, ballotBoxId);
			return result;
		} catch (ResourceNotFoundException e) {
			LOGGER.error("Ballot box information not found. [tenantId: {}, ballotBoxId: {}]", tenantId, ballotBoxId);
			throw e;

		}
	}

}
