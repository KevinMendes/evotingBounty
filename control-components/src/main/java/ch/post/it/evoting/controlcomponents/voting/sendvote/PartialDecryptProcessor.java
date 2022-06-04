/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.voting.sendvote;

import static ch.post.it.evoting.controlcomponents.ControlComponentsApplicationBootstrap.RABBITMQ_EXCHANGE;
import static ch.post.it.evoting.domain.SharedQueue.PARTIAL_DECRYPT_PCC_REQUEST_PATTERN;
import static ch.post.it.evoting.domain.SharedQueue.PARTIAL_DECRYPT_PCC_RESPONSE_PATTERN;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.KeyManagementException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Arrays;

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
import ch.post.it.evoting.controlcomponents.keymanagement.ElectionSigningKeysService;
import ch.post.it.evoting.controlcomponents.voting.EncryptedVerifiableVoteService;
import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesPayloadSignature;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.domain.Context;
import ch.post.it.evoting.domain.ContextIds;
import ch.post.it.evoting.domain.election.model.messaging.PayloadSignatureException;
import ch.post.it.evoting.domain.voting.sendvote.EncryptedVerifiableVote;
import ch.post.it.evoting.domain.voting.sendvote.EncryptedVerifiableVotePayload;
import ch.post.it.evoting.domain.voting.sendvote.PartiallyDecryptedEncryptedPCC;
import ch.post.it.evoting.domain.voting.sendvote.PartiallyDecryptedEncryptedPCCPayload;

/**
 * Consumes the messages asking for the partial decryption of the encrypted Partial Choice Return Codes.
 */
