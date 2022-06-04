/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.ws.application.operation;

import java.io.IOException;
import java.security.cert.X509Certificate;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.factory.CryptoX509Certificate;
import ch.post.it.evoting.cryptolib.certificates.utils.CryptographicOperationException;
import ch.post.it.evoting.cryptolib.certificates.utils.PemUtils;
import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication.AuthenticationTokenFactory;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication.AuthenticationTokenMessage;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.validation.CertificateChainValidationRequest;
import ch.post.it.evoting.votingserver.authentication.services.domain.service.certificate.CertificateChainValidationService;
import ch.post.it.evoting.votingserver.authentication.services.domain.service.challenge.ChallengeInformationValidationService;
import ch.post.it.evoting.votingserver.authentication.services.domain.service.exception.AuthenticationTokenGenerationException;
import ch.post.it.evoting.votingserver.authentication.services.domain.service.exception.AuthenticationTokenSigningException;
import ch.post.it.evoting.votingserver.commons.beans.challenge.ChallengeInformation;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationExceptionMessages;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.SemanticErrorException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.SyntaxErrorException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.commons.ui.Constants;
import ch.post.it.evoting.votingserver.commons.util.ValidationUtils;

/**
 * The end point for authentication token resources.
 */
@Path("/tokens")
@Stateless
public class AuthenticationTokenResource {

	private static final String PARAMETER_VALUE_TENANT_ID = "tenantId";

	private static final String PARAMETER_VALUE_CREDENTIAL_ID = "credentialId";

	private static final String PARAMETER_VALUE_ELECTION_EVENT_ID = "electionEventId";

	private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationTokenResource.class);

	private static final ObjectMapper objectMapper = DomainObjectMapper.getNewInstance();

	@Inject
	private TrackIdInstance trackIdInstance;

	@Inject
	private AuthenticationTokenFactory authenticationTokenFactory;

	@Inject
	private ChallengeInformationValidationService challengeInformationValidationService;

	@Inject
	private CertificateChainValidationService certificateChainValidationSerivce;

	/**
	 * Return an authentication token object for a specific credential id
	 *
	 * @param trackingId           - the track id to be used for logging purposes.
	 * @param tenantId             - the tenant identifier.
	 * @param electionEventId      - the election event identifier.
	 * @param credentialId         - the credential identifier.
	 * @param challengeInformation - the challenge information including client and server challenge messages.
	 * @param request              - the HTTP servlet request.
	 * @return If the operation is successfully performed, returns a response with HTTP status code 200 and the authentication token in json format.
	 * @throws AuthenticationTokenGenerationException
	 * @throws AuthenticationTokenSigningException
	 * @throws ApplicationException
	 * @throws SyntaxErrorException
	 * @throws SemanticErrorException
	 * @throws ResourceNotFoundException
	 * @throws CryptographicOperationException
	 * @throws IOException
	 */

	@Path("/tenant/{tenantId}/electionevent/{electionEventId}/credential/{credentialId}")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response getAuthenticationToken(
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
			final
			String trackingId,
			@PathParam(PARAMETER_VALUE_TENANT_ID)
			final String tenantId,
			@PathParam(PARAMETER_VALUE_ELECTION_EVENT_ID)
			final String electionEventId,
			@PathParam(PARAMETER_VALUE_CREDENTIAL_ID)
			final String credentialId,
			@NotNull
			final ChallengeInformation challengeInformation,
			@Context
			final HttpServletRequest request)
			throws AuthenticationTokenGenerationException, AuthenticationTokenSigningException, ApplicationException, SyntaxErrorException,
			SemanticErrorException, ResourceNotFoundException, CryptographicOperationException, IOException {
		trackIdInstance.setTrackId(trackingId);

		validateInput(tenantId, electionEventId, credentialId);

		// validate input challengeInformation for syntax or semantic errors
		ValidationUtils.validate(challengeInformation);

		// get certificate and certificate subject common name.
		final String certificatePem = challengeInformation.getCertificate();
		final CryptoX509Certificate cryptoCertificate;
		try {
			cryptoCertificate = new CryptoX509Certificate((X509Certificate) PemUtils.certificateFromPem(certificatePem));
		} catch (final GeneralCryptoLibException e) {
			LOGGER.error("Cryptographic error when opening credential ID authorization certificate: ", e);
			return Response.status(Status.PRECONDITION_FAILED).build();
		}
		final String subjectCommonName = cryptoCertificate.getSubjectDn().getCommonName();

		// validate subject common name.
		if (!subjectCommonName.contains(challengeInformation.getCredentialId())) {
			LOGGER.error("Validation of credential ID authorization certificate subject common name failed.");
			return Response.status(Status.PRECONDITION_FAILED).build();
		}

		final ValidationResult certChainValidationResult = certificateChainValidationSerivce.validate(tenantId, electionEventId, certificatePem);

		// if the certificate chain is successfully validated, then validate
		// the challenge and generate the token
		if (certChainValidationResult.isResult()) {

			// validate the challenge
			final ValidationResult challengeValidationResult = challengeInformationValidationService
					.validate(tenantId, electionEventId, credentialId, challengeInformation);

			// if the challenge is successfully validated, then generate the
			// authentication token
			if (challengeValidationResult.isResult()) {
				// generate authentication token
				final AuthenticationTokenMessage authenticationTokenMessage = authenticationTokenFactory
						.buildAuthenticationToken(tenantId, electionEventId, credentialId);
				// returns the result of operation
				final String json = objectMapper.writeValueAsString(authenticationTokenMessage);
				return Response.ok(json).build();
			} else {
				LOGGER.error("Validation of credential ID authorization challenge failed.");
				return Response.status(Status.PRECONDITION_FAILED).build();
			}
		} else {
			LOGGER.error("Validation of credential ID authorization certificate chain failed.");
			return Response.status(Status.PRECONDITION_FAILED).build();
		}
	}

	private void validateInput(final String tenantId, final String electionEventId, final String credentialId) throws ApplicationException {
		if (tenantId == null || tenantId.isEmpty()) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_TENANT_ID_IS_NULL);
		}
		if (electionEventId == null || electionEventId.isEmpty()) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_ELECTION_EVENT_ID_IS_NULL);
		}
		if (credentialId == null || credentialId.isEmpty()) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_CREDENTIAL_ID_IS_NULL);
		}
	}

	@Path("/tenant/{tenantId}/electionevent/{electionEventId}/chain/validate")
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response validateCertificateChain(
			@PathParam(PARAMETER_VALUE_TENANT_ID)
			final String tenantId,
			@PathParam(PARAMETER_VALUE_ELECTION_EVENT_ID)
			final String electionEventId,
			@NotNull
			final CertificateChainValidationRequest certificateChainValidationRequest) throws JsonProcessingException {

		// validate certificate chain
		final ValidationResult certChainValidationResult = certificateChainValidationSerivce
				.validate(tenantId, electionEventId, certificateChainValidationRequest.getCertificateContent());

		final String json = objectMapper.writeValueAsString(certChainValidationResult);

		if (certChainValidationResult.isResult()) {
			LOGGER.info("Requested certificate chain verification has passed: VALID CHAIN.");
		} else {
			LOGGER.warn("Requested certificate chain verification has not passed: INVALID CHAIN.");
		}

		return Response.ok(json).build();
	}
}
