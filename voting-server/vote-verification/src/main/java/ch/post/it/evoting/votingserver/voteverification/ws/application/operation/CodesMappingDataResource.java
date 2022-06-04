/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.voteverification.ws.application.operation;

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
import ch.post.it.evoting.votingserver.voteverification.domain.model.content.CodesMapping;
import ch.post.it.evoting.votingserver.voteverification.domain.model.content.CodesMappingRepository;
import ch.post.it.evoting.votingserver.voteverification.infrastructure.remote.VvRemoteCertificateService;

/**
 * Web service for handling credential data resource.
 */
@Stateless
@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
@javax.ws.rs.Path(CodesMappingDataResource.RESOURCE_PATH)
public class CodesMappingDataResource {

	static final String RESOURCE_PATH = "/codesmappingdata";

	static final String SAVE_CODES_MAPPING_DATA_PATH = "/tenant/{tenantId}/electionevent/{electionEventId}/verificationcardset/{verificationCardSetId}/adminboard/{adminBoardId}";
	static final String QUERY_PARAMETER_TENANT_ID = "tenantId";
	static final String QUERY_PARAMETER_ELECTION_EVENT_ID = "electionEventId";
	static final String QUERY_PARAMETER_VERIFICATION_CARD_SET_ID = "verificationCardSetId";
	static final String QUERY_PARAMETER_ADMIN_BOARD_ID = "adminBoardId";
	private static final String RESOURCE_NAME = "codesmappingdata";
	private static final String ADMINISTRATION_BOARD_CN_PREFIX = "AdministrationBoard ";

	private static final int BATCH_SIZE = Integer.parseInt(InfrastructureConfig.getEnvWithDefaultOption("synchronization.batch.size", "1000"));
	private static final Logger LOGGER = LoggerFactory.getLogger(CodesMappingDataResource.class);
	@Inject
	private CodesMappingRepository codesMappingRepository;

	@Inject
	@VvRemoteCertificateService
	private RemoteCertificateService remoteCertificateService;

	@EJB(beanName = "VoteVerificationTransactionController")
	private TransactionController controller;

	// The track id instance
	@Inject
	private TrackIdInstance trackIdInstance;

	private static void validateParameter(final String value, final String parameter) throws ApplicationException {
		if (value == null || value.isEmpty()) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_QUERY_PARAMETER_IS_NULL, RESOURCE_NAME,
					ErrorCodes.MISSING_QUERY_PARAMETER, parameter);
		}
	}

	private static void validateParameters(final String tenantId, final String electionEventId, final String verificationCardSetId,
			final String adminBoardId) throws ApplicationException {
		validateParameter(tenantId, QUERY_PARAMETER_TENANT_ID);
		validateParameter(electionEventId, QUERY_PARAMETER_ELECTION_EVENT_ID);
		validateParameter(verificationCardSetId, QUERY_PARAMETER_VERIFICATION_CARD_SET_ID);
		validateParameter(adminBoardId, QUERY_PARAMETER_ADMIN_BOARD_ID);
	}

	/**
	 * Save a set of codes mapping data given the tenant, the election event id and the verification
	 * card set identifier.
	 *
	 * @param tenantId              - the tenant identifier
	 * @param electionEventId       - the election event identifier
	 * @param verificationCardSetId - the verification card set identifier
	 * @param data                  - the codes mapping data
	 * @param request               - the http servlet request
	 * @return Returns status 200 on success
	 * @throws ApplicationException if the input parameters are not valid
	 * @throws IOException
	 * @throws JsonMappingException
	 * @throws JsonParseException
	 */
	@POST
	@javax.ws.rs.Path(SAVE_CODES_MAPPING_DATA_PATH)
	@Consumes("text/csv")
	public Response saveCodesMappingData(
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
			final InputStream data,
			@Context
			final HttpServletRequest request) throws ApplicationException, IOException {

		trackIdInstance.setTrackId(trackingId);

		Response.Status status;
		LOGGER.info("Saving codes mapping data for verification card set id: {}, electionEventId: {}, and tenantId: {}.", verificationCardSetId,
				electionEventId, tenantId);
		validateParameters(tenantId, electionEventId, verificationCardSetId, adminBoardId);

		Path file = createTemporaryFile(data);
		try {
			if (verifyAndRemoveSignature(adminBoardId, file)) {
				saveCodesMappingData(tenantId, electionEventId, file);
				status = Status.OK;
				LOGGER.info("Codes mapping data for verifiction card set id: {}, electionEventId: {}, and tenantId: {} saved.", verificationCardSetId,
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
		Path file = createTempFile("codesMappingData", ".csv");
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

	private void saveCodesMappingData(final String tenantId, final String electionEventId, final Path file) throws IOException {
		try (Reader reader = newBufferedReader(file, StandardCharsets.UTF_8);
				MappingIterator<CodesMapping> iterator = CsvObjectReader.readCsv(reader, CodesMapping.class, "verificationCardId", "json")) {
			TransactionalAction<Void> action = context -> saveCodesMappingDataBatch(tenantId, electionEventId, iterator, BATCH_SIZE);
			while (iterator.hasNext()) {
				controller.doInNewTransaction(action);
			}
		} catch (TransactionalActionException e) {
			// checked exceptions are not expected
			throw new EJBException(e);
		}
	}

	private Void saveCodesMappingDataBatch(final String tenantId, final String electionEventId, final MappingIterator<CodesMapping> iterator,
			final int batchSize) {
		for (int i = 0; i < batchSize && iterator.hasNext(); i++) {
			CodesMapping mapping = iterator.next();
			mapping.setTenantId(tenantId);
			mapping.setElectionEventId(electionEventId);
			saveCodesMappingIfDoesNotExist(mapping);
		}
		return null;
	}

	private void saveCodesMappingIfDoesNotExist(final CodesMapping mapping) {
		String tenantId = mapping.getTenantId();
		String electionEventId = mapping.getElectionEventId();
		String verificationCardId = mapping.getVerificationCardId();
		if (!codesMappingRepository.hasWithTenantIdElectionEventIdVerificationCardId(tenantId, electionEventId, verificationCardId)) {
			try {
				codesMappingRepository.save(mapping);
			} catch (DuplicateEntryException e) {
				// exception is unexpected and the transaction is likely to be
				// marked rollback only, so abort now
				throw new EJBException(MessageFormat
						.format("Failed to save codes mapping: {0}, electionEventId: {1}, and tenantId: {2}.", verificationCardId, electionEventId,
								tenantId), e);
			}
		} else {
			LOGGER.warn("Duplicate entry tried to be inserted for codes mapping: {}, electionEventId: {}, and tenantId: {}.", verificationCardId,
					electionEventId, tenantId);
		}
	}

	private boolean verifyAndRemoveSignature(final String adminBoardId, final Path file) throws IOException {
		boolean valid = false;
		CSVVerifier verifier = new CSVVerifier();
		try {
			PublicKey key = getAdminBoardPublicKey(adminBoardId);
			valid = verifier.verify(key, file);
		} catch (GeneralCryptoLibException | RetrofitException e) {
			LOGGER.error("Codes mapping data signature could not be verified", e);
		}
		return valid;
	}
}
