/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.messagebrokerorchestrator.aggregator;

import static ch.post.it.evoting.votingserver.messagebrokerorchestrator.aggregator.AggregatorService.AGGREGATOR_TOPIC_EXCHANGE;
import static ch.post.it.evoting.votingserver.messagebrokerorchestrator.aggregator.AggregatorService.ROUTING_KEY;

import java.io.UncheckedIOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class BroadcastAggregatorProducer {

	private static final Logger LOGGER = LoggerFactory.getLogger(BroadcastAggregatorProducer.class);

	private final RabbitTemplate rabbitTemplate;
	private final ObjectMapper objectMapper;

	public BroadcastAggregatorProducer(final RabbitTemplate rabbitTemplate, final ObjectMapper objectMapper) {
		this.rabbitTemplate = rabbitTemplate;
		this.objectMapper = objectMapper;
	}

	public void broadcastAggregatorMessage(final String correlationId) {
		final BroadcastAggregatorMessage broadcastAggregatorMessage = new BroadcastAggregatorMessage(correlationId);
		final byte[] encodedAggregatorEvent;
		try {
			encodedAggregatorEvent = objectMapper.writeValueAsBytes(broadcastAggregatorMessage);
		} catch (JsonProcessingException e) {
			throw new UncheckedIOException(e);
		}
		final Message message = new Message(encodedAggregatorEvent);
		rabbitTemplate.send(AGGREGATOR_TOPIC_EXCHANGE, ROUTING_KEY, message);

		LOGGER.info("Sent aggregator message for correlationId. [correlationId: {}]", correlationId);
	}
}
