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
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote.admin.ElectionInformationAdminClient;
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
 * Web service for handling ballot data resources.
 */
@Stateless(name = "ag-BallotDataResource")
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
@Path(BallotDataResource.RESOURCE_PATH)
public class BallotDataResource {

	static final String SAVE_BALLOT_DATA = "tenant/{tenantId}/electionevent/{electionEventId}/ballot/{ballotId}/adminboard/{adminBoardId}";

	static final String RESOURCE_PATH = "/ei/ballotdata";

	private static final String QUERY_PARAMETER_TENANT_ID = "tenantId";

	private static final String QUERY_PARAMETER_ELECTION_EVENT_ID = "electionEventId";

	private static final String QUERY_PARAMETER_BALLOT_ID = "ballotId";

	private static final PropertiesFileReader PROPERTIES = PropertiesFileReader.getInstance();

	static final String BALLOTDATA_PATH = PROPERTIES.getPropertyValue("BALLOTDATA_PATH");

	private static final String QUERY_PARAMETER_ADMIN_BOARD_ID = "adminBoardId";
	private static final Logger LOGGER = LoggerFactory.getLogger(BallotDataResource.class);
	private final XForwardedForFactory xForwardedForFactory = XForwardedForFactoryImpl.getInstance();
	private final ElectionInformationAdminClient electionInformationAdminClient;
	private final TrackIdGenerator trackIdGenerator;

	@Inject
	BallotDataResource(ElectionInformationAdminClient electionInformationAdminClient, TrackIdGenerator trackIdGenerator) {
		this.electionInformationAdminClient = electionInformationAdminClient;
		this.trackIdGenerator = trackIdGenerator;
	}

	@POST
	@Path(SAVE_BALLOT_DATA)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response saveBallotData(
			@PathParam(QUERY_PARAMETER_TENANT_ID)
			final String tenantId,
			@PathParam(QUERY_PARAMETER_ELECTION_EVENT_ID)
			final String electionEventId,
			@PathParam(QUERY_PARAMETER_BALLOT_ID)
			final String ballotId,
			@PathParam(QUERY_PARAMETER_ADMIN_BOARD_ID)
			final String adminBoardId,
			@NotNull
			final InputStream data,
			@Context
			final HttpServletRequest request) {
		String xForwardedFor = xForwardedForFactory.newXForwardedFor(request);
		String trackingId = trackIdGenerator.generate();

		RequestBody body = new InputStreamTypedOutput(MediaType.APPLICATION_JSON, data);

		try (ResponseBody responseBody = RetrofitConsumer.processResponse(electionInformationAdminClient
				.saveBallotData(BALLOTDATA_PATH, tenantId, electionEventId, ballotId, adminBoardId, xForwardedFor, trackingId, body))) {
			return Response.ok().build();
		} catch (RetrofitException rfE) {
			LOGGER.error("Error trying to save ballot data.", rfE);
			return Response.status(rfE.getHttpCode()).build();
		}
	}
}
