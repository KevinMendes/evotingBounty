/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.mixdec.infrastructure.persistence;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;

import javax.inject.Inject;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.MixnetInitialPayload;
import ch.post.it.evoting.domain.election.model.ballotbox.BallotBoxId;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitConsumer;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.orchestrator.mixdec.infrastructure.remote.ElectionInformationClient;
import ch.post.it.evoting.votingserver.orchestrator.mixdec.infrastructure.remote.ResourceNotReadyException;

import okhttp3.ResponseBody;

public class CleansedBallotBoxRepository {

	private static final int HTTP_NOT_FOUND_STATUS_CODE = 404;
	private static final int HTTP_PRECONDITION_FAILED = 412;
	private static final String PATH_CLEANSED_BALLOT_BOXES = "cleansedballotboxes";
	private static final String CLEANSED_BALLOT_BOX = "Cleansed ballot box";
	private static final String MISSING = "is missing";
	private static final String NOT_READY = "not ready";
	private static final String TENANT_ID = "100";

	@Inject
	private TrackIdInstance trackId;

	@Inject
	private ElectionInformationClient electionInformationClient;

	@Inject
	private ObjectMapper mapper;

	/**
	 * Checks if the given ballot box is empty, i.e. does not contain any vote.
	 *
	 * @param electionEventId the election event id. Must be non-null.
	 * @param ballotBoxId     the ballot box id. Must be non-null.
	 * @return {@code true} if the ballot box is empty, {@code false} otherwise.
	 * @throws ApplicationException if a Retrofit or response parsing exception occurs.
	 */
	public boolean isBallotBoxEmpty(final String electionEventId, final String ballotBoxId) throws ApplicationException {
		checkNotNull(electionEventId);
		checkNotNull(ballotBoxId);

		try {
			final ResponseBody responseBody = RetrofitConsumer.processResponse(electionInformationClient
					.isBallotBoxEmpty(trackId.getTrackId(), PATH_CLEANSED_BALLOT_BOXES, TENANT_ID, electionEventId, ballotBoxId));
			final JsonNode jsonNode = mapper.readTree(responseBody.string());

			return jsonNode.get("empty").asBoolean();
		} catch (RetrofitException e) {
			throw new ApplicationException("Retrofit error, failed to check if ballot box is empty.", e);
		} catch (IOException e) {
			throw new ApplicationException("Failed to parse response body.", e);
		}
	}

	public MixnetInitialPayload getMixnetInitialPayload(final BallotBoxId ballotBoxId)
			throws ResourceNotFoundException, ResourceNotReadyException, IOException {

		try {
			final ResponseBody mixnetInitialPayloadBody = RetrofitConsumer.processResponse(electionInformationClient
					.getMixnetInitialPayload(trackId.getTrackId(), PATH_CLEANSED_BALLOT_BOXES, ballotBoxId.getTenantId(),
							ballotBoxId.getElectionEventId(), ballotBoxId.getId()));

			return mapper.readValue(mixnetInitialPayloadBody.string(), MixnetInitialPayload.class);
		} catch (RetrofitException e) {
			switch (e.getHttpCode()) {
			case HTTP_NOT_FOUND_STATUS_CODE:
				throw new ResourceNotFoundException(String.join(" ", CLEANSED_BALLOT_BOX, ballotBoxId.getId(), MISSING), e);
			case HTTP_PRECONDITION_FAILED:
				throw new ResourceNotReadyException(String.join(" ", CLEANSED_BALLOT_BOX, ballotBoxId.getId(), NOT_READY), e);
			default:
				throw e;
			}
		}
	}

}
