/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.service;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import ch.post.it.evoting.domain.election.model.vote.Vote;
import ch.post.it.evoting.domain.election.validation.ValidationErrorType;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.domain.model.rule.AbstractRule;
import ch.post.it.evoting.votingserver.commons.domain.service.RuleExecutor;

/**
 * Junit tests for the class {@link RuleExecutor}.
 */
@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class RuleExecutorTest<T> {
	@SuppressWarnings("rawtypes")
	@InjectMocks
	private final RuleExecutor ruleExecutor = new RuleExecutor();
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	@Mock
	private Logger logger;
	private List<AbstractRule<T>> rulesList;

	private Vote vote;

	@Test
	public void executeThrowsApplicationException() throws ApplicationException {
		expectedException.expect(ApplicationException.class);

		rulesList = new ArrayList<AbstractRule<T>>();
		ruleExecutor.execute(rulesList, vote);
	}

	@Test
	public void executeWithEmptyList4ListNull() throws ApplicationException {
		vote = new Vote();
		assertEquals(ruleExecutor.execute(rulesList, vote).getValidationErrorType(), ValidationErrorType.SUCCESS);
	}

	@Test
	public void executeWithEmptyList4ListEmpty() throws ApplicationException {
		vote = new Vote();
		rulesList = new ArrayList<AbstractRule<T>>();
		assertEquals(ruleExecutor.execute(rulesList, vote).getValidationErrorType(), ValidationErrorType.SUCCESS);
	}
}
