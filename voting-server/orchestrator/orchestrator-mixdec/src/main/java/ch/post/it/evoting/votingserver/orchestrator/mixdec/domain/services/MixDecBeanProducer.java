/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.mixdec.domain.services;

import java.time.Duration;
import java.util.List;

import javax.ejb.Stateless;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;

import ch.post.it.evoting.votingserver.commons.messaging.MessageListener;
import ch.post.it.evoting.votingserver.orchestrator.commons.infrastructure.persistence.Jdbc;
import ch.post.it.evoting.votingserver.orchestrator.commons.infrastructure.persistence.PartialResultsRepository;
import ch.post.it.evoting.votingserver.orchestrator.commons.messaging.PartialResultsReadyListener;
import ch.post.it.evoting.votingserver.orchestrator.commons.polling.PollingService;
import ch.post.it.evoting.votingserver.orchestrator.commons.polling.PollingServiceImpl;
import ch.post.it.evoting.votingserver.orchestrator.commons.polling.ReactivePartialResultsHandlerImpl;
import ch.post.it.evoting.votingserver.orchestrator.commons.polling.ReactiveResultsHandler;

@Stateless
public class MixDecBeanProducer {

	private static final Duration KEY_GENERATION_POLLING_TIMEOUT = getDuration("mixing_key_generation_polling_timeout_ms", Duration.ofMillis(20000));

	private static final Duration KEY_GENERATION_INTER_POLLS_DELAY = getDuration("mixing_key_generation_inter_polls_delay_ms",
			Duration.ofMillis(100));

	private static Duration getDuration(String propertyName, Duration defaultValue) {
		String propertyValue = System.getenv(propertyName);
		if (propertyValue == null) {
			return defaultValue;
		}
		return Duration.ofMillis(Long.parseLong(propertyValue));
	}

	@Produces
	@ApplicationScoped
	@MixDecKeyGeneration
	public ReactiveResultsHandler<List<byte[]>> keyGenerationResultsHandler(
			@Jdbc
					PartialResultsRepository<byte[]> repository) {
		Duration timeout = KEY_GENERATION_POLLING_TIMEOUT.multipliedBy(2);
		ReactivePartialResultsHandlerImpl<byte[]> handler = new ReactivePartialResultsHandlerImpl<>(repository, timeout);
		handler.start();
		return handler;
	}

	public void stopKeyGenerationResultsHandler(
			@Disposes
			@MixDecKeyGeneration
					ReactiveResultsHandler<List<byte[]>> handler) {
		((ReactivePartialResultsHandlerImpl<byte[]>) handler).stop();
	}

	@Produces
	@MixDecKeyGeneration
	public PollingService<List<byte[]>> keyGenerationPollingService(
			@MixDecKeyGeneration
					ReactiveResultsHandler<List<byte[]>> handler) {
		return new PollingServiceImpl<>(handler, KEY_GENERATION_POLLING_TIMEOUT, KEY_GENERATION_INTER_POLLS_DELAY);
	}

	@Produces
	@MixDecKeyGeneration
	@ResultsReady
	public MessageListener keyGenerationResultsReadyListener(
			@MixDecKeyGeneration
					ReactiveResultsHandler<List<byte[]>> handler) {
		return new PartialResultsReadyListener(handler);
	}
}
