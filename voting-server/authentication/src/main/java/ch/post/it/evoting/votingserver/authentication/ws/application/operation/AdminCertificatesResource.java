/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.ws.application.operation;

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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.votingserver.authentication.services.domain.service.certificate.AdminBoardCertificateService;
import ch.post.it.evoting.votingserver.commons.beans.authentication.AdminBoardCertificates;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationExceptionMessages;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.commons.ui.Constants;

/**
 * The end point for authentication information resource.
 */
@Path("/certificates")
@Stateless
public class AdminCertificatesResource {

	private static final String PARAMETER_VALUE_TENANT_ID = "tenantId";

	private static final String PARAMETER_VALUE_ELECTION_EVENT_ID = "electionEventId";

	@Inject
	private TrackIdInstance trackIdInstance;

	@Inject
	private AdminBoardCertificateService adminBoardCertificateService;

	/**
	 * Get the admin board certificate data for a specific election event
	 *
	 * @param tenantId        - the identifier of the tenant.
	 * @param electionEventId - the identifier of the election event.
	 * @param trackingId      - the track id to be used for logging purposes.
	 * @param request         - the http servlet request.
	 * @return If the operation is successfully performed, returns a response with HTTP status code 200 and the admin board certificate in JSON
	 * format.
	 * @throws ApplicationException      if one of the input parameters is not valid.
	 * @throws ResourceNotFoundException if the resource cannot be found
	 * @throws JsonProcessingException   if there was an error during processing of the JSON file
	 */
	@GET
	@Path("/tenant/{tenantId}/electionevent/{electionEventId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCertificatesInformation(
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
			HttpServletRequest request) throws ApplicationException, ResourceNotFoundException, JsonProcessingException {
		trackIdInstance.setTrackId(trackingId);

		validateInput(tenantId, electionEventId);

		final AdminBoardCertificates certs = adminBoardCertificateService.getAdminBoardAndTenantCA(tenantId, electionEventId);

		final ObjectMapper objectMapper = DomainObjectMapper.getNewInstance();
		return Response.ok(objectMapper.writeValueAsString(certs)).build();
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
