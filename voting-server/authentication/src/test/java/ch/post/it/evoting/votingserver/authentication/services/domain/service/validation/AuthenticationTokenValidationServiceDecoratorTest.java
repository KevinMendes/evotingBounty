/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.service.validation;

import static org.junit.Assert.assertFalse;
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
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;

/**
 * Test class for the decorator
 */
@RunWith(MockitoJUnitRunner.class)
public class AuthenticationTokenValidationServiceDecoratorTest {

	public static final String TENANT_ID = "100";

	public static final String ELECTION_EVENT_ID = "100";

	public static final String VOTING_CARD_ID = "100";
	public static final String TRACK_ID = "trackId";
	@InjectMocks
	private final AuthenticationTokenValidationServiceDecorator sut = new AuthenticationTokenValidationServiceDecorator();
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	@Mock
	private AuthenticationTokenValidationService authenticationTokenValidationService;
	@Mock
	private AuthenticationToken token;
	@Mock
	private Logger logger;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this.getClass());
	}

	@Test
	public void validate() throws ResourceNotFoundException, CertificateException {

		when(authenticationTokenValidationService.validate(anyString(), anyString(), any(), any(AuthenticationToken.class)))
				.thenReturn(new ValidationResult(true));
		assertTrue(sut.validate(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID, token).isResult());

	}

	@Test
	public void validateException() throws ResourceNotFoundException, CertificateException {

		expectedException.expect(CertificateException.class);
		when(authenticationTokenValidationService.validate(anyString(), anyString(), any(), any(AuthenticationToken.class)))
				.thenThrow(new CertificateException("exception"));
		assertFalse(sut.validate(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID, token).isResult());

	}

	@Test
	public void getAuthenticationContent() throws ResourceNotFoundException {

		expectedException.expect(ResourceNotFoundException.class);
		when(authenticationTokenValidationService.getAuthenticationContent(anyString(), anyString()))
				.thenThrow(new ResourceNotFoundException("exception"));
		sut.getAuthenticationContent(TENANT_ID, ELECTION_EVENT_ID);
	}

}
