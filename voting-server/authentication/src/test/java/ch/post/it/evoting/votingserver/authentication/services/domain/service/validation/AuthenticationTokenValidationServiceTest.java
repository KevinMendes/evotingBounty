/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.service.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.security.cert.CertificateException;
import java.util.Iterator;

import javax.enterprise.inject.Instance;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.domain.election.model.authentication.AuthenticationContent;
import ch.post.it.evoting.domain.election.validation.ValidationErrorType;
import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication.AuthenticationToken;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.information.VoterInformation;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.information.VoterInformationRepository;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.validation.AuthenticationTokenValidation;
import ch.post.it.evoting.votingserver.authentication.services.domain.service.authenticationcontent.AuthenticationContentService;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.AuthTokenValidationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationTokenValidationServiceTest {

	@InjectMocks
	@Spy
	AuthenticationTokenValidationServiceImpl validationService = new AuthenticationTokenValidationServiceImpl();

	@InjectMocks
	AuthenticationToken authenticationTokenMock = new AuthenticationToken();

	@Mock
	VoterInformationRepository voterInformationRepositoryMock;

	@Mock
	Instance<AuthenticationTokenValidation> authenticationTokenValidationsMock;

	@Mock
	Iterator<AuthenticationTokenValidation> iteratorMock;

	@Mock
	AuthenticationTokenValidation authenticationTokenValidationMock;
	@Mock
	Logger logger;
	@Mock
	private AuthenticationContent authenticationContentMock;
	@Mock
	private AuthenticationContentService authenticationContentServiceMock;

	@Before
	public void setUp() {
		when(authenticationTokenValidationsMock.iterator()).thenReturn(iteratorMock);
	}

	@Test
	public void validateFalse() throws GeneralCryptoLibException, ResourceNotFoundException, CertificateException {
		when(iteratorMock.next()).thenReturn(authenticationTokenValidationMock);
		when(iteratorMock.hasNext()).thenReturn(true, false);
		validationService.setValidations(authenticationTokenValidationsMock);

		String tenantId = "1";
		String electionEventId = "1";
		String votingCardId = "1";
		ValidationResult resultMock = new ValidationResult();
		resultMock.setResult(false);
		when(authenticationTokenValidationMock.execute(tenantId, electionEventId, votingCardId, authenticationTokenMock))
				.thenThrow(new AuthTokenValidationException(ValidationErrorType.FAILED));

		ValidationResult result = validationService.validate(tenantId, electionEventId, votingCardId, authenticationTokenMock);

		assertFalse(result.isResult());
	}

	@Test
	public void validateTrue() throws GeneralCryptoLibException, ResourceNotFoundException, CertificateException {
		when(iteratorMock.next()).thenReturn(authenticationTokenValidationMock);
		when(iteratorMock.hasNext()).thenReturn(true, false);
		validationService.setValidations(authenticationTokenValidationsMock);

		String tenantId = "1";
		String electionEventId = "1";
		String votingCardId = "1";
		VoterInformation voterInformation = new VoterInformation();
		ValidationResult resultMock = new ValidationResult();
		when(authenticationTokenValidationMock.execute(tenantId, electionEventId, votingCardId, authenticationTokenMock)).thenReturn(resultMock);

		ValidationResult result = validationService.validate(tenantId, electionEventId, votingCardId, authenticationTokenMock);

		assertTrue(result.isResult());
	}

	@Test
	public void getAuthenticationContent() throws ResourceNotFoundException {

		String tenantId = "1";
		String electionEventId = "1";
		when(authenticationContentServiceMock.getAuthenticationContent(anyString(), anyString())).thenReturn(authenticationContentMock);
		try {
			validationService.getAuthenticationContent(tenantId, electionEventId);
		} catch (Exception e) {
			fail("Test shouldn't throw exception " + e.getMessage());
		}

	}
}
