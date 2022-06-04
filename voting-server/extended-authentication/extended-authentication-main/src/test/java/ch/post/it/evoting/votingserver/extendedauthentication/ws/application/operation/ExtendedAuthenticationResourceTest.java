/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.extendedauthentication.ws.application.operation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.junit.Assert;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.domain.election.model.Information.VoterInformation;
import ch.post.it.evoting.domain.election.model.authentication.AuthenticationToken;
import ch.post.it.evoting.domain.election.model.authentication.ExtendedAuthenticationUpdate;
import ch.post.it.evoting.domain.election.model.authentication.ExtendedAuthenticationUpdateRequest;
import ch.post.it.evoting.domain.election.validation.ValidationErrorType;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.AuthTokenValidationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ExtendedAuthValidationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.extendedauthentication.domain.actions.ValidateExtendedAuthUpdateAction;
import ch.post.it.evoting.votingserver.extendedauthentication.domain.model.EncryptedSVK;
import ch.post.it.evoting.votingserver.extendedauthentication.domain.model.extendedauthentication.AllowedAttemptsExceededException;
import ch.post.it.evoting.votingserver.extendedauthentication.domain.model.extendedauthentication.ExtendedAuthenticationRepository;
import ch.post.it.evoting.votingserver.extendedauthentication.services.infrastructure.persistence.AuthenticationException;
import ch.post.it.evoting.votingserver.extendedauthentication.services.infrastructure.persistence.ExtendedAuthenticationService;

public class ExtendedAuthenticationResourceTest extends ExtendedAuthenticationResource {

	private static final ObjectMapper objectMapper = DomainObjectMapper.getNewInstance();

	public ExtendedAuthenticationResourceTest() {
		super();
		this.extendedAuthenticationRepository = mock(ExtendedAuthenticationRepository.class);
		this.extendedAuthenticationService = mock(ExtendedAuthenticationService.class);
		this.trackIdInstance = mock(TrackIdInstance.class);
		this.validateExtendedAuthUpdateAction = mock(ValidateExtendedAuthUpdateAction.class);

	}

	@Test
	public void setSuccessfulResponse()
			throws ResourceNotFoundException, AllowedAttemptsExceededException, AuthenticationException, ApplicationException,
			GeneralCryptoLibException {

		when(extendedAuthenticationService.authenticate(any(), any(), any(), any())).thenReturn(new EncryptedSVK("authenticated"));

		ExtendedAuthentication extendedAuthentication = new ExtendedAuthentication();
		extendedAuthentication.setExtraParam("eP");
		extendedAuthentication.setAuthId("voCId2");
		HttpServletRequest request = mock(HttpServletRequest.class);
		Response authenticate = authenticate("1", "2", "trackid", extendedAuthentication, request);
		Assert.assertEquals(authenticate.getStatus(), Status.OK.getStatusCode());
		Assert.assertEquals(((ExtendedAuthResponse) authenticate.getEntity()).getEncryptedSVK(), "authenticated");
	}

	@Test
	public void setVotingCardNotFoundResponse()
			throws ResourceNotFoundException, AllowedAttemptsExceededException, AuthenticationException, ApplicationException,
			GeneralCryptoLibException {
		when(extendedAuthenticationService.authenticate(any(), any(), any(), any())).thenThrow(new ResourceNotFoundException(""));
		ExtendedAuthentication extendedAuthentication = new ExtendedAuthentication();
		extendedAuthentication.setExtraParam("eP");
		extendedAuthentication.setAuthId("voCId2");
		HttpServletRequest request = mock(HttpServletRequest.class);
		Response authenticate = authenticate("1", "2", "trackid", extendedAuthentication, request);
		Assert.assertEquals(((ExtendedAuthResponse) authenticate.getEntity()).getResponseCode(), Status.NOT_FOUND.name());
	}

	@Test
	public void setAllowedAttemptsExceededResponse()
			throws ResourceNotFoundException, AllowedAttemptsExceededException, AuthenticationException, ApplicationException,
			GeneralCryptoLibException {
		when(extendedAuthenticationService.authenticate(any(), any(), any(), any())).thenThrow(new AllowedAttemptsExceededException());
		ExtendedAuthentication extendedAuthentication = new ExtendedAuthentication();
		extendedAuthentication.setExtraParam("eP");
		extendedAuthentication.setAuthId("voCId2");
		HttpServletRequest request = mock(HttpServletRequest.class);
		Response authenticate = authenticate("1", "2", "trackid", extendedAuthentication, request);
		Assert.assertEquals(((ExtendedAuthResponse) authenticate.getEntity()).getResponseCode(), Status.FORBIDDEN.name());
		Assert.assertEquals(((ExtendedAuthResponse) authenticate.getEntity()).getNumberOfRemainingAttempts(), 0);
	}

