/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.ws.application.operation;

import java.security.PublicKey;
import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.List;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.JsonArray;
import javax.json.JsonObject;
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

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.utils.PemUtils;
import ch.post.it.evoting.cryptolib.commons.serialization.JsonSignatureService;
import ch.post.it.evoting.domain.election.model.certificate.CertificateEntity;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationExceptionMessages;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitException;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.service.RemoteCertificateService;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.commons.ui.Constants;
import ch.post.it.evoting.votingserver.commons.ui.ws.rs.persistence.ErrorCodes;
import ch.post.it.evoting.votingserver.commons.util.JsonUtils;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballot.Ballot;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballot.BallotData;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballot.BallotRepository;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballot.BallotText;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballot.BallotTextRepository;
import ch.post.it.evoting.votingserver.electioninformation.services.infrastructure.remote.EiRemoteCertificateService;

/**
 * Web service for handling ballot data resource.
 */
@Path("/ballotdata")
@Stateless
public class BallotDataResource {

	public static final String ADMINISTRATION_BOARD_CN_PREFIX = "AdministrationBoard ";

	public static final String JSON_ATTRIBUTE_NAME_SIGNED_OBJECT = "signedObject";

	private static final String RESOURCE_NAME = "ballotdata";

	private static final String QUERY_PARAMETER_TENANT_ID = "tenantId";

	private static final String QUERY_PARAMETER_ELECTION_EVENT_ID = "electionEventId";

	private static final String QUERY_PARAMETER_BALLOT_ID = "ballotId";

	private static final String QUERY_PARAMETER_ADMIN_BOARD_ID = "adminBoardId";
	private static final Logger LOGGER = LoggerFactory.getLogger(BallotDataResource.class);
	@EJB
	private BallotRepository ballotRepository;
	@EJB
	private BallotTextRepository ballotTextRepository;

	@Inject
	@EiRemoteCertificateService
	private RemoteCertificateService remoteCertificateService;

	@Inject
	private TrackIdInstance trackIdInstance;

