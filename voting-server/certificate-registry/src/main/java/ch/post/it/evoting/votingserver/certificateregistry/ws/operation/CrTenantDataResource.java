/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.certificateregistry.ws.operation;

import java.io.IOException;
import java.io.Reader;

import javax.ejb.Stateless;
import javax.inject.Inject;
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
import ch.post.it.evoting.cryptolib.certificates.configuration.X509CertificateValidationResult;
import ch.post.it.evoting.cryptolib.certificates.utils.CryptographicOperationException;
import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.domain.election.model.tenant.TenantInstallationData;
import ch.post.it.evoting.votingserver.certificateregistry.services.domain.model.certificate.CertificateEntity;
import ch.post.it.evoting.votingserver.certificateregistry.services.domain.model.certificate.CertificateRepository;
import ch.post.it.evoting.votingserver.certificateregistry.services.domain.model.service.CertificateService;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.commons.ui.Constants;

/**
 * Endpoint for upload the information during the installation of the tenant in the system
 */
@Path(CrTenantDataResource.RESOURCE_PATH)
@Stateless(name = "cr-tenantDataResource")
public class CrTenantDataResource {

	public static final String RESOURCE_PATH = "tenantdata";

	public static final String SAVE_TENANT_DATA = "secured/tenant/{tenantId}";

	public static final String GET_TENANT_CA_CERTIFICATE = "/tenant/{tenantId}";

	private static final Logger LOGGER = LoggerFactory.getLogger(CrTenantDataResource.class);

	// The name of the query parameter tenantId
	private static final String QUERY_PARAMETER_TENANT_ID = "tenantId";

	@Inject
	private CertificateRepository certificateRepository;

	@Inject
	private CertificateService certificateService;

	// The track id instance
	@Inject
	private TrackIdInstance trackIdInstance;

	/**
	 * Stores
	 *
	 * @param tenantId
	 * @param data
	 * @return
	 * @throws CryptographicOperationException
	 * @throws ResourceNotFoundException
	 * @throws DuplicateEntryException
	 */
	@Path("secured/tenant/{tenantId}")
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveTenantData(
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId,
			@PathParam(QUERY_PARAMETER_TENANT_ID)
			final String tenantId,
			@NotNull
			final Reader data,
			@Context
					HttpServletRequest request) throws CryptographicOperationException, ResourceNotFoundException, DuplicateEntryException {

		trackIdInstance.setTrackId(trackingId);

		Response response;

		LOGGER.info("CR - received install tenant request");

		try {
			ObjectMapper objectMapper = DomainObjectMapper.getNewInstance();
			TenantInstallationData tenantInstallationData = objectMapper.readValue(data, TenantInstallationData.class);
			CertificateEntity certificateEntity = new CertificateEntity();
			certificateEntity.setCertificateContent(tenantInstallationData.getEncodedData());
			certificateEntity.setTenantId(tenantId);
			X509CertificateValidationResult validationResult = certificateService.saveCertificateInDB(certificateEntity);
			if (!validationResult.isValidated()) {

				LOGGER.error("CR - validation failed. Setting response to failed");

				response = Response.status(Response.Status.PRECONDITION_FAILED).entity(validationResult.getFailedValidationTypes()).build();
			} else {
				LOGGER.info("CR - validation passed");

				response = Response.ok().build();
			}

		} catch (IOException | GeneralCryptoLibException e) {
			LOGGER.info("Error trying to save tenant data.", e);
			response = Response.status(Response.Status.BAD_REQUEST).build();
		}
		return response;

	}

	/**
	 * Searches the tenant certificate
	 *
	 * @return an object with the certificate data
	 * @throws DuplicateEntryException
	 * @throws ResourceNotFoundException
	 */
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Path("/tenant/{tenantId}")
	public Response getTenantCACertificate(
			@PathParam(QUERY_PARAMETER_TENANT_ID)
			final String tenantId) throws DuplicateEntryException, ResourceNotFoundException {

		CertificateEntity certificateEntity = certificateRepository.getTenantCertificate(tenantId);
		return Response.ok(certificateEntity).build();
	}

}
