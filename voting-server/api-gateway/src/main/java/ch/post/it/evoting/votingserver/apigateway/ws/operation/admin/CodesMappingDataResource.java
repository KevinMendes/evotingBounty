/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.apigateway.ws.operation.admin;

import java.io.InputStream;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote.admin.VoteVerificationAdminClient;
import ch.post.it.evoting.votingserver.apigateway.ws.proxy.XForwardedForFactory;
import ch.post.it.evoting.votingserver.apigateway.ws.proxy.XForwardedForFactoryImpl;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.InputStreamTypedOutput;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitConsumer;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdGenerator;
import ch.post.it.evoting.votingserver.commons.util.PropertiesFileReader;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;

/**
 * Web service for handling the codes mapping table data
 */
@Stateless(name = "ag-CodesMappingResource")
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
@Path(CodesMappingDataResource.RESOURCE_PATH)
public class CodesMappingDataResource {

	static final String SAVE_CODES_MAPPING = "tenant/{tenantId}/electionevent/{electionEventId}/verificationcardset/{verificationCardSetId}/adminboard/{adminBoardId}";

	static final String RESOURCE_PATH = "/vv/codesmappingdata";

	private static final String QUERY_PARAMETER_TENANT_ID = "tenantId";

	private static final String QUERY_PARAMETER_ELECTION_EVENT_ID = "electionEventId";

	private static final String QUERY_PARAMETER_VERIFICATION_CARD_SET_ID = "verificationCardSetId";

	private static final PropertiesFileReader PROPERTIES = PropertiesFileReader.getInstance();

	static final String CODES_MAPPING_DATA_PATH = PROPERTIES.getPropertyValue("CODES_MAPPING_DATA_PATH");

	private static final String QUERY_PARAMETER_ADMIN_BOARD_ID = "adminBoardId";
	private static final Logger LOGGER = LoggerFactory.getLogger(CodesMappingDataResource.class);
	private final XForwardedForFactory xForwardedForFactory = XForwardedForFactoryImpl.getInstance();
	private final VoteVerificationAdminClient voteVerificationAdminClient;
	private final TrackIdGenerator trackIdGenerator;

	@Inject
	CodesMappingDataResource(VoteVerificationAdminClient voteVerificationAdminClient, TrackIdGenerator trackIdGenerator) {
		this.voteVerificationAdminClient = voteVerificationAdminClient;
		this.trackIdGenerator = trackIdGenerator;
	}

	@POST
	@Path(SAVE_CODES_MAPPING)
	@Consumes("text/csv")
	public Response saveCodesMappingData(
			@PathParam(QUERY_PARAMETER_TENANT_ID)
			final String tenantId,
			@PathParam(QUERY_PARAMETER_ELECTION_EVENT_ID)
			final String electionEventId,
			@PathParam(QUERY_PARAMETER_VERIFICATION_CARD_SET_ID)
			final String verificationCardSetId,
			@PathParam(QUERY_PARAMETER_ADMIN_BOARD_ID)
			final String adminBoardId,
			@NotNull
			final InputStream data,
			@Context
			final HttpServletRequest request) {
		String xForwardedFor = xForwardedForFactory.newXForwardedFor(request);
		String trackingId = trackIdGenerator.generate();
		RequestBody body = new InputStreamTypedOutput("text/csv", data);

		try (ResponseBody responseBody = RetrofitConsumer.processResponse(voteVerificationAdminClient
				.saveCodesMappingData(CODES_MAPPING_DATA_PATH, tenantId, electionEventId, verificationCardSetId, adminBoardId, xForwardedFor,
						trackingId, body))) {

			return Response.ok().build();
		} catch (RetrofitException rfE) {
			LOGGER.error("Error trying to save codes mapping data.", rfE);
			return Response.status(rfE.getHttpCode()).build();
		}
	}
}
