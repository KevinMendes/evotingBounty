/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.voteverification.ws.application.operation;

import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.Certificate;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.utils.PemUtils;
import ch.post.it.evoting.cryptolib.commons.serialization.JsonSignatureService;
import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.domain.election.VoteVerificationContextData;
import ch.post.it.evoting.domain.election.model.certificate.CertificateEntity;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationExceptionMessages;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitException;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.service.RemoteCertificateService;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.commons.ui.Constants;
import ch.post.it.evoting.votingserver.commons.ui.ws.rs.persistence.ErrorCodes;
import ch.post.it.evoting.votingserver.voteverification.domain.common.SignedObject;
import ch.post.it.evoting.votingserver.voteverification.domain.model.content.VerificationContent;
import ch.post.it.evoting.votingserver.voteverification.domain.model.content.VerificationContentRepository;
import ch.post.it.evoting.votingserver.voteverification.infrastructure.remote.VvRemoteCertificateService;

/**
 * Web service for handling ballot data resource.
 */
@Path(VerificationCardSetDataResource.RESOURCE_PATH)
@Stateless
public class VerificationCardSetDataResource {

	public static final String ADMINISTRATION_BOARD_CN_PREFIX = "AdministrationBoard ";

	static final String RESOURCE_PATH = "/verificationcardsetdata";

	static final String SAVE_VERIFICATION_CARD_SET_DATA_PATH = "/tenant/{tenantId}/electionevent/{electionEventId}/verificationcardset/{verificationCardSetId}/adminboard/{adminBoardId}";

	// The name of the query parameter tenantId
	static final String QUERY_PARAMETER_TENANT_ID = "tenantId";

	// The name of the query parameter electionEventId
	static final String QUERY_PARAMETER_ELECTION_EVENT_ID = "electionEventId";

	// The name of the query parameter verificationCardSetId
	static final String QUERY_PARAMETER_VERIFICATION_CARD_SET_ID = "verificationCardSetId";

	static final String QUERY_PARAMETER_ADMIN_BOARD_ID = "adminBoardId";

	// The name of the resource handle by this web service.
	private static final String RESOURCE_NAME = "verificationcardsetdata";

	private static final Logger LOGGER = LoggerFactory.getLogger(VerificationCardSetDataResource.class);

	private static final ObjectMapper objectMapper = DomainObjectMapper.getNewInstance();

	// An instance of the verification content repository
	@EJB
	private VerificationContentRepository verificationContentRepository;

	@Inject
	@VvRemoteCertificateService
	private RemoteCertificateService remoteCertificateService;

	@Inject
	private TrackIdInstance trackIdInstance;

