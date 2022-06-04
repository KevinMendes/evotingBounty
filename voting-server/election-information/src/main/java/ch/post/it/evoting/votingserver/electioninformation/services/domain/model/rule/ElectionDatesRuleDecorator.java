/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.model.rule;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

import ch.post.it.evoting.domain.election.model.vote.Vote;
import ch.post.it.evoting.domain.election.validation.ValidationError;
import ch.post.it.evoting.votingserver.commons.domain.model.rule.AbstractRule;

/**
 * Rule which validates if the election is active when the vote is about to be saved in the Ballot
 * Box.
 */
@Decorator
public class ElectionDatesRuleDecorator implements AbstractRule<Vote> {

	@Inject
	@Delegate
	private ElectionDatesRule electionDateRule;

	/**
	 * This method validates whether the ballot box is still active before storing the vote.
	 *
	 * @param vote - The vote to be validated.
	 * @return True if the vote satisfies the rule. Otherwise, false.
	 */
	@Override
	public ValidationError execute(Vote vote) {
		return electionDateRule.execute(vote);
	}

	/**
	 * @see AbstractRule#getName()
	 */
	@Override
	public String getName() {
		return electionDateRule.getName();
	}
}
