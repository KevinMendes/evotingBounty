/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.mixdec.infrastructure.remote;

import javax.ws.rs.core.MediaType;

import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.ui.Constants;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Streaming;

/**
 * Defines the methods to access via REST to a set of operations.
 */
public interface ElectionInformationClient {

	/**
	 * The endpoint to validate if an election is in dates.
	 *
	 * @param requestId       - the request id for logging purposes.
	 * @param pathValidations the path materials
	 * @param tenantId        - the tenant id
	 * @param electionEventId - the election event id
	 * @param ballotBoxId     - the ballot box id
	 * @return the validation error for a given tenant, election event and ballot box ids.
	 * @throws ResourceNotFoundException if the rest operation fails.
	 */
	@POST("{pathValidations}/tenant/{tenantId}/electionevent/{electionEventId}/ballotbox/{ballotBoxId}")
	Call<ValidationResult> validateElectionInDates(
			@Header(Constants.PARAMETER_X_REQUEST_ID)
					String requestId,
			@Path(Constants.PATH_VALIDATIONS)
					String pathValidations,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@Path(Constants.PARAMETER_VALUE_BALLOT_BOX_ID)
					String ballotBoxId) throws ResourceNotFoundException;

	/**
	 * Checks if a ballot box is empty, i.e. does not contain any vote.
	 *
	 * @param requestId               the tracking identifier.
	 * @param pathCleansedBallotBoxes the URL path prefix for cleansed ballot boxes
	 * @param tenantId                the tenant id.
	 * @param electionEventId         the election event id.
	 * @param ballotBoxId             the ballot box id.
	 * @return a json indicating if the request ballot is empty or not.
	 */
	@GET("{pathCleansedBallotBoxes}/tenant/{tenantId}/electionevent/{electionEventId}/ballotbox/{ballotBoxId}")
	@Headers("Accept:" + MediaType.APPLICATION_JSON)
	@Streaming
	Call<ResponseBody> isBallotBoxEmpty(
			@Header(Constants.PARAMETER_X_REQUEST_ID)
			final String requestId,
			@Path(Constants.PATH_CLEANSED_BALLOT_BOXES)
			final String pathCleansedBallotBoxes,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
			final String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
			final String electionEventId,
			@Path(Constants.PARAMETER_VALUE_BALLOT_BOX_ID)
			final String ballotBoxId);

	/**
	 * Retrieves all encrypted cleansed votes of a specific ballot box in the form of mixing payloads, to be sent to the control components' mixing
	 * nodes.
	 *
	 * @param requestId               a tracking identifier
	 * @param pathCleansedBallotBoxes the URL path prefix for cleansed ballot boxes
	 * @param tenantId                the tenant identifier
	 * @param electionEventId         the election event identifier
	 * @param ballotBoxId             the ballot box identifier
	 * @return a stream of signed mixing payloads
	 */
	@GET("{pathCleansedBallotBoxes}/tenant/{tenantId}/electionevent/{electionEventId}/ballotbox/{ballotBoxId}/encryptedvotes")
	@Headers("Accept:" + MediaType.APPLICATION_JSON)
	@Streaming
	Call<ResponseBody> getMixnetInitialPayload(
			@Header(Constants.PARAMETER_X_REQUEST_ID)
					String requestId,
			@Path(Constants.PATH_CLEANSED_BALLOT_BOXES)
					String pathCleansedBallotBoxes,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@Path(Constants.PARAMETER_VALUE_BALLOT_BOX_ID)
					String ballotBoxId);

}
