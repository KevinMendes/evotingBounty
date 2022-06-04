/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.messagebrokerorchestrator;

import static java.time.temporal.ChronoUnit.SECONDS;

import java.time.Duration;
import java.util.HashMap;

import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.RabbitMQContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import ch.post.it.evoting.domain.SharedQueue;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public abstract class IntegrationTestSupport {

	protected static final String RABBITMQ_EXCHANGE = "evoting-exchange";
	protected static final String DESTINATION_TYPE = "queue";

	protected static final String PARTIAL_DECRYPT_REQUEST_ROUTING_KEY_1 = "voting.return-codes.PartialDecryptPCCRequest.1";
	protected static final String PARTIAL_DECRYPT_RESPONSE_ROUTING_KEY_1 = "voting.return-codes.PartialDecryptPCCResponse.1";
	protected static final String PARTIAL_DECRYPT_REQUEST_ROUTING_KEY_2 = "voting.return-codes.PartialDecryptPCCRequest.2";
	protected static final String PARTIAL_DECRYPT_RESPONSE_ROUTING_KEY_2 = "voting.return-codes.PartialDecryptPCCResponse.2";
	protected static final String PARTIAL_DECRYPT_REQUEST_ROUTING_KEY_3 = "voting.return-codes.PartialDecryptPCCRequest.3";
	protected static final String PARTIAL_DECRYPT_RESPONSE_ROUTING_KEY_3 = "voting.return-codes.PartialDecryptPCCResponse.3";
	protected static final String PARTIAL_DECRYPT_REQUEST_ROUTING_KEY_4 = "voting.return-codes.PartialDecryptPCCRequest.4";
	protected static final String PARTIAL_DECRYPT_RESPONSE_ROUTING_KEY_4 = "voting.return-codes.PartialDecryptPCCResponse.4";

	protected static final String PARTIAL_DECRYPT_REQUEST_QUEUE_1 = "voting.return-codes.PartialDecryptPCCRequest.1";
	protected static final String PARTIAL_DECRYPT_RESPONSE_QUEUE_1 = "voting.return-codes.PartialDecryptPCCResponse.1";
	protected static final String PARTIAL_DECRYPT_REQUEST_QUEUE_2 = "voting.return-codes.PartialDecryptPCCRequest.2";
	protected static final String PARTIAL_DECRYPT_RESPONSE_QUEUE_2 = "voting.return-codes.PartialDecryptPCCResponse.2";
	protected static final String PARTIAL_DECRYPT_REQUEST_QUEUE_3 = "voting.return-codes.PartialDecryptPCCRequest.3";
	protected static final String PARTIAL_DECRYPT_RESPONSE_QUEUE_3 = "voting.return-codes.PartialDecryptPCCResponse.3";
	protected static final String PARTIAL_DECRYPT_REQUEST_QUEUE_4 = "voting.return-codes.PartialDecryptPCCRequest.4";
	protected static final String PARTIAL_DECRYPT_RESPONSE_QUEUE_4 = "voting.return-codes.PartialDecryptPCCResponse.4";

	protected static final String LCC_REQUEST_QUEUE_1 = "voting.return-codes.CreateLCCShareRequest.1";
	protected static final String LCC_RESPONSE_QUEUE_1 = "voting.return-codes.CreateLCCShareResponse.1";
	protected static final String LCC_REQUEST_QUEUE_2 = "voting.return-codes.CreateLCCShareRequest.2";
	protected static final String LCC_RESPONSE_QUEUE_2 = "voting.return-codes.CreateLCCShareResponse.2";
	protected static final String LCC_REQUEST_QUEUE_3 = "voting.return-codes.CreateLCCShareRequest.3";
	protected static final String LCC_RESPONSE_QUEUE_3 = "voting.return-codes.CreateLCCShareResponse.3";
	protected static final String LCC_REQUEST_QUEUE_4 = "voting.return-codes.CreateLCCShareRequest.4";
	protected static final String LCC_RESPONSE_QUEUE_4 = "voting.return-codes.CreateLCCShareResponse.4";



	protected static final String LVCC_REQUEST_QUEUE_1 = "voting.return-codes.CreateLVCCShareRequest.1";
	protected static final String LVCC_RESPONSE_QUEUE_1 = "voting.return-codes.CreateLVCCShareResponse.1";
	protected static final String LVCC_REQUEST_QUEUE_2 = "voting.return-codes.CreateLVCCShareRequest.2";
	protected static final String LVCC_RESPONSE_QUEUE_2 = "voting.return-codes.CreateLVCCShareResponse.2";
	protected static final String LVCC_REQUEST_QUEUE_3 = "voting.return-codes.CreateLVCCShareRequest.3";
	protected static final String LVCC_RESPONSE_QUEUE_3 = "voting.return-codes.CreateLVCCShareResponse.3";
	protected static final String LVCC_REQUEST_QUEUE_4 = "voting.return-codes.CreateLVCCShareRequest.4";
	protected static final String LVCC_RESPONSE_QUEUE_4 = "voting.return-codes.CreateLVCCShareResponse.4";



	protected static final String GEN_ENC_REQUEST_QUEUE_1 = "configuration.return-codes.GenEncLongCodeSharesRequest.1";
	protected static final String GEN_ENC_RESPONSE_QUEUE_1 = "configuration.return-codes.GenEncLongCodeSharesResponse.1";
	protected static final String GEN_ENC_REQUEST_QUEUE_2 = "configuration.return-codes.GenEncLongCodeSharesRequest.2";
	protected static final String GEN_ENC_RESPONSE_QUEUE_2 = "configuration.return-codes.GenEncLongCodeSharesResponse.2";
	protected static final String GEN_ENC_REQUEST_QUEUE_3 = "configuration.return-codes.GenEncLongCodeSharesRequest.3";
	protected static final String GEN_ENC_RESPONSE_QUEUE_3 = "configuration.return-codes.GenEncLongCodeSharesResponse.3";
	protected static final String GEN_ENC_REQUEST_QUEUE_4 = "configuration.return-codes.GenEncLongCodeSharesRequest.4";
	protected static final String GEN_ENC_RESPONSE_QUEUE_4 = "configuration.return-codes.GenEncLongCodeSharesResponse.4";

	protected static final String GEN_KEYS_REQUEST_QUEUE_1 = "configuration.return-codes.GenKeysCCRRequest.1";
	protected static final String GEN_KEYS_RESPONSE_QUEUE_1 = "configuration.return-codes.GenKeysCCRResponse.1";
	protected static final String GEN_KEYS_REQUEST_QUEUE_2 = "configuration.return-codes.GenKeysCCRRequest.2";
	protected static final String GEN_KEYS_RESPONSE_QUEUE_2 = "configuration.return-codes.GenKeysCCRResponse.2";
	protected static final String GEN_KEYS_REQUEST_QUEUE_3 = "configuration.return-codes.GenKeysCCRRequest.3";
	protected static final String GEN_KEYS_RESPONSE_QUEUE_3 = "configuration.return-codes.GenKeysCCRResponse.3";
	protected static final String GEN_KEYS_REQUEST_QUEUE_4 = "configuration.return-codes.GenKeysCCRRequest.4";
	protected static final String GEN_KEYS_RESPONSE_QUEUE_4 = "configuration.return-codes.GenKeysCCRResponse.4";

	protected static final String MIX_DEC_ONLINE_REQUEST_QUEUE_1 = SharedQueue.NEW_ORCHESTRATOR_MIX_DEC_ONLINE_REQUEST_PATTERN + "1";
	protected static final String MIX_DEC_ONLINE_RESPONSE_QUEUE_1 = SharedQueue.NEW_ORCHESTRATOR_MIX_DEC_ONLINE_RESPONSE_PATTERN + "1";
	protected static final String MIX_DEC_ONLINE_REQUEST_QUEUE_2 = SharedQueue.NEW_ORCHESTRATOR_MIX_DEC_ONLINE_REQUEST_PATTERN + "2";
	protected static final String MIX_DEC_ONLINE_RESPONSE_QUEUE_2 = SharedQueue.NEW_ORCHESTRATOR_MIX_DEC_ONLINE_RESPONSE_PATTERN + "2";
	protected static final String MIX_DEC_ONLINE_REQUEST_QUEUE_3 = SharedQueue.NEW_ORCHESTRATOR_MIX_DEC_ONLINE_REQUEST_PATTERN + "3";
	protected static final String MIX_DEC_ONLINE_RESPONSE_QUEUE_3 = SharedQueue.NEW_ORCHESTRATOR_MIX_DEC_ONLINE_RESPONSE_PATTERN + "3";
	protected static final String MIX_DEC_ONLINE_REQUEST_QUEUE_4 = SharedQueue.NEW_ORCHESTRATOR_MIX_DEC_ONLINE_REQUEST_PATTERN + "4";
	protected static final String MIX_DEC_ONLINE_RESPONSE_QUEUE_4 = SharedQueue.NEW_ORCHESTRATOR_MIX_DEC_ONLINE_RESPONSE_PATTERN + "4";

	protected static final String ELECTION_CONTEXT_REQUEST_ROUTING_KEY_1 = "configuration.electioncontext.ElectionContextRequest.1";
	protected static final String ELECTION_CONTEXT_RESPONSE_ROUTING_KEY_1 = "configuration.electioncontext.ElectionContextResponse.1";
	protected static final String ELECTION_CONTEXT_REQUEST_ROUTING_KEY_2 = "configuration.electioncontext.ElectionContextRequest.2";
	protected static final String ELECTION_CONTEXT_RESPONSE_ROUTING_KEY_2 = "configuration.electioncontext.ElectionContextResponse.2";
	protected static final String ELECTION_CONTEXT_REQUEST_ROUTING_KEY_3 = "configuration.electioncontext.ElectionContextRequest.3";
	protected static final String ELECTION_CONTEXT_RESPONSE_ROUTING_KEY_3 = "configuration.electioncontext.ElectionContextResponse.3";
	protected static final String ELECTION_CONTEXT_REQUEST_ROUTING_KEY_4 = "configuration.electioncontext.ElectionContextRequest.4";
	protected static final String ELECTION_CONTEXT_RESPONSE_ROUTING_KEY_4 = "configuration.electioncontext.ElectionContextResponse.4";

	protected static final String ELECTION_CONTEXT_REQUEST_QUEUE_1 = "configuration.electioncontext.ElectionContextRequest.1";
	protected static final String ELECTION_CONTEXT_RESPONSE_QUEUE_1 = "configuration.electioncontext.ElectionContextResponse.1";
	protected static final String ELECTION_CONTEXT_REQUEST_QUEUE_2 = "configuration.electioncontext.ElectionContextRequest.2";
	protected static final String ELECTION_CONTEXT_RESPONSE_QUEUE_2 = "configuration.electioncontext.ElectionContextResponse.2";
	protected static final String ELECTION_CONTEXT_REQUEST_QUEUE_3 = "configuration.electioncontext.ElectionContextRequest.3";
	protected static final String ELECTION_CONTEXT_RESPONSE_QUEUE_3 = "configuration.electioncontext.ElectionContextResponse.3";
	protected static final String ELECTION_CONTEXT_REQUEST_QUEUE_4 = "configuration.electioncontext.ElectionContextRequest.4";
	protected static final String ELECTION_CONTEXT_RESPONSE_QUEUE_4 = "configuration.electioncontext.ElectionContextResponse.4";

	// These must match the ids in the test jsons.
	protected static final String electionEventId = "e3e3c2fd8a16489291c5c24e7b74b26e";
	protected static final String verificationCardSetId = "e77dbe3c70874ea584c490a0c6ac0ca4";
	protected static final String verificationCardId = "dd4063884c144446a6dfb63c42eb9e86";

	protected static final RabbitMQContainer rabbit = new RabbitMQContainer("rabbitmq:3.7-management")
			.withExposedPorts(5672, 15672)
			.withStartupTimeout(Duration.of(240, SECONDS))
			.withQueue(PARTIAL_DECRYPT_REQUEST_QUEUE_1, false, true, new HashMap<>())
			.withQueue(PARTIAL_DECRYPT_RESPONSE_QUEUE_1, false, true, new HashMap<>())
			.withQueue(PARTIAL_DECRYPT_REQUEST_QUEUE_2, false, true, new HashMap<>())
			.withQueue(PARTIAL_DECRYPT_RESPONSE_QUEUE_2, false, true, new HashMap<>())
			.withQueue(PARTIAL_DECRYPT_REQUEST_QUEUE_3, false, true, new HashMap<>())
			.withQueue(PARTIAL_DECRYPT_RESPONSE_QUEUE_3, false, true, new HashMap<>())
			.withQueue(PARTIAL_DECRYPT_REQUEST_QUEUE_4, false, true, new HashMap<>())
			.withQueue(PARTIAL_DECRYPT_RESPONSE_QUEUE_4, false, true, new HashMap<>())

			.withQueue(LCC_REQUEST_QUEUE_1, false, true, new HashMap<>())
			.withQueue(LCC_RESPONSE_QUEUE_1, false, true, new HashMap<>())
			.withQueue(LCC_REQUEST_QUEUE_2, false, true, new HashMap<>())
			.withQueue(LCC_RESPONSE_QUEUE_2, false, true, new HashMap<>())
			.withQueue(LCC_REQUEST_QUEUE_3, false, true, new HashMap<>())
			.withQueue(LCC_RESPONSE_QUEUE_3, false, true, new HashMap<>())
			.withQueue(LCC_REQUEST_QUEUE_4, false, true, new HashMap<>())
			.withQueue(LCC_RESPONSE_QUEUE_4, false, true, new HashMap<>())

			.withQueue(LVCC_REQUEST_QUEUE_1, false, true, new HashMap<>())
			.withQueue(LVCC_RESPONSE_QUEUE_1, false, true, new HashMap<>())
			.withQueue(LVCC_REQUEST_QUEUE_2, false, true, new HashMap<>())
			.withQueue(LVCC_RESPONSE_QUEUE_2, false, true, new HashMap<>())
			.withQueue(LVCC_REQUEST_QUEUE_3, false, true, new HashMap<>())
			.withQueue(LVCC_RESPONSE_QUEUE_3, false, true, new HashMap<>())
			.withQueue(LVCC_REQUEST_QUEUE_4, false, true, new HashMap<>())
			.withQueue(LVCC_RESPONSE_QUEUE_4, false, true, new HashMap<>())

			.withQueue(GEN_ENC_REQUEST_QUEUE_1, false, true, new HashMap<>())
			.withQueue(GEN_ENC_RESPONSE_QUEUE_1, false, true, new HashMap<>())
			.withQueue(GEN_ENC_REQUEST_QUEUE_2, false, true, new HashMap<>())
			.withQueue(GEN_ENC_RESPONSE_QUEUE_2, false, true, new HashMap<>())
			.withQueue(GEN_ENC_REQUEST_QUEUE_3, false, true, new HashMap<>())
			.withQueue(GEN_ENC_RESPONSE_QUEUE_3, false, true, new HashMap<>())
			.withQueue(GEN_ENC_REQUEST_QUEUE_4, false, true, new HashMap<>())
			.withQueue(GEN_ENC_RESPONSE_QUEUE_4, false, true, new HashMap<>())

			.withQueue(GEN_KEYS_REQUEST_QUEUE_1, false, true, new HashMap<>())
			.withQueue(GEN_KEYS_RESPONSE_QUEUE_1, false, true, new HashMap<>())
			.withQueue(GEN_KEYS_REQUEST_QUEUE_2, false, true, new HashMap<>())
			.withQueue(GEN_KEYS_RESPONSE_QUEUE_2, false, true, new HashMap<>())
			.withQueue(GEN_KEYS_REQUEST_QUEUE_3, false, true, new HashMap<>())
			.withQueue(GEN_KEYS_RESPONSE_QUEUE_3, false, true, new HashMap<>())
			.withQueue(GEN_KEYS_REQUEST_QUEUE_4, false, true, new HashMap<>())
			.withQueue(GEN_KEYS_RESPONSE_QUEUE_4, false, true, new HashMap<>())

			.withQueue(MIX_DEC_ONLINE_REQUEST_QUEUE_1, false, true, new HashMap<>())
			.withQueue(MIX_DEC_ONLINE_RESPONSE_QUEUE_1, false, true, new HashMap<>())
			.withQueue(MIX_DEC_ONLINE_REQUEST_QUEUE_2, false, true, new HashMap<>())
			.withQueue(MIX_DEC_ONLINE_RESPONSE_QUEUE_2, false, true, new HashMap<>())
			.withQueue(MIX_DEC_ONLINE_REQUEST_QUEUE_3, false, true, new HashMap<>())
			.withQueue(MIX_DEC_ONLINE_RESPONSE_QUEUE_3, false, true, new HashMap<>())
			.withQueue(MIX_DEC_ONLINE_REQUEST_QUEUE_4, false, true, new HashMap<>())
			.withQueue(MIX_DEC_ONLINE_RESPONSE_QUEUE_4, false, true, new HashMap<>())

			.withQueue(ELECTION_CONTEXT_REQUEST_QUEUE_1, false, true, new HashMap<>())
			.withQueue(ELECTION_CONTEXT_RESPONSE_QUEUE_1, false, true, new HashMap<>())
			.withQueue(ELECTION_CONTEXT_REQUEST_QUEUE_2, false, true, new HashMap<>())
			.withQueue(ELECTION_CONTEXT_RESPONSE_QUEUE_2, false, true, new HashMap<>())
			.withQueue(ELECTION_CONTEXT_REQUEST_QUEUE_3, false, true, new HashMap<>())
			.withQueue(ELECTION_CONTEXT_RESPONSE_QUEUE_3, false, true, new HashMap<>())
			.withQueue(ELECTION_CONTEXT_REQUEST_QUEUE_4, false, true, new HashMap<>())
			.withQueue(ELECTION_CONTEXT_RESPONSE_QUEUE_4, false, true, new HashMap<>())

			.withExchange(RABBITMQ_EXCHANGE, "direct", false, false, true, new HashMap<>())

			.withBinding(RABBITMQ_EXCHANGE, PARTIAL_DECRYPT_REQUEST_QUEUE_1, new HashMap<>(), PARTIAL_DECRYPT_REQUEST_ROUTING_KEY_1, DESTINATION_TYPE)
			.withBinding(RABBITMQ_EXCHANGE, PARTIAL_DECRYPT_RESPONSE_QUEUE_1, new HashMap<>(), PARTIAL_DECRYPT_RESPONSE_ROUTING_KEY_1,
					DESTINATION_TYPE)
			.withBinding(RABBITMQ_EXCHANGE, PARTIAL_DECRYPT_REQUEST_QUEUE_2, new HashMap<>(), PARTIAL_DECRYPT_REQUEST_ROUTING_KEY_2, DESTINATION_TYPE)
			.withBinding(RABBITMQ_EXCHANGE, PARTIAL_DECRYPT_RESPONSE_QUEUE_2, new HashMap<>(), PARTIAL_DECRYPT_RESPONSE_ROUTING_KEY_2,
					DESTINATION_TYPE)
			.withBinding(RABBITMQ_EXCHANGE, PARTIAL_DECRYPT_REQUEST_QUEUE_3, new HashMap<>(), PARTIAL_DECRYPT_REQUEST_ROUTING_KEY_3, DESTINATION_TYPE)
			.withBinding(RABBITMQ_EXCHANGE, PARTIAL_DECRYPT_RESPONSE_QUEUE_3, new HashMap<>(), PARTIAL_DECRYPT_RESPONSE_ROUTING_KEY_3,
					DESTINATION_TYPE)
			.withBinding(RABBITMQ_EXCHANGE, PARTIAL_DECRYPT_REQUEST_QUEUE_4, new HashMap<>(), PARTIAL_DECRYPT_REQUEST_ROUTING_KEY_4, DESTINATION_TYPE)
			.withBinding(RABBITMQ_EXCHANGE, PARTIAL_DECRYPT_RESPONSE_QUEUE_4, new HashMap<>(), PARTIAL_DECRYPT_RESPONSE_ROUTING_KEY_4,
					DESTINATION_TYPE)

			.withBinding(RABBITMQ_EXCHANGE, ELECTION_CONTEXT_REQUEST_QUEUE_1, new HashMap<>(), ELECTION_CONTEXT_REQUEST_ROUTING_KEY_1,
					DESTINATION_TYPE)
			.withBinding(RABBITMQ_EXCHANGE, ELECTION_CONTEXT_RESPONSE_QUEUE_1, new HashMap<>(), ELECTION_CONTEXT_RESPONSE_ROUTING_KEY_1,
					DESTINATION_TYPE)
			.withBinding(RABBITMQ_EXCHANGE, ELECTION_CONTEXT_REQUEST_QUEUE_2, new HashMap<>(), ELECTION_CONTEXT_REQUEST_ROUTING_KEY_2,
					DESTINATION_TYPE)
			.withBinding(RABBITMQ_EXCHANGE, ELECTION_CONTEXT_RESPONSE_QUEUE_2, new HashMap<>(), ELECTION_CONTEXT_RESPONSE_ROUTING_KEY_2,
					DESTINATION_TYPE)
			.withBinding(RABBITMQ_EXCHANGE, ELECTION_CONTEXT_REQUEST_QUEUE_3, new HashMap<>(), ELECTION_CONTEXT_REQUEST_ROUTING_KEY_3,
					DESTINATION_TYPE)
			.withBinding(RABBITMQ_EXCHANGE, ELECTION_CONTEXT_RESPONSE_QUEUE_3, new HashMap<>(), ELECTION_CONTEXT_RESPONSE_ROUTING_KEY_3,
					DESTINATION_TYPE)
			.withBinding(RABBITMQ_EXCHANGE, ELECTION_CONTEXT_REQUEST_QUEUE_4, new HashMap<>(), ELECTION_CONTEXT_REQUEST_ROUTING_KEY_4,
					DESTINATION_TYPE)
			.withBinding(RABBITMQ_EXCHANGE, ELECTION_CONTEXT_RESPONSE_QUEUE_4, new HashMap<>(), ELECTION_CONTEXT_RESPONSE_ROUTING_KEY_4,
					DESTINATION_TYPE)

			.withBinding(RABBITMQ_EXCHANGE, MIX_DEC_ONLINE_REQUEST_QUEUE_1, new HashMap<>(), MIX_DEC_ONLINE_REQUEST_QUEUE_1, DESTINATION_TYPE)
			.withBinding(RABBITMQ_EXCHANGE, MIX_DEC_ONLINE_RESPONSE_QUEUE_1, new HashMap<>(), MIX_DEC_ONLINE_RESPONSE_QUEUE_1, DESTINATION_TYPE)
			.withBinding(RABBITMQ_EXCHANGE, MIX_DEC_ONLINE_REQUEST_QUEUE_2, new HashMap<>(), MIX_DEC_ONLINE_REQUEST_QUEUE_2, DESTINATION_TYPE)
			.withBinding(RABBITMQ_EXCHANGE, MIX_DEC_ONLINE_RESPONSE_QUEUE_2, new HashMap<>(), MIX_DEC_ONLINE_RESPONSE_QUEUE_2, DESTINATION_TYPE)
			.withBinding(RABBITMQ_EXCHANGE, MIX_DEC_ONLINE_REQUEST_QUEUE_3, new HashMap<>(), MIX_DEC_ONLINE_REQUEST_QUEUE_3, DESTINATION_TYPE)
			.withBinding(RABBITMQ_EXCHANGE, MIX_DEC_ONLINE_RESPONSE_QUEUE_3, new HashMap<>(), MIX_DEC_ONLINE_RESPONSE_QUEUE_3, DESTINATION_TYPE)
			.withBinding(RABBITMQ_EXCHANGE, MIX_DEC_ONLINE_REQUEST_QUEUE_4, new HashMap<>(), MIX_DEC_ONLINE_REQUEST_QUEUE_4, DESTINATION_TYPE)
			.withBinding(RABBITMQ_EXCHANGE, MIX_DEC_ONLINE_RESPONSE_QUEUE_4, new HashMap<>(), MIX_DEC_ONLINE_RESPONSE_QUEUE_4, DESTINATION_TYPE)

			.withUser("user", "password")
			.waitingFor(Wait.forListeningPort());

	@DynamicPropertySource
	static void setup(final DynamicPropertyRegistry registry) {
		registry.add("spring.rabbitmq.host=", rabbit::getContainerIpAddress);
		registry.add("spring.rabbitmq.port=", () -> rabbit.getMappedPort(5672));
	}

	@BeforeAll
	static void setUpAll() {
		rabbit.start();
	}

}
