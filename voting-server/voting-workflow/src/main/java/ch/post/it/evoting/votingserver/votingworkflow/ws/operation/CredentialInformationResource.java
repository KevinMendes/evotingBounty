/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.ws.operation;

import java.io.IOException;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
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
import ch.post.it.evoting.votingserver.commons.beans.authentication.CredentialInformation;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationExceptionMessages;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.commons.ui.Constants;
import ch.post.it.evoting.votingserver.commons.util.JsonUtils;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.authentication.CredentialInformationRepository;

/**
 * The end point for authentication information resource.
 */
@Path("/informations")
@Stateless(name = "vw-CredentialInformationResource")
public class CredentialInformationResource {

	// The name of the parameter value tenant id.
	private static final String PARAMETER_VALUE_TENANT_ID = "tenantId";

	// The name of the parameter value credential id.
	private static final String PARAMETER_CREDENTIAL_ID = "credentialId";

	// The name of the parameter value election event id.
	private static final String PARAMETER_VALUE_ELECTION_EVENT_ID = "electionEventId";

	private static final String CERTIFICATES = "certificates";

	private static final String CREDENTIAL_DATA = "credentialData";

	private static final String SERVER_CHALLANGE_MESSAGE = "serverChallengeMessage";

	private static final String CERTS_SIGNATURE = "certificatesSignature";

	@EJB
	private CredentialInformationRepository credentialInformationRepository;

	@Inject
	private TrackIdInstance trackIdInstance;

	/**
	 * Reads authentication information requested for the tenant identified by tenantId, election event and voting card.
	 *
	 * @param tenantId        - the identifier of the tenant.
	 * @param electionEventId - the identifier of the election event.
	 * @param credentialId    - the identifier of the credential.
	 * @return If the operation is successfully performed, returns a response with HTTP status code 200 and the authentication information in json
	 * format.
	 * @throws ApplicationException      if one of the input parameters is not valid.
	 * @throws ResourceNotFoundException if there is no voter material found.
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
			@PathParam(PARAMETER_CREDENTIAL_ID)
					String credentialId,
			@Context
					HttpServletRequest request) throws ApplicationException, ResourceNotFoundException, IOException {

		// set the track id to be logged
		trackIdInstance.setTrackId(trackingId);

		validateInput(tenantId, electionEventId, credentialId);

		CredentialInformation credentialInformation = credentialInformationRepository
				.findByTenantElectionEventCredential(tenantId, electionEventId, credentialId);

		// convert from json to object response
		JsonObjectBuilder credentialInfoJson = Json.createObjectBuilder();

		ObjectMapper objectMapper = DomainObjectMapper.getNewInstance();
		JsonObject certificatesJson = JsonUtils.getJsonObject(credentialInformation.getCertificates());
		JsonObject credentialDataJson = JsonUtils.getJsonObject(objectMapper.writeValueAsString(credentialInformation.getCredentialData()));
		JsonObject serverChallengeMessageJson = JsonUtils
				.getJsonObject(objectMapper.writeValueAsString(credentialInformation.getServerChallengeMessage()));
		credentialInfoJson.add(CERTIFICATES, certificatesJson).add(CREDENTIAL_DATA, credentialDataJson)
				.add(SERVER_CHALLANGE_MESSAGE, serverChallengeMessageJson);
		credentialInfoJson.add(CERTS_SIGNATURE, credentialInformation.getCertificatesSignature());

		JsonObject result = credentialInfoJson.build();

		// find authentication information
		return Response.ok().entity(result.toString()).build();

	}

	// Does a basic validation of the input. In case something is wrong, just
	// throws an exception.
	private void validateInput(String tenantId, String electionEventId, String credentialId) throws ApplicationException {
		if (tenantId == null || "".equals(tenantId)) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_TENANT_ID_IS_NULL);
		}
		if (electionEventId == null || "".equals(electionEventId)) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_ELECTION_EVENT_ID_IS_NULL);
		}
		if (credentialId == null || "".equals(credentialId)) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_CREDENTIAL_ID_IS_NULL);
		}
	}

}
