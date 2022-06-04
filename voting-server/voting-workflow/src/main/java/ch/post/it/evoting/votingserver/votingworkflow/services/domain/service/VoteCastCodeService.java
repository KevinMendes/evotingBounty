/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.domain.service;

import java.io.IOException;

import javax.ejb.EJB;
import javax.ejb.Stateless;

import ch.post.it.evoting.domain.returncodes.ShortVoteCastReturnCodeAndComputeResults;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.confirmation.VoteCastMessage;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.confirmation.VoteCastResult;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.vote.VoteRepository;

/**
 * Service for handling vote cast codes.
 */
@Stateless
public class VoteCastCodeService {

	@EJB
	private VoteRepository voteRepository;

	/**
	 * Generates vote cast result with valid=true, vote message, encypted vote, auth token, vote cast message.
	 *
	 * @param electionEventId        the election event id
	 * @param votingCardId           the voting card id
	 * @param verificationCardId
	 * @param castCodeComputeResults the vote cast message
	 * @return the vote cast result
	 * @throws ResourceNotFoundException if the vote can not be found.
	 * @throws IOException               if there are problems converting json string to object.
	 */
	public VoteCastResult generateVoteCastResult(String electionEventId, String votingCardId, String verificationCardId,
			ShortVoteCastReturnCodeAndComputeResults castCodeComputeResults) throws ResourceNotFoundException, IOException {
		VoteCastResult voteCastResult = new VoteCastResult();

		// values
		voteCastResult.setElectionEventId(electionEventId);
		voteCastResult.setVotingCardId(votingCardId);
		voteCastResult.setValid(true);
		voteCastResult.setVoteCastMessage(new VoteCastMessage(castCodeComputeResults.getShortVoteCastReturnCode()));
		voteCastResult.setVerificationCardId(verificationCardId);

		// result
		return voteCastResult;
	}

}
