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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote.admin.AuthenticationAdminClient;
import ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote.admin.ElectionInformationAdminClient;
import ch.post.it.evoting.votingserver.apigateway.services.infrastructure.remote.admin.VoteVerificationAdminClient;
import ch.post.it.evoting.votingserver.apigateway.ws.RestApplication;
import ch.post.it.evoting.votingserver.apigateway.ws.proxy.XForwardedForFactory;
import ch.post.it.evoting.votingserver.apigateway.ws.proxy.XForwardedForFactoryImpl;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitConsumer;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdGenerator;
import ch.post.it.evoting.votingserver.commons.util.PropertiesFileReader;

/**
 * Resource to operate on tenant-related information
 */
@Stateless(name = "ag-tenantDataResource")
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
@Path(AgTenantDataResource.RESOURCE_PATH)
public class AgTenantDataResource {

	static final String CHECK_TENANT_ACTIVATION = "activatetenant/tenant/{tenantId}";

	static final String RESOURCE_PATH = "/tenantdata";
	private static final PropertiesFileReader PROPERTIES = PropertiesFileReader.getInstance();
	static final String PATH_TENANT_DATA = PROPERTIES.getPropertyValue("TENANT_DATA_PATH");
	private static final String TENANT_PARAMETER = "tenantId";
	private static final Logger LOGGER = LoggerFactory.getLogger(AgTenantDataResource.class);
	private final XForwardedForFactory xForwardedForFactory = XForwardedForFactoryImpl.getInstance();
	private final VoteVerificationAdminClient voteVerificationAdminClient;
	private final ElectionInformationAdminClient electionInformationAdminClient;
	private final AuthenticationAdminClient authenticationAdminClient;
	private final TrackIdGenerator trackIdGenerator;

	@Inject
	AgTenantDataResource(AuthenticationAdminClient authenticationAdminClient, ElectionInformationAdminClient electionInformationAdminClient,
			VoteVerificationAdminClient voteVerificationAdminClient, TrackIdGenerator trackIdGenerator) {
		this.authenticationAdminClient = authenticationAdminClient;
		this.electionInformationAdminClient = electionInformationAdminClient;
		this.voteVerificationAdminClient = voteVerificationAdminClient;
		this.trackIdGenerator = trackIdGenerator;
	}

	/**
	 * Check if a tenant is active for the contexts where the operation is available
	 *
	 * @param tenantId - identifier of the tenant
	 * @return
	 */
	@GET
	@Path(CHECK_TENANT_ACTIVATION)
	@Produces(RestApplication.MEDIA_TYPE_JSON_UTF_8)
	public Response checkTenantActivation(
			@PathParam(TENANT_PARAMETER)
			final String tenantId,
			@Context
			final HttpServletRequest request) {
		String xForwardedFor = xForwardedForFactory.newXForwardedFor(request);
		String trackingId = trackIdGenerator.generate();
		try {
			JsonObject au = RetrofitConsumer
					.processResponse(authenticationAdminClient.checkTenantActivation(PATH_TENANT_DATA, tenantId, xForwardedFor, trackingId));
			JsonObject ei = RetrofitConsumer
					.processResponse(electionInformationAdminClient.checkTenantActivation(PATH_TENANT_DATA, tenantId, xForwardedFor, trackingId));
			JsonObject vv = RetrofitConsumer
					.processResponse(voteVerificationAdminClient.checkTenantActivation(PATH_TENANT_DATA, tenantId, xForwardedFor, trackingId));

			JsonArray array = new JsonArray();
			array.add(au);
			array.add(ei);
			array.add(vv);

			return Response.ok(array.toString()).build();
		} catch (RetrofitException rfE) {
			LOGGER.error("Error trying to check tenant activation.", rfE);
			return Response.status(rfE.getHttpCode()).build();
		}
	}
}
