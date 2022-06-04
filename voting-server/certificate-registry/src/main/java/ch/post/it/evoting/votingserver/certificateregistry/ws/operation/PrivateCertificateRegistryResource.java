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
import ch.post.it.evoting.votingserver.certificateregistry.services.domain.model.certificate.CertificateEntity;
import ch.post.it.evoting.votingserver.certificateregistry.services.domain.model.service.CertificateService;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.commons.ui.Constants;

/**
 * Private Certificate Registry Resource
 */
@Path("certificates/private")
@Stateless(name = "cert-PrivateCertificatesDataResource")
public class PrivateCertificateRegistryResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(PrivateCertificateRegistryResource.class);

	private static final String QUERY_PARAMETER_TENANT_ID = "tenantId";

	private static final String QUERY_PARAMETER_ELECTION_EVENT_ID = "electionEventId";

	private static final ObjectMapper objectMapper = DomainObjectMapper.getNewInstance();

	@Inject
	private CertificateService certificateService;

	// The track id instance
	@Inject
	private TrackIdInstance trackIdInstance;

	@POST
	@Path("tenant/{tenantId}/electionevent/{electionEventId}")
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
					HttpServletRequest request) throws DuplicateEntryException, ResourceNotFoundException, CryptographicOperationException {

		trackIdInstance.setTrackId(trackingId);

		Response response;
		try {
			CertificateEntity certificateEntity = objectMapper.readValue(certificateReader, CertificateEntity.class);
			certificateEntity.setElectionEventId(electionEventId);
			certificateEntity.setTenantId(tenantId);

			X509CertificateValidationResult validationResult = certificateService.saveCertificateInDB(certificateEntity);
			if (!validationResult.isValidated()) {
				response = Response.status(Response.Status.PRECONDITION_FAILED).build();
			} else {
				response = Response.ok().build();
			}

		} catch (IOException | GeneralCryptoLibException e) {
			LOGGER.error("Failed to save certificate.", e);
			response = Response.status(Response.Status.BAD_REQUEST).build();
		}
		return response;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveCertificate(
			@HeaderParam(Constants.PARAMETER_X_REQUEST_ID)
					String trackingId,
			@NotNull
					Reader certificateReader,
			@Context
					HttpServletRequest request) throws DuplicateEntryException, ResourceNotFoundException, CryptographicOperationException {

		trackIdInstance.setTrackId(trackingId);

		Response response;
		try {
			CertificateEntity certificateEntity = objectMapper.readValue(certificateReader, CertificateEntity.class);

			X509CertificateValidationResult validationResult = certificateService.saveCertificateInDB(certificateEntity);
			if (!validationResult.isValidated()) {
				response = Response.status(Response.Status.PRECONDITION_FAILED).build();
			} else {
				response = Response.ok().build();
			}
		} catch (IOException | GeneralCryptoLibException e) {
			LOGGER.error("Failed to save certificate.", e);
			response = Response.status(Response.Status.BAD_REQUEST).build();
		}
		return response;
	}
}
