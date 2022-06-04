/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.voting;

import static ch.post.it.evoting.domain.SharedQueue.CREATE_LCC_SHARE_REQUEST_PATTERN;
import static ch.post.it.evoting.domain.SharedQueue.CREATE_LCC_SHARE_RESPONSE_PATTERN;
import static ch.post.it.evoting.domain.SharedQueue.CREATE_LVCC_SHARE_REQUEST_PATTERN;
import static ch.post.it.evoting.domain.SharedQueue.CREATE_LVCC_SHARE_RESPONSE_PATTERN;
import static ch.post.it.evoting.domain.SharedQueue.ELECTION_CONTEXT_REQUEST_PATTERN;
import static ch.post.it.evoting.domain.SharedQueue.ELECTION_CONTEXT_RESPONSE_PATTERN;
import static ch.post.it.evoting.domain.SharedQueue.GEN_ENC_LONG_CODE_SHARES_REQUEST_PATTERN;
import static ch.post.it.evoting.domain.SharedQueue.GEN_ENC_LONG_CODE_SHARES_RESPONSE_PATTERN;
import static ch.post.it.evoting.domain.SharedQueue.GEN_KEYS_CCR_REQUEST_PATTERN;
import static ch.post.it.evoting.domain.SharedQueue.GEN_KEYS_CCR_RESPONSE_PATTERN;
import static ch.post.it.evoting.domain.SharedQueue.PARTIAL_DECRYPT_PCC_REQUEST_PATTERN;
import static ch.post.it.evoting.domain.SharedQueue.PARTIAL_DECRYPT_PCC_RESPONSE_PATTERN;
import static java.time.temporal.ChronoUnit.SECONDS;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.containers.wait.strategy.Wait;

@ActiveProfiles("test")
@SpringBootTest(properties = { "application.bootstrap.enabled=true", "mixing.rabbitmq.listeners.enabled=false" })
public abstract class VotingIntegrationTestBase {

	protected static final String RABBITMQ_EXCHANGE = "evoting-exchange";
	protected static final String PARTIAL_DECRYPT_REQUEST_ROUTING_KEY = PARTIAL_DECRYPT_PCC_REQUEST_PATTERN + "1";
	protected static final String PARTIAL_DECRYPT_RESPONSE_ROUTING_KEY = PARTIAL_DECRYPT_PCC_RESPONSE_PATTERN + "1";
	protected static final String ELECTION_CONTEXT_REQUEST_ROUTING_KEY = ELECTION_CONTEXT_REQUEST_PATTERN + "1";
	protected static final String ELECTION_CONTEXT_RESPONSE_ROUTING_KEY = ELECTION_CONTEXT_RESPONSE_PATTERN + "1";
	protected static final String DESTINATION_TYPE = "queue";
	protected static final String PARTIAL_DECRYPT_REQUEST_QUEUE = PARTIAL_DECRYPT_PCC_REQUEST_PATTERN + "1";
	protected static final String PARTIAL_DECRYPT_RESPONSE_QUEUE = PARTIAL_DECRYPT_PCC_RESPONSE_PATTERN + "1";
	protected static final String LCC_REQUEST_QUEUE = CREATE_LCC_SHARE_REQUEST_PATTERN + "1";
	protected static final String LCC_RESPONSE_QUEUE = CREATE_LCC_SHARE_RESPONSE_PATTERN + "1";
	protected static final String LVCC_REQUEST_QUEUE = CREATE_LVCC_SHARE_REQUEST_PATTERN + "1";
	protected static final String LVCC_RESPONSE_QUEUE = CREATE_LVCC_SHARE_RESPONSE_PATTERN + "1";
	protected static final String GEN_ENC_REQUEST_QUEUE = GEN_ENC_LONG_CODE_SHARES_REQUEST_PATTERN + "1";
	protected static final String GEN_ENC_RESPONSE_QUEUE = GEN_ENC_LONG_CODE_SHARES_RESPONSE_PATTERN + "1";
	protected static final String GEN_KEYS_REQUEST_QUEUE = GEN_KEYS_CCR_REQUEST_PATTERN + "1";
	protected static final String GEN_KEYS_RESPONSE_QUEUE = GEN_KEYS_CCR_RESPONSE_PATTERN + "1";
	protected static final String ELECTION_CONTEXT_REQUEST_QUEUE = ELECTION_CONTEXT_REQUEST_PATTERN + "1";
	protected static final String ELECTION_CONTEXT_RESPONSE_QUEUE = ELECTION_CONTEXT_RESPONSE_PATTERN + "1";

