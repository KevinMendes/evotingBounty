/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.commons.messaging;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;

import ch.post.it.evoting.votingserver.commons.messaging.MessagingService;
import ch.post.it.evoting.votingserver.commons.messaging.MessagingServiceImpl;

/**
 * Producer for {@link MessagingService}.
 */
@Dependent
public class MessagingServiceProducer {

	/**
	 * Returns a {@link MessagingService} instance.
	 *
	 * @return a {@link MessagingService} instance.
	 */
	@Produces
	@ApplicationScoped
	public MessagingService messagingService() {
		return new MessagingServiceImpl.Builder().setHostName(System.getenv("CC_MB_HOSTNAME")).setPort(Integer.parseInt(System.getenv("CC_MB_PORT")))
				.setVirtualHost(System.getenv("CC_MB_VHOST")).setUsername(System.getenv("CC_MB_USER")).setPassword(System.getenv("CC_MB_PASSWORD"))
				.setSenderPoolSize(Runtime.getRuntime().availableProcessors()).useSSL().build();
	}

	public void shutdownMessagingService(
			@Disposes
					MessagingService service) {
		service.shutdown();
	}
}