	@Test
	public void setAllowedAttemptsExceededResponseWithInvalidSalt()
			throws ResourceNotFoundException, AllowedAttemptsExceededException, AuthenticationException, ApplicationException,
			GeneralCryptoLibException {
		when(extendedAuthenticationService.authenticate(any(), any(), any(), any()))
				.thenThrow(new AuthenticationException("invalid extra parameter", 0));
		ExtendedAuthentication extendedAuthentication = new ExtendedAuthentication();
		extendedAuthentication.setExtraParam("eP");
		extendedAuthentication.setAuthId("voCId2");
		HttpServletRequest request = mock(HttpServletRequest.class);
		Response authenticate = authenticate("1", "2", "trackid", extendedAuthentication, request);
		Assert.assertEquals(((ExtendedAuthResponse) authenticate.getEntity()).getResponseCode(), Status.FORBIDDEN.name());
	}

	@Test
	public void setDatabaseErrorException()
			throws ResourceNotFoundException, AllowedAttemptsExceededException, AuthenticationException, ApplicationException,
			GeneralCryptoLibException {
		when(extendedAuthenticationService.authenticate(any(), any(), any(), any())).thenThrow(new ApplicationException("DbException"));
		ExtendedAuthentication extendedAuthentication = new ExtendedAuthentication();
		extendedAuthentication.setExtraParam("eP");
		extendedAuthentication.setAuthId("voCId2");
		HttpServletRequest request = mock(HttpServletRequest.class);
		Response authenticate = authenticate("1", "2", "trackid", extendedAuthentication, request);
		Assert.assertEquals(((ExtendedAuthResponse) authenticate.getEntity()).getResponseCode(), Status.NOT_FOUND.name());
	}

	@Test
	public void setAuthenticationExceptionResponse()
			throws ResourceNotFoundException, AllowedAttemptsExceededException, AuthenticationException, ApplicationException,
			GeneralCryptoLibException {
		when(extendedAuthenticationService.authenticate(any(), any(), any(), any())).thenThrow(new AuthenticationException("", 1));
		ExtendedAuthentication extendedAuthentication = new ExtendedAuthentication();
		extendedAuthentication.setExtraParam("eP");
		extendedAuthentication.setAuthId("voCId2");
		HttpServletRequest request = mock(HttpServletRequest.class);
		Response authenticate = authenticate("1", "2", "trackid", extendedAuthentication, request);
		Assert.assertEquals(((ExtendedAuthResponse) authenticate.getEntity()).getResponseCode(), Status.UNAUTHORIZED.name());
	}

	@Test
	public void successfulUpdate() throws JsonProcessingException, ResourceNotFoundException, ApplicationException {
		AuthenticationToken authenticationTokenObject = createDummyAuthenticationToken();
		String authenticationToken = objectMapper.writeValueAsString(authenticationTokenObject);
		ExtendedAuthenticationUpdateRequest extendedAuthenticationUpdateRequest = new ExtendedAuthenticationUpdateRequest();
		ExtendedAuthenticationUpdate updateValidation = prepareUpdateValidationInfo(extendedAuthenticationUpdateRequest);
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(validateExtendedAuthUpdateAction.validate(any(), any(), any(), any())).thenReturn(updateValidation);
		ch.post.it.evoting.votingserver.extendedauthentication.domain.model.extendedauthentication.ExtendedAuthentication extendedAuthentication = createFromRequest(
				updateValidation);
		when(extendedAuthenticationService.renewExistingExtendedAuthentication(any(), any(), any(), any(), any())).thenReturn(extendedAuthentication);
		Response update = update("1", "2", "trackid", authenticationToken, extendedAuthenticationUpdateRequest, request);
		Assert.assertEquals(update.getStatus(), 200);
		Assert.assertEquals(((ExtendedAuthResponse) update.getEntity()).getResponseCode(), Status.OK.name());
	}

	private ExtendedAuthenticationUpdate prepareUpdateValidationInfo(ExtendedAuthenticationUpdateRequest extendedAuthenticationUpdateRequest) {
		extendedAuthenticationUpdateRequest.setCertificate("cert");
		extendedAuthenticationUpdateRequest.setSignature("signature");
		ExtendedAuthenticationUpdate updateValidation = new ExtendedAuthenticationUpdate();
		updateValidation.setAuthenticationTokenSignature("signature");
		updateValidation.setNewAuthID("newAuthId");
		updateValidation.setNewSVK("newSvk");
		updateValidation.setOldAuthID("oldAuthId");
		return updateValidation;
	}

	@Test
	public void updateWithInvalidAuthentication() throws JsonProcessingException, ResourceNotFoundException, ApplicationException {
		AuthenticationToken authenticationTokenObject = createDummyAuthenticationToken();
		String authenticationToken = objectMapper.writeValueAsString(authenticationTokenObject);
		ExtendedAuthenticationUpdateRequest extendedAuthenticationUpdateRequest = new ExtendedAuthenticationUpdateRequest();
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(validateExtendedAuthUpdateAction.validate(any(), any(), any(), any()))
				.thenThrow(new ExtendedAuthValidationException(ValidationErrorType.AUTH_TOKEN_EXPIRED));
		Response update = update("1", "2", "trackid", authenticationToken, extendedAuthenticationUpdateRequest, request);
		Assert.assertEquals(update.getStatus(), 200);
		Assert.assertEquals(((ExtendedAuthResponse) update.getEntity()).getResponseCode(), Status.BAD_REQUEST.name());
	}

