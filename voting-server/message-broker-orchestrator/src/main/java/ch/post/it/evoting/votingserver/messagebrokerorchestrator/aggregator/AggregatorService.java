/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.messagebrokerorchestrator.aggregator;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.benmanes.caffeine.cache.Cache;

import ch.post.it.evoting.distributedprocessing.commands.CommandService;
import ch.post.it.evoting.votingserver.messagebrokerorchestrator.MessageBrokerOrchestratorApplication;

@Service
public class AggregatorService {

	static final String AGGREGATOR_TOPIC_EXCHANGE = "aggregator-topic-exchange";
	static final String ROUTING_KEY = "aggregator";

	private static final Logger LOGGER = LoggerFactory.getLogger(AggregatorService.class);

	private final Cache<String, CompletableFuture<String>> inFlightRequests;
	private final BroadcastAggregatorProducer broadcastAggregatorProducer;
	private final CommandService commandService;
	private final ApplicationEventPublisher applicationEventPublisher;

	public AggregatorService(
			final Cache<String, CompletableFuture<String>> inFlightRequests,
			final BroadcastAggregatorProducer broadcastAggregatorProducer,
			final CommandService commandService,
			final ApplicationEventPublisher applicationEventPublisher) {
		this.inFlightRequests = inFlightRequests;
		this.broadcastAggregatorProducer = broadcastAggregatorProducer;
		this.commandService = commandService;
		this.applicationEventPublisher = applicationEventPublisher;
	}

	@Transactional
	public void notifyPartialResponseReceived(final String correlationId, final String contextId) {
		if (commandService.countAllCommandsWithResponsePayload(correlationId) == MessageBrokerOrchestratorApplication.NODE_IDS.size()) {
			LOGGER.info("All nodes have returned their contributions. [contextId: {}, correlationId: {}]", contextId, correlationId);
			applicationEventPublisher.publishEvent(new LocalAggregatorApplicationEvent(this, correlationId));
		}
	}

	@EventListener
	public void processLocalAggregatorEvent(final LocalAggregatorApplicationEvent localAggregatorApplicationEvent) {
		final String correlationId = localAggregatorApplicationEvent.getCorrelationId();

		final CompletableFuture<String> requestsIfPresent = inFlightRequests.getIfPresent(correlationId);
		if (requestsIfPresent != null) {
			notifyController(correlationId, requestsIfPresent);
		} else {
			broadcastAggregatorProducer.broadcastAggregatorMessage(correlationId);
			LOGGER.info("Found no entry for correlationId locally. Sent aggregator event to aggregator topic. [correlationId: {}]", correlationId);
		}
	}

	void processBroadcastAggregatorMessage(final BroadcastAggregatorMessage broadcastAggregatorMessage) {
		final String correlationId = broadcastAggregatorMessage.getCorrelationId();

		final CompletableFuture<String> requestsIfPresent = inFlightRequests.getIfPresent(correlationId);
		if (requestsIfPresent != null) {
			notifyController(correlationId, requestsIfPresent);
		} else {
			LOGGER.debug(
					"Found no entry for aggregator message corresponding to correlationId locally, no further action required. [correlationId: {}]",
					correlationId);
		}
	}

	private void notifyController(String correlationId, CompletableFuture<String> completableFuture) {
		completableFuture.complete(correlationId);
		LOGGER.info("Found entry for correlationId locally. CompletableFuture completed, controller can now return the response. [correlationId: {}]",
				correlationId);
	}
}
