/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.service.ballotbox;

import javax.inject.Inject;
import javax.json.JsonObject;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.util.JsonUtils;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.BallotBoxInformation;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.BallotBoxInformationRepository;

/**
 * Service for managing ballot box information.
 */
public class BallotBoxInformationService {

	private static final String JSON_PARAMETER_TEST = "test";

	@Inject
	private BallotBoxInformationRepository ballotBoxInformationRepository;

	/**
	 * Returns true if the ballot box is for testing purposes.
	 *
	 * @param tenantId        the tenant id.
	 * @param electionEventId the election event id.
	 * @param ballotBoxId     the ballot box id.
	 * @return True if the ballot box is for testing. Otherwise, false.
	 * @throws ResourceNotFoundException
	 */
	public boolean isBallotBoxForTest(String tenantId, String electionEventId, String ballotBoxId) throws ResourceNotFoundException {
		BallotBoxInformation ballotBoxInformation = ballotBoxInformationRepository
				.findByTenantIdElectionEventIdBallotBoxId(tenantId, electionEventId, ballotBoxId);
		JsonObject ballotBoxInformationJson = JsonUtils.getJsonObject(ballotBoxInformation.getJson());

		return ballotBoxInformationJson.getBoolean(JSON_PARAMETER_TEST);
	}

}
