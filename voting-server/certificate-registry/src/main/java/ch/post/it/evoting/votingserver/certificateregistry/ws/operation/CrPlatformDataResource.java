/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.certificateregistry.ws.operation;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.domain.election.model.platform.PlatformInstallationData;
import ch.post.it.evoting.votingserver.certificateregistry.services.domain.model.certificate.CertificateEntity;
import ch.post.it.evoting.votingserver.certificateregistry.services.domain.model.certificate.CertificateRepository;
import ch.post.it.evoting.votingserver.certificateregistry.services.domain.model.platform.CrCertificateValidationService;
import ch.post.it.evoting.votingserver.commons.beans.validation.CertificateValidationService;
import ch.post.it.evoting.votingserver.commons.domain.model.platform.PlatformInstallationDataHandler;

/**
 * Endpoint for upload the information during the installation of the platform in the system
 */
@Path(CrPlatformDataResource.RESOURCE_PATH)
@Stateless(name = "cr-platformDataResource")
public class CrPlatformDataResource {

	public static final String RESOURCE_PATH = "platformdata";

	private static final Logger LOGGER = LoggerFactory.getLogger(CrPlatformDataResource.class);

	private static final String CONTEXT = "CR";

	@Inject
	CertificateRepository certificateRepository;

	@Inject
	@CrCertificateValidationService
	CertificateValidationService certificateValidationService;

	/**
	 * Installs platform CA in the service.
	 *
	 * @param data all the platform data.
	 */

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response savePlatformData(final PlatformInstallationData data,
			@Context
					HttpServletRequest request) {

		PlatformInstallationDataHandler
				.savePlatformCertificateChain(data, certificateRepository, certificateValidationService, new CertificateEntity(),
						new CertificateEntity());

		LOGGER.debug(CONTEXT, " - platform CA successfully installed");

		return Response.ok().build();

	}
}