@Service
public class PartialDecryptProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(PartialDecryptProcessor.class);

	private final ObjectMapper objectMapper;
	private final RabbitTemplate rabbitTemplate;
	private final ExactlyOnceProcessor exactlyOnceProcessor;
	private final PartialDecryptService partialDecryptService;
	private final ElectionSigningKeysService electionSigningKeysService;
	private final ElectionEventService electionEventService;
	private final EncryptedVerifiableVoteService encryptedVerifiableVoteService;
	private final CryptolibPayloadSignatureService cryptolibPayloadSignatureService;

	private String responseQueue;

	@Value("${nodeID}")
	private int nodeId;

	public PartialDecryptProcessor(
			final ObjectMapper objectMapper,
			final RabbitTemplate rabbitTemplate,
			final ExactlyOnceProcessor exactlyOnceProcessor,
			final PartialDecryptService partialDecryptService,
			final ElectionSigningKeysService electionSigningKeysService,
			final ElectionEventService electionEventService,
			final EncryptedVerifiableVoteService encryptedVerifiableVoteService,
			final CryptolibPayloadSignatureService cryptolibPayloadSignatureService) {
		this.objectMapper = objectMapper;
		this.rabbitTemplate = rabbitTemplate;
		this.exactlyOnceProcessor = exactlyOnceProcessor;
		this.partialDecryptService = partialDecryptService;
		this.electionSigningKeysService = electionSigningKeysService;
		this.electionEventService = electionEventService;
		this.encryptedVerifiableVoteService = encryptedVerifiableVoteService;
		this.cryptolibPayloadSignatureService = cryptolibPayloadSignatureService;
	}

	@PostConstruct
	public void initQueue() {
		responseQueue = String.format("%s%s", PARTIAL_DECRYPT_PCC_RESPONSE_PATTERN, nodeId);
	}

	@RabbitListener(queues = PARTIAL_DECRYPT_PCC_REQUEST_PATTERN + "${nodeID}", id =
			ControlComponentsApplicationBootstrap.CHOICE_CODES_CONTAINER_PREFIX + ".verification.decryption.request.queue", autoStartup = "false")
	public void onMessage(final Message message) throws IOException {

		final String correlationId = message.getMessageProperties().getCorrelationId();
		checkNotNull(correlationId, "Correlation Id must not be null.");

		// Deserialize message.
		final byte[] messageBytes = message.getBody();
		final EncryptedVerifiableVotePayload encryptedVerifiableVotePayload = objectMapper.readValue(messageBytes,
				EncryptedVerifiableVotePayload.class);

		// Verify payload signature and consistency.
		verifyPayload(encryptedVerifiableVotePayload);

		final ContextIds contextIds = encryptedVerifiableVotePayload.getEncryptedVerifiableVote().getContextIds();
		final String electionEventId = contextIds.getElectionEventId();
		final String verificationCardSetId = contextIds.getVerificationCardSetId();
		final String verificationCardId = contextIds.getVerificationCardId();

		final String contextId = String.join("-", Arrays.asList(electionEventId, verificationCardSetId, verificationCardId));
		LOGGER.info("Received partial decrypt request. [contextId: {}, correlationId: {}, nodeId: {}]", contextId, correlationId, nodeId);

		// Perform the partial decryption and create response payload.
		final ExactlyOnceTask partiallyDecryptInput = new ExactlyOnceTask.Builder()
				.setCorrelationId(correlationId)
				.setContextId(contextId)
				.setContext(Context.VOTING_RETURN_CODES_PARTIAL_DECRYPT_PCC.toString())
				.setTask(() -> generatePartiallyDecryptedEncryptedPayload(encryptedVerifiableVotePayload))
				.setRequestContent(messageBytes)
				.build();
		final byte[] payloadBytes = exactlyOnceProcessor.process(partiallyDecryptInput);

		final Message responseMessage = Messages.createMessage(correlationId, payloadBytes);

		rabbitTemplate.send(RABBITMQ_EXCHANGE, responseQueue, responseMessage);
		LOGGER.info("Partial decryption response sent. [contextIds: {}]", contextIds);
	}

	private void verifyPayload(final EncryptedVerifiableVotePayload encryptedVerifiableVotePayload) {

		final ContextIds contextIds = encryptedVerifiableVotePayload.getEncryptedVerifiableVote().getContextIds();
		final String electionEventId = contextIds.getElectionEventId();

		// Verify signature.
		// Currently, the EncryptedVerifiableVote is not signed (empty signature) because the voting-server does not have a signing key.
		final CryptoPrimitivesPayloadSignature signature = encryptedVerifiableVotePayload.getSignature();
		if (signature == null) {
			throw new IllegalArgumentException(
					String.format("The EncryptedVerifiableVotePayload signature is invalid. [contextIds: %s]", contextIds));
		}

		// Verify consistency.
		final GqGroup ccGqGroup = electionEventService.getEncryptionGroup(electionEventId);

		if (!encryptedVerifiableVotePayload.getEncryptionGroup().equals(ccGqGroup)) {
			throw new IllegalArgumentException(
					String.format("The payload's group is different from the control-component's group. [contextIds: %s]", contextIds));
		}
	}

	private byte[] generatePartiallyDecryptedEncryptedPayload(final EncryptedVerifiableVotePayload encryptedVerifiableVotePayload) {
		final EncryptedVerifiableVote encryptedVerifiableVote = encryptedVerifiableVotePayload.getEncryptedVerifiableVote();
		final ContextIds contextIds = encryptedVerifiableVote.getContextIds();
		final String electionEventId = contextIds.getElectionEventId();
		final GqGroup gqGroup = encryptedVerifiableVote.getEncryptedVote().getGroup();

		encryptedVerifiableVoteService.save(encryptedVerifiableVote);
		LOGGER.info("Saved encrypted verifiable vote. [contextIds: {}]", contextIds);

		// Perform partial decryption.
		final PartiallyDecryptedEncryptedPCC partiallyDecryptedEncryptedPCC = partialDecryptService.performPartialDecrypt(encryptedVerifiableVote);

		// Create and sign response payload.
		final PartiallyDecryptedEncryptedPCCPayload payload = new PartiallyDecryptedEncryptedPCCPayload(gqGroup, partiallyDecryptedEncryptedPCC,
				encryptedVerifiableVotePayload.getRequestId());

		final ElectionSigningKeys electionSigningKeys;
		try {
			electionSigningKeys = electionSigningKeysService.getElectionSigningKeys(electionEventId);
		} catch (final KeyManagementException e) {
			throw new IllegalStateException(String.format("Could not retrieve election signing keys. [contextIds: %s]", electionEventId), e);
		}
		final PrivateKey signingKey = electionSigningKeys.privateKey();
		final X509Certificate[] certificateChain = electionSigningKeys.certificateChain();

		final PartiallyDecryptedEncryptedPCCPayload signedPayload;
		try {
			signedPayload = cryptolibPayloadSignatureService.sign(payload, signingKey, certificateChain);
		} catch (final PayloadSignatureException e) {
			throw new IllegalStateException(String.format("Could not sign payload. [contextIds: %s]", electionEventId));
		}
		LOGGER.info("Successfully signed partially decrypted encrypted pcc payload. [contextIds: {}]", contextIds);

		try {
			return objectMapper.writeValueAsBytes(signedPayload);
		} catch (final JsonProcessingException e) {
			throw new UncheckedIOException(
					String.format("Could not serialize partially decrypted encrypted PCC payload. [contextIds: %s]", electionEventId), e);
		}
	}

}
