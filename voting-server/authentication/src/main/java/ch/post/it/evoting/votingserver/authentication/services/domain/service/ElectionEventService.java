/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.service;

import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.Certificate;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.utils.PemUtils;
import ch.post.it.evoting.cryptolib.commons.serialization.JsonSignatureService;
import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.domain.election.AuthenticationContextData;
import ch.post.it.evoting.domain.election.AuthenticationVoterData;
import ch.post.it.evoting.domain.election.model.certificate.CertificateEntity;
import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.votingserver.authentication.services.domain.common.SignedObject;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.adminboard.AdminBoard;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.adminboard.AdminBoardRepository;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication.AuthenticationCerts;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication.AuthenticationCertsRepository;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication.AuthenticationContent;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication.AuthenticationContentRepository;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication.ElectionEventData;
import ch.post.it.evoting.votingserver.authentication.services.domain.service.exception.ElectionEventException;
import ch.post.it.evoting.votingserver.authentication.services.infrastructure.remote.AuRemoteCertificateService;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationExceptionMessages;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitException;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.service.RemoteCertificateService;
import ch.post.it.evoting.votingserver.commons.ui.ws.rs.persistence.ErrorCodes;

/**
 * Service for operating on election event data.
 */
@Stateless
public class ElectionEventService {

	public static final String ADMINISTRATION_BOARD_CN_PREFIX = "AdministrationBoard ";

	// The name of the resource handle by this web service.
	private static final String RESOURCE_NAME = "electioneventdata";

	// The name of the query parameter tenantId
	private static final String QUERY_PARAMETER_TENANT_ID = "tenantId";

	// The name of the query parameter electionEventId
	private static final String QUERY_PARAMETER_ELECTION_EVENT_ID = "electionEventId";
	private static final Logger LOGGER = LoggerFactory.getLogger(ElectionEventService.class);

	private static final ObjectMapper objectMapper = DomainObjectMapper.getNewInstance();

	// An instance of the authentication content repository
	@Inject
	private AuthenticationContentRepository authenticationContentRepository;
	// An instance of the authentication certificates repository
	@Inject
	private AuthenticationCertsRepository authenticationCertsRepository;
	@Inject
	@AuRemoteCertificateService
	private RemoteCertificateService remoteCertificateService;

	@Inject
	private AdminBoardRepository adminBoardRepository;

	/**
	 * Save the election event data
	 *
	 * @param tenantId          Id of the tenant.
	 * @param electionEventId   Id of the election event.
	 * @param adminBoardId      Id of the administrator board.
	 * @param electionEventData The election data itself.
	 * @throws ElectionEventException
	 * @throws IOException
	 * @throws ApplicationException
	 */
	public void saveElectionEventData(String tenantId, String electionEventId, String adminBoardId, ElectionEventData electionEventData)
			throws ElectionEventException, IOException, ApplicationException {
		LOGGER.info("Saving election event information for electionEventId: {}, and tenantId: {}.", electionEventId, tenantId);

		validateParameters(tenantId, electionEventId);

		PublicKey adminBoardPublicKey = getAdminBoardPublicKey(adminBoardId);

		try {
			saveAuthenticationContext(tenantId, electionEventId, electionEventData, adminBoardPublicKey);

			saveAuthenticationCertificates(tenantId, electionEventId, electionEventData, adminBoardPublicKey);

			saveAdminBoard(tenantId, electionEventId, adminBoardId);

			LOGGER.info("Election event information with electionEventId: {}, and tenantId: {} saved.", electionEventId, tenantId);

		} catch (DuplicateEntryException ex) {
			// debug because this exception should not be logged in production
			LOGGER.debug(ex.getMessage(), ex);
			LOGGER.warn("Duplicate entry tried to be inserted for electionEventId: {}, and tenantId: {}.", electionEventId, tenantId);
		}
	}

	private void saveAdminBoard(String tenantId, String electionEventId, String adminBoardId) throws DuplicateEntryException {
		// saving the relationship between election event and admin board
		AdminBoard adminBoard = new AdminBoard();
		adminBoard.setAdminBoardId(adminBoardId);
		adminBoard.setElectionEventId(electionEventId);
		adminBoard.setTenantId(tenantId);
		adminBoardRepository.save(adminBoard);
	}

