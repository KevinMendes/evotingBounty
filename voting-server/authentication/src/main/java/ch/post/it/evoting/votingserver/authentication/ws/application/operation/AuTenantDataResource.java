/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.ws.application.operation;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.cryptolib.certificates.bean.X509CertificateType;
import ch.post.it.evoting.domain.election.model.tenant.TenantActivationData;
import ch.post.it.evoting.domain.election.model.tenant.TenantInstallationData;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.tenant.AuTenantKeystoreRepository;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.tenant.TenantKeystore;
import ch.post.it.evoting.votingserver.authentication.services.infrastructure.persistence.AuTenantSystemKeys;
import ch.post.it.evoting.votingserver.commons.beans.validation.ContextValidationResult;
import ch.post.it.evoting.votingserver.commons.beans.validation.ValidationType;
import ch.post.it.evoting.votingserver.commons.domain.model.tenant.TenantKeystoreRepository;
import ch.post.it.evoting.votingserver.commons.tenant.TenantActivator;

/**
 * Endpoint for uploading the information during the installation of the tenant
 */
@Path("tenantdata")
@Stateless(name = "au-tenantDataResource")
public class AuTenantDataResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(AuTenantDataResource.class);

	private static final String TENANT_PARAMETER = "tenantId";

	private static final String CONTEXT = "AU";

	@EJB
	@AuTenantKeystoreRepository
	TenantKeystoreRepository tenantKeystoreRepository;

	@EJB
	private AuTenantSystemKeys auTenantSystemKeys;

	/**
	 * Install the tenant keystores in the service.
	 *
	 * @param tenantId the ID of the tenant.
	 * @param data     json object containing the tenant information including the tenant certificate.
	 * @return
	 * @throws IllegalStateException
	 */
	@POST
	@Path("/tenant/{tenantId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveTenantData(
			@PathParam(TENANT_PARAMETER)
			final String tenantId, final TenantInstallationData data) {

		try {

			LOGGER.info("AU - install tenant request received");

			final TenantKeystore tenantKeystoreEntity = new TenantKeystore();
			tenantKeystoreEntity.setKeystoreContent(data.getEncodedData());
			tenantKeystoreEntity.setTenantId(tenantId);
			tenantKeystoreEntity.setKeyType(X509CertificateType.ENCRYPT.name());
			tenantKeystoreRepository.save(tenantKeystoreEntity);

			final TenantActivator tenantActivator = new TenantActivator(tenantKeystoreRepository, auTenantSystemKeys, CONTEXT);
			tenantActivator.activateUsingReceivedKeystore(tenantId, data.getEncodedData());

			return Response.ok().build();

		} catch (final Exception e) {
			final String errorMsg = "AU - error while trying to install a tenant: " + e.getMessage();
			LOGGER.error(errorMsg);
			throw new IllegalStateException(errorMsg, e);
		}
	}

	/**
	 * Activate a tenant in the service.
	 *
	 * @param tenantActivationData the tenant data.
	 * @return
	 * @throws IllegalStateException
	 */
	@POST
	@Path("activatetenant")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response activateTenant(final TenantActivationData tenantActivationData) {

		try {

			LOGGER.info("AU - activate tenant request received");

			final TenantActivator tenantActivator = new TenantActivator(tenantKeystoreRepository, auTenantSystemKeys, CONTEXT);
			tenantActivator.activateFromDB(tenantActivationData);

			return Response.ok().build();

		} catch (final Exception e) {
			final String errorMsg = "AU - error while trying to install a tenant: " + e.getMessage();
			LOGGER.error(errorMsg);
			throw new IllegalStateException(errorMsg, e);
		}
	}

	/**
	 * Check if a tenant has been activated for this context
	 *
	 * @param tenantId - identifier of the tenant
	 * @return
	 */
	@GET
	@Path("activatetenant/tenant/{tenantId}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response checkTenantActivation(
			@PathParam(TENANT_PARAMETER)
			final String tenantId) {

		final ContextValidationResult validations = new ContextValidationResult.Builder().setContextName(CONTEXT)
				.setValidationType(ValidationType.TENANT_ACTIVATION).setResult(auTenantSystemKeys.getInitialized(tenantId)).build();
		return Response.ok(validations).build();
	}
}
