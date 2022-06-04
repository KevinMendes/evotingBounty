/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.infrastructure.remote;

import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.ui.Constants;

import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Defines the methods to access via REST to a set of operations.
 */
public interface ElectionInformationClient {

	/**
	 * The endpoint to validate if an election is in dates.
	 *
	 * @param requestId       - the request id for logging purposes.
	 * @param pathBallotBoxes the path materials
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
					String pathBallotBoxes,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@Path(Constants.PARAMETER_VALUE_BALLOT_BOX_ID)
					String ballotBoxId) throws ResourceNotFoundException;
}