	// These must match the ids in the test jsons.
	protected static final String electionEventId = "e3e3c2fd8a16489291c5c24e7b74b26e";
	protected static final String verificationCardSetId = "e77dbe3c70874ea584c490a0c6ac0ca4";
	protected static final String verificationCardId = "dd4063884c144446a6dfb63c42eb9e86";

	protected static final RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3.7-management")
			.withNetworkAliases("bridge")
			.withExposedPorts(5672, 15672)
			.withStartupTimeout(Duration.of(240, SECONDS))
			.withQueue(PARTIAL_DECRYPT_REQUEST_QUEUE, false, true, new HashMap<>())
			.withQueue(PARTIAL_DECRYPT_RESPONSE_QUEUE, false, true, new HashMap<>())
			.withQueue(LCC_REQUEST_QUEUE, false, true, new HashMap<>())
			.withQueue(LCC_RESPONSE_QUEUE, false, true, new HashMap<>())
			.withQueue(LVCC_REQUEST_QUEUE, false, true, new HashMap<>())
			.withQueue(LVCC_RESPONSE_QUEUE, false, true, new HashMap<>())
			.withQueue(GEN_ENC_REQUEST_QUEUE, false, true, new HashMap<>())
			.withQueue(GEN_ENC_RESPONSE_QUEUE, false, true, new HashMap<>())
			.withQueue(GEN_KEYS_REQUEST_QUEUE, false, true, new HashMap<>())
			.withQueue(GEN_KEYS_RESPONSE_QUEUE, false, true, new HashMap<>())
			.withQueue(ELECTION_CONTEXT_REQUEST_QUEUE, false, true, new HashMap<>())
			.withQueue(ELECTION_CONTEXT_RESPONSE_QUEUE, false, true, new HashMap<>())
			.withExchange(RABBITMQ_EXCHANGE, "direct", false, false, true, new HashMap<>())
			.withBinding(RABBITMQ_EXCHANGE, PARTIAL_DECRYPT_REQUEST_QUEUE, new HashMap<>(), PARTIAL_DECRYPT_REQUEST_ROUTING_KEY, DESTINATION_TYPE)
			.withBinding(RABBITMQ_EXCHANGE, PARTIAL_DECRYPT_RESPONSE_QUEUE, new HashMap<>(), PARTIAL_DECRYPT_RESPONSE_ROUTING_KEY, DESTINATION_TYPE)
			.withBinding(RABBITMQ_EXCHANGE, ELECTION_CONTEXT_REQUEST_QUEUE, new HashMap<>(), ELECTION_CONTEXT_REQUEST_ROUTING_KEY, DESTINATION_TYPE)
			.withBinding(RABBITMQ_EXCHANGE, ELECTION_CONTEXT_RESPONSE_QUEUE, new HashMap<>(), ELECTION_CONTEXT_RESPONSE_ROUTING_KEY, DESTINATION_TYPE)
			.withUser("user", "password")
			.waitingFor(Wait.forListeningPort());

	@DynamicPropertySource
	static void setup(final DynamicPropertyRegistry registry) {
		registry.add("spring.rabbitmq.host=", rabbit::getContainerIpAddress);
		registry.add("spring.rabbitmq.port=", () -> rabbit.getMappedPort(5672));
	}

	@BeforeAll
	static void setUpAll() throws IOException {
		// When the control-component start and bootstrap is enable, it performs the node activation which deletes the password file.
		// If multiple tests run with bootstrapping, make sure the password file is put back in the target folder.
		final Path passwordTargetPath = Paths.get(System.getProperty("testBuildDirectory")).resolve("keystore").resolve("CCN_C1.txt");
		final Path passwordSourcePath = Paths.get(System.getProperty("testResourcesDirectory")).resolve("keystore").resolve("CCN_C1.txt");
		if (!Files.exists(passwordTargetPath)) {
			Files.copy(passwordSourcePath, passwordTargetPath);
		}

		rabbit.start();
	}

}
