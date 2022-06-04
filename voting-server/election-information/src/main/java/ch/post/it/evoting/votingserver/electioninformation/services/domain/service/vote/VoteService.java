/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.service.vote;

import ch.post.it.evoting.domain.election.payload.verify.ValidationException;
import ch.post.it.evoting.domain.returncodes.VoteAndComputeResults;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;

/**
 * Interface that performs operations regarding the votes of a ballot box
 */
public interface VoteService {

	/**
	 * Saves a vote with it's associated token
	 *
	 * @param vote                the vote to be saved
	 * @param authenticationToken the token used to send the vote
	 * @throws ValidationException     if there is any problem validating the vote
	 * @throws DuplicateEntryException if there was already a vote for this voter in the ballot box
	 *                                 found
	 */
	void saveVote(VoteAndComputeResults vote, String authenticationToken) throws DuplicateEntryException, ValidationException;

	VoteAndComputeResults retrieveVote(String tenantId, String electionEventId, String votingCardId)
			throws ApplicationException, ResourceNotFoundException;
}
