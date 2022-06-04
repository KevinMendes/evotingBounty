/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votermaterial.ws.application.operation;

import static java.nio.file.Files.copy;
import static java.nio.file.Files.createTempFile;
import static java.nio.file.Files.delete;
import static java.nio.file.Files.newBufferedReader;
import static java.text.MessageFormat.format;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.text.MessageFormat;

import javax.ejb.EJB;
import javax.ejb.EJBException;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.MappingIterator;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.utils.PemUtils;
import ch.post.it.evoting.domain.csv.CsvObjectReader;
import ch.post.it.evoting.domain.election.model.certificate.CertificateEntity;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationExceptionMessages;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.infrastructure.config.InfrastructureConfig;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitException;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.service.RemoteCertificateService;
import ch.post.it.evoting.votingserver.commons.infrastructure.transaction.TransactionController;
import ch.post.it.evoting.votingserver.commons.infrastructure.transaction.TransactionalAction;
import ch.post.it.evoting.votingserver.commons.infrastructure.transaction.TransactionalActionException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.commons.ui.Constants;
import ch.post.it.evoting.votingserver.commons.ui.ws.rs.persistence.ErrorCodes;
import ch.post.it.evoting.votingserver.commons.verify.CSVVerifier;
import ch.post.it.evoting.votingserver.votermaterial.domain.model.information.VoterInformation;
import ch.post.it.evoting.votingserver.votermaterial.domain.model.information.VoterInformationRepository;
import ch.post.it.evoting.votingserver.votermaterial.infrastructure.remote.VmRemoteCertificateService;

