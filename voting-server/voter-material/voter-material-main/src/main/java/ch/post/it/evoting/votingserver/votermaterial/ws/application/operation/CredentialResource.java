/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votermaterial.ws.application.operation;

import java.io.IOException;
import java.net.URI;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.SemanticErrorException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.SyntaxErrorException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.commons.ui.Constants;
import ch.post.it.evoting.votingserver.commons.util.ValidationUtils;
import ch.post.it.evoting.votingserver.votermaterial.domain.model.credential.Credential;
import ch.post.it.evoting.votingserver.votermaterial.domain.model.credential.CredentialDataService;
import ch.post.it.evoting.votingserver.votermaterial.domain.model.credential.CredentialRepository;
import ch.post.it.evoting.votingserver.votermaterial.domain.model.information.VoterInformationRepository;

/**
 * Service which offers the possibility of creating credentials in the system.
 */
@Path("/credentials")
@Stateless
public class CredentialResource {

	private static final String PATH_GET_VOTER_MATERIALS = "credentials/{credentialId}/";

	private static final String PARAMETER_VALUE_TENANT_ID = "tenantId";

	private static final String PARAMETER_VALUE_CREDENTIAL_ID = "credentialId";

	private static final String PARAMETER_VALUE_ELECTION_EVENT_ID = "electionEventId";

	private static final String RESOURCE = "CREDENTIAL";

	private static final String ERROR_CODE_MANDATORY_FIELD = "mandatory.field";

	private static final String CREDENTIAL_ID_IS_NULL = "Voting card id is null";

	private static final String ELECTION_EVENT_ID_IS_NULL = "Election event id is null";

	private static final String TENANT_ID_IS_NULL = "Tenant id is null";

	private static final Logger LOGGER = LoggerFactory.getLogger(CredentialResource.class);

	@Inject
	private TrackIdInstance trackIdInstance;

	@EJB
	private CredentialRepository credentialRepository;

	@EJB
	private VoterInformationRepository voterInformationRepository;

	@Inject
	private CredentialDataService credentialDataService;

	/**
	 * Creates a set of identifiers of voter information.
	 *
	 * @param voterCredential - the information to be stored.
	 * @param trackId         - the track id to be used for logging purposes.
	 * @return The http response of execute the operation. HTTP status code 201 if the request has
	 * succeed.
	 * @throws DuplicateEntryException  If voter credential already exists
	 * @throws IllegalArgumentException if there location of the resulting created voter information
	 *                                  resource or any of its parameters is null.
	 * @throws UriBuilderException      if the URI of the resulting created voter information cannot be
	 *                                  constructed.
	 * @throws SemanticErrorException   if there are semantic errors in json input for voter credential.
	 * @throws SyntaxErrorException     if there are syntax errors in json input for voter credential.
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createVoterCredential(
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId,
			@NotNull
					Credential voterCredential) throws DuplicateEntryException, SyntaxErrorException, SemanticErrorException {
		// set the track id to be logged
		trackIdInstance.setTrackId(trackingId);

		// validate input parameter
		ValidationUtils.validate(voterCredential);

		LOGGER.info("Creating the voter credential for tenant: {} and credentialId: {}.", voterCredential.getTenantId(),
				voterCredential.getCredentialId());

		// save the voter credential
		if (credentialRepository.save(voterCredential) != null) {
			// create URI for locating the created resource
			UriBuilder uriBuilder = UriBuilder.fromPath(PATH_GET_VOTER_MATERIALS);
			URI uri = uriBuilder.build(voterCredential.getCredentialId());

			LOGGER.info("Voter credential for tenant: {} and credentialId: {} created.", voterCredential.getTenantId(),
					voterCredential.getCredentialId());

			// return the location of resource
			return Response.created(uri).build();
		}
		return Response.noContent().build();
	}

	/**
	 * Returns a credential data for a given tenant, election event and voting card.
	 *
	 * @param tenantId        The tenant identifier.
	 * @param electionEventId The election event identifier.
	 * @param credentialId    The credential identifier.
	 * @param trackId         The track id to be used for logging purposes.
	 * @param request         - the http servlet request.
	 * @return a credential data.
	 * @throws ResourceNotFoundException If voter information is not found.
	 * @throws IOException               Conversion exception from object to json.
	 * @throws ApplicationException      if one of the input parameters is null.
	 */
	@Path("/tenant/{tenantId}/electionevent/{electionEventId}/credential/{credentialId}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCredential(
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId,
			@PathParam(PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@PathParam(PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@PathParam(PARAMETER_VALUE_CREDENTIAL_ID)
					String credentialId,
			@Context
					HttpServletRequest request) throws IOException, ResourceNotFoundException, ApplicationException {

		// set the track id to be logged
		trackIdInstance.setTrackId(trackingId);

		validateInput(tenantId, electionEventId, credentialId);

		// get the credential data base on the tenant, election event and
		// credential id
		Credential credential = credentialDataService.getCredentialData(tenantId, electionEventId, credentialId);

		// convert to json format
		String jsonCredential = DomainObjectMapper.getNewInstance().writeValueAsString(credential);
		return Response.ok().entity(jsonCredential).build();
	}

	private void validateInput(String tenantId, String electionEventId, String credentialId) throws ApplicationException {
		if (tenantId == null) {
			throw new ApplicationException(TENANT_ID_IS_NULL, RESOURCE, ERROR_CODE_MANDATORY_FIELD, PARAMETER_VALUE_TENANT_ID);
		}
		if (electionEventId == null) {
			throw new ApplicationException(ELECTION_EVENT_ID_IS_NULL, RESOURCE, ERROR_CODE_MANDATORY_FIELD, PARAMETER_VALUE_ELECTION_EVENT_ID);
		}
		if (credentialId == null) {
			throw new ApplicationException(CREDENTIAL_ID_IS_NULL, RESOURCE, ERROR_CODE_MANDATORY_FIELD, PARAMETER_VALUE_CREDENTIAL_ID);
		}
	}
}
