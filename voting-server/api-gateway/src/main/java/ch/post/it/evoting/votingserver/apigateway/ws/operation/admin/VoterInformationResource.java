/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.apigateway.ws.operation.admin;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote.admin.VoterMaterialAdminClient;
import ch.post.it.evoting.votingserver.apigateway.ws.RestApplication;
import ch.post.it.evoting.votingserver.apigateway.ws.proxy.XForwardedForFactory;
import ch.post.it.evoting.votingserver.apigateway.ws.proxy.XForwardedForFactoryImpl;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RestClientInterceptor;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitConsumer;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdGenerator;
import ch.post.it.evoting.votingserver.commons.util.PropertiesFileReader;

/**
 * Web service for handling voter information
 */
@Stateless(name = "ag-VoterInformationResource")
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
@Path(VoterInformationResource.RESOURCE_PATH)
public class VoterInformationResource {

	static final String GET_VOTING_CARDS_BY_QUERY = "tenant/{tenantId}/electionevent/{electionEventId}/votingcards/query";

	static final String RESOURCE_PATH = "/vm/informations";

	private static final String PATH_PARAMETER_TENANT_ID = "tenantId";

	private static final String PATH_PARAMETER_ELECTION_EVENT_ID = "electionEventId";

	private static final String QUERY_PARAMETER_SEARCH_WITH_VOTING_CARD_ID = "id";

	private static final String QUERY_PARAMETER_OFFSET = "offset";

	private static final String QUERY_PARAMETER_SIZE = "size";

	private static final PropertiesFileReader PROPERTIES = PropertiesFileReader.getInstance();

	static final String CREDENTIAL_INFORMATION_PATH = PROPERTIES.getPropertyValue("CREDENTIAL_INFORMATION_PATH");
	private static final Logger LOGGER = LoggerFactory.getLogger(VoterInformationResource.class);
	private final XForwardedForFactory xForwardedForFactory = XForwardedForFactoryImpl.getInstance();
	private final VoterMaterialAdminClient voterMaterialAdminClient;
	private final TrackIdGenerator trackIdGenerator;

	@Inject
	VoterInformationResource(VoterMaterialAdminClient voterMaterialAdminClient, TrackIdGenerator trackIdGenerator) {
		this.voterMaterialAdminClient = voterMaterialAdminClient;
		this.trackIdGenerator = trackIdGenerator;
	}

	/**
	 * Search voting cards.
	 *
	 * @param tenantId         the tenant id
	 * @param electionEventId  the election event id
	 * @param termVotingcardId the votingcard id
	 * @param offset           the offset
	 * @param sizeLimit        the size limit
	 * @return the response
	 */
	@GET
	@Path(GET_VOTING_CARDS_BY_QUERY)
	@Produces(RestApplication.MEDIA_TYPE_JSON_UTF_8)
	public Response getVotingCardsByQuery(
			@PathParam(PATH_PARAMETER_TENANT_ID)
			final String tenantId,
			@PathParam(PATH_PARAMETER_ELECTION_EVENT_ID)
			final String electionEventId,
			@QueryParam(QUERY_PARAMETER_SEARCH_WITH_VOTING_CARD_ID)
			final String termVotingcardId,
			@QueryParam(QUERY_PARAMETER_OFFSET)
			final String offset,
			@QueryParam(QUERY_PARAMETER_SIZE)
			final String sizeLimit,
			@Context
			final HttpServletRequest request) {
		String xForwardedFor = xForwardedForFactory.newXForwardedFor(request);
		String trackingId = trackIdGenerator.generate();

		String signature = request.getHeader(RestClientInterceptor.HEADER_SIGNATURE);
		String originator = request.getHeader(RestClientInterceptor.HEADER_ORIGINATOR);

		try {
			JsonObject processResponse = RetrofitConsumer.processResponse(voterMaterialAdminClient
					.getVotingCardsByQuery(CREDENTIAL_INFORMATION_PATH, tenantId, electionEventId, termVotingcardId, offset, sizeLimit, xForwardedFor,
							trackingId, originator, signature));
			return Response.ok().entity(processResponse.toString()).build();
		} catch (RetrofitException rfE) {
			LOGGER.error("Error trying to get voting cards by query.", rfE);
			return Response.status(rfE.getHttpCode()).build();
		}
	}
}
