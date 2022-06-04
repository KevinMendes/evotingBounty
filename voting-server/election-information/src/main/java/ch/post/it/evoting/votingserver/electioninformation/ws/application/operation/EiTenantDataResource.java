/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.ws.application.operation;

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
import ch.post.it.evoting.votingserver.commons.beans.validation.ContextValidationResult;
import ch.post.it.evoting.votingserver.commons.beans.validation.ValidationType;
import ch.post.it.evoting.votingserver.commons.domain.model.tenant.TenantKeystoreRepository;
import ch.post.it.evoting.votingserver.commons.tenant.TenantActivator;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.tenant.EiTenantKeystoreRepository;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.tenant.EiTenantSystemKeys;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.tenant.TenantKeystore;

/**
 * Endpoint for upload the information during the installation of the tenant in the system
 */
@Path(EiTenantDataResource.RESOURCE_PATH)
@Stateless(name = "ei-tenantDataResource")
public class EiTenantDataResource {

	public static final String RESOURCE_PATH = "tenantdata";

	private static final Logger LOGGER = LoggerFactory.getLogger(EiTenantDataResource.class);

	private static final String TENANT_PARAMETER = "tenantId";

	private static final String CONTEXT = "EI";

	@EJB
	@EiTenantKeystoreRepository
	TenantKeystoreRepository tenantKeystoreRepository;

	@EJB
	private EiTenantSystemKeys eiTenantSystemKeys;

	/**
	 * Installs the tenant keystores in the Service
	 *
	 * @param data, json object containing the information
	 * @return
	 * @throws Exception
	 */
	@POST
	@Path("/tenant/{tenantId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveTenantData(
			@PathParam(TENANT_PARAMETER)
			final String tenantId, final TenantInstallationData data) {
		TenantKeystore tenantKeystoreEntity = new TenantKeystore();
		try {

			LOGGER.info("EI - install tenant request received");

			tenantKeystoreEntity.setKeystoreContent(data.getEncodedData());
			tenantKeystoreEntity.setTenantId(tenantId);
			tenantKeystoreEntity.setKeyType(X509CertificateType.ENCRYPT.name());

			tenantKeystoreRepository.save(tenantKeystoreEntity);

			TenantActivator tenantActivator = new TenantActivator(tenantKeystoreRepository, eiTenantSystemKeys, CONTEXT);
			tenantActivator.activateUsingReceivedKeystore(tenantId, data.getEncodedData());

			return Response.ok().build();

		} catch (Exception e) {
			String errorMsg = "EI - error while trying to install a tenant: " + e.getMessage();
			LOGGER.error(tenantKeystoreEntity.toString());
			LOGGER.error(errorMsg);
			throw new IllegalStateException(errorMsg, e);
		}
	}

	@POST
	@Path("activatetenant")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response activateTenant(final TenantActivationData tenantActivationData) {

		try {

			LOGGER.info("EI - activate tenant request received");

			TenantActivator tenantActivator = new TenantActivator(tenantKeystoreRepository, eiTenantSystemKeys, CONTEXT);
			tenantActivator.activateFromDB(tenantActivationData);

			return Response.ok().build();

		} catch (Exception e) {
			String errorMsg = "EI - error while trying to install a tenant: " + e.getMessage();
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

		ContextValidationResult validation = new ContextValidationResult.Builder().setContextName(CONTEXT)
				.setValidationType(ValidationType.TENANT_ACTIVATION).setResult(eiTenantSystemKeys.getInitialized(tenantId)).build();
		return Response.ok(validation).build();
	}
}
