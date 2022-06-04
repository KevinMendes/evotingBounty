/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.domain.service;

import ch.post.it.evoting.votingserver.commons.beans.confirmation.ConfirmationInformation;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.confirmation.VoteCastResult;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.information.VoterInformation;

/**
 * Service responsible for casting the votes.
 */
public interface CastVoteService {
	/**
	 * Cast the vote of the specified voter using provided confirmation. It is assumed that the vote
	 * has already been sent and is stored in the database.
	 *
	 * @param authenticationToken the authentication token
	 * @param voter               the voter
	 * @param confirmation        the confirmation
	 * @return the result
	 * @throws ResourceNotFoundException
	 */
	VoteCastResult castVote(String authenticationToken, VoterInformation voter, ConfirmationInformation confirmation)
			throws ResourceNotFoundException;
}
