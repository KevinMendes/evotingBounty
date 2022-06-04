/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.infrastructure.remote;

import ch.post.it.evoting.cryptolib.certificates.utils.CryptographicOperationException;
import ch.post.it.evoting.domain.election.model.confirmation.TraceableConfirmationMessage;
import ch.post.it.evoting.domain.returncodes.ShortChoiceReturnCodeAndComputeResults;
import ch.post.it.evoting.domain.returncodes.ShortVoteCastReturnCodeAndComputeResults;
import ch.post.it.evoting.domain.returncodes.VoteAndComputeResults;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.beans.verificationset.VerificationSet;
import ch.post.it.evoting.votingserver.commons.ui.Constants;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.verification.Verification;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * The Interface for vote verification client.
 */
public interface VerificationClient {

	/**
	 * Generate choice codes.
	 *
	 * @param trackId            the track id for logging purposes
	 * @param choiceCodePath     the choice code path
	 * @param tenantId           the tenant id
	 * @param electionEventId    the election event id
	 * @param verificationCardId the verification card id
	 * @param vote               the vote
	 * @return the generated choice code with all compute results
	 * @throws ResourceNotFoundException when the webservice fails
	 */
	@POST("{choiceCodePath}/tenant/{tenantId}/electionevent/{electionEventId}/verificationcard/{verificationCardId}")
	Call<ShortChoiceReturnCodeAndComputeResults> generateChoiceCodes(
			@Header(Constants.PARAMETER_X_REQUEST_ID)
					String trackId,
			@Path(Constants.PARAMETER_CHOICE_CODE_PATH)
					String choiceCodePath,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@Path(Constants.VERIFICATION_CARD_ID)
					String verificationCardId,
			@Body
					VoteAndComputeResults vote) throws ResourceNotFoundException;

	/**
	 * Find verification by tenant, election event and verification card.
	 *
	 * @param trackId            the track id for logging purposes
	 * @param verificationPath   the verification path
	 * @param tenantId           the tenant id
	 * @param electionEventId    the election event id
	 * @param verificationCardId the verification card id
	 * @return the verification data for the given parameters
	 * @throws ResourceNotFoundException when the webservice fails
	 */
	@GET("{verificationPath}/tenant/{tenantId}/electionevent/{electionEventId}/verificationcard/{verificationCardId}")
	Call<Verification> findVerificationByTenantElectionEventVerificationCard(
			@Header(Constants.PARAMETER_X_REQUEST_ID)
					String trackId,
			@Path(Constants.PARAMETER_VERIFICATION_PATH)
					String verificationPath,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@Path(Constants.VERIFICATION_CARD_ID)
					String verificationCardId) throws ResourceNotFoundException;

	/**
	 * Find verification set by tenant, election event and verification card set id.
	 *
	 * @param trackId               the track id for logging purposes
	 * @param verificationSetPath   the verification set path
	 * @param tenantId              the tenant id
	 * @param electionEventId       the election event id
	 * @param verificationCardSetId the verification card set id
	 * @return the verification set data for the given parameters
	 * @throws ResourceNotFoundException when the webservice fails
	 */
	@GET("{verificationSetPath}/tenant/{tenantId}/electionevent/{electionEventId}/verificationcardset/{verificationCardSetId}")
	Call<VerificationSet> findVerificationSetByTenantElectionEventVerificationCardSetId(
			@Header(Constants.PARAMETER_X_REQUEST_ID)
					String trackId,
			@Path(Constants.PARAMETER_VERIFICATION_SET_PATH)
					String verificationSetPath,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@Path(Constants.PARAMETER_VALUE_VERIFICATION_CARD_SET_ID)
					String verificationCardSetId) throws ResourceNotFoundException;

	/**
	 * Generate cast code.
	 *
	 * @param trackId             the track id for logging purposes
	 * @param castCodePath        the cast code path
	 * @param tenantId            the tenant id
	 * @param electionEventId     the election event id
	 * @param verificationCardId  the verification card id
	 * @param confirmationMessage the confirmation message
	 * @return the vote cast message
	 */
	@POST("{castCodePath}/tenant/{tenantId}/electionevent/{electionEventId}/verificationcard/{verificationCardId}")
	Call<ShortVoteCastReturnCodeAndComputeResults> generateCastCode(
			@Header(Constants.PARAMETER_X_REQUEST_ID)
					String trackId,
			@Path(Constants.PARAMETER_CAST_CODE_PATH)
					String castCodePath,
			@Path(Constants.PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@Path(Constants.VERIFICATION_CARD_ID)
					String verificationCardId,
			@Body
					TraceableConfirmationMessage confirmationMessage) throws CryptographicOperationException;

}
