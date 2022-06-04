/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.infrastructure.remote;

import ch.post.it.evoting.domain.election.model.vote.Vote;
import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.domain.returncodes.ShortVoteCastReturnCodeAndComputeResults;
import ch.post.it.evoting.domain.returncodes.VoteAndComputeResults;
import ch.post.it.evoting.votingserver.commons.beans.confirmation.ConfirmationInformation;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.confirmation.ConfirmationInformationResult;
import ch.post.it.evoting.votingserver.commons.ui.Constants;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * The Interface for election information client.
 */
public interface ElectionInformationClient {

	/**
	 * Gets the ballot box information.
	 *
	 * @param acceptType      the content type which is accepted
	 * @param trackId         the track id
	 * @param pathBallotBox   the path ballot box
	 * @param tenantId        the tenant id
	 * @param electionEventId the election event id
	 * @param ballotBoxId     the ballot box id
	 * @return the ballot box information
	 * @throws ResourceNotFoundException the resource not found exception
	 */
	@GET("{pathBallotBox}/tenant/{tenantId}/electionevent/{electionEventId}/ballotbox/{ballotBoxId}")
	Call<String> getBallotBoxInformation(
			@Header(Constants.PARAMETER_ACCEPT)
					String acceptType,
			@Header(Constants.PARAMETER_X_REQUEST_ID)
					String trackId,
			@Path(Constants.PARAMETER_PATH_BALLOT_BOX)
					String pathBallotBox,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@Path(Constants.PARAMETER_VALUE_BALLOT_BOX_ID)
					String ballotBoxId) throws ResourceNotFoundException;

	/**
	 * Gets the ballot.
	 *
	 * @param trackId         the track id
	 * @param pathBallot      the path ballot
	 * @param tenantId        the tenant id
	 * @param electionEventId the election event id
	 * @param ballotId        the ballot id
	 * @return the ballot
	 * @throws ResourceNotFoundException the resource not found exception
	 */
	@GET("{pathBallot}/tenant/{tenantId}/electionevent/{electionEventId}/ballot/{ballotId}")
	Call<String> getBallot(
			@Header(Constants.PARAMETER_X_REQUEST_ID)
					String trackId,
			@Path(Constants.PARAMETER_PATH_BALLOT)
					String pathBallot,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@Path(Constants.PARAMETER_VALUE_BALLOT_ID)
					String ballotId) throws ResourceNotFoundException;

	/**
	 * Find ballot text by tenant id election event id ballot id.
	 *
	 * @param trackId         the track id
	 * @param pathBallotText  the path ballot text
	 * @param tenantId        the tenant id
	 * @param electionEventId the election event id
	 * @param ballotId        the ballot id
	 * @return the string
	 * @throws ResourceNotFoundException the resource not found exception
	 */
	@GET("{pathBallotText}/tenant/{tenantId}/electionevent/{electionEventId}/ballot/{ballotId}")
	Call<String> findBallotTextByTenantIdElectionEventIdBallotId(
			@Header(Constants.PARAMETER_X_REQUEST_ID)
					String trackId,
			@Path(Constants.PARAMETER_PATH_BALLOT_TEXT)
					String pathBallotText,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@Path(Constants.PARAMETER_VALUE_BALLOT_ID)
					String ballotId) throws ResourceNotFoundException;

	/**
	 * Validate confirmation message.
	 *
	 * @param trackId                 the track id
	 * @param pathConfirmationMessage the path confirmation message
	 * @param tenantId                the tenant id
	 * @param electionEventId         the election event id
	 * @param votingCardId            the voting card id
	 * @param confirmationInformation the confirmation information
	 * @param token                   the token
	 * @return the confirmation information result
	 */
	@POST("{pathConfirmationMessage}/tenant/{tenantId}/electionevent/{electionEventId}/votingcard/{votingCardId}")
	Call<ConfirmationInformationResult> validateConfirmationMessage(
			@Header(Constants.PARAMETER_X_REQUEST_ID)
					String trackId,
			@Path(Constants.PARAMETER_PATH_CONFIRMATION)
					String pathConfirmationMessage,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@Path(Constants.PARAMETER_VALUE_VOTING_CARD_ID)
					String votingCardId,
			@Body
					ConfirmationInformation confirmationInformation,
			@Header(Constants.PARAMETER_VALUE_AUTHENTICATION_TOKEN)
					String token);

	/**
	 * Validate vote.
	 *
	 * @param tenantId        - the tenant id
	 * @param electionEventId - the election event id
	 * @param trackId         the track id
	 * @param pathValidations the path validations
	 * @param vote            the vote
	 * @return the validation result
	 */
	@POST("{pathValidations}/tenant/{tenantId}/electionevent/{electionEventId}")
	Call<ValidationResult> validateVote(
			@Header(Constants.PARAMETER_X_REQUEST_ID)
					String trackId,
			@Path(Constants.PATH_VALIDATIONS)
					String pathValidations,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@Body
					Vote vote);

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

