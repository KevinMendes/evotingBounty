/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.model.rule;

import javax.inject.Inject;

import ch.post.it.evoting.domain.election.model.vote.Vote;
import ch.post.it.evoting.domain.election.validation.ValidationError;
import ch.post.it.evoting.votingserver.commons.domain.model.rule.AbstractRule;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.validation.ElectionValidationRequest;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.service.election.ElectionService;

/**
 * Rule which validates if the election is active when the vote is about to be saved in the Ballot Box. It takes into account the gracePeriod
 */
public class ElectionDatesRule implements AbstractRule<Vote> {

	@Inject
	private ElectionService electionService;

	/**
	 * This method validates whether the ballot box is still active before storing the vote
	 *
	 * @param vote - the vote to be validated.
	 * @return A ValidationError containing information about the execution of the rule. If fails, the dates of election are added as additional
	 * information.
	 */
	@Override
	public ValidationError execute(final Vote vote) {
		final ElectionValidationRequest request = ElectionValidationRequest
				.create(vote.getTenantId(), vote.getElectionEventId(), vote.getBallotBoxId(), true);
		return electionService.validateIfElectionIsOpen(request);
	}

	/**
	 * @see AbstractRule#getName()
	 */
	@Override
	public String getName() {
		return RuleNames.VOTE_ELECTION_DATES.getText();
	}
}
