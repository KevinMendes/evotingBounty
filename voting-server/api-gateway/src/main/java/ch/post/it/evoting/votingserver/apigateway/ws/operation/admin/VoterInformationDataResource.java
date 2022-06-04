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

import ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote.admin.VoterMaterialAdminClient;
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
 * Web service for handling voter information data.
 */
@Stateless(name = "ag-VoterInformationDataResource")
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
@Path(VoterInformationDataResource.RESOURCE_PATH)
public class VoterInformationDataResource {

	static final String SAVE_VOTER_INFORMATION_DATA = "tenant/{tenantId}/electionevent/{electionEventId}/votingcardset/{votingCardSetId}/adminboard/{adminBoardId}";

	static final String RESOURCE_PATH = "/vm/voterinformationdata";
	private static final String QUERY_PARAMETER_ADMIN_BOARD_ID = "adminBoardId";
	private static final String QUERY_PARAMETER_TENANT_ID = "tenantId";
	private static final String QUERY_PARAMETER_ELECTION_EVENT_ID = "electionEventId";
	private static final String QUERY_PARAMETER_VOTING_CARD_SET_ID = "votingCardSetId";
	private static final PropertiesFileReader PROPERTIES = PropertiesFileReader.getInstance();
	static final String VOTER_INFORMATION_DATA_PATH = PROPERTIES.getPropertyValue("VOTER_INFORMATION_DATA_PATH");
	private static final Logger LOGGER = LoggerFactory.getLogger(VoterInformationDataResource.class);

	private final XForwardedForFactory xForwardedForFactory = XForwardedForFactoryImpl.getInstance();
	private final VoterMaterialAdminClient voterMaterialAdminClient;
	private final TrackIdGenerator trackIdGenerator;

	@Inject
	VoterInformationDataResource(VoterMaterialAdminClient voterMaterialAdminClient, TrackIdGenerator trackIdGenerator) {
		this.voterMaterialAdminClient = voterMaterialAdminClient;
		this.trackIdGenerator = trackIdGenerator;
	}

	@POST
	@Path(SAVE_VOTER_INFORMATION_DATA)
	@Consumes("text/csv")
	public Response saveVoterInformationData(
			@PathParam(QUERY_PARAMETER_TENANT_ID)
			final String tenantId,
			@PathParam(QUERY_PARAMETER_ELECTION_EVENT_ID)
			final String electionEventId,
			@PathParam(QUERY_PARAMETER_VOTING_CARD_SET_ID)
			final String votingCardSetId,
			@PathParam(QUERY_PARAMETER_ADMIN_BOARD_ID)
			final String adminBoardId,
			@NotNull
			final InputStream info,
			@Context
			final HttpServletRequest request) {
		String xForwardedFor = xForwardedForFactory.newXForwardedFor(request);
		String trackingId = trackIdGenerator.generate();

		RequestBody body = new InputStreamTypedOutput("text/csv", info);

		try (ResponseBody responseBody = RetrofitConsumer.processResponse(voterMaterialAdminClient
				.saveVoterInformationData(VOTER_INFORMATION_DATA_PATH, tenantId, electionEventId, votingCardSetId, adminBoardId, xForwardedFor,
						trackingId, body))) {
			return Response.ok().build();
		} catch (RetrofitException rfE) {
			LOGGER.error("Error trying to save voter information data.", rfE);
			return Response.status(rfE.getHttpCode()).build();
		}
	}
}
