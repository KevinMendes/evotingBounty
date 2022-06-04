/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.infrastructure.log;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Defines any steps to be performed when the VW context is first initialized and destroyed.
 */
public class VwContextListener implements ServletContextListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(VwContextListener.class);

	private static final String TWO_PARAMETERS_TEMPLATE = "{} - {}";

	private static final String CONTEXT = "VW";

	/**
	 * @see ServletContextListener#contextInitialized(ServletContextEvent)
	 */
	@Override
	public void contextInitialized(final ServletContextEvent servletContextEvent) {
		LOGGER.info(TWO_PARAMETERS_TEMPLATE, CONTEXT, "context initialized");
	}

	/**
	 * @see ServletContextListener#contextDestroyed(ServletContextEvent)
	 */
	@Override
	public void contextDestroyed(final ServletContextEvent sce) {
		// Nothing to do on context destruction.
	}
}