	/**
	 * Save data for a verification card set given the tenant, the election event id and the verification card set identifier.
	 *
	 * @param tenantId              - the tenant identifier.
	 * @param electionEventId       - the election event identifier.
	 * @param verificationCardSetId - the verification card set identifier.
	 * @param request               - the http servlet request.
	 * @return Returns status 200 on success.
	 * @throws ApplicationException if the input parameters are not valid.
	 */
	@POST
	@Path(SAVE_VERIFICATION_CARD_SET_DATA_PATH)
	@Consumes(MediaType.APPLICATION_JSON)
	public Response saveVerificationCardSetData(
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId,
			@PathParam(QUERY_PARAMETER_TENANT_ID)
			final String tenantId,
			@PathParam(QUERY_PARAMETER_ELECTION_EVENT_ID)
			final String electionEventId,
			@PathParam(QUERY_PARAMETER_VERIFICATION_CARD_SET_ID)
			final String verificationCardSetId,
			@PathParam(QUERY_PARAMETER_ADMIN_BOARD_ID)
			final String adminBoardId,
			@NotNull
			final VoteVerificationContextDataPayload voteVerificationContextDataPayload,
			@Context
			final HttpServletRequest request) throws ApplicationException, IOException {

		trackIdInstance.setTrackId(trackingId);

		LOGGER.info("Saving verification card set data for verificationCardSetId: {}, electionEventId: {}, and tenantId: {}.", verificationCardSetId,
				electionEventId, tenantId);

		// validate parameters
		validateParameters(tenantId, electionEventId, verificationCardSetId);

		LOGGER.info("Fetching the administration board certificate");
		final String adminBoardCommonName = ADMINISTRATION_BOARD_CN_PREFIX + adminBoardId;

		Certificate adminBoardCert;
		try {
			final CertificateEntity adminBoardCertificateEntity = remoteCertificateService.getAdminBoardCertificate(adminBoardCommonName);
			final String adminBoardCertPEM = adminBoardCertificateEntity.getCertificateContent();
			adminBoardCert = PemUtils.certificateFromPem(adminBoardCertPEM);
		} catch (final GeneralCryptoLibException | RetrofitException e) {
			LOGGER.error("An error occurred while fetching the administration board certificate", e);
			return Response.status(Response.Status.PRECONDITION_FAILED).build();
		}
		final PublicKey adminBoardPublicKey = adminBoardCert.getPublicKey();

		try {
			// Build verification content data
			final VerificationContent verificationContent = new VerificationContent();
			verificationContent.setElectionEventId(electionEventId);
			verificationContent.setTenantId(tenantId);
			verificationContent.setVerificationCardSetId(verificationCardSetId);

			final String signedVoteVerificationContextData = voteVerificationContextDataPayload.getVoteVerificationContextData();
			final SignedObject signedVoteVerificationContextDataObject = objectMapper
					.readValue(signedVoteVerificationContextData, SignedObject.class);
			final String signatureVoteVerificationContextData = signedVoteVerificationContextDataObject.getSignature();
			VoteVerificationContextData voteVerificationContextData;
			try {
				LOGGER.info("Verifying vote verification context data signature");
				voteVerificationContextData = JsonSignatureService
						.verify(adminBoardPublicKey, signatureVoteVerificationContextData, VoteVerificationContextData.class);
				LOGGER.info("Vote verification context data signature was successfully verified");
			} catch (final Exception e) {
				LOGGER.error("Vote verification context data signature could not be verified", e);
				return Response.status(Response.Status.PRECONDITION_FAILED).build();
			}
			final String voteVerificationContextDataJSON = objectMapper.writeValueAsString(voteVerificationContextData);
			verificationContent.setJson(voteVerificationContextDataJSON);

			// Save verification content
			verificationContentRepository.save(verificationContent);

			LOGGER.info("Verification card set data with verificationCardSetId: {}, electionEventId: {}, and tenantId: {} saved.",
					verificationCardSetId, electionEventId, tenantId);
		} catch (final DuplicateEntryException ex) {
			LOGGER.warn("Duplicate entry tried to be inserted for verification card set: {}, electionEventId: {}, and tenantId: {}.",
					verificationCardSetId, electionEventId, tenantId, ex);
		}

		// return on success
		return Response.ok().build();
	}

	// Validate parameters.
	private void validateParameters(final String tenantId, final String electionEventId, final String verificationCardSetId)
			throws ApplicationException {
		if (tenantId == null || tenantId.isEmpty()) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_QUERY_PARAMETER_IS_NULL, RESOURCE_NAME,
					ErrorCodes.MISSING_QUERY_PARAMETER, QUERY_PARAMETER_TENANT_ID);
		}

		if (electionEventId == null || electionEventId.isEmpty()) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_QUERY_PARAMETER_IS_NULL, RESOURCE_NAME,
					ErrorCodes.MISSING_QUERY_PARAMETER, QUERY_PARAMETER_ELECTION_EVENT_ID);
		}

		if (verificationCardSetId == null || verificationCardSetId.isEmpty()) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_QUERY_PARAMETER_IS_NULL, RESOURCE_NAME,
					ErrorCodes.MISSING_QUERY_PARAMETER, QUERY_PARAMETER_VERIFICATION_CARD_SET_ID);
		}
	}

	private static class VoteVerificationContextDataPayload {

		@NotNull
		private String voteVerificationContextData;

		public String getVoteVerificationContextData() {
			return voteVerificationContextData;
		}
	}
}