	/**
	 * Save vote.
	 *
	 * @param trackId         the track id
	 * @param pathVote        the path vote
	 * @param tenantId        - the tenant id
	 * @param electionEventId - the election event id
	 * @param vote            the vote with computation proofs and signature
	 * @param token           the token
	 * @return whether the vote was saved successfully
	 */
	@POST("{pathVote}/tenant/{tenantId}/electionevent/{electionEventId}")
	Call<ResponseBody> saveVote(
			@Header(Constants.PARAMETER_X_REQUEST_ID)
					String trackId,
			@Path(Constants.PARAMETER_PATH_VOTE)
					String pathVote,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@Body
					VoteAndComputeResults vote,
			@Header(Constants.PARAMETER_VALUE_AUTHENTICATION_TOKEN)
					String token);

	/**
	 * Gets the vote.
	 *
	 * @param trackId         the track id
	 * @param pathVote        the path vote
	 * @param tenantId        the tenant id
	 * @param electionEventId the election event id
	 * @param votingCardId    the voting card id
	 * @return the vote
	 * @throws ResourceNotFoundException the resource not found exception
	 */
	@GET("{pathVote}/tenant/{tenantId}/electionevent/{electionEventId}/votingcard/{votingCardId}")
	Call<VoteAndComputeResults> getVote(
			@Header(Constants.PARAMETER_X_REQUEST_ID)
					String trackId,
			@Path(Constants.PARAMETER_PATH_VOTE)
					String pathVote,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@Path(Constants.PARAMETER_VALUE_VOTING_CARD_ID)
					String votingCardId) throws ResourceNotFoundException;

	/**
	 * Store cast code.
	 *
	 * @param trackId         the track id
	 * @param castCode        the cast code
	 * @param tenantId        the tenant id
	 * @param electionEventId the election event id
	 * @param votingCardId    the voting card id
	 * @param voteCastMessage the vote cast message
	 * @return the validation result
	 */
	@POST("{pathCastCode}/tenant/{tenantId}/electionevent/{electionEventId}/votingcard/{votingCardId}")
	Call<ValidationResult> storeCastCode(
			@Header(Constants.PARAMETER_X_REQUEST_ID)
					String trackId,
			@Path(Constants.PARAMETER_PATH_CAST_CODE)
					String castCode,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@Path(Constants.PARAMETER_VALUE_VOTING_CARD_ID)
					String votingCardId,
			@Body
					ShortVoteCastReturnCodeAndComputeResults voteCastMessage);

	/**
	 * Get the cast code.
	 *
	 * @param trackId         the track id
	 * @param castCode        the cast code
	 * @param tenantId        the tenant id
	 * @param electionEventId the election event id
	 * @param votingCardId    the voting card id
	 * @return the cast code
	 */
	@GET("{pathCastCode}/tenant/{tenantId}/electionevent/{electionEventId}/votingcard/{votingCardId}")
	Call<ShortVoteCastReturnCodeAndComputeResults> getVoteCastCode(
			@Header(Constants.PARAMETER_X_REQUEST_ID)
					String trackId,
			@Path(Constants.PARAMETER_PATH_CAST_CODE)
					String castCode,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@Path(Constants.PARAMETER_VALUE_VOTING_CARD_ID)
					String votingCardId);

	/**
	 * Check if a VoteCastCode exists
	 *
	 * @param trackId         the track id
	 * @param castCode        the cast code
	 * @param tenantId        the tenant id
	 * @param electionEventId the election event id
	 * @param votingCardId    the voting card id
	 * @return the cast code
	 */
	@GET("{pathCastCode}/tenant/{tenantId}/electionevent/{electionEventId}/votingcard/{votingCardId}/available")
	Call<ResponseBody> checkVoteCastCode(
			@Header(Constants.PARAMETER_X_REQUEST_ID)
					String trackId,
			@Path(Constants.PARAMETER_PATH_CAST_CODE)
					String castCode,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@Path(Constants.PARAMETER_VALUE_VOTING_CARD_ID)
					String votingCardId);

	/**
	 * Check if a vote exists.
	 *
	 * @param trackId         the track id
	 * @param pathVote        the path vote
	 * @param tenantId        the tenant id
	 * @param electionEventId the election event id
	 * @param votingCardId    the voting card id
	 * @return votes exists (200 OK), does not exist (404 Not Found)
	 */
	@GET("{pathVote}/tenant/{tenantId}/electionevent/{electionEventId}/votingcard/{votingCardId}/available")
	Call<ResponseBody> checkVote(
			@Header(Constants.PARAMETER_X_REQUEST_ID)
					String trackId,
			@Path(Constants.PARAMETER_PATH_VOTE)
					String pathVote,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@Path(Constants.PARAMETER_VALUE_VOTING_CARD_ID)
					String votingCardId);

}
