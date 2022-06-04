/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.choicecodes.infrastructure.queues;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.votingserver.commons.messaging.MessagingException;
import ch.post.it.evoting.votingserver.orchestrator.choicecodes.domain.services.ChoiceCodesGenerationContributionsService;

/**
 * Defines any steps to be performed when the ORCHESTRATOR context is first initialized and destroyed.
 */
public class OrchestratorChoiceCodesQueuesContextListener implements ServletContextListener {

	private static final Logger LOGGER = LoggerFactory.getLogger(OrchestratorChoiceCodesQueuesContextListener.class);

	private static final String CONTEXT = "OR";

	@Inject
	ChoiceCodesGenerationContributionsService choiceCodesGenerationContributionsService;

	@Override
	public void contextInitialized(final ServletContextEvent servletContextEvent) {

		LOGGER.info(CONTEXT + " - triggering the consumption of the Control Components response queues");

		try {
			choiceCodesGenerationContributionsService.startup();
		} catch (MessagingException e) {
			throw new IllegalStateException("Error consuming a Control Component response queue: ", e);
		}

	}

	@Override
	public void contextDestroyed(final ServletContextEvent sce) {
		LOGGER.info(CONTEXT + " - triggering the disconnetion of the Control Components response queues");

		try {
			choiceCodesGenerationContributionsService.shutdown();
		} catch (MessagingException e) {
			throw new IllegalStateException("Error disconnectiong a Control Component response queue: ", e);
		}
	}
}
