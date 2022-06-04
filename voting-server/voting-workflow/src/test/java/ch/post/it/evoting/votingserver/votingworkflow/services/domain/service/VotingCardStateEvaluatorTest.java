/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.domain.service;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.state.VotingCardStateEvaluator;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.state.VotingCardStates;

@RunWith(MockitoJUnitRunner.class)
public class VotingCardStateEvaluatorTest {

	private VotingCardStateEvaluator sut;

	@Test
	public void when_confirmation_required_and_state_cast_then_cannot_block() {

		// given
		VotingCardStates cast = VotingCardStates.CAST;
		// when
		sut = VotingCardStateEvaluator.forState(cast);
		boolean canBeBlocked = sut.canBeBlocked();

		// then
		Assert.assertEquals(false, canBeBlocked);
	}

	@Test
	public void when_confirmation_required_and_state_sent_but_not_cast_then_can_block() {

		// given
		VotingCardStates cast = VotingCardStates.SENT_BUT_NOT_CAST;
		// when
		sut = VotingCardStateEvaluator.forState(cast);
		boolean canBeBlocked = sut.canBeBlocked();

		// then
		Assert.assertEquals(true, canBeBlocked);
	}

}