	@Test
	public void updateWithExpiredAuthentication() throws JsonProcessingException, ResourceNotFoundException, ApplicationException {
		AuthenticationToken authenticationTokenObject = createDummyAuthenticationToken();
		String authenticationToken = objectMapper.writeValueAsString(authenticationTokenObject);
		ExtendedAuthenticationUpdateRequest extendedAuthenticationUpdateRequest = new ExtendedAuthenticationUpdateRequest();
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(validateExtendedAuthUpdateAction.validate(any(), any(), any(), any()))
				.thenThrow(new AuthTokenValidationException(ValidationErrorType.AUTH_TOKEN_EXPIRED));
		Response update = update("1", "2", "trackid", authenticationToken, extendedAuthenticationUpdateRequest, request);
		Assert.assertEquals(update.getStatus(), 200);
		Assert.assertEquals(((ExtendedAuthResponse) update.getEntity()).getResponseCode(), Status.BAD_REQUEST.name());
	}

	@Test
	public void updateWithResourceNotFound() throws JsonProcessingException, ResourceNotFoundException, ApplicationException {
		AuthenticationToken authenticationTokenObject = createDummyAuthenticationToken();
		String authenticationToken = objectMapper.writeValueAsString(authenticationTokenObject);
		ExtendedAuthenticationUpdateRequest extendedAuthenticationUpdateRequest = new ExtendedAuthenticationUpdateRequest();
		HttpServletRequest request = mock(HttpServletRequest.class);
		ExtendedAuthenticationUpdate updateValidation = prepareUpdateValidationInfo(extendedAuthenticationUpdateRequest);
		when(validateExtendedAuthUpdateAction.validate(any(), any(), any(), any())).thenReturn(updateValidation);
		when(extendedAuthenticationService.renewExistingExtendedAuthentication(any(), any(), any(), any(), any()))
				.thenThrow(new ResourceNotFoundException(""));
		Response update = update("1", "2", "trackid", authenticationToken, extendedAuthenticationUpdateRequest, request);
		Assert.assertEquals(update.getStatus(), 200);
		Assert.assertEquals(((ExtendedAuthResponse) update.getEntity()).getResponseCode(), Status.NOT_FOUND.name());
	}

	@Test
	public void updateWithInvalidSvk() throws JsonProcessingException, ResourceNotFoundException, ApplicationException {
		AuthenticationToken authenticationTokenObject = createDummyAuthenticationToken();
		String authenticationToken = objectMapper.writeValueAsString(authenticationTokenObject);
		ExtendedAuthenticationUpdateRequest extendedAuthenticationUpdateRequest = new ExtendedAuthenticationUpdateRequest();
		HttpServletRequest request = mock(HttpServletRequest.class);
		ExtendedAuthenticationUpdate updateValidation = prepareUpdateValidationInfo(extendedAuthenticationUpdateRequest);
		when(validateExtendedAuthUpdateAction.validate(any(), any(), any(), any())).thenReturn(updateValidation);
		when(extendedAuthenticationService.renewExistingExtendedAuthentication(any(), any(), any(), any(), any()))
				.thenThrow(new ApplicationException(""));
		Response update = update("1", "2", "trackid", authenticationToken, extendedAuthenticationUpdateRequest, request);
		Assert.assertEquals(update.getStatus(), 200);
		Assert.assertEquals(((ExtendedAuthResponse) update.getEntity()).getResponseCode(), Status.CONFLICT.name());
	}

	private ch.post.it.evoting.votingserver.extendedauthentication.domain.model.extendedauthentication.ExtendedAuthentication createFromRequest(
			ExtendedAuthenticationUpdate updateValidation) {
		ch.post.it.evoting.votingserver.extendedauthentication.domain.model.extendedauthentication.ExtendedAuthentication updated = new ch.post.it.evoting.votingserver.extendedauthentication.domain.model.extendedauthentication.ExtendedAuthentication();
		updated.setAttempts(2);
		updated.setAuthId(updateValidation.getNewAuthID());
		updated.setEncryptedStartVotingKey(updateValidation.getNewSVK());
		return updated;
	}

	private AuthenticationToken createDummyAuthenticationToken() {
		VoterInformation voterInformation = new VoterInformation();
		voterInformation.setBallotBoxId("");
		voterInformation.setBallotId("");
		voterInformation.setCredentialId("");
		voterInformation.setElectionEventId("");
		voterInformation.setTenantId("");
		voterInformation.setVerificationCardId("");
		voterInformation.setVerificationCardSetId("");
		voterInformation.setVotingCardId("");
		voterInformation.setVotingCardSetId("");
		voterInformation.setVerificationCardSetId("");
		voterInformation.setVotingCardSetId("");
		AuthenticationToken authenticationToken = new AuthenticationToken();
		authenticationToken.setId("id");
		authenticationToken.setSignature("signature");
		authenticationToken.setTimestamp("timestamp");
		authenticationToken.setVoterInformation(voterInformation);
		return authenticationToken;
	}

}
