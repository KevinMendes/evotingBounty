/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.service.validation;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.security.cert.CertificateException;

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

import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication.AuthenticationToken;
import ch.post.it.evoting.votingserver.authentication.services.domain.utils.BeanUtils;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.AuthTokenValidationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;

/**
 * Test class for the decorator of the validation
 */
@RunWith(MockitoJUnitRunner.class)
public class AuthenticationTokenElectionEventIdValidationDecoratorTest {

	public static final String TENANT_ID = "100";
	public static final String ELECTION_EVENT_ID = "100";
	public static final String VOTING_CARD_ID = "100";
	@InjectMocks
	private final AuthenticationTokenElectionEventIdValidationDecorator sut = new AuthenticationTokenElectionEventIdValidationDecorator() {
		@Override
		public ValidationResult execute(String tenantId, String electionEventId, String votingCardId, AuthenticationToken authenticationToken)
				throws ResourceNotFoundException, CertificateException {
			return super.execute(tenantId, electionEventId, votingCardId, authenticationToken);
		}
	};
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	@Mock
	private AuthenticationTokenElectionEventIdValidation authenticationTokenTenantIdValidationMock;
	@Mock
	private Logger logger;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this.getClass());
	}

	@Test
	public void testValidation() throws ResourceNotFoundException, CertificateException {

		when(authenticationTokenTenantIdValidationMock.execute(anyString(), anyString(), anyString(), any(AuthenticationToken.class)))
				.thenReturn(new ValidationResult(true));
		final ValidationResult execute = sut.execute(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID, new AuthenticationToken());
		assertTrue(execute.isResult());

	}

	@Test
	public void testValidationFails() throws ResourceNotFoundException, CertificateException {

		expectedException.expect(AuthTokenValidationException.class);
		final AuthenticationToken authenticationToken = BeanUtils.createAuthenticationToken();

		when(authenticationTokenTenantIdValidationMock.execute(anyString(), anyString(), anyString(), any(AuthenticationToken.class)))
				.thenThrow(new AuthTokenValidationException(null));
		sut.execute(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID, authenticationToken);

	}

}
