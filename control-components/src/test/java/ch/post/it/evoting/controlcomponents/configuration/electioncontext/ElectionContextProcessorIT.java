/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.configuration.electioncontext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.after;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;

import ch.post.it.evoting.controlcomponents.ElectionEventService;
import ch.post.it.evoting.controlcomponents.TestDatabaseCleanUpService;
import ch.post.it.evoting.controlcomponents.voting.VotingIntegrationTestBase;
import ch.post.it.evoting.cryptoprimitives.test.tools.data.GroupTestData;
import ch.post.it.evoting.distributedprocessing.commands.Command;
import ch.post.it.evoting.distributedprocessing.commands.CommandId;
import ch.post.it.evoting.distributedprocessing.commands.CommandService;
import ch.post.it.evoting.domain.Context;

@DisplayName("ElectionContextProcessor consuming")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ElectionContextProcessorIT extends VotingIntegrationTestBase {

	private static byte[] electionContextPayloadBytes;
	private static String electionEventId;
	private static String firstRequestUUID;

	@Value("${nodeID}")
	private int nodeId;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@SpyBean
	private ElectionContextProcessor electionContextProcessor;

	@SpyBean
	private CommandService commandService;

	@SpyBean
	private ElectionContextService electionContextService;

	@BeforeAll
	static void setUpAll(
			@Autowired
			final ElectionEventService electionEventService) throws IOException, URISyntaxException {

		electionEventId = "e3e3c2fd8a16489291c5c24e7b74b26e";
		electionEventService.save(electionEventId, GroupTestData.getGqGroup());

		// Request payload.
		final Path electionContextPayloadPath = Paths.get(
				ElectionContextProcessorIT.class.getResource("/configuration/electioncontext/election-event-context-payload.json").toURI());

		electionContextPayloadBytes = Files.readAllBytes(electionContextPayloadPath);

		// UUID of the first request received.
		firstRequestUUID = UUID.randomUUID().toString();
	}

	@AfterAll
	static void cleanUp(
			@Autowired
			final TestDatabaseCleanUpService testDatabaseCleanUpService) {

		testDatabaseCleanUpService.cleanUp();
	}

	@Test
	@Order(0)
	@DisplayName("a request for the first time saves ElectionEventContext")
	void firstTimeCommand() throws IOException {

		// Send to request queue the ElectionContextPayload.
		final MessageProperties messageProperties = new MessageProperties();
		final String correlationId = firstRequestUUID;
		messageProperties.setCorrelationId(correlationId);

		final Message message = new Message(electionContextPayloadBytes, messageProperties);
		rabbitTemplate.send(RABBITMQ_EXCHANGE, ELECTION_CONTEXT_REQUEST_QUEUE, message);

		// Verifications.
		final Message responseMessage = rabbitTemplate.receive(ELECTION_CONTEXT_RESPONSE_QUEUE, 5000);
		assertNotNull(responseMessage);
		assertEquals(correlationId, responseMessage.getMessageProperties().getCorrelationId());

		verify(electionContextProcessor, after(5000).times(1)).onMessage(any());
		verify(electionContextService, times(1)).save(any());

		final CommandId commandId =
				new CommandId.Builder()
						.contextId(electionEventId)
						.context(Context.CONFIGURATION_ELECTION_CONTEXT.toString())
						.correlationId(correlationId)
						.nodeId(nodeId)
						.build();
		final Optional<Command> command = commandService.findIdenticalCommand(commandId);
		assertTrue(command.isPresent());
		assertNotNull(command.get().getResponsePayload());
	}

	@Test
	@Order(1)
	@DisplayName("an identical command does not save request/response and sends previous response")
	void sendPreviousResponseWithIdenticalCommand() throws IOException {
		// Send to request queue the ElectionContextPayload.
		final MessageProperties messageProperties = new MessageProperties();
		messageProperties.setCorrelationId(firstRequestUUID);

		final Message message = new Message(electionContextPayloadBytes, messageProperties);
		rabbitTemplate.send(RABBITMQ_EXCHANGE, ELECTION_CONTEXT_REQUEST_QUEUE, message);

		// Verifications.
		final Message responseMessage = rabbitTemplate.receive(ELECTION_CONTEXT_RESPONSE_QUEUE, 5000);
		assertNotNull(responseMessage);
		assertEquals(firstRequestUUID, responseMessage.getMessageProperties().getCorrelationId());

		verify(commandService, times(0)).saveRequest(any(), any());
		verify(commandService, times(0)).saveResponse(any(), any());
		verify(electionContextProcessor, after(5000).times(1)).onMessage(any());
		verify(electionContextService, times(0)).save(any());
	}
}
