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
 * Decorator of the max number of allowed votes rule.
 */
@Decorator
public class MaxNumberOfAllowedVotesDecorator implements AbstractRule<Vote> {

	@Inject
	@Delegate
	private MaxNumberOfAllowedVotes maxNumberOfAllowedVotes;

	/**
	 * @see AbstractRule#execute(Object)
	 */
	@Override
	public ValidationError execute(Vote vote) {
		return maxNumberOfAllowedVotes.execute(vote);
	}

	/**
	 * @see AbstractRule#getName()
	 */
	@Override
	public String getName() {
		return maxNumberOfAllowedVotes.getName();
	}

}
