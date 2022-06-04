/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votermaterial.ws.application.operation;

import java.io.IOException;
import java.net.URI;
import java.util.List;

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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.domain.election.payload.verify.ValidationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.commons.ui.Constants;
import ch.post.it.evoting.votingserver.commons.util.ValidationUtils;
import ch.post.it.evoting.votingserver.votermaterial.domain.model.information.ListPagination;
import ch.post.it.evoting.votingserver.votermaterial.domain.model.information.VoterInformation;
import ch.post.it.evoting.votingserver.votermaterial.domain.model.information.VoterInformationRepository;
import ch.post.it.evoting.votingserver.votermaterial.domain.model.information.VoterInformationService;
import ch.post.it.evoting.votingserver.votermaterial.domain.model.information.VotingCard;
import ch.post.it.evoting.votingserver.votermaterial.domain.model.information.VotingCardList;

/**
 * Web service for creating and getting voter information.
 */
@Path("/informations")
@Stateless
public class VoterInformationResource {

	public static final int LENGTH_IN_CHARS = 16;

	private static final String PATH_GET_VOTER_INFORMATION = "informations/tenant/{tenantId}/electionevent/{electionEventId}/votingcard/{votingCardId}";

	private static final String PARAMETER_VALUE_TENANT_ID = "tenantId";

	private static final String PARAMETER_VALUE_VOTING_CARD_ID = "votingCardId";

	private static final String PARAMETER_VALUE_CREDENTIAL_ID = "credentialId";

	private static final String PARAMETER_VALUE_ELECTION_EVENT_ID = "electionEventId";

	private static final String RESOURCE = "VOTER_INFORMATION";

	private static final String ERROR_CODE_MANDATORY_FIELD = "mandatory.field";

	private static final String CREDENTIAL_ID_IS_NULL = "Credential id is null";

	private static final String VOTING_CARD_ID_IS_NULL = "Voting card id is null";

	private static final String ELECTION_EVENT_ID_IS_NULL = "Election event id is null";

	private static final String TENANT_ID_IS_NULL = "Tenant id is null";

	private static final String QUERY_PARAMETER_SEARCH_WITH_ID = "id";

	private static final String QUERY_PARAMETER_OFFSET = "offset";

	private static final String QUERY_PARAMETER_SIZE = "size";

	private static final Logger LOGGER = LoggerFactory.getLogger(VoterInformationResource.class);

	private static final ObjectMapper objectMapper = DomainObjectMapper.getNewInstance();

	@Inject
	private TrackIdInstance trackIdInstance;

	@EJB
	private VoterInformationRepository voterInformationRepository;

	@Inject
	private VoterInformationService voterInformationService;

	/**
	 * Returns a VoterInformation object which matches with the provided parameters.
	 *
	 * @param tenantId        - the tenant identifier.
	 * @param electionEventId - the election event identifier.
	 * @param credentialId    - the credential identifier.
	 * @param trackingId         - the track id to be used for logging purposes.
	 * @param request         - the http servlet request.
	 * @return a voter information object.
	 * @throws ResourceNotFoundException if resource is not found.
	 * @throws IOException               if there are errors during conversion of vote to json format.
	 * @throws ApplicationException      if the input parameters not satisfies the validations.
	 */
	@Path("/tenant/{tenantId}/electionevent/{electionEventId}/credential/{credentialId}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getVoterInformationsByCredential(
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId,
			@PathParam(PARAMETER_VALUE_TENANT_ID)
			final String tenantId,
			@PathParam(PARAMETER_VALUE_ELECTION_EVENT_ID)
			final String electionEventId,
			@PathParam(PARAMETER_VALUE_CREDENTIAL_ID)
			final String credentialId,
			@Context
			final HttpServletRequest request) throws ResourceNotFoundException, IOException, ApplicationException {
		// set the track id to be logged
		trackIdInstance.setTrackId(trackingId);

		validateInputByCredential(tenantId, electionEventId, credentialId);

		LOGGER.info("Getting the voter information for tenant: {} election event: {} and credential: {}.", tenantId, electionEventId, credentialId);

		// search voter information
		VoterInformation voterInformation = voterInformationRepository
				.findByTenantIdElectionEventIdCredentialId(tenantId, electionEventId, credentialId);

		LOGGER.info("Voter information for tenant: {} election event: {} and credential: {} found.", tenantId, electionEventId, credentialId);

		// return voter information in json format
		String jsonVoterInformation = objectMapper.writeValueAsString(voterInformation);
		return Response.ok().entity(jsonVoterInformation).build();
	}

