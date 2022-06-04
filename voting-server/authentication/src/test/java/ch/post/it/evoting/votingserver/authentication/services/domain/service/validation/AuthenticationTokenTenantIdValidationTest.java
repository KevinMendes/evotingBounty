/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.service.validation;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication.AuthenticationToken;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.information.VoterInformation;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.AuthTokenValidationException;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationTokenTenantIdValidationTest {

	public static final String TENANT_ID = "100";

	public static final String ELECTION_EVENT_ID = "100";

	public static final String VOTING_CARD_ID = "100";

	public static final String OTHER_TENANT_ID = "OTHER_TENANT_ID";
	@InjectMocks
	private final AuthenticationTokenTenantIdValidation validation = new AuthenticationTokenTenantIdValidation();
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	@Mock
	private Logger logger;

	@Mock
	private AuthenticationToken authenticationTokenMock;

	@Mock
	private VoterInformation voterInformation;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this.getClass());
	}

	@Test
	public void validate() {
		when(voterInformation.getTenantId()).thenReturn(TENANT_ID);
		when(authenticationTokenMock.getVoterInformation()).thenReturn(voterInformation);
		assertTrue(validation.execute(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID, authenticationTokenMock).isResult());
	}

	@Test
	public void validateAndFail() {

		expectedException.expect(AuthTokenValidationException.class);
		when(voterInformation.getTenantId()).thenReturn(OTHER_TENANT_ID);
		when(authenticationTokenMock.getVoterInformation()).thenReturn(voterInformation);
		validation.execute(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID, authenticationTokenMock);
	}

}
