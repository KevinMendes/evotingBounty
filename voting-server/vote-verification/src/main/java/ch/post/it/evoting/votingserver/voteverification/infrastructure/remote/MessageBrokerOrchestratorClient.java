/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.voteverification.infrastructure.remote;

import java.util.List;

import javax.ws.rs.core.MediaType;

import ch.post.it.evoting.domain.configuration.ElectionContextResponsePayload;
import ch.post.it.evoting.domain.configuration.ElectionEventContextPayload;
import ch.post.it.evoting.domain.voting.confirmvote.ConfirmationKeyPayload;
import ch.post.it.evoting.domain.voting.sendvote.CombinedPartiallyDecryptedEncryptedPCCPayload;
import ch.post.it.evoting.domain.voting.sendvote.EncryptedVerifiableVotePayload;
import ch.post.it.evoting.domain.voting.sendvote.LongReturnCodesSharePayload;
import ch.post.it.evoting.domain.voting.sendvote.PartiallyDecryptedEncryptedPCCPayload;
import ch.post.it.evoting.votingserver.commons.ui.Constants;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

/**
 * Defines the methods to access via REST to a set of operations.
 */
public interface MessageBrokerOrchestratorClient {

	/**
	 * Requests the control components' partial decryption of the encrypted partial Choice Return Codes.
	 *
	 * @param electionEventId       the election event id.
	 * @param verificationCardSetId the verification card set id.
	 * @param verificationCardId    the verification card id.
	 * @return the list of PartiallyDecryptedEncryptedPCCPayloads (one per control component).
	 */
	@POST("api/v1/voting/sendvote/partialdecrypt/electionevent/{electionEventId}/verificationCardSetId/{verificationCardSetId}/verificationCardId/{verificationCardId}")
	@Headers("Accept:" + MediaType.APPLICATION_JSON)
	Call<List<PartiallyDecryptedEncryptedPCCPayload>> getChoiceReturnCodesPartialDecryptContributions(
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
			final String electionEventId,
			@Path(Constants.PARAMETER_VALUE_VERIFICATION_CARD_SET_ID)
			final String verificationCardSetId,
			@Path(Constants.VERIFICATION_CARD_ID)
			final String verificationCardId,
			@Body
			final EncryptedVerifiableVotePayload encryptedVerifiableVotePayload);

	/**
	 * Requests the Control Components contributions for the computation of the partial choice return codes.
	 *
	 * @param electionEventId       the election event id.
	 * @param verificationCardSetId the verification card set id.
	 * @param verificationCardId    the verification card id.
	 * @return the list of LongReturnCodesSharePayload (one per control component).
	 */
	@POST("api/v1/voting/sendvote/lccshare/electionevent/{electionEventId}/verificationCardSetId/{verificationCardSetId}/verificationCardId/{verificationCardId}")
	@Headers("Accept:" + MediaType.APPLICATION_JSON)
	Call<List<LongReturnCodesSharePayload>> getLongChoiceReturnCodesContributions(
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
			final String electionEventId,
			@Path(Constants.PARAMETER_VALUE_VERIFICATION_CARD_SET_ID)
			final String verificationCardSetId,
			@Path(Constants.VERIFICATION_CARD_ID)
			final String verificationCardId,
			@Body
			final CombinedPartiallyDecryptedEncryptedPCCPayload combinedPartiallyDecryptedEncryptedPCCPayload);

	/**
	 * Requests the Control Components contributions for the computation of the Long Vote Cast Return Code.
	 *
	 * @param electionEventId        the election event id.
	 * @param verificationCardSetId  the verification card set id.
	 * @param verificationCardId     the verification card id.
	 * @param confirmationKeyPayload the payload with the confirmation key.
	 * @return the list of LongReturnCodesSharePayload (one per control component).
	 */
	@POST("api/v1/voting/confirmvote/lvccshare/electionevent/{electionEventId}/verificationCardSetId/{verificationCardSetId}/verificationCardId/{verificationCardId}")
	@Headers("Accept:" + MediaType.APPLICATION_JSON)
	Call<List<LongReturnCodesSharePayload>> getLongVoteCastReturnCodeContributions(
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
			final String electionEventId,
			@Path(Constants.PARAMETER_VALUE_VERIFICATION_CARD_SET_ID)
			final String verificationCardSetId,
			@Path(Constants.VERIFICATION_CARD_ID)
			final String verificationCardId,
			@Body
			final ConfirmationKeyPayload confirmationKeyPayload);

	/**
	 * Requests the Control Components to save the election event context.
	 *
	 * @param electionEventId             the election event id.
	 * @param electionEventContextPayload the payload with the election event context.
	 * @return the list of ElectionContextResponsePayload (one per control component)
	 */
	@POST("api/v1/configuration/electioncontext/electionevent/{electionEventId}")
	@Headers("Accept:" + MediaType.APPLICATION_JSON)
	Call<List<ElectionContextResponsePayload>> saveElectionEventContext(
			@Path(Constants.PARAMETER_VALUE_ELECTION_EVENT_ID)
			final String electionEventId,
			@Body
			final ElectionEventContextPayload electionEventContextPayload);

}
