/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.certificateregistry.ws.operation;

import java.io.IOException;
import java.io.Reader;
import java.security.cert.Certificate;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.PersistenceException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.cryptoapi.CryptoAPIX509Certificate;
import ch.post.it.evoting.cryptolib.certificates.utils.CryptographicOperationException;
import ch.post.it.evoting.cryptolib.certificates.utils.PemUtils;
import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.domain.election.validation.ValidationError;
import ch.post.it.evoting.domain.election.validation.ValidationErrorType;
import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.votingserver.certificateregistry.services.domain.model.certificate.CertificateEntity;
import ch.post.it.evoting.votingserver.certificateregistry.services.domain.model.certificate.CertificateRepository;
import ch.post.it.evoting.votingserver.certificateregistry.services.domain.model.platform.CrCertificateValidationService;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.beans.validation.CertificateTools;
import ch.post.it.evoting.votingserver.commons.beans.validation.CertificateValidationResult;
import ch.post.it.evoting.votingserver.commons.beans.validation.CertificateValidationService;
import ch.post.it.evoting.votingserver.commons.ui.Constants;

@Path(CertificateRegistryResource.RESOURCE_PATH)
@Stateless(name = "cert-CertificatesDataResource")
public class CertificateRegistryResource {

	public static final String RESOURCE_PATH = "certificates/public";

	public static final String SAVE_CERTIFICATE = "secured";

	public static final String SAVE_CERTIFICATE_FOR_TENANT = "secured/tenant/{tenantId}";

	public static final String SAVE_CERTIFICATE_FOR_ELECTION_EVENT = "secured/tenant/{tenantId}/electionevent/{electionEventId}";

	public static final String GET_CERTIFICATE = "name/{certificateName}";

	public static final String GET_CERTIFICATE_FOR_TENANT = "tenant/{tenantId}/name/{certificateName}";

	public static final String GET_CERTIFICATE_FOR_TENANT_EEID = "tenant/{tenantId}/electionevent/{electionEventId}/name/{certificateName}";

	public static final String CHECK_IF_CERTIFICATE_EXIST = "tenant/{tenantId}/name/{certificateName}/status";

	private static final String QUERY_PARAMETER_TENANT_ID = "tenantId";

	private static final String QUERY_PARAMETER_ELECTION_EVENT_ID = "electionEventId";

	private static final String QUERY_PARAMETER_CERTIFICATE_NAME = "certificateName";

	private static final Logger LOGGER = LoggerFactory.getLogger(CertificateRegistryResource.class);

	private static final ObjectMapper objectMapper = DomainObjectMapper.getNewInstance();

	@Inject
	private CertificateRepository certificateRepository;

	@Inject
	@CrCertificateValidationService
	private CertificateValidationService certificateValidationService;

	/**
	 * Saves a certificate in the repository
	 *
	 * @param tenantId          - tenant identifier
	 * @param electionEventId   - electionEvent identifier
	 * @param certificateReader - content of the certificate
	 * @return
	 * @throws DuplicateEntryException
	 * @throws ResourceNotFoundException
	 * @throws CryptographicOperationException
	 */
	@POST
	@Path(SAVE_CERTIFICATE_FOR_ELECTION_EVENT)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveCertificate(
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId,
			@PathParam(QUERY_PARAMETER_TENANT_ID)
					String tenantId,
			@PathParam(QUERY_PARAMETER_ELECTION_EVENT_ID)
					String electionEventId,
			@NotNull
					Reader certificateReader,
			@Context
					HttpServletRequest servletRequest) throws DuplicateEntryException, ResourceNotFoundException, CryptographicOperationException {

		Response response;
		try {
			CertificateEntity certificateEntity = objectMapper.readValue(certificateReader, CertificateEntity.class);
			certificateEntity.setElectionEventId(electionEventId);
			certificateEntity.setTenantId(tenantId);
			Certificate certificateToValidate = PemUtils.certificateFromPem(certificateEntity.getCertificateContent());
			CryptoAPIX509Certificate cryptoAPIX509Certificate = CertificateTools.getCryptoX509Certificate(certificateToValidate);
			String signerCommonName = cryptoAPIX509Certificate.getIssuerDn().getCommonName();
			CertificateEntity signerEntity = certificateRepository.findByName(signerCommonName);
			Certificate signerCertificate = PemUtils.certificateFromPem(signerEntity.getCertificateContent());
			CertificateValidationResult x509CertificateValidationResult = certificateValidationService
					.validateCertificate(certificateToValidate, signerCertificate);
			if (x509CertificateValidationResult.isValid()) {
				certificateRepository.save(certificateEntity);
				response = Response.ok().build();
			} else {
				response = Response.status(Response.Status.PRECONDITION_FAILED).build();
			}

		} catch (IOException | GeneralCryptoLibException e) {
			LOGGER.info("Error trying to save certificate for election event.", e);
			response = Response.status(Response.Status.BAD_REQUEST).entity(e.getMessage()).build();
		}
		return response;

	}

