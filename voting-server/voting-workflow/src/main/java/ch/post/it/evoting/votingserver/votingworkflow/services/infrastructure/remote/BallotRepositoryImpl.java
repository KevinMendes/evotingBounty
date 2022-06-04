/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.infrastructure.remote;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitConsumer;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.commons.util.PropertiesFileReader;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.ballot.BallotRepository;

/**
 * This the implementation of the ballot repository.
 */
@Stateless(name = "vw-BallotRepositoryImpl")
public class BallotRepositoryImpl implements BallotRepository {

	private static final Logger LOGGER = LoggerFactory.getLogger(BallotRepositoryImpl.class);

	private static final PropertiesFileReader PROPERTIES = PropertiesFileReader.getInstance();

	private static final String PATH_BALLOTS = PROPERTIES.getPropertyValue("BALLOTS_PATH");
	private final ElectionInformationClient electionInformationClient;
	@Inject
	private TrackIdInstance trackId;

	@Inject
	BallotRepositoryImpl(final ElectionInformationClient electionInformationClient) {
		this.electionInformationClient = electionInformationClient;
	}

	/**
	 * Searches for a ballot identified by tenant, election event and ballot identifier. The implementation is a REST Client invoking to an get
	 * operation of a REST web service to obtain the ballot for the given parameters.
	 *
	 * @param tenantId        - the identifier of the tenant.
	 * @param electionEventId - the identifier of the election event.
	 * @param ballotId        - the identifier of the ballot.
	 * @return a Ballot of the tenant identified by tenant id, and identified by ballot id.
	 * @throws ResourceNotFoundException if ballot is not found.
	 */
	@Override
	public String findByTenantIdElectionEventIdBallotId(String tenantId, String electionEventId, String ballotId) throws ResourceNotFoundException {
		LOGGER.info("Getting ballot. [tenantId: {}, ballotId: {}]", tenantId, ballotId);

		try {
			String result = RetrofitConsumer
					.processResponse(electionInformationClient.getBallot(trackId.getTrackId(), PATH_BALLOTS, tenantId, electionEventId, ballotId));
			LOGGER.info("Ballot found. [tenantId: {}, ballotId: {}]", tenantId, ballotId);
			return result;
		} catch (ResourceNotFoundException e) {
			LOGGER.info("Ballot not found. [tenantId: {}, ballotId: {}]", tenantId, ballotId);
			throw e;
		}
	}

}
