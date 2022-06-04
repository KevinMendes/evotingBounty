/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.infrastructure.log;

import java.util.List;

import javax.ejb.EJB;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.votingserver.commons.domain.model.tenant.TenantKeystoreRepository;
import ch.post.it.evoting.votingserver.commons.tenant.TenantActivator;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.tenant.EiTenantKeystoreRepository;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.tenant.EiTenantSystemKeys;

/**
 * Defines any steps to be performed when the EI context is first initialized and destroyed.
 */
public class EiContextListener implements ServletContextListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(EiContextListener.class);

	private static final String CONTEXT = "EI";

	@EJB
	@EiTenantKeystoreRepository
	TenantKeystoreRepository tenantKeystoreRepository;

	@EJB
	private EiTenantSystemKeys eiTenantSystemKeys;

	/**
	 * @see ServletContextListener#contextInitialized(ServletContextEvent)
	 */
	@Override
	public void contextInitialized(final ServletContextEvent servletContextEvent) {

		LOGGER.info(CONTEXT + " - context initialized, will attempt to activate tenant if data exists in DB");

		final TenantActivator tenantActivator = new TenantActivator(tenantKeystoreRepository, eiTenantSystemKeys, CONTEXT);

		final List<String> tenantIDsActivatedTenants = tenantActivator.activateTenantsFromDbAndFiles();

		LOGGER.info(CONTEXT + " - completed tenant activation process");
		for (final String tenantID : tenantIDsActivatedTenants) {
			LOGGER.info(CONTEXT + " - activated tenant: {}", tenantID);
		}
	}

	/**
	 * @see ServletContextListener#contextDestroyed(ServletContextEvent)
	 */
	@Override
	public void contextDestroyed(final ServletContextEvent sce) {
		// Nothing to do on context destruction.
	}
}
