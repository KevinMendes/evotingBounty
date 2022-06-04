/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.ws.application.operation;

import java.io.IOException;

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

import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.domain.election.model.authentication.AuthenticationContent;
import ch.post.it.evoting.domain.election.model.authentication.AuthenticationData;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication.AuthenticationCerts;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication.CredentialInformationFactory;
import ch.post.it.evoting.votingserver.authentication.services.domain.service.authenticationcontent.AuthenticationContentService;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationExceptionMessages;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.commons.ui.Constants;

@Path(AuthenticationDataResource.RESOURCE_PATH)
public class AuthenticationDataResource {

	static final String RESOURCE_PATH = "/contents";

	static final String GET_AUTHENTICATION_INFORMATION_PATH = "tenant/{tenantId}/electionevent/{electionEventId}";

	static final String PARAMETER_VALUE_TENANT_ID = "tenantId";

	static final String PARAMETER_VALUE_CREDENTIAL_ID = "credentialId";

	static final String PARAMETER_VALUE_ELECTION_EVENT_ID = "electionEventId";

	@Inject
	private TrackIdInstance trackIdInstance;

	@Inject
	private CredentialInformationFactory credentialInformationFactory;

	@Inject
	private AuthenticationContentService authenticationContentService;

	/**
	 * Get the authentication information for a specific election event.
	 *
	 * @param trackingId      - the track id to be used for logging purposes.
	 * @param tenantId        - the identifier of the tenant.
	 * @param electionEventId - the identifier of the election event.
	 * @param request         - the http servlet request.
	 * @return If the operation is successfully performed, returns a response with HTTP status code 200 and the authentication information in json
	 * format.
	 * @throws ApplicationException      if one of the input parameters is not valid.
	 * @throws ResourceNotFoundException if the resource is not found
	 * @throws IOException               if there are some error during conversion from object to json format.
	 */
	@GET
	@Path(GET_AUTHENTICATION_INFORMATION_PATH)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAuthenticationInformation(
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
			final
			String trackingId,
			@PathParam(PARAMETER_VALUE_TENANT_ID)
			final
			String tenantId,
			@PathParam(PARAMETER_VALUE_ELECTION_EVENT_ID)
			final
			String electionEventId,
			@Context
			final
			HttpServletRequest request) throws ApplicationException, ResourceNotFoundException, IOException {
		trackIdInstance.setTrackId(trackingId);

		validateInput(tenantId, electionEventId);

		final AuthenticationContent authenticationContent = authenticationContentService.getAuthenticationContent(tenantId, electionEventId);

		// gets the authentication certificates
		final AuthenticationCerts authenticationCerts = credentialInformationFactory.buildAuthenticationCertificates(tenantId, electionEventId);

		final AuthenticationData result = new AuthenticationData();
		result.setAuthenticationContent(authenticationContent);
		result.setCertificates(authenticationCerts.getJson());

		final ObjectMapper objectMapper = DomainObjectMapper.getNewInstance();
		return Response.ok(objectMapper.writeValueAsString(result)).build();
	}

	private void validateInput(final String tenantId, final String electionEventId) throws ApplicationException {
		if (tenantId == null || tenantId.isEmpty()) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_TENANT_ID_IS_NULL);
		}
		if (electionEventId == null || electionEventId.isEmpty()) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_ELECTION_EVENT_ID_IS_NULL);
		}
	}

}