	/**
	 * Returns a VoterInformation object which matches with the provided parameters.
	 *
	 * @param tenantId        - the tenant identifier.
	 * @param electionEventId - the election event identifier.
	 * @param votingCardId    - the voting card Identifier.
	 * @param trackingId         - the track id to be used for logging purposes.
	 * @param request         - the http servlet request.
	 * @return a voter information object.
	 * @throws ResourceNotFoundException if resource is not found.
	 * @throws IOException               if there are errors during conversion of vote to json format.
	 * @throws ApplicationException      if the input parameters not satisfies the validations.
	 */
	@Path("/tenant/{tenantId}/electionevent/{electionEventId}/votingcard/{votingCardId}")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getVoterInformationsByVotingCard(
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId,
			@PathParam(PARAMETER_VALUE_TENANT_ID)
					String tenantId,
			@PathParam(PARAMETER_VALUE_ELECTION_EVENT_ID)
					String electionEventId,
			@PathParam(PARAMETER_VALUE_VOTING_CARD_ID)
					String votingCardId,
			@Context
					HttpServletRequest request) throws ResourceNotFoundException, IOException, ApplicationException {
		// set the track id to be logged
		trackIdInstance.setTrackId(trackingId);

		validateInputByVotingCard(tenantId, electionEventId, votingCardId);

		LOGGER.info("Getting the voter information for tenant: {} election event: {} and voting card: {}.", tenantId, electionEventId, votingCardId);

		// search voter information
		VoterInformation voterInformation = voterInformationRepository
				.findByTenantIdElectionEventIdVotingCardId(tenantId, electionEventId, votingCardId);

		LOGGER.info("Voter information for tenant: {} election event: {} and voting card: {} found.", tenantId, electionEventId, votingCardId);

		// return voter information in json format
		String jsonVoterInformation = objectMapper.writeValueAsString(voterInformation);
		return Response.ok().entity(jsonVoterInformation).build();
	}

	/**
	 * Creates a set of identifiers of voter information.
	 *
	 * @param voterInformation The information to be stored.
	 * @param trackingId          The track id to be used for logging purposes.
	 * @return The http response of execute the operation. HTTP status code 201 if the request has
	 * succeed.
	 * @throws IllegalArgumentException if there location of the resulting created voter information
	 *                                  resource or any of its parameters is null.
	 * @throws UriBuilderException      if the URI of the resulting created voter information cannot be
	 *                                  constructed.
	 * @throws DuplicateEntryException  if voter information already exists.
	 * @throws ValidationException      if there are errors in json input for voter information.
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response createVoterInformation(
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId,
			@NotNull
			final VoterInformation voterInformation) throws DuplicateEntryException, ValidationException {
		// set the track id to be logged
		trackIdInstance.setTrackId(trackingId);

		// validate input parameter
		ValidationUtils.validate(voterInformation);

		LOGGER.info("Creating the voter information for tenant: {} election event: {} and voting card: {}.", voterInformation.getTenantId(),
				voterInformation.getElectionEventId(), voterInformation.getVotingCardId());

		// save the voter information
		if (voterInformationRepository.save(voterInformation) != null) {
			// create URI for locating the created resource
			UriBuilder uriBuilder = UriBuilder.fromPath(PATH_GET_VOTER_INFORMATION);
			URI uri = uriBuilder.build(voterInformation.getTenantId(), voterInformation.getElectionEventId(), voterInformation.getVotingCardId());

			LOGGER.info("Voter information for tenant: {} election event: {} and voting card: {} created.", voterInformation.getTenantId(),
					voterInformation.getElectionEventId(), voterInformation.getVotingCardId());

			// return the location of resource
			return Response.created(uri).build();
		}
		return Response.noContent().build();
	}

	/**
	 * Gets the voting cards by query.
	 *
	 * @param tenantId        the tenant id
	 * @param electionEventId the election event id
	 * @param votingcardId    the search term
	 * @param offset          the offset
	 * @param sizeLimit       the limit
	 * @param request         the request
	 * @return the voter information by query
	 * @throws IOException               Signals that an I/O exception has occurred.
	 * @throws ResourceNotFoundException the resource not found exception
	 * @throws ApplicationException      the application exception
	 */
	@Path("/secured/tenant/{tenantId}/electionevent/{electionEventId}/votingcards/query")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public Response getVotingCardsByQuery(
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId,
			@PathParam(PARAMETER_VALUE_TENANT_ID)
			final String tenantId,
			@PathParam(PARAMETER_VALUE_ELECTION_EVENT_ID)
			final String electionEventId,
			@QueryParam(QUERY_PARAMETER_SEARCH_WITH_ID)
			final String votingcardId,
			@QueryParam(QUERY_PARAMETER_OFFSET)
			final String offset,
			@QueryParam(QUERY_PARAMETER_SIZE)
			final String sizeLimit,
			@Context
			final HttpServletRequest request) throws IOException, ResourceNotFoundException, ApplicationException {

		// set the track id to be logged
		trackIdInstance.setTrackId(trackingId);

		validateInput(tenantId, electionEventId);

		int offsetAsInt = extractOffset(offset);
		int limitAsInt = extractLimit(sizeLimit);

		// get the voter information base on the tenant, election event and
		// search terms such as id.
		List<VoterInformation> voterInformations = voterInformationService
				.searchVoterInformation(tenantId, electionEventId, votingcardId, offsetAsInt, limitAsInt);

		long countOfTotalVotingCards = voterInformationService.getCountOfVotingCardsForSearchTerms(tenantId, electionEventId, votingcardId);

		// convert to json format
		String jsonVoterMaterial = objectMapper.writeValueAsString(copyVotingCardsInfo(voterInformations, offsetAsInt, limitAsInt, countOfTotalVotingCards));
		return Response.ok().entity(jsonVoterMaterial).build();
	}

