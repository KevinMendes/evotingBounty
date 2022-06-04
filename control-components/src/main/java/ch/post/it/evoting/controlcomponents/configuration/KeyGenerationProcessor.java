/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.configuration;

import static ch.post.it.evoting.controlcomponents.ControlComponentsApplicationBootstrap.RABBITMQ_EXCHANGE;
import static ch.post.it.evoting.domain.SharedQueue.GEN_KEYS_CCR_REQUEST_PATTERN;
import static ch.post.it.evoting.domain.SharedQueue.GEN_KEYS_CCR_RESPONSE_PATTERN;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.KeyManagementException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.controlcomponents.ControlComponentsApplicationBootstrap;
import ch.post.it.evoting.controlcomponents.CryptolibPayloadSignatureService;
import ch.post.it.evoting.controlcomponents.ElectionEventService;
import ch.post.it.evoting.controlcomponents.ExactlyOnceProcessor;
import ch.post.it.evoting.controlcomponents.ExactlyOnceTask;
import ch.post.it.evoting.controlcomponents.Messages;
import ch.post.it.evoting.controlcomponents.keymanagement.ElectionSigningKeys;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.domain.Context;
import ch.post.it.evoting.domain.configuration.ControlComponentKeyGenerationRequestPayload;
import ch.post.it.evoting.domain.configuration.ControlComponentPublicKeys;
import ch.post.it.evoting.domain.configuration.ControlComponentPublicKeysPayload;
import ch.post.it.evoting.domain.election.model.messaging.PayloadSignatureException;

@Service
public class KeyGenerationProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(KeyGenerationProcessor.class);

	private final ObjectMapper objectMapper;
	private final RabbitTemplate rabbitTemplate;
	private final KeyGenerationService keyGenerationService;
	private final ExactlyOnceProcessor exactlyOnceProcessor;
	private final ElectionEventService electionEventService;
	private final CryptolibPayloadSignatureService payloadSignatureService;

	@Value("${nodeID}")
	private int nodeId;

	private String responseQueue;

	KeyGenerationProcessor(
			final ObjectMapper objectMapper,
			final RabbitTemplate rabbitTemplate,
			final KeyGenerationService keyGenerationService,
			final ExactlyOnceProcessor exactlyOnceProcessor,
			final ElectionEventService electionEventService,
			final CryptolibPayloadSignatureService payloadSignatureService) {
		this.objectMapper = objectMapper;
		this.rabbitTemplate = rabbitTemplate;
		this.keyGenerationService = keyGenerationService;
		this.exactlyOnceProcessor = exactlyOnceProcessor;
		this.electionEventService = electionEventService;
		this.payloadSignatureService = payloadSignatureService;
	}

	@PostConstruct
	public void initQueue() {
		responseQueue = String.format("%s%s", GEN_KEYS_CCR_RESPONSE_PATTERN, nodeId);
	}

	@RabbitListener(queues = GEN_KEYS_CCR_REQUEST_PATTERN + "${nodeID}", id =
			ControlComponentsApplicationBootstrap.CHOICE_CODES_CONTAINER_PREFIX + ".generation.keygen.request.queue", autoStartup = "false")
	public void onMessage(final Message message) throws IOException {

		final String correlationId = message.getMessageProperties().getCorrelationId();
		checkNotNull(correlationId, "Correlation Id must not be null");

		final byte[] messageBytes = message.getBody();
		final ControlComponentKeyGenerationRequestPayload controlComponentKeyGenerationRequestPayload = objectMapper.readValue(messageBytes,
				ControlComponentKeyGenerationRequestPayload.class);

		final String electionEventId = controlComponentKeyGenerationRequestPayload.getElectionEventId();
		LOGGER.info("Received key generation request. [contextId: {}, correlationId: {}, nodeId {}]", electionEventId, correlationId, nodeId);

		// Process key generation payload.
		final ExactlyOnceTask exactlyOnceTask = new ExactlyOnceTask.Builder()
				.setCorrelationId(correlationId)
				.setContextId(electionEventId)
				.setContext(Context.CONFIGURATION_RETURN_CODES_GEN_KEYS_CCR.toString())
				.setTask(() -> generateKeysPayload(controlComponentKeyGenerationRequestPayload))
				.setRequestContent(messageBytes)
				.build();
		final byte[] payloadBytes = exactlyOnceProcessor.process(exactlyOnceTask);

		final Message responseMessage = Messages.createMessage(correlationId, payloadBytes);

		rabbitTemplate.send(RABBITMQ_EXCHANGE, responseQueue, responseMessage);
		LOGGER.info("CCR generation key response sent. [contextIds: {}]", electionEventId);
	}

	private byte[] generateKeysPayload(final ControlComponentKeyGenerationRequestPayload controlComponentKeyGenerationRequestPayload) {
		final String electionEventId = controlComponentKeyGenerationRequestPayload.getElectionEventId();
		final GqGroup encryptionParameters = controlComponentKeyGenerationRequestPayload.getEncryptionParameters();

		// Save the encryption parameters.
		electionEventService.save(electionEventId, controlComponentKeyGenerationRequestPayload.getEncryptionParameters());
		LOGGER.info("Saved encryption parameters. [electionEventId: {}]", electionEventId);

		// Generate ccrj and ccmj keys.
		final ControlComponentPublicKeys controlComponentPublicKeys = keyGenerationService.generateCCKeys(electionEventId, encryptionParameters);
		final ControlComponentPublicKeysPayload controlComponentPublicKeysPayload = new ControlComponentPublicKeysPayload(encryptionParameters,
				electionEventId, controlComponentPublicKeys);

		// Create and sign payload.
		final ElectionSigningKeys electionSigningKeys;
		try {
			electionSigningKeys = keyGenerationService.generateElectionSigningKeys(electionEventId);
		} catch (final KeyManagementException e) {
			throw new IllegalStateException(String.format("Could not retrieve election signing keys. [contextIds: %s]", electionEventId));
		}

		final PrivateKey signingKey = electionSigningKeys.privateKey();
		final X509Certificate[] certificateChain = electionSigningKeys.certificateChain();

		final ControlComponentPublicKeysPayload signedPayload;
		try {
			signedPayload = payloadSignatureService.sign(controlComponentPublicKeysPayload, signingKey,
					certificateChain);
		} catch (final PayloadSignatureException e) {
			throw new IllegalStateException(String.format("Could not sign control component public keys payload. [contextIds: %s]", electionEventId));
		}
		LOGGER.info("Successfully signed control component public keys payload. [contextIds: {}]", electionEventId);

		try {
			return objectMapper.writeValueAsBytes(signedPayload);
		} catch (final JsonProcessingException e) {
			throw new UncheckedIOException(
					String.format("Could not serialize control component public keys payload. [contextIds: %s]", electionEventId), e);
		}
	}

}
