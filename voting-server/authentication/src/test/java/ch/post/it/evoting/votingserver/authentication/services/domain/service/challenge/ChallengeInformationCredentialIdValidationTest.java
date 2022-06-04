/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.service.challenge;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;

import ch.post.it.evoting.votingserver.commons.beans.challenge.ChallengeInformation;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;

/**
 * Tests of {@link ChallengeInformationCredentialIdValidation}.
 */
public class ChallengeInformationCredentialIdValidationTest {
	private static final String TENANT_ID = "tenantId";

	private static final String ELECTION_ID = "electionId";

	private static final String CREDENTIAL_ID = "credentialId";

	private Logger logger;

	private ChallengeInformationCredentialIdValidation validation;

	@Before
	public void setUp() {
		logger = mock(Logger.class);
		validation = new ChallengeInformationCredentialIdValidation(logger);
	}

	@Test
	public void testExecute() throws ResourceNotFoundException {
		ChallengeInformation information = new ChallengeInformation();
		information.setCredentialId(CREDENTIAL_ID);
		assertTrue(validation.execute(TENANT_ID, ELECTION_ID, CREDENTIAL_ID, information));
		verify(logger).info(any(String.class), eq(TENANT_ID), eq(ELECTION_ID), eq(CREDENTIAL_ID));
	}

	@Test
	public void testExecuteInvalid() throws ResourceNotFoundException {
		ChallengeInformation information = new ChallengeInformation();
		information.setCredentialId(CREDENTIAL_ID + "Corrupted");
		assertFalse(validation.execute(TENANT_ID, ELECTION_ID, CREDENTIAL_ID, information));
		verify(logger).info(any(String.class), eq(TENANT_ID), eq(ELECTION_ID), eq(CREDENTIAL_ID), eq(information.getCredentialId()));
	}
}
