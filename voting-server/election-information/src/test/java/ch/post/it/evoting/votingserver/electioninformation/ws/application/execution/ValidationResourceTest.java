/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.ws.application.execution;

import org.junit.Test;

import ch.post.it.evoting.domain.election.model.vote.Vote;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;

/**
 * Junit test for the class {@link ValidationResource}
 */
public class ValidationResourceTest {

	/**
	 * Test method for
	 * {@link ValidationResource#validate(ch.post.it.evoting.votingserver.electioninformation.services.domain.model.vote.Vote)} .
	 *
	 * @throws ApplicationException
	 */
	@Test
	public void testValidatetenantIdNull() throws ApplicationException {
		final Vote validateInput = new Vote();
		validateInput.setTenantId(null);
	}
}
