/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.ws.application.operation;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.domain.election.model.platform.PlatformInstallationData;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.platform.AuCertificateValidationService;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.platform.PlatformCertificate;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.platform.PlatformCertificateRepository;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.validation.CertificateValidationService;
import ch.post.it.evoting.votingserver.commons.domain.model.platform.PlatformCARepository;
import ch.post.it.evoting.votingserver.commons.domain.model.platform.PlatformInstallationDataHandler;

/**
 * Endpoint for uploading the information during the installation of the platform
 */
@Path("platformdata")
@Stateless(name = "au-platformDataResource")
public class AuPlatformDataResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(AuPlatformDataResource.class);

	private static final String CONTEXT = "AU";

	@EJB
	@PlatformCertificateRepository
	PlatformCARepository platformRepository;

	@EJB
	@AuCertificateValidationService
	CertificateValidationService certificateValidationService;

	/**
	 * Install the logging keystores and platform CA in the authentication context (AU).
	 *
	 * @param data the platform data.
	 * @throws IllegalStateException
	 * @throws DuplicateEntryException
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response savePlatformData(final PlatformInstallationData data) {

		PlatformInstallationDataHandler
				.savePlatformCertificateChain(data, platformRepository, certificateValidationService, new PlatformCertificate(),
						new PlatformCertificate());

		LOGGER.debug(CONTEXT, " - platform CA successfully installed");

		return Response.ok().build();
	}
}
