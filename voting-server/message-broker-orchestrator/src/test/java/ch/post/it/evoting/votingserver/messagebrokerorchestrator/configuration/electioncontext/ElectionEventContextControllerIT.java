/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.messagebrokerorchestrator.configuration.electioncontext;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.distributedprocessing.commands.Command;
import ch.post.it.evoting.distributedprocessing.commands.CommandRepository;
import ch.post.it.evoting.domain.Context;
import ch.post.it.evoting.domain.configuration.ElectionContextResponsePayload;
import ch.post.it.evoting.domain.configuration.ElectionEventContextPayload;
import ch.post.it.evoting.votingserver.messagebrokerorchestrator.IntegrationTestSupport;

@DisplayName("ElectionEventContextController integration test")
class ElectionEventContextControllerIT extends IntegrationTestSupport {

	private static ObjectMapper domainMapper;

	@Autowired
	CommandRepository commandRepository;

	@Autowired
	private WebTestClient webTestClient;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@BeforeAll
	public static void setup() {
		domainMapper = DomainObjectMapper.getNewInstance();
	}

	@Test
	@DisplayName("Process ElectionEventContextPayload")
	@Transactional
	void firstTimeCommand() throws IOException, InterruptedException {

		final Context context = Context.CONFIGURATION_ELECTION_CONTEXT;

		final CountDownLatch webClientCountDownLatch = new CountDownLatch(1);

		final Resource payloadsResource = new ClassPathResource("configuration/electioncontext/election-event-context-payload.json");

		final ElectionEventContextPayload requestPayload = domainMapper.readValue(payloadsResource.getFile(), ElectionEventContextPayload.class);

		final ExecutorService executorService = Executors.newFixedThreadPool(1, new CustomizableThreadFactory("http-pool-"));

		//Send the HTTP request in a separate thread and wait for the results.
		executorService.execute(() -> {
			webTestClient.post()
					.uri(uriBuilder -> uriBuilder
							.path("/api/v1/configuration/electioncontext/electionevent/")
							.pathSegment(electionEventId)
							.build(1L))
					.accept(MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML)
					.bodyValue(requestPayload)
					.exchange()
					.expectStatus().isOk()
					.expectBody()
					.jsonPath("$..electionEventId").hasJsonPath()
					.jsonPath("$..nodeId").value(Matchers.hasSize(4));

			webClientCountDownLatch.countDown();
		});

		await().atMost(300, SECONDS).until(() -> commandRepository.existsByContextIdAndContext(electionEventId, context.toString()));

		final Optional<Command> optionalCommand = commandRepository.findByContextIdAndContextAndNodeId(electionEventId, context.toString(), 1);
		assertTrue(optionalCommand.isPresent());
		final Command command = optionalCommand.get();
		final String correlationId = command.getCorrelationId();

		final Resource ccPayloadsResource = new ClassPathResource("configuration/electioncontext/election-context-response-payloads.json");
		final List<ElectionContextResponsePayload> responsePayloads = domainMapper.readValue(ccPayloadsResource.getFile(),
				new TypeReference<List<ElectionContextResponsePayload>>() {
				});

		IntStream.rangeClosed(1, 4).forEach((node) -> {
			ElectionContextResponsePayload electionContextResponseCCPayload = responsePayloads.get(node - 1);
			final MessageProperties messageProperties = new MessageProperties();
			messageProperties.setCorrelationId(correlationId);
			byte[] payloadBytes;
			try {
				payloadBytes = domainMapper.writeValueAsBytes(electionContextResponseCCPayload);
			} catch (JsonProcessingException e) {
				throw new UncheckedIOException(e);
			}

			final String responseQueue = "configuration.electioncontext.ElectionContextRequest." + node;
			final Message responseMessage = rabbitTemplate.receive(responseQueue, 5000);
			assertNotNull(responseMessage);
			assertEquals(correlationId, responseMessage.getMessageProperties().getCorrelationId());

			final Message message = new Message(payloadBytes, messageProperties);
			final String requestQueue = "configuration.electioncontext.ElectionContextResponse." + node;
			rabbitTemplate.send(RABBITMQ_EXCHANGE, requestQueue, message);
		});

		webClientCountDownLatch.await();
	}
}
