/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.mixdec.domain.infrastructure.queues;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.votingserver.commons.messaging.MessagingException;
import ch.post.it.evoting.votingserver.orchestrator.mixdec.domain.services.MixDecBallotBoxService;
import ch.post.it.evoting.votingserver.orchestrator.mixdec.domain.services.MixDecKeyGenerationService;

/**
 * Defines any steps to be performed when the ORCHESTRATOR context is first initialized and destroyed.
 */
public class OrchestratorMixDecQueuesContextListener implements ServletContextListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(OrchestratorMixDecQueuesContextListener.class);

	private static final String CONTEXT = "OR";

	@Inject
	MixDecKeyGenerationService mixDecKeyGenerationService;

	@Inject
	MixDecBallotBoxService mixDecBallotBoxService;

	/**
	 * @see ServletContextListener#contextInitialized(ServletContextEvent)
	 */
	@Override
	public void contextInitialized(final ServletContextEvent servletContextEvent) {

		LOGGER.info(CONTEXT + " - triggering the consumption of the Control Components response queues");

		try {
			mixDecKeyGenerationService.startup();
			mixDecBallotBoxService.startup();
		} catch (MessagingException e) {
			throw new IllegalStateException("Error consuming a Control Component response queue: ", e);
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
