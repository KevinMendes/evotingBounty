/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.choicecode;

import ch.post.it.evoting.domain.returncodes.ShortChoiceReturnCodeAndComputeResults;
import ch.post.it.evoting.domain.returncodes.VoteAndComputeResults;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;

/**
 * Interface for providing operations related with choice codes generation.
 */
public interface ChoiceCodeRepository {

	/**
	 * Generates the choice codes taking into account a tenant, election event and verification card
	 * for a given encrypted vote.
	 *
	 * @param tenantId           - the identifier of the tenant.
	 * @param electionEventId    - the identifier of the election event.
	 * @param verificationCardId - the identifier of the verification card.
	 * @param vote               - the vote used during the generation of choice codes.
	 * @return choice codes generated and computation results
	 * @throws ResourceNotFoundException if the choice codes can not be generated.
	 */
	ShortChoiceReturnCodeAndComputeResults generateChoiceCodes(String tenantId, String electionEventId, String verificationCardId,
			VoteAndComputeResults vote)
			throws ResourceNotFoundException;

}
