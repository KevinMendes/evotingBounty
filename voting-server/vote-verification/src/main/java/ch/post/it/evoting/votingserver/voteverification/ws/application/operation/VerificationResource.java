/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.voteverification.ws.application.operation;

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
import ch.post.it.evoting.votingserver.voteverification.domain.model.verification.Verification;
import ch.post.it.evoting.votingserver.voteverification.domain.model.verification.VerificationRepository;

/**
 * Service which offers the possibility of creating voter verifications in the system.
 */
@Path(VerificationResource.RESOURCE_PATH)
@Stateless
public class VerificationResource {

	static final String RESOURCE_PATH = "/verifications";

	static final String GET_VOTER_INFORMATION_PATH = "/tenant/{tenantId}/electionevent/{electionEventId}/verificationcard/{verificationCardId}";

	static final String PARAMETER_VALUE_TENANT_ID = "tenantId";

	static final String PARAMETER_VALUE_VERIFICATION_CARD_ID = "verificationCardId";

	static final String PARAMETER_VALUE_ELECTION_EVENT_ID = "electionEventId";

	private static final String PATH_GET_VOTER_MATERIALS = "verifications/{verificationId}";

	private static final String RESOURCE = "VERIFICATION";

	private static final String ERROR_CODE_MANDATORY_FIELD = "mandatory.field";

	private static final String VERIFIATION_CARD_ID_IS_NULL = "Verification card id is null";

	private static final String ELECTION_EVENT_ID_IS_NULL = "Election event id is null";

	private static final String TENANT_ID_IS_NULL = "Tenant id is null";
	private static final Logger LOGGER = LoggerFactory.getLogger(VerificationResource.class);

	@EJB
	private VerificationRepository voterVerificationRepository;
	// The track id instance
	@Inject
	private TrackIdInstance tackIdInstance;

	/**
	 * Creates a set of identifiers of voter information.
	 *
	 * @param voterVerification - the information to be stored.
	 * @param trackingId        - the track id to be used for logging purposes.
	 * @param request           - the http servlet request.
	 * @return The http response of execute the operation. HTTP status code 201 if the request has succeed.
	 * @throws DuplicateEntryException  Duplicate entry in database for this resource.
	 * @throws UriBuilderException      if it is not possible to build the URI of the created resource.
	 * @throws IllegalArgumentException if it is not possible to build the URI of the created resource.
	 * @throws SemanticErrorException   Errors during validation of input parameters.
	 * @throws SyntaxErrorException     Errors during validation of input parameters.
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createVoterVerification(
			@NotNull
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId,
			@NotNull
					Verification voterVerification,
			@Context
					HttpServletRequest request) throws DuplicateEntryException, SyntaxErrorException, SemanticErrorException {
		// set the track id to be logged
		tackIdInstance.setTrackId(trackingId);

		// validate voter verification
		ValidationUtils.validate(voterVerification);

		LOGGER.info("Creating the voter verification for tenant: {}, election event: {} and verification card: {}.", voterVerification.getTenantId(),
				voterVerification.getElectionEventId(), voterVerification.getVerificationCardId());

		// store the voter verification
		if (voterVerificationRepository.save(voterVerification) != null) {
			UriBuilder uriBuilder = UriBuilder.fromPath(PATH_GET_VOTER_MATERIALS);
			URI uri = uriBuilder.build(voterVerification.getVerificationCardId());

			LOGGER.info("Voter verification for tenant: {}, election event: {} and verification card: {} created.", voterVerification.getTenantId(),
					voterVerification.getElectionEventId(), voterVerification.getVerificationCardId());

			// return location of resource created
			return Response.created(uri).build();
		}
		return Response.noContent().build();
	}

	/**
	 * Returns a credential and verification data for a given tenant, election event and verification card.
	 *
	 * @param trackingId         The track id to be used for logging purposes.
	 * @param tenantId           The tenant identifier.
	 * @param electionEventId    The election event identifier.
	 * @param verificationCardId The verification card identifier.
	 * @param request            The http servlet request.
	 * @return a credential and verification data.
	 * @throws ResourceNotFoundException If voter information is not found.
	 * @throws IOException               Conversion exception from object to json. material data. For example, both credential and verification data
	 *                                   are null.
	 * @throws ApplicationException      if one of the input parameters is null.
	 */
	@Path(GET_VOTER_INFORMATION_PATH)
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getVoterInformation(
			@NotNull
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId,
			@PathParam(PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@PathParam(PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@PathParam(PARAMETER_VALUE_VERIFICATION_CARD_ID)
					String verificationCardId,
			@Context
					HttpServletRequest request) throws IOException, ResourceNotFoundException, ApplicationException {

		// set the track id to be logged
		tackIdInstance.setTrackId(trackingId);

		validateInput(tenantId, electionEventId, verificationCardId);

		Verification verification = voterVerificationRepository
				.findByTenantIdElectionEventIdVerificationCardId(tenantId, electionEventId, verificationCardId);

		String jsonVoterVerification = DomainObjectMapper.getNewInstance().writeValueAsString(verification);
		return Response.ok().entity(jsonVoterVerification).build();
	}

	private void validateInput(String tenantId, String electionEventId, String verificationCardId) throws ApplicationException {
		if (tenantId == null) {
			throw new ApplicationException(TENANT_ID_IS_NULL, RESOURCE, ERROR_CODE_MANDATORY_FIELD, PARAMETER_VALUE_TENANT_ID);
		}
		if (electionEventId == null) {
			throw new ApplicationException(ELECTION_EVENT_ID_IS_NULL, RESOURCE, ERROR_CODE_MANDATORY_FIELD, PARAMETER_VALUE_ELECTION_EVENT_ID);
		}
		if (verificationCardId == null) {
			throw new ApplicationException(VERIFIATION_CARD_ID_IS_NULL, RESOURCE, ERROR_CODE_MANDATORY_FIELD, PARAMETER_VALUE_VERIFICATION_CARD_ID);
		}
	}
}