/**
 * Web service for handling voter information data resource.
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
@javax.ws.rs.Path("/voterinformationdata")
public class VoterInformationDataResource {

	private static final String RESOURCE_NAME = "voterinformationdata";
	private static final String QUERY_PARAMETER_TENANT_ID = "tenantId";
	private static final String QUERY_PARAMETER_ELECTION_EVENT_ID = "electionEventId";
	private static final String QUERY_PARAMETER_VOTING_CARD_SET_ID = "votingCardSetId";
	private static final String QUERY_PARAMETER_ADMIN_BOARD_ID = "adminBoardId";
	private static final String ADMINISTRATION_BOARD_CN_PREFIX = "AdministrationBoard ";
	private static final int BATCH_SIZE = Integer.parseInt(InfrastructureConfig.getEnvWithDefaultOption("synchronization.batch.size", "1000"));

	private static final Logger LOGGER = LoggerFactory.getLogger(VoterInformationDataResource.class);

	@Inject
	private VoterInformationRepository voterInformationRepository;

	@Inject
	@VmRemoteCertificateService
	private RemoteCertificateService remoteCertificateService;

	@EJB(beanName = "VoterMaterialTransactionController")
	private TransactionController controller;

	@Inject
	private TrackIdInstance trackIdInstance;

	@Inject
	private CSVVerifier verifier;

	private static void validateParameter(final String value) throws ApplicationException {
		if (value == null || value.isEmpty()) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_QUERY_PARAMETER_IS_NULL, RESOURCE_NAME,
					ErrorCodes.MISSING_QUERY_PARAMETER, QUERY_PARAMETER_ADMIN_BOARD_ID);
		}
	}

	private static void validateParameters(final String tenantId, final String electionEventId, final String votingCardSetId,
			final String adminBoardId) throws ApplicationException {
		validateParameter(tenantId);
		validateParameter(electionEventId);
		validateParameter(votingCardSetId);
		validateParameter(adminBoardId);
	}

	/**
	 * Save a set of voter information data given the tenant, the election event id and the voting
	 * card set identifier.
	 *
	 * @param tenantId        - the tenant identifier.
	 * @param electionEventId - the election event identifier.
	 * @param votingCardSetId - the voting card set identifier.
	 * @param data            - the voter informations data.
	 * @param request         - the http servlet request.
	 * @return Returns status 200 on success.
	 * @throws ApplicationException if the input parameters are not valid.
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	@POST
	@javax.ws.rs.Path("tenant/{tenantId}/electionevent/{electionEventId}/votingcardset/{votingCardSetId}/adminboard/{adminBoardId}")
	@Consumes("text/csv")
	public Response saveVoterInformationData(
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId,
			@PathParam(QUERY_PARAMETER_TENANT_ID)
			final String tenantId,
			@PathParam(QUERY_PARAMETER_ELECTION_EVENT_ID)
			final String electionEventId,
			@PathParam(QUERY_PARAMETER_VOTING_CARD_SET_ID)
			final String votingCardSetId,
			@PathParam(QUERY_PARAMETER_ADMIN_BOARD_ID)
			final String adminBoardId,
			@NotNull
			final InputStream data,
			@Context
			final HttpServletRequest request) throws ApplicationException, IOException {

		trackIdInstance.setTrackId(trackingId);

		Response.Status status;
		LOGGER.info("Saving voter information data for voting card set id: {}, electionEventId: {}, and tenantId: {}.", votingCardSetId,
				electionEventId, tenantId);
		validateParameters(tenantId, electionEventId, votingCardSetId, adminBoardId);

		Path file = createTemporaryFile(data);
		try {
			if (verifyAndRemoveSignature(adminBoardId, file)) {
				saveVoterInformationData(tenantId, file);
				status = Status.OK;
				LOGGER.info("Voter information data for voting card set id: {}, electionEventId: {}, and tenantId: {} saved.", votingCardSetId,
						electionEventId, tenantId);
			} else {
				status = Status.PRECONDITION_FAILED;
			}
		} finally {
			deleteTemporaryFile(file);
		}
		return Response.status(status).build();
	}

	private Path createTemporaryFile(final InputStream data) throws IOException {
		Path file = createTempFile("voterInformationData", ".csv");
		try {
			copy(data, file, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			deleteTemporaryFile(file);
			throw e;
		}
		return file;
	}

	private void deleteTemporaryFile(final Path file) {
		try {
			delete(file);
		} catch (IOException e) {
			LOGGER.warn(format("Failed to delete temporary file ''{0}''.", file), e);
		}
	}

	private PublicKey getAdminBoardPublicKey(final String adminBoardId) throws GeneralCryptoLibException, RetrofitException {
		LOGGER.info("Fetching the administration board certificate");
		String name = ADMINISTRATION_BOARD_CN_PREFIX + adminBoardId;
		CertificateEntity entity = remoteCertificateService.getAdminBoardCertificate(name);
		String content = entity.getCertificateContent();
		Certificate certificate = PemUtils.certificateFromPem(content);
		return certificate.getPublicKey();
	}

	private void saveVoterInformationData(final String tenantId, final Path file) throws IOException {
		String[] columnNames = { "votingCardId", "ballotId", "ballotBoxId", "credentialId", QUERY_PARAMETER_ELECTION_EVENT_ID,
				QUERY_PARAMETER_VOTING_CARD_SET_ID, "verificationCardId", "verificationCardSetId" };
		try (Reader reader = newBufferedReader(file, StandardCharsets.UTF_8);
				MappingIterator<VoterInformation> iterator = CsvObjectReader.readCsv(reader, VoterInformation.class, columnNames)) {
			TransactionalAction<Void> action = context -> saveVoterInformationDataBatch(tenantId, iterator, BATCH_SIZE);
			while (iterator.hasNext()) {
				controller.doInNewTransaction(action);
			}
		} catch (TransactionalActionException e) {
			// checked exceptions are not expected
			throw new EJBException(e);
		}
	}

	private Void saveVoterInformationDataBatch(final String tenantId, final MappingIterator<VoterInformation> iterator, final int batchSize) {
		for (int i = 0; i < batchSize && iterator.hasNext(); i++) {
			VoterInformation information = iterator.next();
			information.setTenantId(tenantId);
			saveVoterInformationIfDoesNotExist(information);
		}
		return null;
	}

	private void saveVoterInformationIfDoesNotExist(final VoterInformation information) {
		String tenantId = information.getTenantId();
		String electionEventId = information.getElectionEventId();
		String votingCardId = information.getVotingCardId();
		if (!voterInformationRepository.hasWithTenantIdElectionEventIdVotingCardId(tenantId, electionEventId, votingCardId)) {
			try {
				voterInformationRepository.save(information);
			} catch (final DuplicateEntryException e) {
				// exception is unexpected and the transaction is likely to be
				// marked rollback only, so abort now
				throw new EJBException(MessageFormat
						.format("Failed to save voter information: {0}, electionEventId: {1}, and tenantId: {2}.", votingCardId, electionEventId,
								tenantId), e);
			}
		} else {
			LOGGER.warn("Duplicate entry tried to be inserted for voter information: {}, electionEventId: {}, and tenantId: {}.", votingCardId,
					electionEventId, tenantId);
		}
	}

	private boolean verifyAndRemoveSignature(final String adminBoardId, final Path file) throws IOException {
		boolean valid = false;
		try {
			PublicKey key = getAdminBoardPublicKey(adminBoardId);
			valid = verifier.verify(key, file);
		} catch (GeneralCryptoLibException | RetrofitException e) {
			LOGGER.error("Voter information signature could not be verified", e);
		}
		return valid;
	}
}
