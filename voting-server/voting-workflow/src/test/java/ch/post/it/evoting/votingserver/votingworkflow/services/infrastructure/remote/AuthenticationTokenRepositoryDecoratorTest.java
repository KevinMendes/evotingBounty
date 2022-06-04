/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.infrastructure.remote;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.domain.election.validation.ValidationError;
import ch.post.it.evoting.domain.election.validation.ValidationErrorType;
import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.votingserver.commons.beans.challenge.ChallengeInformation;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitException;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.authentication.AuthenticationToken;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.authentication.AuthenticationTokenMessage;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.authentication.AuthenticationTokenRepository;

import okhttp3.ResponseBody;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationTokenRepositoryDecoratorTest {

	private final String TENANT_ID = "100";

	private final String ELECTION_EVENT_ID = "1";

	private final String CREDENTIAL_ID = "1";

	private static final ObjectMapper objectMapper = DomainObjectMapper.getNewInstance();

	@InjectMocks
	AuthenticationTokenRepositoryDecorator rut;

	@Mock
	private AuthenticationTokenRepository authenticationTokenRepository;

	@BeforeClass
	public static void setup() {
		MockitoAnnotations.initMocks(AuthenticationTokenRepositoryDecoratorTest.class);
	}

	@Test
	public void testGetAuthenticationTokenSuccessful() throws ResourceNotFoundException, ApplicationException {
		ChallengeInformation challengeInformation = new ChallengeInformation();
		AuthenticationTokenMessage authTokenMessageMock = new AuthenticationTokenMessage();

		when(authenticationTokenRepository.getAuthenticationToken(TENANT_ID, ELECTION_EVENT_ID, CREDENTIAL_ID, challengeInformation))
				.thenReturn(authTokenMessageMock);
		AuthenticationTokenMessage authTokenMessage = rut.getAuthenticationToken(TENANT_ID, ELECTION_EVENT_ID, CREDENTIAL_ID, challengeInformation);

		assertEquals(authTokenMessageMock, authTokenMessage);
	}

	@Test(expected = ResourceNotFoundException.class)
	public void testGetAuthenticationTokenResourceNotFoundException() throws ResourceNotFoundException, ApplicationException {
		ChallengeInformation challengeInformation = new ChallengeInformation();

		when(authenticationTokenRepository.getAuthenticationToken(TENANT_ID, ELECTION_EVENT_ID, CREDENTIAL_ID, challengeInformation))
				.thenThrow(new ResourceNotFoundException("exception"));

		rut.getAuthenticationToken(TENANT_ID, ELECTION_EVENT_ID, CREDENTIAL_ID, challengeInformation);
	}

	@Test(expected = ResourceNotFoundException.class)
	public void testGetAuthenticationTokenResource404Error() throws ResourceNotFoundException, ApplicationException {
		ChallengeInformation challengeInformation = new ChallengeInformation();

		RetrofitException retrofitErrorMock = new RetrofitException(404, ResponseBody.create(okhttp3.MediaType.parse("text/html"), new byte[0]));

		when(authenticationTokenRepository.getAuthenticationToken(TENANT_ID, ELECTION_EVENT_ID, CREDENTIAL_ID, challengeInformation))
				.thenThrow(retrofitErrorMock);

		rut.getAuthenticationToken(TENANT_ID, ELECTION_EVENT_ID, CREDENTIAL_ID, challengeInformation);
	}

	@Test(expected = ApplicationException.class)
	public void testGetAuthenticationTokenResource500Error() throws ResourceNotFoundException, ApplicationException {
		ChallengeInformation challengeInformation = new ChallengeInformation();

		RetrofitException retrofitErrorMock = new RetrofitException(500, ResponseBody.create(okhttp3.MediaType.parse("text/html"), new byte[0]));

		when(authenticationTokenRepository.getAuthenticationToken(TENANT_ID, ELECTION_EVENT_ID, CREDENTIAL_ID, challengeInformation))
				.thenThrow(retrofitErrorMock);

		rut.getAuthenticationToken(TENANT_ID, ELECTION_EVENT_ID, CREDENTIAL_ID, challengeInformation);
	}

	@Test
	public void testValidateAuthenticationTokenIsValidSuccessful() throws IOException, ResourceNotFoundException, ApplicationException {
		AuthenticationToken authTokenMock = new AuthenticationToken();

		ValidationResult validationResultMock = new ValidationResult();
		validationResultMock.setResult(true);
		validationResultMock.setValidationError(new ValidationError(ValidationErrorType.SUCCESS));

		when(authenticationTokenRepository
				.validateAuthenticationToken(TENANT_ID, ELECTION_EVENT_ID, CREDENTIAL_ID, objectMapper.writeValueAsString(authTokenMock)))
				.thenReturn(validationResultMock);
		ValidationResult validationResult = rut
				.validateAuthenticationToken(TENANT_ID, ELECTION_EVENT_ID, CREDENTIAL_ID, objectMapper.writeValueAsString(authTokenMock));

		assertEquals(validationResultMock.isResult(), validationResult.isResult());
	}

	@Test
	public void testValidateAuthenticationTokenIsNotValidSuccessful() throws IOException, ResourceNotFoundException, ApplicationException {
		AuthenticationToken authTokenMock = new AuthenticationToken();

		ValidationResult validationResultMock = new ValidationResult();
		validationResultMock.setResult(false);
		validationResultMock.setValidationError(new ValidationError(ValidationErrorType.SUCCESS));

		when(authenticationTokenRepository
				.validateAuthenticationToken(TENANT_ID, ELECTION_EVENT_ID, CREDENTIAL_ID, objectMapper.writeValueAsString(authTokenMock)))
				.thenReturn(validationResultMock);
		ValidationResult validationResult = rut
				.validateAuthenticationToken(TENANT_ID, ELECTION_EVENT_ID, CREDENTIAL_ID, objectMapper.writeValueAsString(authTokenMock));

		assertEquals(validationResultMock.isResult(), validationResult.isResult());
	}

	@Test(expected = ResourceNotFoundException.class)
	public void testValidateAuthenticationToken404Error() throws IOException, ResourceNotFoundException, ApplicationException {
		AuthenticationToken authTokenMock = new AuthenticationToken();

		RetrofitException retrofitErrorMock = new RetrofitException(404, ResponseBody.create(okhttp3.MediaType.parse("text/html"), new byte[0]));

		when(authenticationTokenRepository
				.validateAuthenticationToken(TENANT_ID, ELECTION_EVENT_ID, CREDENTIAL_ID, objectMapper.writeValueAsString(authTokenMock)))
				.thenThrow(retrofitErrorMock);
		rut.validateAuthenticationToken(TENANT_ID, ELECTION_EVENT_ID, CREDENTIAL_ID, objectMapper.writeValueAsString(authTokenMock));
	}

	@Test(expected = ResourceNotFoundException.class)
	public void testValidateAuthenticationResourceNotFoundException() throws IOException, ResourceNotFoundException, ApplicationException {
		AuthenticationToken authTokenMock = new AuthenticationToken();

		when(authenticationTokenRepository
				.validateAuthenticationToken(TENANT_ID, ELECTION_EVENT_ID, CREDENTIAL_ID, objectMapper.writeValueAsString(authTokenMock)))
				.thenThrow(new ResourceNotFoundException("exception"));
		rut.validateAuthenticationToken(TENANT_ID, ELECTION_EVENT_ID, CREDENTIAL_ID, objectMapper.writeValueAsString(authTokenMock));
	}

	@Test(expected = ApplicationException.class)
	public void testValidateAuthenticationToken500Error() throws IOException, ResourceNotFoundException, ApplicationException {
		AuthenticationToken authTokenMock = new AuthenticationToken();

		RetrofitException retrofitErrorMock = new RetrofitException(500, ResponseBody.create(okhttp3.MediaType.parse("text/html"), new byte[0]));

		when(authenticationTokenRepository
				.validateAuthenticationToken(TENANT_ID, ELECTION_EVENT_ID, CREDENTIAL_ID, objectMapper.writeValueAsString(authTokenMock)))
				.thenThrow(retrofitErrorMock);
		rut.validateAuthenticationToken(TENANT_ID, ELECTION_EVENT_ID, CREDENTIAL_ID, objectMapper.writeValueAsString(authTokenMock));
	}
}
