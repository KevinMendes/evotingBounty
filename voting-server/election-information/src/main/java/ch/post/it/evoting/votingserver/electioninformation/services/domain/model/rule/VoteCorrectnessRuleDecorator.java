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
 * Decorator of the vote correctness rule.
 */
@Decorator
public class VoteCorrectnessRuleDecorator implements AbstractRule<Vote> {

	@Inject
	@Delegate
	VoteCorrectnessRule voteCorrectnessRule;

	/**
	 * @see AbstractRule#execute(Object)
	 */
	@Override
	public ValidationError execute(final Vote vote) {
		return voteCorrectnessRule.execute(vote);
	}

	/**
	 * @see AbstractRule#getName()
	 */
	@Override
	public String getName() {
		return voteCorrectnessRule.getName();
	}
}
