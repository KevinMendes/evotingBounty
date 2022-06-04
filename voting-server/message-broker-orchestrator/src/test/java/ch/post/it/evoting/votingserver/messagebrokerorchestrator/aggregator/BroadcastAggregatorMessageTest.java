/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.messagebrokerorchestrator.aggregator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;

class BroadcastAggregatorMessageTest {

	@Test
	void deserializationTest() throws IOException {
		final ObjectMapper objectMapper = DomainObjectMapper.getNewInstance();

		final String messageJson = "{\"correlationId\":\"f65f5634-9865-46d1-b91a-fab76f733bad\"}";
		final Message message = new Message(messageJson.getBytes(StandardCharsets.UTF_8));

		final BroadcastAggregatorMessage broadcastAggregatorMessage = objectMapper.readValue(message.getBody(),
				BroadcastAggregatorMessage.class);

		assertEquals("f65f5634-9865-46d1-b91a-fab76f733bad", broadcastAggregatorMessage.getCorrelationId());
	}

}