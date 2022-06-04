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
 * Rule to check the vote ids vs. the auth token ids.
 */
@Decorator
public class VoteIdsRuleDecorator implements AbstractRule<Vote> {

	@Inject
	@Delegate
	private VoteIdsRule voteIdsRule;

	@Override
	public ValidationError execute(Vote vote) {
		return voteIdsRule.execute(vote);
	}

	/**
	 * @see AbstractRule#getName()
	 */
	@Override
	public String getName() {
		return voteIdsRule.getName();
	}
}
