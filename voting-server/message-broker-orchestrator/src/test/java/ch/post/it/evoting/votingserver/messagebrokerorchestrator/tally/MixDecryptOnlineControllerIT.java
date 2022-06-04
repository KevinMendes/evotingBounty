/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

package ch.post.it.evoting.votingserver.messagebrokerorchestrator.tally;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.test.web.reactive.server.WebTestClient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.MixnetInitialPayload;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.MixnetShufflePayload;
import ch.post.it.evoting.domain.SharedQueue;
import ch.post.it.evoting.domain.tally.MixDecryptOnlinePayload;
import ch.post.it.evoting.domain.tally.MixDecryptOnlineRequestPayload;
import ch.post.it.evoting.domain.tally.MixDecryptOnlineResponsePayload;
import ch.post.it.evoting.votingserver.messagebrokerorchestrator.IntegrationTestSupport;

@DisplayName("MixDecryptOnlineController end to end integration test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MixDecryptOnlineControllerIT extends IntegrationTestSupport {

	private static final Logger LOGGER = LoggerFactory.getLogger(MixDecryptOnlineControllerIT.class);
	private static final String ballotBoxId = "e3e3c2fd8a16489291c5c2222222222e";
	private static final String MIX_DEC_ONLINE_PATH = "tally/mixonline";
	private static final String BASE_URL = "/api/v1/" + MIX_DEC_ONLINE_PATH + "/electionevent/{electionevent}/ballotbox/{ballotboxid}";
	private static final String MIX_URL = BASE_URL + "/mix";
	private static final String STATUS_URL = BASE_URL + "/status";
	private static final String DOWNLOAD_URL = BASE_URL + "/download";

	private static ObjectMapper domainMapper;
	private static MixnetInitialPayload responseMixnetInitialPayload;
	private static List<MixnetShufflePayload> responseMixnetShufflePayloads;

	@Autowired
	private WebTestClient webTestClient;

	@Autowired
	private MixnetPayloadService mixnetPayloadService;

	@Autowired
	private InitialPayloadRepository initialPayloadRepository;

	@Autowired
	private ShufflePayloadRepository shufflePayloadRepository;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	@BeforeAll
	public static void setup() throws IOException {
		domainMapper = DomainObjectMapper.getNewInstance();

		// Responses for each node
		final Resource initialPayloadsResource = new ClassPathResource(MIX_DEC_ONLINE_PATH + "/mixnet-initial-payload.json");
		responseMixnetInitialPayload = domainMapper.readValue(initialPayloadsResource.getFile(),
				MixnetInitialPayload.class);

		final Resource shufflePayloadsResource = new ClassPathResource(MIX_DEC_ONLINE_PATH + "/mixnet-shuffle-payloads.json");
		responseMixnetShufflePayloads = domainMapper.readValue(shufflePayloadsResource.getFile(),
				new TypeReference<List<MixnetShufflePayload>>() {
				});
	}

	@AfterEach
	void cleanUpDatabase() {
		shufflePayloadRepository.deleteAll();
		initialPayloadRepository.deleteAll();
	}

	@Test()
	@Order(1)
	@DisplayName("mix happy path")
	void happyPath() throws Exception {

		final CompletableFuture<WebTestClient.ResponseSpec> resultFuture = new CompletableFuture<>();

		final ExecutorService executorService = Executors.newFixedThreadPool(1, new CustomizableThreadFactory("http-pool-"));

		//Send the HTTP request in a separate thread and wait for the results.
		executorService.execute(() -> {
			try {
				final WebTestClient.ResponseSpec response = webTestClient.put()
						.uri(uriBuilder -> uriBuilder
								.path(MIX_URL)
								.build(electionEventId, ballotBoxId))
						.accept(MediaType.APPLICATION_JSON)
						.exchange();

				resultFuture.complete(response);

			} catch (final Exception ex) {
				resultFuture.completeExceptionally(ex);
			}
		});

		IntStream.rangeClosed(1, 4).forEach(action(responseMixnetInitialPayload, responseMixnetShufflePayloads));

		final WebTestClient.ResponseSpec response = resultFuture.get();

		response.expectStatus().isCreated();

		assertNull(response.expectBody(MixDecryptOnlineRequestPayload.class).returnResult().getResponseBody());

		// Wait till the status is MIXED
		await()
			.atMost(3, TimeUnit.SECONDS)
			.pollDelay(100, TimeUnit.MILLISECONDS)
			.until(() -> Objects.equals(webTestClient.get()
											.uri(uriBuilder -> uriBuilder
													.path(STATUS_URL)
													.build(electionEventId, ballotBoxId))
											.accept(MediaType.APPLICATION_JSON)
											.exchange()
											.expectBody(MixDecryptOnlineStatus.class)
											.returnResult()
											.getResponseBody(),
					MixDecryptOnlineStatus.MIXED)
			);

		// Download the processed MixDecryptOnlineRequestPayload
		final WebTestClient.ResponseSpec responseSpec = webTestClient.get()
				.uri(uriBuilder -> uriBuilder
						.path(DOWNLOAD_URL)
						.build(electionEventId, ballotBoxId))
				.accept(MediaType.APPLICATION_JSON)
				.exchange();

		responseSpec.expectStatus().isOk();

		final MixDecryptOnlinePayload payload = responseSpec.expectBody(MixDecryptOnlinePayload.class).returnResult().getResponseBody();

		assertNotNull(payload);
		assertEquals(electionEventId, payload.getElectionEventId(), "Invalid electionEventId");
		assertEquals(ballotBoxId, payload.getBallotBoxId(), "Invalid ballotBoxId");
		assertNotNull(payload.getInitialPayload(), "payload with initialPayload expected");
		assertNotNull(payload.getShufflePayloads(), "payload with a list of shufflePayload elements expected");
		assertEquals(4, payload.getShufflePayloads().size(), "payload with a list of 4 shufflePayload elements expected");
	}

	private IntConsumer action(final MixnetInitialPayload mixnetInitialPayload, final List<MixnetShufflePayload> mixnetShufflePayloads) {

		return nodeId -> {

			// Wait for request
			final String queueName = SharedQueue.NEW_ORCHESTRATOR_MIX_DEC_ONLINE_REQUEST_PATTERN + nodeId;
			final Message requestMessage = rabbitTemplate.receive(queueName, 5000);

			LOGGER.debug("Message[nodeId: {}, queueName: {}]: {}", nodeId, queueName, requestMessage);

			// Check request
			assertNotNull(requestMessage, "Request message must not be null");
			assertNotNull(requestMessage.getMessageProperties().getCorrelationId(), "Correlation Id must not be null");
			assertNotNull(requestMessage.getBody(), "message.body must not be null");
			LOGGER.debug("Message received[CorrelationId:{}]\n{}", requestMessage.getMessageProperties().getCorrelationId(),
					new String(requestMessage.getBody()));

			// Create response
			Message responseMessage = null;

			switch (nodeId) {
			case 1: { // IN: MixDecryptOnlineRequestPayload (w/ ee, bbid), OUT: MixDecryptOnlineRequestPayload (w/ InitialPayload & ShufflePayload)
				try {
					final MixDecryptOnlineRequestPayload payload = domainMapper.readValue(requestMessage.getBody(),
							MixDecryptOnlineRequestPayload.class);

					assert(payload.getShufflePayloads().isEmpty());

					final MixnetShufflePayload shuffle1 = mixnetShufflePayloads.get(0);

					LOGGER.debug("node: 1, received: {}", payload);
					LOGGER.debug("         response: {}", shuffle1);

					final byte[] byteContent = domainMapper.writeValueAsBytes(
							new MixDecryptOnlineResponsePayload(mixnetInitialPayload, shuffle1));

					responseMessage = MessageBuilder
							.withBody(byteContent)
							.setCorrelationId(requestMessage.getMessageProperties().getCorrelationId())
							.build();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			break;

			case 2: { // IN: MixDecryptOnlineRequestPayload (w/ ShufflePayload 1), OUT: MixDecryptOnlineRequestPayload (w/ InitialPayload? & ShufflePayload)
				try {
					final MixDecryptOnlineRequestPayload payload = domainMapper.readValue(requestMessage.getBody(),
							MixDecryptOnlineRequestPayload.class);

					assert(payload.getShufflePayloads().size() == 1);

					final MixnetShufflePayload shuffle_2 = mixnetShufflePayloads.get(nodeId - 1);

					LOGGER.debug("node: 2, received: #{}, {}", payload.getShufflePayloads().size(), payload.getShufflePayloads().toArray());
					LOGGER.debug("         response: {}", shuffle_2);

					final byte[] byteContent = domainMapper.writeValueAsBytes(
							new MixDecryptOnlineResponsePayload(null,shuffle_2));

					responseMessage = MessageBuilder
							.withBody(byteContent)
							.setCorrelationId(requestMessage.getMessageProperties().getCorrelationId())
							.build();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			break;

			case 3: { // IN: MixDecryptOnlineRequestPayload (w/ ShufflePayload 1 & 2), OUT: MixDecryptOnlineRequestPayload (w/ InitialPayload? & ShufflePayload)
				try {
					final MixDecryptOnlineRequestPayload payload = domainMapper.readValue(requestMessage.getBody(),
							MixDecryptOnlineRequestPayload.class);

					assert(payload.getShufflePayloads().size() == 2);

					final MixnetShufflePayload shuffle_3 = mixnetShufflePayloads.get(nodeId - 1);

					LOGGER.debug("node: 3, received: #{}, {}", payload.getShufflePayloads().size(), payload.getShufflePayloads().toArray());
					LOGGER.debug("         response: {}", shuffle_3);

					final byte[] byteContent = domainMapper.writeValueAsBytes(
							new MixDecryptOnlineResponsePayload(null, shuffle_3));

					responseMessage = MessageBuilder
							.withBody(byteContent)
							.setCorrelationId(requestMessage.getMessageProperties().getCorrelationId())
							.build();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			break;

			case 4: { // IN: MixDecryptOnlineRequestPayload (w/ ShufflePayload 1, 2 & 3), OUT: MixDecryptOnlineRequestPayload (w/ InitialPayload? & ShufflePayload)
				try {
					final MixDecryptOnlineRequestPayload payload = domainMapper.readValue(requestMessage.getBody(),
							MixDecryptOnlineRequestPayload.class);

					assert(payload.getShufflePayloads().size() == 3);

					final MixnetShufflePayload shuffle_4 = mixnetShufflePayloads.get(nodeId - 1);

					LOGGER.debug("node: 4, received: #{}, {}", payload.getShufflePayloads().size(), payload.getShufflePayloads().toArray());
					LOGGER.debug("         response: [{},{}]", responseMixnetInitialPayload, shuffle_4);

					final byte[] byteContent = domainMapper.writeValueAsBytes(
							new MixDecryptOnlineResponsePayload(null, shuffle_4));

					responseMessage = MessageBuilder
							.withBody(byteContent)
							.setCorrelationId(requestMessage.getMessageProperties().getCorrelationId())
							.build();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			break;
			}

			// Send response
			final String routingKey = SharedQueue.NEW_ORCHESTRATOR_MIX_DEC_ONLINE_RESPONSE_PATTERN + nodeId;
			assert responseMessage != null;
			LOGGER.debug("Response[CorrelationId:{}]: {}", responseMessage.getMessageProperties().getCorrelationId(), responseMessage);
			rabbitTemplate.send(routingKey, responseMessage);
			LOGGER.debug("Response sent[CorrelationId:{}]", responseMessage.getMessageProperties().getCorrelationId());
		};
	}

	@Test
	@Order(2)
	@DisplayName("save but none initialPayload")
	void saveWithoutInitialPayload() {

		try {
			IntStream.rangeClosed(1, 4).forEach(node ->
					mixnetPayloadService.saveShufflePayload(responseMixnetShufflePayloads.get(node - 1))
			);

			fail("MixnetInitialPayload found!");
		} catch (Exception ex) {
			assertEquals(IllegalStateException.class, ex.getClass(), "MissingInitialPayloadException expected");
		}

		assertEquals(Long.valueOf(0), Long.valueOf(mixnetPayloadService.countMixDecryptOnlinePayloads(electionEventId, ballotBoxId)),
				"MixnetInitialPayload without initialPayload was saved found!");
	}

	@Test
	@Order(3)
	@DisplayName("check status when not yet Started")
	void checkStatusWhenNotStarted() {

		//Send the HTTP request in a separate thread and wait for the results.
		final MixDecryptOnlineStatus result = webTestClient.get()
				.uri(uriBuilder -> uriBuilder
						.path(STATUS_URL)
						.build(electionEventId, ballotBoxId))
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectBody(MixDecryptOnlineStatus.class)
				.returnResult()
				.getResponseBody();

		assertEquals(MixDecryptOnlineStatus.NOT_STARTED, result);
	}

	@Test
	@Order(4)
	@DisplayName("check status when uncompleted")
	void checkStatusWhenUncompleted() {

		mixnetPayloadService.saveInitialPayload(responseMixnetInitialPayload);

		IntStream.rangeClosed(1, 2).forEach(node ->
				mixnetPayloadService.saveShufflePayload(responseMixnetShufflePayloads.get(node - 1))
		);

		//Send the HTTP request in a separate thread and wait for the results.
		final MixDecryptOnlineStatus result = webTestClient.get()
				.uri(uriBuilder -> uriBuilder
						.path(STATUS_URL)
						.build(electionEventId, ballotBoxId))
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectBody(MixDecryptOnlineStatus.class)
				.returnResult()
				.getResponseBody();

		assertEquals(MixDecryptOnlineStatus.PROCESSING, result);
	}

	@Test
	@Order(5)
	@DisplayName("check status when done processing")
	void checkStatusWhenDoneProcessing() {

		mixnetPayloadService.saveInitialPayload(responseMixnetInitialPayload);

		IntStream.rangeClosed(1, responseMixnetShufflePayloads.size()).forEach(node ->
				mixnetPayloadService.saveShufflePayload(responseMixnetShufflePayloads.get(node - 1))
		);

		//Send the HTTP request in a separate thread and wait for the results.
		final MixDecryptOnlineStatus result = webTestClient.get()
				.uri(uriBuilder -> uriBuilder
						.path(STATUS_URL)
						.build(electionEventId, ballotBoxId))
				.accept(MediaType.APPLICATION_JSON)
				.exchange()
				.expectBody(MixDecryptOnlineStatus.class)
				.returnResult()
				.getResponseBody();

		assertEquals(MixDecryptOnlineStatus.MIXED, result);
	}

	@Test
	@Order(6)
	@DisplayName("download MixDecryptOnlineRequestPayload when status is uncompleted")
	void downloadMixDecryptOnlinePayloadWhenUncompleted() throws Exception {

		final CompletableFuture<WebTestClient.ResponseSpec> resultFuture = new CompletableFuture<>();

		final ExecutorService executorService = Executors.newFixedThreadPool(1, new CustomizableThreadFactory("http-pool-"));

		mixnetPayloadService.saveInitialPayload(responseMixnetInitialPayload);

		IntStream.rangeClosed(1, 2).forEach(node ->
				mixnetPayloadService.saveShufflePayload(responseMixnetShufflePayloads.get(node - 1))
		);

		//Send the HTTP request in a separate thread and wait for the results.
		executorService.execute(() -> {

			try {
				final WebTestClient.ResponseSpec response = webTestClient.get()
						.uri(uriBuilder -> uriBuilder
								.path(DOWNLOAD_URL)
								.build(electionEventId, ballotBoxId))
						.accept(MediaType.APPLICATION_JSON)
						.exchange();

				resultFuture.complete(response);

			} catch (Exception ex) {
				resultFuture.completeExceptionally(ex);
			}
		});

		final WebTestClient.ResponseSpec response = resultFuture.get();

		response.expectStatus().isNotFound();

		final MixDecryptOnlineRequestPayload payload = response.expectBody(MixDecryptOnlineRequestPayload.class).returnResult().getResponseBody();

		assertNull(payload);
	}

	@Test
	@Order(7)
	@DisplayName("download MixDecryptOnlineRequestPayload when status completed")
	void downloadMixDecryptOnlinePayloadWhenCompleted() throws Exception {

		final CompletableFuture<WebTestClient.ResponseSpec> resultFuture = new CompletableFuture<>();

		final ExecutorService executorService = Executors.newFixedThreadPool(1, new CustomizableThreadFactory("http-pool-"));

		mixnetPayloadService.saveInitialPayload(responseMixnetInitialPayload);

		IntStream.rangeClosed(1, responseMixnetShufflePayloads.size()).forEach(node ->
				mixnetPayloadService.saveShufflePayload(responseMixnetShufflePayloads.get(node - 1))
		);

		//Send the HTTP request in a separate thread and wait for the results.
		executorService.execute(() -> {

			try {
				final WebTestClient.ResponseSpec response = webTestClient.get()
						.uri(uriBuilder -> uriBuilder
								.path(DOWNLOAD_URL)
								.build(electionEventId, ballotBoxId))
						.accept(MediaType.APPLICATION_JSON)
						.exchange();

				resultFuture.complete(response);

			} catch (Exception ex) {
				resultFuture.completeExceptionally(ex);
			}
		});

		final WebTestClient.ResponseSpec response = resultFuture.get();

		response.expectStatus().isOk();

		final MixDecryptOnlinePayload payload = response.expectBody(MixDecryptOnlinePayload.class).returnResult().getResponseBody();

		assertNotNull(payload);
		assertEquals(electionEventId, payload.getElectionEventId(), "Invalid ElectionEventId");
		assertEquals(ballotBoxId, payload.getBallotBoxId(), "Invalid BallotBoxId");
		assertNotNull(payload.getInitialPayload(), "payload with initialPayload expected");
		assertNotNull(payload.getShufflePayloads(), "payload with a list of shufflePayload elements expected");
		assertEquals(4, payload.getShufflePayloads().size(), "payload with a list of 4 shufflePayload elements expected");
	}

}
