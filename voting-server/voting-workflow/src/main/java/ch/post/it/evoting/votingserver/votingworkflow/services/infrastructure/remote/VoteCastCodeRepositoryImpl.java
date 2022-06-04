/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.infrastructure.remote;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.cryptolib.certificates.utils.CryptographicOperationException;
import ch.post.it.evoting.domain.election.model.confirmation.ConfirmationMessage;
import ch.post.it.evoting.domain.election.model.confirmation.TraceableConfirmationMessage;
import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.domain.returncodes.ShortVoteCastReturnCodeAndComputeResults;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.VoteCastCodeRepositoryException;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitConsumer;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.commons.util.PropertiesFileReader;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.confirmation.VoteCastCodeRepository;

import okhttp3.ResponseBody;

/**
 * Implementation of the VoteCastCodeRepository using a REST client.
 */
@Stateless(name = "vw-VoteCastCodeRepositoryImpl")
public class VoteCastCodeRepositoryImpl implements VoteCastCodeRepository {

	private static final int HTTP_NOT_FOUND_STATUS_CODE = 404;

	private static final PropertiesFileReader PROPERTIES = PropertiesFileReader.getInstance();

	private static final String VOTE_CAST_CODE_PATH = PROPERTIES.getPropertyValue("VOTE_CAST_CODE_PATH");

	private static final Logger LOGGER = LoggerFactory.getLogger(VoteCastCodeRepositoryImpl.class);

	@Inject

	private TrackIdInstance trackId;

	private VerificationClient verificationClient;

	private ElectionInformationClient electionInformationClient;

	@Inject
	VoteCastCodeRepositoryImpl(final VerificationClient verificationClient, final ElectionInformationClient electionInformationClient) {
		this.verificationClient = verificationClient;
		this.electionInformationClient = electionInformationClient;
	}

	/**
	 * Generates the vote cast codes taking into account a tenant, election event and verification
	 * card for a given confirmationMessage. This implementation is based on a rest client which call
	 * to a web service rest operation.
	 *
	 * @param tenantId            - the identifier of the tenant.
	 * @param electionEventId     - the identifier of the election event.
	 * @param verificationCardId  - the identifier of the verification card.
	 * @param confirmationMessage - the confirmation message.
	 * @return The vote cast codes generated.
	 */
	@Override
	public ShortVoteCastReturnCodeAndComputeResults generateCastCode(String tenantId, String electionEventId, String verificationCardId,
			String votingCardId,
			String authenticationTokenSignature, ConfirmationMessage confirmationMessage) throws CryptographicOperationException {
		try {
			TraceableConfirmationMessage traceableConfirmationMessage = new TraceableConfirmationMessage();
			traceableConfirmationMessage.setAuthenticationTokenSignature(authenticationTokenSignature);
			traceableConfirmationMessage.setConfirmationKey(confirmationMessage.getConfirmationKey());
			traceableConfirmationMessage.setVotingCardId(votingCardId);
			return RetrofitConsumer.processResponse(verificationClient
					.generateCastCode(trackId.getTrackId(), VOTE_CAST_CODE_PATH, tenantId, electionEventId, verificationCardId,
							traceableConfirmationMessage));
		} catch (RetrofitException e) {
			throw new CryptographicOperationException("", e);
		}
	}

	/**
	 * Stores the vote cast codes taking into account a tenant, election event and verification card for a given confirmationMessage. This
	 * implementation is based on a rest client which call to a web service rest operation.
	 *
	 * @return Ok if the cast code is successfully stored.
	 */
	@Override
	public boolean storesCastCode(String tenantId, String electionEventId, String votingCardId,
			ShortVoteCastReturnCodeAndComputeResults voteCastMessage)
			throws ResourceNotFoundException {

		ValidationResult result = RetrofitConsumer.processResponse(electionInformationClient
				.storeCastCode(trackId.getTrackId(), VOTE_CAST_CODE_PATH, tenantId, electionEventId, votingCardId, voteCastMessage));

		return result.isResult();
	}

	/**
	 * Returns the vote cast codes taking into account a tenant, election event and voting card. This implementation is based on a rest client which
	 * call to a web service rest operation.
	 *
	 * @param tenantId        - the identifier of the tenant.
	 * @param electionEventId - the identifier of the election event.
	 * @param votingCardId    - the identifier of the voting card.
	 * @return The vote cast codes generated.
	 * @throws ResourceNotFoundException if the cast codes can not be recovered.
	 */
	@Override
	public ShortVoteCastReturnCodeAndComputeResults getCastCode(String tenantId, String electionEventId, String votingCardId)
			throws ResourceNotFoundException {
		return RetrofitConsumer.processResponse(
				electionInformationClient.getVoteCastCode(trackId.getTrackId(), VOTE_CAST_CODE_PATH, tenantId, electionEventId, votingCardId));
	}

	@Override
	public boolean voteCastCodeExists(final String tenantId, final String electionEventId, final String votingCardId)
			throws VoteCastCodeRepositoryException {
		try (ResponseBody responseBody = RetrofitConsumer.processResponse(
				electionInformationClient.checkVoteCastCode(trackId.getTrackId(), VOTE_CAST_CODE_PATH, tenantId, electionEventId, votingCardId))) {
			return true;
		} catch (RetrofitException rfE) {
			// Check if the HTTP status indicates that the vote cast return code was not found.
			if (rfE.getHttpCode() == HTTP_NOT_FOUND_STATUS_CODE) {
				return false;
			} else {
				LOGGER.error("Error trying to find a VoteCastCode exists", rfE);
				throw new VoteCastCodeRepositoryException("Error trying to find a VoteCastCode exists.", rfE);
			}
		}
	}
}
