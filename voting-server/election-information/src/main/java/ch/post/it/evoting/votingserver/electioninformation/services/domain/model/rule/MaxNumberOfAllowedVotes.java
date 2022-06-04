/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.model.rule;

import java.util.List;

import javax.inject.Inject;

import ch.post.it.evoting.domain.election.model.vote.Vote;
import ch.post.it.evoting.domain.election.validation.ValidationError;
import ch.post.it.evoting.domain.election.validation.ValidationErrorType;
import ch.post.it.evoting.votingserver.commons.domain.model.rule.AbstractRule;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.BallotBox;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.BallotBoxRepository;

/**
 * Validates number of allowed votes for voting card id and authentication token.
 */
public class MaxNumberOfAllowedVotes implements AbstractRule<Vote> {

	@Inject
	private BallotBoxRepository ballotBoxRepository;

	@Override
	public ValidationError execute(Vote vote) {
		// validation result. By default is set to false
		ValidationError result = new ValidationError();
		result.setValidationErrorType(ValidationErrorType.FAILED);

		// recover all votes for voting card
		List<BallotBox> storedVotesInBallotBox = ballotBoxRepository
				.findByTenantIdElectionEventIdVotingCardIdBallotBoxIdBallotId(vote.getTenantId(), vote.getElectionEventId(), vote.getVotingCardId(),
						vote.getBallotBoxId(), vote.getBallotId());

		// perform validations
		if (storedVotesInBallotBox.isEmpty()) {
			result.setValidationErrorType(ValidationErrorType.SUCCESS);
		}

		return result;
	}

	@Override
	public String getName() {
		return RuleNames.VOTE_MAX_NUMBER_OF_ALLOWED_VOTES.getText();
	}

}
