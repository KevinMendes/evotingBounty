/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.voting.sendvote;

import static ch.post.it.evoting.controlcomponents.ControlComponentsApplicationBootstrap.RABBITMQ_EXCHANGE;
import static ch.post.it.evoting.domain.SharedQueue.CREATE_LCC_SHARE_REQUEST_PATTERN;
import static ch.post.it.evoting.domain.SharedQueue.CREATE_LCC_SHARE_RESPONSE_PATTERN;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.KeyManagementException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
import ch.post.it.evoting.controlcomponents.ExactlyOnceProcessor;
import ch.post.it.evoting.controlcomponents.ExactlyOnceTask;
import ch.post.it.evoting.controlcomponents.Messages;
import ch.post.it.evoting.controlcomponents.keymanagement.ElectionSigningKeys;
import ch.post.it.evoting.controlcomponents.keymanagement.ElectionSigningKeysService;
import ch.post.it.evoting.controlcomponents.keymanagement.KeysManager;
import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesPayloadSignature;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.distributedprocessing.commands.Command;
import ch.post.it.evoting.distributedprocessing.commands.CommandRepository;
import ch.post.it.evoting.domain.Context;
import ch.post.it.evoting.domain.ContextIds;
import ch.post.it.evoting.domain.InvalidPayloadSignatureException;
import ch.post.it.evoting.domain.election.model.messaging.PayloadSignatureException;
import ch.post.it.evoting.domain.election.model.messaging.PayloadVerificationException;
import ch.post.it.evoting.domain.voting.sendvote.CombinedPartiallyDecryptedEncryptedPCCPayload;
import ch.post.it.evoting.domain.voting.sendvote.LongReturnCodesShare;
import ch.post.it.evoting.domain.voting.sendvote.LongReturnCodesSharePayload;
import ch.post.it.evoting.domain.voting.sendvote.PartiallyDecryptedEncryptedPCC;
import ch.post.it.evoting.domain.voting.sendvote.PartiallyDecryptedEncryptedPCCPayload;

/**
 * Consumes the messages asking for the Long Choice Return Codes Share.
 */
