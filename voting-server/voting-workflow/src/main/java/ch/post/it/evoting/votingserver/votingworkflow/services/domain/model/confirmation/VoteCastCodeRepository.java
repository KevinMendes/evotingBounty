/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.confirmation;

import ch.post.it.evoting.cryptolib.certificates.utils.CryptographicOperationException;
import ch.post.it.evoting.domain.election.model.confirmation.ConfirmationMessage;
import ch.post.it.evoting.domain.returncodes.ShortVoteCastReturnCodeAndComputeResults;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.VoteCastCodeRepositoryException;

/**
 * The Interface VoteCastCodeRepository.
 */
public interface VoteCastCodeRepository {

	/**
	 * Generate cast code.
	 *
	 * @param tenantId                     the tenant id
	 * @param electionEventId              the election event id
	 * @param verificationCardId           the verification card id
	 * @param authenticationTokenSignature
	 * @param votingCardId
	 * @param confirmationMessage          the confirmation message
	 * @return the vote cast message
	 */
	ShortVoteCastReturnCodeAndComputeResults generateCastCode(String tenantId, String electionEventId, String verificationCardId, String votingCardId,
			String authenticationTokenSignature, ConfirmationMessage confirmationMessage) throws CryptographicOperationException;

	/**
	 * Stores cast code.
	 *
	 * @param tenantId        the tenant id
	 * @param electionEventId the election event id
	 * @param votingCardId    the voting card id
	 * @param voteCastMessage the vote cast message
	 * @return true if the cast code is successfully stored. Otherwise, false.
	 * @throws ResourceNotFoundException if the cast codes can not be stored.
	 */
	boolean storesCastCode(String tenantId, String electionEventId, String votingCardId, ShortVoteCastReturnCodeAndComputeResults voteCastMessage)
			throws ResourceNotFoundException;

	/**
	 * Returns the vote cast codes taking into account a tenant, election event and voting card.
	 *
	 * @param tenantId        - the identifier of the tenant.
	 * @param electionEventId - the identifier of the election event.
	 * @param votingCardId    - the identifier of the voting card.
	 * @return The vote cast codes generated.
	 * @throws ResourceNotFoundException if the cast codes can not be recovered.
	 */
	ShortVoteCastReturnCodeAndComputeResults getCastCode(String tenantId, String electionEventId, String votingCardId)
			throws ResourceNotFoundException;

	boolean voteCastCodeExists(String tenantId, String electionEventId, String votingCardId) throws VoteCastCodeRepositoryException;
}
