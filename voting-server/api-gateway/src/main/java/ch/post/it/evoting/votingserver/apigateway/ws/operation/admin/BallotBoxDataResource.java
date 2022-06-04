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
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote.admin.ElectionInformationAdminClient;
import ch.post.it.evoting.votingserver.apigateway.ws.RestApplication;
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
 * Web service for handling ballot box data information.
 */
@Stateless(name = "ag-BallotBoxDataResource")
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
@Path(BallotBoxDataResource.RESOURCE_PATH)
public class BallotBoxDataResource {

	static final String ADD_BALLOT_BOX_INFORMATION = "tenant/{tenantId}/electionevent/{electionEventId}/ballotbox/{ballotBoxId}/adminboard/{adminBoardId}";

	static final String RESOURCE_PATH = "/ei/ballotboxdata";

	private static final String QUERY_PARAMETER_TENANT_ID = "tenantId";

	private static final String QUERY_PARAMETER_ELECTION_EVENT_ID = "electionEventId";

	private static final String QUERY_PARAMETER_BALLOT_BOX_ID = "ballotBoxId";

	private static final String QUERY_PARAMETER_ADMIN_BOARD_ID = "adminBoardId";

	private static final PropertiesFileReader PROPERTIES = PropertiesFileReader.getInstance();

	static final String BALLOT_BOX_DATA_PATH = PROPERTIES.getPropertyValue("BALLOT_BOX_DATA_PATH");
	private static final Logger LOGGER = LoggerFactory.getLogger(BallotBoxDataResource.class);
	private final XForwardedForFactory xForwardedForFactory = XForwardedForFactoryImpl.getInstance();
	private ElectionInformationAdminClient electionInformationAdminClient;
	private TrackIdGenerator trackIdGenerator;

	@Inject
	BallotBoxDataResource(ElectionInformationAdminClient electionInformationAdminClient, TrackIdGenerator trackIdGenerator) {
		this.electionInformationAdminClient = electionInformationAdminClient;
		this.trackIdGenerator = trackIdGenerator;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(RestApplication.MEDIA_TYPE_JSON_UTF_8)
	@Path(ADD_BALLOT_BOX_INFORMATION)
	public Response addBallotBoxInformation(
			@PathParam(QUERY_PARAMETER_TENANT_ID)
			final String tenantId,
			@PathParam(QUERY_PARAMETER_ELECTION_EVENT_ID)
			final String electionEventId,
			@PathParam(QUERY_PARAMETER_BALLOT_BOX_ID)
			final String ballotBoxId,
			@PathParam(QUERY_PARAMETER_ADMIN_BOARD_ID)
			final String adminBoardId,
			@NotNull
			final InputStream info,
			@Context
			final HttpServletRequest request) {
		String xForwardedFor = xForwardedForFactory.newXForwardedFor(request);
		final String trackingId = trackIdGenerator.generate();

		RequestBody body = new InputStreamTypedOutput(MediaType.APPLICATION_JSON, info);

		try (ResponseBody responseBody = RetrofitConsumer.processResponse(electionInformationAdminClient
				.addBallotBoxInformation(BALLOT_BOX_DATA_PATH, tenantId, electionEventId, ballotBoxId, adminBoardId, xForwardedFor,
						trackingId, body))) {
			return Response.ok().build();
		} catch (RetrofitException rfE) {
			LOGGER.error("Error trying to process ballot boxes.", rfE);
			return Response.status(rfE.getHttpCode()).build();
		}
	}
}
