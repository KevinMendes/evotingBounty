/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.ws.operation;

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
import ch.post.it.evoting.votingserver.commons.beans.validation.CertificateValidationService;
import ch.post.it.evoting.votingserver.commons.domain.model.platform.PlatformCARepository;
import ch.post.it.evoting.votingserver.commons.domain.model.platform.PlatformInstallationDataHandler;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.platform.PlatformCertificate;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.platform.VwCertificateValidationService;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.platform.VwPlatformCARepository;

/**
 * Endpoint for uploading the information during the installation of the platform in the system
 */
@Path(VwPlatformDataResource.RESOURCE_PATH)
@Stateless(name = "vw-platformDataResource")
public class VwPlatformDataResource {

	public static final String RESOURCE_PATH = "platformdata";

	private static final Logger LOGGER = LoggerFactory.getLogger(VwPlatformDataResource.class);

	@EJB
	@VwPlatformCARepository
	PlatformCARepository platformRepository;

	@EJB
	@VwCertificateValidationService
	CertificateValidationService certificateValidationService;

	/**
	 * Installs the platform CA in the service.
	 *
	 * @param data all the platform data.
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response savePlatformData(final PlatformInstallationData data) {

		PlatformInstallationDataHandler
				.savePlatformCertificateChain(data, platformRepository, certificateValidationService, new PlatformCertificate(),
						new PlatformCertificate());

		LOGGER.debug("VW - platform CA successfully installed.");

		return Response.ok().build();
	}
}
