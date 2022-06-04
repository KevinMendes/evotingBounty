/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
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

import ch.post.it.evoting.domain.election.validation.ValidationError;
import ch.post.it.evoting.domain.election.validation.ValidationErrorType;
import ch.post.it.evoting.votingserver.authentication.services.domain.service.exception.AuthenticationTokenGenerationException;
import ch.post.it.evoting.votingserver.authentication.services.domain.service.exception.AuthenticationTokenSigningException;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationTokenFactoryDecoratorTest {

	public static final String VOTING_CARD_ID = "100";

	public static final String ELECTION_EVENT_ID = "100";

	public static final String TENANT_ID = "100";
	@InjectMocks
	private final AuthenticationTokenFactoryDecorator decorator = new AuthenticationTokenFactoryDecorator();
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	@Mock
	private AuthenticationTokenFactory authenticationTokenFactory;

	@Mock
	private AuthenticationTokenMessage authenticationTokenMessage;

	@Mock
	private ValidationError validationErrorMock;

	@Mock
	private Logger LOGGER;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this.getClass());
	}

	@Test
	public void buildAuthenticationToken() throws AuthenticationTokenGenerationException, AuthenticationTokenSigningException {

		when(authenticationTokenFactory.buildAuthenticationToken(anyString(), anyString(), anyString())).thenReturn(authenticationTokenMessage);
		final AuthenticationTokenMessage authenticationTokenMessage = decorator
				.buildAuthenticationToken(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID);
		assertNotNull(authenticationTokenMessage);
	}

	@Test
	public void buildAuthenticationTokenGenerationException() throws AuthenticationTokenGenerationException, AuthenticationTokenSigningException {

		expectedException.expect(AuthenticationTokenGenerationException.class);
		when(authenticationTokenFactory.buildAuthenticationToken(anyString(), anyString(), anyString()))
				.thenThrow(new AuthenticationTokenGenerationException("exception", null));

		decorator.buildAuthenticationToken(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID);

	}

	@Test
	public void buildAuthenticationTokenErrorNull() throws AuthenticationTokenGenerationException, AuthenticationTokenSigningException {

		when(authenticationTokenFactory.buildAuthenticationToken(anyString(), anyString(), anyString())).thenReturn(authenticationTokenMessage);
		when(authenticationTokenMessage.getValidationError()).thenReturn(null);

		decorator.buildAuthenticationToken(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID);

	}

	@Test
	public void buildAuthenticationTokenWithKnownError() throws AuthenticationTokenGenerationException, AuthenticationTokenSigningException {

		final String[] errors = { "" };
		when(authenticationTokenFactory.buildAuthenticationToken(anyString(), anyString(), anyString())).thenReturn(authenticationTokenMessage);
		when(authenticationTokenMessage.getValidationError()).thenReturn(validationErrorMock);
		when(validationErrorMock.getValidationErrorType()).thenReturn(ValidationErrorType.ELECTION_NOT_STARTED);

		decorator.buildAuthenticationToken(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID);

	}

	@Test
	public void buildAuthenticationTokenWithSuccess() throws AuthenticationTokenGenerationException, AuthenticationTokenSigningException {

		when(authenticationTokenFactory.buildAuthenticationToken(anyString(), anyString(), anyString())).thenReturn(authenticationTokenMessage);
		when(authenticationTokenMessage.getValidationError()).thenReturn(validationErrorMock);
		when(validationErrorMock.getValidationErrorType()).thenReturn(ValidationErrorType.SUCCESS);

		decorator.buildAuthenticationToken(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID);

	}

}