@Service
public class LCCShareProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(LCCShareProcessor.class);

	private final KeysManager keysManager;
	private final CommandRepository commandRepository;
	private final RabbitTemplate rabbitTemplate;
	private final LCCShareService lccShareService;
	private final ElectionSigningKeysService electionSigningKeysService;
	private final CryptolibPayloadSignatureService cryptolibPayloadSignatureService;
	private final ObjectMapper objectMapper;
	private final ExactlyOnceProcessor exactlyOnceProcessor;

	private String responseQueue;

	@Value("${nodeID}")
	private int nodeId;

	public LCCShareProcessor(
			final KeysManager keysManager,
			final ObjectMapper objectMapper,
			final RabbitTemplate rabbitTemplate,
			final LCCShareService lccShareService,
			final CommandRepository commandRepository,
			final ExactlyOnceProcessor exactlyOnceProcessor,
			final ElectionSigningKeysService electionSigningKeysService,
			final CryptolibPayloadSignatureService cryptolibPayloadSignatureService) {
		this.keysManager = keysManager;
		this.objectMapper = objectMapper;
		this.rabbitTemplate = rabbitTemplate;
		this.lccShareService = lccShareService;
		this.commandRepository = commandRepository;
		this.exactlyOnceProcessor = exactlyOnceProcessor;
		this.electionSigningKeysService = electionSigningKeysService;
		this.cryptolibPayloadSignatureService = cryptolibPayloadSignatureService;
	}

	@PostConstruct
	public void initQueue() {
		responseQueue = String.format("%s%s", CREATE_LCC_SHARE_RESPONSE_PATTERN, nodeId);
	}

	@RabbitListener(queues = CREATE_LCC_SHARE_REQUEST_PATTERN + "${nodeID}", id = ControlComponentsApplicationBootstrap.CHOICE_CODES_CONTAINER_PREFIX
			+ ".voting.createLccShare.request.queue", autoStartup = "false")
	public void onMessage(final Message message) throws IOException, PayloadVerificationException {

		final String correlationId = message.getMessageProperties().getCorrelationId();
		checkNotNull(correlationId, "Correlation Id must not be null.");

		// Deserialize message.
		final byte[] messageBytes = message.getBody();
		final CombinedPartiallyDecryptedEncryptedPCCPayload combinedPartiallyDecryptedEncryptedPCCPayload = objectMapper.readValue(messageBytes,
				CombinedPartiallyDecryptedEncryptedPCCPayload.class);

		// Verify payload signature and consistency.
		verifyPayload(combinedPartiallyDecryptedEncryptedPCCPayload);

		// Construct request command.
		final List<PartiallyDecryptedEncryptedPCCPayload> partiallyDecryptedEncryptedPCCPayloads = combinedPartiallyDecryptedEncryptedPCCPayload.getPartiallyDecryptedEncryptedPCCPayloads();
		final ContextIds contextIds = partiallyDecryptedEncryptedPCCPayloads.get(0).getPartiallyDecryptedEncryptedPCC().getContextIds();
		final String electionEventId = contextIds.getElectionEventId();
		final String verificationCardSetId = contextIds.getVerificationCardSetId();
		final String verificationCardId = contextIds.getVerificationCardId();

		final String contextId = String.join("-", Arrays.asList(electionEventId, verificationCardSetId, verificationCardId));
		LOGGER.info("Received LCC share request. [contextId: {}, correlationId: {}, nodeId: {}]", contextId, correlationId, nodeId);

		final ExactlyOnceTask createLCCShareInput = new ExactlyOnceTask.Builder()
				.setCorrelationId(correlationId)
				.setContextId(contextId)
				.setContext(Context.VOTING_RETURN_CODES_CREATE_LCC_SHARE.toString())
				.setTask(() -> generateLongReturnCodesSharePayload(combinedPartiallyDecryptedEncryptedPCCPayload))
				.setRequestContent(messageBytes)
				.build();
		final byte[] payloadBytes = exactlyOnceProcessor.process(createLCCShareInput);

		final Message responseMessage = Messages.createMessage(correlationId, payloadBytes);

		rabbitTemplate.send(RABBITMQ_EXCHANGE, responseQueue, responseMessage);
		LOGGER.info("LCC share response sent. [contextIds: {}]", contextIds);
	}

	private void verifyPayload(final CombinedPartiallyDecryptedEncryptedPCCPayload combinedPartiallyDecryptedEncryptedPCCPayload)
			throws IOException, PayloadVerificationException {

		final ContextIds contextIds = combinedPartiallyDecryptedEncryptedPCCPayload.getPartiallyDecryptedEncryptedPCCPayloads().get(0)
				.getPartiallyDecryptedEncryptedPCC().getContextIds();
		final String electionEventId = contextIds.getElectionEventId();
		final String verificationCardSetId = contextIds.getVerificationCardSetId();
		final String verificationCardId = contextIds.getVerificationCardId();

		// Verify signature of combined payload.
		// Currently, we do not have a signing key in the vote-verification.
		final CryptoPrimitivesPayloadSignature signature = combinedPartiallyDecryptedEncryptedPCCPayload.getSignature();
		if (signature == null) {
			throw new InvalidPayloadSignatureException(CombinedPartiallyDecryptedEncryptedPCCPayload.class,
					String.format("[contextIds: %s]", contextIds));
		}

		// Retrieve the partially decrypted encrypted PCC previously computed.
		final String contextId = String.join("-", Arrays.asList(electionEventId, verificationCardSetId, verificationCardId));
		final Optional<Command> command = commandRepository.findByContextIdAndContextAndNodeId(contextId,
				Context.VOTING_RETURN_CODES_PARTIAL_DECRYPT_PCC.toString(), nodeId);
		final PartiallyDecryptedEncryptedPCC previouslyComputedPCCPayload = objectMapper.readValue(command
						.orElseThrow(() -> new IllegalStateException(String.format("Command not found. [contextIds: %s]", contextIds)))
						.getResponsePayload(), PartiallyDecryptedEncryptedPCCPayload.class)
				.getPartiallyDecryptedEncryptedPCC();

		// Get the partially decrypted encrypted PCC corresponding to this node id.
		final PartiallyDecryptedEncryptedPCCPayload receivedPCCPayload = combinedPartiallyDecryptedEncryptedPCCPayload.getPartiallyDecryptedEncryptedPCCPayloads()
				.stream()
				.filter(payload -> payload.getPartiallyDecryptedEncryptedPCC().getNodeId() == nodeId)
				.findAny() // Uniqueness ensured by the combined payload.
				.orElseThrow(() -> new IllegalStateException("The combined payload does not contain payload for this node id."));

		// Check that they are equal.
		if (!previouslyComputedPCCPayload.equals(receivedPCCPayload.getPartiallyDecryptedEncryptedPCC())) {
			throw new IllegalStateException("The received partially decrypted encrypted PCC is not equal to the previously computed one.");
		}

		// Verify signature of the received PartiallyDecryptedEncryptedPCCPayload.
		final X509Certificate platformCACertificate = keysManager.getPlatformCACertificate();
		final boolean signatureValid = cryptolibPayloadSignatureService.verify(receivedPCCPayload, platformCACertificate);

		if (!signatureValid) {
			throw new InvalidPayloadSignatureException(PartiallyDecryptedEncryptedPCCPayload.class, String.format("[contextIds: %s]", contextIds));
		}

	}

	private byte[] generateLongReturnCodesSharePayload(final CombinedPartiallyDecryptedEncryptedPCCPayload input) {
		final List<PartiallyDecryptedEncryptedPCCPayload> partiallyDecryptedEncryptedPCCPayloads = input.getPartiallyDecryptedEncryptedPCCPayloads();
		final ContextIds contextIds = partiallyDecryptedEncryptedPCCPayloads.get(0).getPartiallyDecryptedEncryptedPCC().getContextIds();
		final String electionEventId = contextIds.getElectionEventId();
		final GqGroup gqGroup = partiallyDecryptedEncryptedPCCPayloads.get(0).getEncryptionGroup();

		// Perform LCC share computation.
		final LongReturnCodesShare longReturnCodesShare = lccShareService.computeLCCShares(partiallyDecryptedEncryptedPCCPayloads);
		LOGGER.info("Successfully generated the Long Choice Return Codes Share. [contextIds: {}]", contextIds);

		// Create and sign response payload.
		final LongReturnCodesSharePayload payload = new LongReturnCodesSharePayload(gqGroup, longReturnCodesShare);

		final ElectionSigningKeys electionSigningKeys;
		try {
			electionSigningKeys = electionSigningKeysService.getElectionSigningKeys(electionEventId);
		} catch (final KeyManagementException e) {
			throw new IllegalStateException(String.format("Could not retrieve election signing keys. [contextIds: %s]", electionEventId));
		}
		final PrivateKey signingKey = electionSigningKeys.privateKey();
		final X509Certificate[] certificateChain = electionSigningKeys.certificateChain();

		final LongReturnCodesSharePayload signedPayload;
		try {
			signedPayload = cryptolibPayloadSignatureService.sign(payload, signingKey, certificateChain);
		} catch (final PayloadSignatureException e) {
			throw new IllegalStateException(String.format("Could not sign payload. [contextIds: %s]", electionEventId));
		}
		LOGGER.info("Successfully signed Long Return Codes Share payload. [contextIds: {}]", contextIds);

		try {
			return objectMapper.writeValueAsBytes(signedPayload);
		} catch (final JsonProcessingException e) {
			throw new UncheckedIOException(String.format("Could not serialize long return codes share payload. [contextIds: %s]", contextIds), e);
		}
	}

}