	/**
	 * Saves a certificate in the repository
	 *
	 * @param tenantId          - tenant identifier
	 * @param certificateReader - content of the certificate
	 * @return
	 * @throws DuplicateEntryException
	 * @throws ResourceNotFoundException
	 * @throws CryptographicOperationException
	 */
	@POST
	@Path(SAVE_CERTIFICATE_FOR_TENANT)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveCertificate(
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId,
			@PathParam(QUERY_PARAMETER_TENANT_ID)
					String tenantId,
			@NotNull
					Reader certificateReader,
			@Context
					HttpServletRequest servletRequest) throws DuplicateEntryException, ResourceNotFoundException, CryptographicOperationException {

		Response response;
		try {
			CertificateEntity certificateEntity = objectMapper.readValue(certificateReader, CertificateEntity.class);
			certificateEntity.setTenantId(tenantId);

			Certificate certificateToValidate = PemUtils.certificateFromPem(certificateEntity.getCertificateContent());
			CryptoAPIX509Certificate cryptoAPIX509Certificate = CertificateTools.getCryptoX509Certificate(certificateToValidate);
			String signerCommonName = cryptoAPIX509Certificate.getIssuerDn().getCommonName();
			String platformName = cryptoAPIX509Certificate.getSubjectDn().getOrganization();
			certificateEntity.setPlatformName(platformName);
			CertificateEntity signerEntity = certificateRepository.findByName(signerCommonName);
			Certificate signerCertificate = PemUtils.certificateFromPem(signerEntity.getCertificateContent());
			CertificateValidationResult x509CertificateValidationResult = certificateValidationService
					.validateCertificate(certificateToValidate, signerCertificate);
			if (x509CertificateValidationResult.isValid()) {
				certificateRepository.save(certificateEntity);
				response = Response.ok().build();
			} else {
				response = Response.status(Response.Status.PRECONDITION_FAILED).build();
			}

		} catch (IOException | GeneralCryptoLibException e) {
			LOGGER.info("Error trying to save certificate for tenant.", e);
			response = Response.status(Response.Status.BAD_REQUEST).build();
		}
		return response;
	}

	/**
	 * Saves a certificate in the repository
	 *
	 * @param certificateReader
	 * @return
	 * @throws DuplicateEntryException
	 * @throws ResourceNotFoundException
	 * @throws CryptographicOperationException
	 */
	@POST
	@Path(SAVE_CERTIFICATE)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveCertificate(
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId,
			@NotNull
					Reader certificateReader,
			@Context
					HttpServletRequest servletRequest) throws DuplicateEntryException, ResourceNotFoundException, CryptographicOperationException {

		Response response;
		try {
			CertificateEntity certificateEntity = objectMapper.readValue(certificateReader, CertificateEntity.class);
			Certificate certificateToValidate = PemUtils.certificateFromPem(certificateEntity.getCertificateContent());
			CryptoAPIX509Certificate cryptoAPIX509Certificate = CertificateTools.getCryptoX509Certificate(certificateToValidate);
			String signerCommonName = cryptoAPIX509Certificate.getIssuerDn().getCommonName();
			CertificateEntity signerEntity = certificateRepository.findByName(signerCommonName);
			Certificate signerCertificate = PemUtils.certificateFromPem(signerEntity.getCertificateContent());

			CertificateValidationResult x509CertificateValidationResult = certificateValidationService
					.validateCertificate(certificateToValidate, signerCertificate);
			if (x509CertificateValidationResult.isValid()) {
				certificateRepository.save(certificateEntity);
				response = Response.ok().build();
			} else {
				response = Response.status(Response.Status.PRECONDITION_FAILED).build();
			}

		} catch (PersistenceException e) {
			throw new DuplicateEntryException("Error trying to save certificate.", e);
		} catch (IOException | GeneralCryptoLibException e) {
			LOGGER.info("Error trying to save certificate.", e);
			response = Response.status(Response.Status.BAD_REQUEST).build();
		}
		return response;

	}