	private void saveAuthenticationCertificates(String tenantId, String electionEventId, ElectionEventData electionEventData,
			PublicKey adminBoardPublicKey) throws IOException, ElectionEventException, DuplicateEntryException {
		AuthenticationCerts authenticationCerts = new AuthenticationCerts();
		authenticationCerts.setTenantId(tenantId);
		authenticationCerts.setElectionEventId(electionEventId);

		String signedAuthenticationVoterData = electionEventData.getAuthenticationVoterData();
		SignedObject signedAuthenticationVoterDataObject = objectMapper.readValue(signedAuthenticationVoterData, SignedObject.class);

		String signatureAuthenticationVoterData = signedAuthenticationVoterDataObject.getSignature();
		AuthenticationVoterData authenticationVoterData;
		try {
			LOGGER.info("Verifying authentication voter data configuration signature");
			authenticationVoterData = JsonSignatureService
					.verify(adminBoardPublicKey, signatureAuthenticationVoterData, AuthenticationVoterData.class);
			LOGGER.info("Authentication voter data configuration signature was successfully verified");
		} catch (Exception e) {
			LOGGER.error("Authentication voter data configuration signature could not be verified", e);
			throw new ElectionEventException(e);
		}
		String authenticationVoterDataJSON = objectMapper.writeValueAsString(authenticationVoterData);
		authenticationCerts.setJson(authenticationVoterDataJSON);
		authenticationCerts.setSignature(signatureAuthenticationVoterData);
		authenticationCertsRepository.save(authenticationCerts);
	}

	private void saveAuthenticationContext(String tenantId, String electionEventId, ElectionEventData electionEventData,
			PublicKey adminBoardPublicKey) throws IOException, ElectionEventException, DuplicateEntryException {
		AuthenticationContent authenticationContent = new AuthenticationContent();
		authenticationContent.setTenantId(tenantId);
		authenticationContent.setElectionEventId(electionEventId);

		String signedAuthenticationContextData = electionEventData.getAuthenticationContextData();

		SignedObject signedAuthenticationContextDataObject = objectMapper.readValue(signedAuthenticationContextData, SignedObject.class);
		String signatureAuthenticationContextData = signedAuthenticationContextDataObject.getSignature();

		AuthenticationContextData authenticationContextData;
		try {
			LOGGER.info("Verifying authentication context configuration signature");
			authenticationContextData = JsonSignatureService
					.verify(adminBoardPublicKey, signatureAuthenticationContextData, AuthenticationContextData.class);
			LOGGER.info("Authentication context configuration signature was successfully verified");
		} catch (Exception e) {
			LOGGER.error("Authentication context configuration signature could not be verified", e);
			throw new ElectionEventException(e);
		}

		String authenticationContextDataJSON = objectMapper.writeValueAsString(authenticationContextData);
		authenticationContent.setJson(authenticationContextDataJSON);
		authenticationContentRepository.save(authenticationContent);
	}

	private PublicKey getAdminBoardPublicKey(String adminBoardId) throws ElectionEventException {
		LOGGER.info("Fetching the administration board certificate");
		String adminBoardCommonName = ADMINISTRATION_BOARD_CN_PREFIX + adminBoardId;

		Certificate adminBoardCert;
		try {
			CertificateEntity adminBoardCertificateEntity = remoteCertificateService.getAdminBoardCertificate(adminBoardCommonName);
			String adminBoardCertPEM = adminBoardCertificateEntity.getCertificateContent();
			adminBoardCert = PemUtils.certificateFromPem(adminBoardCertPEM);
		} catch (GeneralCryptoLibException | RetrofitException e) {
			LOGGER.error("An error occurred while fetching the administration board certificate", e);
			throw new ElectionEventException(e);
		}
		return adminBoardCert.getPublicKey();
	}

	// Validate parameters.
	private void validateParameters(String tenantId, String electionEventId) throws ApplicationException {
		if (tenantId == null || tenantId.isEmpty()) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_QUERY_PARAMETER_IS_NULL, RESOURCE_NAME,
					ErrorCodes.MISSING_QUERY_PARAMETER, QUERY_PARAMETER_TENANT_ID);
		}

		if (electionEventId == null || electionEventId.isEmpty()) {
			throw new ApplicationException(ApplicationExceptionMessages.EXCEPTION_MESSAGE_QUERY_PARAMETER_IS_NULL, RESOURCE_NAME,
					ErrorCodes.MISSING_QUERY_PARAMETER, QUERY_PARAMETER_ELECTION_EVENT_ID);
		}
	}

	/**
	 * function for checking if the election event contains sufficient information.
	 *
	 * @param tenantId        Id of the tenant.
	 * @param electionEventId Id of the election.
	 * @return The result of the check, as a ValidationResult containing true or false.
	 */
	public ValidationResult checkIfElectionEventDataIsEmpty(String tenantId, String electionEventId) {
		ValidationResult validationResult = new ValidationResult();
		validationResult.setResult(Boolean.FALSE);
		try {
			authenticationContentRepository.findByTenantIdElectionEventId(tenantId, electionEventId);
			authenticationCertsRepository.findByTenantIdElectionEventId(tenantId, electionEventId);
		} catch (ResourceNotFoundException e) {
			// debug because this is indeed not an exception
			LOGGER.debug(e.getMessage(), e);
			validationResult.setResult(Boolean.TRUE);
		}
		return validationResult;
	}
}
