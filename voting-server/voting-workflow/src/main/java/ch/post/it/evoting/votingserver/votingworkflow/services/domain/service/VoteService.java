/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.domain.service;

import java.io.IOException;

import ch.post.it.evoting.domain.election.model.vote.Vote;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.vote.ValidationVoteResult;

/**
 * Vote Service class
 */

public interface VoteService {

	ValidationVoteResult validateVoteAndStore(String tenantId, String electionEventId, String votingCardId, String verificationCardId, Vote vote,
			String authenticationTokenJsonString) throws ApplicationException, ResourceNotFoundException, DuplicateEntryException, IOException;

}
