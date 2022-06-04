/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.infrastructure.log;

import java.util.List;

import javax.ejb.EJB;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.votingserver.authentication.services.domain.model.tenant.AuTenantKeystoreRepository;
import ch.post.it.evoting.votingserver.authentication.services.infrastructure.persistence.AuTenantSystemKeys;
import ch.post.it.evoting.votingserver.commons.domain.model.tenant.TenantKeystoreRepository;
import ch.post.it.evoting.votingserver.commons.tenant.TenantActivator;

/**
 * Defines any steps to be performed when the AU context is first initialized and destroyed.
 */
public class AuContextListener implements ServletContextListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(AuContextListener.class);

	private static final String CONTEXT = "AU";

	@EJB
	@AuTenantKeystoreRepository
	TenantKeystoreRepository tenantKeystoreRepository;

	@EJB
	private AuTenantSystemKeys auTenantSystemKeys;

	/**
	 * @see ServletContextListener#contextInitialized(ServletContextEvent)
	 */
	@Override
	public void contextInitialized(final ServletContextEvent servletContextEvent) {

		LOGGER.info(CONTEXT + " - context initialized, will attempt to activate tenant if data exists in DB");

		final TenantActivator tenantActivator = new TenantActivator(tenantKeystoreRepository, auTenantSystemKeys, CONTEXT);

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
		// This method is intentionally left blank
	}
}
