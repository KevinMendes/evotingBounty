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
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.ballot.BallotTextRepository;

/**
 * This the implementation of the ballot repository.
 */
@Stateless(name = "vw-BallotTextRepositoryImpl")
public class BallotTextRepositoryImpl implements BallotTextRepository {

	private static final Logger LOGGER = LoggerFactory.getLogger(BallotTextRepositoryImpl.class);

	private static final PropertiesFileReader PROPERTIES = PropertiesFileReader.getInstance();

	private static final String PATH_BALLOT_TEXTS = PROPERTIES.getPropertyValue("BALLOT_TEXTS_PATH");
	ElectionInformationClient electionInformationClient;

	@Inject
	private TrackIdInstance trackId;

	@Inject
	BallotTextRepositoryImpl(final ElectionInformationClient electionInformationClient) {
		this.electionInformationClient = electionInformationClient;
	}

	/**
	 * Searches for a ballot texts identified by tenant, election event and ballot identifiers. The implementation is a REST Client invoking to an get
	 * operation of a REST web service to obtain the ballot texts for the given parameters.
	 *
	 * @param tenantId        - the identifier of the tenant.
	 * @param electionEventId - the identifier of the election event.
	 * @param ballotId        - the identifier of the ballot.
	 * @return a Ballot of the tenant identified by tenant id, and identified by ballot id.
	 * @throws ResourceNotFoundException if ballot is not found.
	 * @see BallotTextRepository#findByTenantIdElectionEventIdBallotId(String, String, String)
	 */
	@Override
	public String findByTenantIdElectionEventIdBallotId(String tenantId, String electionEventId, String ballotId) throws ResourceNotFoundException {
		LOGGER.info("Getting ballot texts. [tenantId: {}, electionEventId: {}, ballotId: {}]", tenantId, electionEventId, ballotId);

		try {
			String ballotText = RetrofitConsumer.processResponse(electionInformationClient
					.findBallotTextByTenantIdElectionEventIdBallotId(trackId.getTrackId(), PATH_BALLOT_TEXTS, tenantId, electionEventId, ballotId));
			LOGGER.info("Ballot found. [tenantId: {}, electionEventId: {}, ballotId: {}]", tenantId, electionEventId, ballotId);
			return ballotText;
		} catch (ResourceNotFoundException e) {
			LOGGER.error("Ballot not found. [tenantId: {}, electionEventId: {}, ballotId: {}]", tenantId, electionEventId, ballotId);
			throw e;
		}
	}

}
