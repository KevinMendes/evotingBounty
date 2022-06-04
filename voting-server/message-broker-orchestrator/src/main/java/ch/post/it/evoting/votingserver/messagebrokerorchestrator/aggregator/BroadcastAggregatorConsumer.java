/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.messagebrokerorchestrator.aggregator;

import static ch.post.it.evoting.votingserver.messagebrokerorchestrator.aggregator.AggregatorService.AGGREGATOR_TOPIC_EXCHANGE;
import static ch.post.it.evoting.votingserver.messagebrokerorchestrator.aggregator.AggregatorService.ROUTING_KEY;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class BroadcastAggregatorConsumer {

	private static final Logger LOGGER = LoggerFactory.getLogger(BroadcastAggregatorConsumer.class);

	private final AggregatorService aggregatorService;
	private final ObjectMapper objectMapper;

	public BroadcastAggregatorConsumer(final AggregatorService aggregatorService, final ObjectMapper objectMapper) {
		this.aggregatorService = aggregatorService;
		this.objectMapper = objectMapper;
	}

	@RabbitListener(bindings = @QueueBinding(value = @Queue, exchange = @Exchange(name = AGGREGATOR_TOPIC_EXCHANGE, type = ExchangeTypes.TOPIC), key = ROUTING_KEY))
	public void consumer(final Message message) throws IOException {

		final byte[] encodedAggregatorEvent = message.getBody();
		final BroadcastAggregatorMessage broadcastAggregatorMessage = objectMapper.readValue(encodedAggregatorEvent,
				BroadcastAggregatorMessage.class);
		final String correlationId = broadcastAggregatorMessage.getCorrelationId();
		checkNotNull(correlationId, "Correlation Id should not be null");

		LOGGER.info("Received aggregator message for correlation Id {} ", correlationId);

		aggregatorService.processBroadcastAggregatorMessage(broadcastAggregatorMessage);
	}
}