	/**
	 * Save data for a ballot given the tenant, the election event id and the ballot identifier.
	 *
	 * @param tenantId        - the tenant identifier.
	 * @param electionEventId - the election event identifier.
	 * @param ballotId        - the ballot identifier.
	 * @param ballotData      - the data for a ballot.
	 * @param request         - the http servlet request.
	 * @return Returns status 200 on success.
	 * @throws ApplicationException if the input parameters are not valid.
	 */
	@POST
	@Path("tenant/{tenantId}/electionevent/{electionEventId}/ballot/{ballotId}/adminboard/{adminBoardId}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response saveBallotData(
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
			final
			String trackingId,
			@PathParam(QUERY_PARAMETER_TENANT_ID)
			final String tenantId,
			@PathParam(QUERY_PARAMETER_ELECTION_EVENT_ID)
			final String electionEventId,
			@PathParam(QUERY_PARAMETER_BALLOT_ID)
			final String ballotId,
			@PathParam(QUERY_PARAMETER_ADMIN_BOARD_ID)
			final String adminBoardId,
			@NotNull
			final BallotData ballotData,
			@Context
			final HttpServletRequest request) throws ApplicationException {

		// set the track id to be logged
		trackIdInstance.setTrackId(trackingId);

		LOGGER.info("Saving ballot data for ballotId: {}, electionEventId: {}, and tenantId: {}.", ballotId, electionEventId, tenantId);

		LOGGER.info("Fetching the administration board certificate");
		final String adminBoardCommonName = ADMINISTRATION_BOARD_CN_PREFIX + adminBoardId;
		final Certificate adminBoardCert;
		try {
			final CertificateEntity adminBoardCertificateEntity = remoteCertificateService.getAdminBoardCertificate(adminBoardCommonName);
			final String adminBoardCertPEM = adminBoardCertificateEntity.getCertificateContent();
			adminBoardCert = PemUtils.certificateFromPem(adminBoardCertPEM);
		} catch (final GeneralCryptoLibException | RetrofitException e) {
			LOGGER.error("An error occurred while fetching the administration board certificate", e);
			return Response.status(Response.Status.PRECONDITION_FAILED).build();
		}
		final PublicKey adminBoardPublicKey = adminBoardCert.getPublicKey();
		validateParameters(tenantId, electionEventId, ballotId);

		try {
			final Ballot ballot = new Ballot();
			ballot.setBallotId(ballotId);
			ballot.setElectionEventId(electionEventId);
			ballot.setTenantId(tenantId);

			final String signedBallot = ballotData.getBallot();
			final String stringBallot;
			try {
				LOGGER.info("Verifying ballot signature");
				stringBallot = JsonSignatureService.verify(adminBoardPublicKey, signedBallot, String.class);
				LOGGER.info("Ballot signature was successfully verified");
			} catch (final Exception e) {
				LOGGER.error("Ballot signature could not be verified", e);
				return Response.status(Response.Status.PRECONDITION_FAILED).build();
			}
			ballot.setJson(stringBallot);
			ballot.setSignature(signedBallot);
			ballotRepository.save(ballot);

			final BallotText ballotText = new BallotText();
			ballotText.setBallotId(ballotId);
			ballotText.setElectionEventId(electionEventId);
			ballotText.setTenantId(tenantId);

			final String signedBallotTextsArray = ballotData.getBallottext();
			final JsonArray signedBallotTextsJsonArray = JsonUtils.getJsonArray(signedBallotTextsArray);

			final List<JsonObject> listBallotTexts = new ArrayList<>();

			boolean signaturesVerification = true;
			for (int i = 0; i < signedBallotTextsJsonArray.size(); i++) {

				final JsonObject ballotTextInArray = signedBallotTextsJsonArray.getJsonObject(i);
				final String ballotTextSignature = ballotTextInArray.getString(JSON_ATTRIBUTE_NAME_SIGNED_OBJECT);

				try {
					LOGGER.info("Verifying ballot text signature");
					final String stringBallotText = JsonSignatureService.verify(adminBoardPublicKey, ballotTextSignature, String.class);
					final JsonObject ballotTextJsonObject = JsonUtils.getJsonObject(stringBallotText);
					listBallotTexts.add(ballotTextJsonObject);

					LOGGER.info("Ballot text signature was successfully verified");
				} catch (final Exception e) {
					signaturesVerification = false;
					LOGGER.error("Ballot text signature could not be verified", e);
					break;
				}
			}
			if (!signaturesVerification) {
				return Response.status(Response.Status.PRECONDITION_FAILED).build();
			}
			final JsonArray ballotTexts = JsonUtils.getJsonArray(listBallotTexts.toString());
			ballotText.setJson(ballotTexts.toString());
			ballotText.setSignature(signedBallotTextsJsonArray.toString());
			ballotTextRepository.save(ballotText);

			LOGGER.info("Ballot with ballotId: {}, electionEventId: {}, and tenantId: {} saved.", ballotId, electionEventId, tenantId);

		} catch (final DuplicateEntryException ex) {
			LOGGER.warn("Duplicate entry tried to be inserted for ballot: {}, electionEventId: {}, and tenantId: {}.", ballotId, electionEventId,
					tenantId, ex);
		}

		return Response.ok().build();
	}

	// Validate parameters.
	private void validateParameters(final String tenantId, final String electionEventId, final String ballotId) throws ApplicationException {
		if (tenantId == null || tenantId.isEmpty()) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_QUERY_PARAMETER_IS_NULL, RESOURCE_NAME,
					ErrorCodes.MISSING_QUERY_PARAMETER, QUERY_PARAMETER_TENANT_ID);
		}

		if (electionEventId == null || electionEventId.isEmpty()) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_QUERY_PARAMETER_IS_NULL, RESOURCE_NAME,
					ErrorCodes.MISSING_QUERY_PARAMETER, QUERY_PARAMETER_ELECTION_EVENT_ID);
		}

		if (ballotId == null || ballotId.isEmpty()) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_QUERY_PARAMETER_IS_NULL, RESOURCE_NAME,
					ErrorCodes.MISSING_QUERY_PARAMETER, QUERY_PARAMETER_BALLOT_ID);
		}
	}
}
