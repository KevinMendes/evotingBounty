/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.ws.operation;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.elgamal.bean.ElGamalEncryptionParameters;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationExceptionMessages;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.beans.key.generation.request.KeyGenerationRequestParameters;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.commons.ui.Constants;
import ch.post.it.evoting.votingserver.commons.ui.ws.rs.persistence.ErrorCodes;
import ch.post.it.evoting.votingserver.orchestrator.mixdec.domain.services.MixDecKeyGenerationService;
import ch.post.it.evoting.votingserver.orchestrator.mixdec.domain.services.MixDecKeysResponse;

@Path(MixDecKeysResource.RESOURCE_PATH)
@Stateless(name = "or-MixDecKeysResource")
public class MixDecKeysResource {

	static final String RESOURCE_PATH = "mixdec/tenant/{tenantId}/electionevent/{electionEventId}/keys";

	static final String PATH_PARAMETER_TENANT_ID = "tenantId";

	static final String PATH_PARAMETER_ELECTION_EVENT_ID = "electionEventId";
	private static final Logger LOGGER = LoggerFactory.getLogger(MixDecKeysResource.class);
	@Inject
	private MixDecKeyGenerationService mixDecKeyGenerationService;
	@Inject
	private TrackIdInstance trackIdInstance;

	/**
	 * Provides synchronous generation of MixDec keys.
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response generateKeys(
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId,
			@PathParam(PATH_PARAMETER_TENANT_ID)
			final String tenantId,
			@PathParam(PATH_PARAMETER_ELECTION_EVENT_ID)
			final String electionEventId,
			@NotNull
			final KeyGenerationRequestParameters keyGenerationRequestParameters,
			@Context
			final HttpServletRequest request) {
		try {
			trackIdInstance.setTrackId(trackingId);

			validateParameters(tenantId, electionEventId);

			List<String> electoralAuthorityIds = keyGenerationRequestParameters.getResourceIds();
			ZonedDateTime keysDateFrom = ZonedDateTime.parse(keyGenerationRequestParameters.getKeysDateFrom());
			ZonedDateTime keysDateTo = ZonedDateTime.parse(keyGenerationRequestParameters.getKeysDateTo());
			ElGamalEncryptionParameters elGamalEncryptionParameters = recoverEncryptionParameters(
					keyGenerationRequestParameters.getElGamalEncryptionParameters());

			LOGGER.info("Requesting MixDec key generation for tenant {} electionEventId {}", tenantId, electionEventId);

			Map<String, List<String>> electoralAuthorityKeys = mixDecKeyGenerationService
					.requestMixDecKeyGenerationSync(trackingId, tenantId, electionEventId, electoralAuthorityIds, keysDateFrom, keysDateTo,
							elGamalEncryptionParameters);

			MixDecKeysResponse mixDecKeysResponse = new MixDecKeysResponse();
			mixDecKeysResponse.setElectoralAuthorityMixDecryptKeys(electoralAuthorityKeys);

			return Response.ok().entity(mixDecKeysResponse).build();
		} catch (ResourceNotFoundException e) {
			LOGGER.error("Resource not found", e);
			return Response.status(Status.NOT_FOUND).build();
		} catch (ApplicationException | GeneralCryptoLibException | IOException e) {
			LOGGER.error("Mixing key generation failed", e);
			return Response.serverError().entity(e).build();
		}
	}

	private ElGamalEncryptionParameters recoverEncryptionParameters(String encodedEncryptionParameters) throws GeneralCryptoLibException {

		String encryptionParamsAsJson = new String(Base64.getDecoder().decode(encodedEncryptionParameters), StandardCharsets.UTF_8);
		return ElGamalEncryptionParameters.fromJson(encryptionParamsAsJson);
	}

	private void validateParameters(String tenantId, String electionEventId) throws ApplicationException {

		if (tenantId == null || tenantId.isEmpty()) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_QUERY_PARAMETER_IS_NULL, RESOURCE_PATH,
					ErrorCodes.MISSING_QUERY_PARAMETER, PATH_PARAMETER_TENANT_ID);
		}

		if (electionEventId == null || electionEventId.isEmpty()) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_QUERY_PARAMETER_IS_NULL, RESOURCE_PATH,
					ErrorCodes.MISSING_QUERY_PARAMETER, PATH_PARAMETER_ELECTION_EVENT_ID);
		}
	}
}
