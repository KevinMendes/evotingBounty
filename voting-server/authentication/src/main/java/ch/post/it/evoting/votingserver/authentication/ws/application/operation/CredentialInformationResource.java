/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.ws.application.operation;

import java.io.IOException;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptolib.certificates.utils.CryptographicOperationException;
import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.domain.election.model.authentication.AuthenticationContent;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication.CredentialInformationFactory;
import ch.post.it.evoting.votingserver.authentication.services.domain.service.authenticationcontent.AuthenticationContentService;
import ch.post.it.evoting.votingserver.commons.beans.authentication.CredentialInformation;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationExceptionMessages;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.commons.ui.Constants;

/**
 * The end point for authentication information resource.
 */
@Path("/informations")
@Stateless
public class CredentialInformationResource {

	private static final String PARAMETER_VALUE_TENANT_ID = "tenantId";

	private static final String PARAMETER_VALUE_CREDENTIAL_ID = "credentialId";

	private static final String PARAMETER_VALUE_ELECTION_EVENT_ID = "electionEventId";

	@Inject
	private TrackIdInstance trackIdInstance;

	@Inject
	private CredentialInformationFactory credentialInformationFactory;

	@Inject
	private AuthenticationContentService authenticationContentService;

	/**
	 * Read credential (authentication) information for a specific credential id
	 *
	 * @param tenantId        - the identifier of the tenant.
	 * @param electionEventId - the identifier of the election event.
	 * @param credentialId    - the identifier of the credential.
	 * @param request         - the http servlet request.
	 * @return If the operation is successfully performed, returns a response with HTTP status code 200 and the credential information in JSON format.
	 * @throws ApplicationException            if one of the input parameters is not valid.
	 * @throws ResourceNotFoundException       if there is no voter material found.
	 * @throws IOException                     if there are some error during conversion from object to json format.
	 * @throws CryptographicOperationException if there is a cryptographic error during the credential generation
	 */
	@GET
	@Path("/tenant/{tenantId}/electionevent/{electionEventId}/credential/{credentialId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAuthenticationInformation(
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId,
			@PathParam(PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@PathParam(PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@PathParam(PARAMETER_VALUE_CREDENTIAL_ID)
					String credentialId,
			@Context
					HttpServletRequest request) throws ApplicationException, ResourceNotFoundException, IOException, CryptographicOperationException {
		// set the track id to be logged
		trackIdInstance.setTrackId(trackingId);

		validateInput(tenantId, electionEventId, credentialId);

		AuthenticationContent authenticationContent = authenticationContentService.getAuthenticationContent(tenantId, electionEventId);

		// create new credential information
		CredentialInformation credentialInformation = credentialInformationFactory
				.buildCredentialInformation(tenantId, electionEventId, credentialId, authenticationContent.getChallengeLength());

		final ObjectMapper objectMapper = DomainObjectMapper.getNewInstance();
		return Response.ok(objectMapper.writeValueAsString(credentialInformation)).build();
	}

	private void validateInput(String tenantId, String electionEventId, String credentialId) throws ApplicationException {
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
}