	/**
	 * Searches for a certificate with the given parameters
	 *
	 * @param tenantId        - tenant identifier
	 * @param electionEventId - election event identifier
	 * @param certificateName - certificate name
	 * @return an object with the certificate data
	 * @throws DuplicateEntryException
	 * @throws ResourceNotFoundException
	 */
	@GET
	@Path(GET_CERTIFICATE_FOR_TENANT_EEID)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCertificate(
			@PathParam(QUERY_PARAMETER_TENANT_ID)
					String tenantId,
			@PathParam(QUERY_PARAMETER_ELECTION_EVENT_ID)
					String electionEventId,
			@PathParam(QUERY_PARAMETER_CERTIFICATE_NAME)
					String certificateName) throws DuplicateEntryException, ResourceNotFoundException {

		CertificateEntity certificateEntity = certificateRepository
				.findByTenanElectionEventAndCertificateName(tenantId, electionEventId, certificateName);
		return Response.ok(certificateEntity).build();

	}

	/**
	 * Searches for a certificate with the given parameters
	 *
	 * @param tenantId        - tenant identifier
	 * @param certificateName - certificate name
	 * @return an object with the certificate data
	 * @throws DuplicateEntryException
	 * @throws ResourceNotFoundException
	 */
	@GET
	@Path(GET_CERTIFICATE_FOR_TENANT)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCertificate(
			@PathParam(QUERY_PARAMETER_TENANT_ID)
					String tenantId,
			@PathParam(QUERY_PARAMETER_CERTIFICATE_NAME)
					String certificateName) throws DuplicateEntryException, ResourceNotFoundException {

		CertificateEntity certificateEntity = certificateRepository.findByTenantAndCertificateName(tenantId, certificateName);
		return Response.ok(certificateEntity).build();

	}

	/**
	 * Searches for a certificate with the given parameters
	 *
	 * @param certificateName - certificate name
	 * @return an object with the certificate data
	 * @throws DuplicateEntryException
	 * @throws ResourceNotFoundException
	 */
	@GET
	@Path(GET_CERTIFICATE)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCertificate(
			@PathParam(QUERY_PARAMETER_CERTIFICATE_NAME)
					String certificateName) throws DuplicateEntryException, ResourceNotFoundException {

		CertificateEntity certificateEntity = certificateRepository.findByName(certificateName);
		return Response.ok(certificateEntity).build();
	}

	/**
	 * Returns the result of validate if certificate exists for the given parameters
	 *
	 * @param tenantId - the tenant identifier.
	 * @param name     - the ballot box identifier.
	 * @return Returns the result of the validation.
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path(CHECK_IF_CERTIFICATE_EXIST)
	public Response checkIfCertificateExist(
			@PathParam(QUERY_PARAMETER_TENANT_ID)
					String tenantId,
			@PathParam(QUERY_PARAMETER_CERTIFICATE_NAME)
					String name) {

		LOGGER.info("Validating if exists any certificate for the tenant {} and name {} is empty.", tenantId, name);

		Long numCertificates = certificateRepository.checkIfCertificateExist(tenantId, name);

		ValidationResult validationResult = new ValidationResult();
		// set to true if we FOUND a certificate
		validationResult.setResult(numCertificates > 0);
		ValidationError validationError = new ValidationError();
		if (validationResult.isResult()) {
			validationError.setValidationErrorType(ValidationErrorType.SUCCESS);
		}
		validationResult.setValidationError(validationError);

		LOGGER.info(" Certificate {} exists?: {}", name, validationResult.isResult());

		return Response.ok().entity(validationResult).build();
	}

}
