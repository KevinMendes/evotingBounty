/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.messagebrokerorchestrator.voting;

import static com.google.common.base.Preconditions.checkNotNull;

import java.io.UncheckedIOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.benmanes.caffeine.cache.Cache;

import ch.post.it.evoting.distributedprocessing.commands.Command;
import ch.post.it.evoting.distributedprocessing.commands.CommandService;
import ch.post.it.evoting.domain.Context;
import ch.post.it.evoting.votingserver.messagebrokerorchestrator.MessageBrokerOrchestratorApplication;

/**
 * Provides functionality for sending requests and collecting the contributions.
 */
@Service
public class BroadcastProducerService {

	private static final Logger LOGGER = LoggerFactory.getLogger(BroadcastProducerService.class);

	private static final String RABBITMQ_EXCHANGE = "evoting-exchange";

	private final RabbitTemplate rabbitTemplate;
	private final ObjectMapper objectMapper;
	private final CommandFacade commandFacade;
	private final CommandService commandService;
	private final Cache<String, CompletableFuture<String>> inFlightRequests;

	@Value("${controller.completable.future.timeout.seconds}")
	private int controllerCompletableFutureTimeout;

	public BroadcastProducerService(final RabbitTemplate rabbitTemplate,
			final ObjectMapper objectMapper, final CommandFacade commandFacade,
			final CommandService commandService,
			final Cache<String, CompletableFuture<String>> inFlightRequests) {
		this.rabbitTemplate = rabbitTemplate;
		this.objectMapper = objectMapper;
		this.commandFacade = commandFacade;
		this.commandService = commandService;
		this.inFlightRequests = inFlightRequests;
	}

	/**
	 * Sends a request for the calculation of the contributions to the queues with the given name pattern and collects the responses.
	 *
	 * @param contextId      the context id for which to collect the contributions. Must be non-null.
	 * @param context        the context in which the contributions are collected. Must be non-null.
	 * @param requestPayload the payload used for calculating the contributions. Must be non-null.
	 * @param queuePattern   the name pattern of the queues to which the request has to be sent. Must be non-null.
	 * @return a list of the collected contributions
	 * @throws ExecutionException   if the handling of the requests encountered an exception.
	 * @throws InterruptedException if the waiting was interrupted.
	 * @throws TimeoutException     if the defined limit of waiting was exceeded without receiving all the responses.
	 * @throws NullPointerException if any of the inputs is null.
	 */
	public <R> List<R> sendMessagesAwaitingNotification(final String contextId,
			final Context context, final Object requestPayload, final String queuePattern, final Function<byte[], R> deserialization)
			throws ExecutionException, InterruptedException, TimeoutException {
		checkNotNull(contextId, "Context Id must not be null");
		checkNotNull(context, "Context must not be null");
		checkNotNull(requestPayload, "Payload must not be null");
		checkNotNull(queuePattern, "Queue pattern must not be null");
		final String correlationId = UUID.randomUUID().toString();

		LOGGER.info("Starting calculations. [contextId: {}, correlationId: {}]", contextId, correlationId);

		final CompletableFuture<String> completableFuture = new CompletableFuture<>();
		inFlightRequests.put(correlationId, completableFuture);

		sendAllMessages(contextId, context, correlationId, requestPayload, queuePattern);

		//Wait for all contributions to have returned or timeout
		completableFuture.get(controllerCompletableFutureTimeout, TimeUnit.SECONDS);
		inFlightRequests.invalidate(correlationId);

		LOGGER.info("All nodes have completed their calculations. [contextId: {}, correlationId: {}]", contextId, correlationId);

		final List<Command> allMessagesWithCorrelationId = commandFacade.getAllNodesResponses(correlationId);

		return allMessagesWithCorrelationId.stream()
				.map(Command::getResponsePayload)
				.map(deserialization)
				.collect(Collectors.toList());
	}

	/**
	 * Sends a request to each node through the queues with the specified name pattern.
	 *
	 * @param contextId      the context id. Must be non-null.
	 * @param context        the context in which the request is sent. Must be non-null.
	 * @param correlationId  the correlation id that must be used by the responses. Must be non-null.
	 * @param requestPayload the request payload to be sent. Must be non-null.
	 * @param queuePattern   the name pattern of the queue to which to send the message. Must be non-null.
	 * @throws NullPointerException if any of the inputs is null.
	 */
	private void sendAllMessages(final String contextId, final Context context, final String correlationId, final Object requestPayload,
			final String queuePattern) {
		checkNotNull(contextId);
		checkNotNull(context);
		checkNotNull(correlationId);
		checkNotNull(requestPayload);
		checkNotNull(queuePattern);

		if (commandService.isRequestForContextIdAndContextAlreadyPresent(contextId, context.toString())) {
			LOGGER.warn("Request already sent [contextId: {}, context: {}]", contextId, context);
		}

		byte[] payload;
		try {
			payload = objectMapper.writeValueAsBytes(requestPayload);
		} catch (JsonProcessingException e) {
			throw new UncheckedIOException(e);
		}

		MessageBrokerOrchestratorApplication.NODE_IDS
				.forEach(nodeId -> {
					commandFacade.saveRequest(payload, correlationId, contextId, context, nodeId);
					MessageProperties messageProperties = new MessageProperties();
					messageProperties.setCorrelationId(correlationId);
					Message message = new Message(payload, messageProperties);
					rabbitTemplate.send(RABBITMQ_EXCHANGE, queuePattern + nodeId, message);
					LOGGER.debug("Message sent. [contextId: {}, context: {}, nodeId: {}]", contextId, context, nodeId);
				});
	}


}