	// Copy only voting card info
	private VotingCardList copyVotingCardsInfo(final List<VoterInformation> voterInformations, final int offset, final int limit, final long count) {

		VotingCardList votingCardList = new VotingCardList();

		for (VoterInformation voterInformation : voterInformations) {
			VotingCard votingCard = new VotingCard();
			votingCard.setId(voterInformation.getVotingCardId());
			votingCard.setVotingCardSetId(voterInformation.getVotingCardSetId());
			votingCardList.getVotingCards().add(votingCard);
		}

		ListPagination listPagination = new ListPagination();
		listPagination.setOffset(offset);
		listPagination.setLimit(limit);
		listPagination.setCount(count);
		votingCardList.setPagination(listPagination);

		return votingCardList;
	}

	private void validateInput(final String tenantId, final String electionEventId) throws ApplicationException {
		if (tenantId == null) {
			throw new ApplicationException(TENANT_ID_IS_NULL, RESOURCE, ERROR_CODE_MANDATORY_FIELD, PARAMETER_VALUE_TENANT_ID);
		}
		if (electionEventId == null) {
			throw new ApplicationException(ELECTION_EVENT_ID_IS_NULL, RESOURCE, ERROR_CODE_MANDATORY_FIELD, PARAMETER_VALUE_ELECTION_EVENT_ID);
		}
	}

	private void validateInputByCredential(final String tenantId, final String electionEventId, final String credentialId)
			throws ApplicationException {
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

	private void validateInputByVotingCard(String tenantId, String electionEventId, String votingCardId) throws ApplicationException {
		if (tenantId == null) {
			throw new ApplicationException(TENANT_ID_IS_NULL, RESOURCE, ERROR_CODE_MANDATORY_FIELD, PARAMETER_VALUE_TENANT_ID);
		}
		if (electionEventId == null) {
			throw new ApplicationException(ELECTION_EVENT_ID_IS_NULL, RESOURCE, ERROR_CODE_MANDATORY_FIELD, PARAMETER_VALUE_ELECTION_EVENT_ID);
		}
		if (votingCardId == null) {
			throw new ApplicationException(VOTING_CARD_ID_IS_NULL, RESOURCE, ERROR_CODE_MANDATORY_FIELD, PARAMETER_VALUE_VOTING_CARD_ID);
		}
	}

	// Check offset and convert to number.
	private int extractOffset(final String offset) {

		String validOffset = offset;

		if (offset == null || offset.length() < 1) {
			validOffset = "0";
		}

		return Integer.parseInt(validOffset);
	}

	// Check limit and convert to number.
	private int extractLimit(final String limit) {

		String validLimit = limit;

		if (limit == null || limit.length() < 1) {
			validLimit = "10";
		}

		return Integer.parseInt(validLimit);
	}
}
