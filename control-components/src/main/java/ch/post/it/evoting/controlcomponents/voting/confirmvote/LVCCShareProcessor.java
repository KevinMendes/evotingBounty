/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.voting.confirmvote;

import static ch.post.it.evoting.controlcomponents.ControlComponentsApplicationBootstrap.RABBITMQ_EXCHANGE;
import static ch.post.it.evoting.domain.SharedQueue.CREATE_LVCC_SHARE_REQUEST_PATTERN;
import static ch.post.it.evoting.domain.SharedQueue.CREATE_LVCC_SHARE_RESPONSE_PATTERN;
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
import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesPayloadSignature;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.domain.Context;
import ch.post.it.evoting.domain.ContextIds;
import ch.post.it.evoting.domain.InvalidPayloadSignatureException;
import ch.post.it.evoting.domain.election.model.messaging.PayloadSignatureException;
import ch.post.it.evoting.domain.voting.confirmvote.ConfirmationKey;
import ch.post.it.evoting.domain.voting.confirmvote.ConfirmationKeyPayload;
import ch.post.it.evoting.domain.voting.sendvote.LongReturnCodesSharePayload;
import ch.post.it.evoting.domain.voting.sendvote.LongVoteCastReturnCodesShare;

/**
 * Consumes the messages asking for the Long Vote Cast Return Codes Share.
 */
@Service
public class LVCCShareProcessor {

	private static final Logger LOGGER = LoggerFactory.getLogger(LVCCShareProcessor.class);

	private final ObjectMapper objectMapper;
	private final RabbitTemplate rabbitTemplate;
	private final LVCCShareService lvccShareService;
	private final ExactlyOnceProcessor exactlyOnceProcessor;
	private final ElectionSigningKeysService electionSigningKeysService;
	private final ElectionEventService electionEventService;
	private final CryptolibPayloadSignatureService payloadSignatureService;

	private String responseQueue;

	@Value("${nodeID}")
	private int nodeId;

	LVCCShareProcessor(
			final ObjectMapper objectMapper,
			final RabbitTemplate rabbitTemplate,
			final LVCCShareService lvccShareService,
			final ElectionSigningKeysService electionSigningKeysService,
			final ElectionEventService electionEventService,
			final CryptolibPayloadSignatureService payloadSignatureService,
			final ExactlyOnceProcessor exactlyOnceProcessor) {
		this.objectMapper = objectMapper;
		this.rabbitTemplate = rabbitTemplate;
		this.lvccShareService = lvccShareService;
		this.exactlyOnceProcessor = exactlyOnceProcessor;
		this.electionSigningKeysService = electionSigningKeysService;
		this.electionEventService = electionEventService;
		this.payloadSignatureService = payloadSignatureService;
	}

	@PostConstruct
	public void initQueue() {
		responseQueue = String.format("%s%s", CREATE_LVCC_SHARE_RESPONSE_PATTERN, nodeId);
	}

	@RabbitListener(queues = CREATE_LVCC_SHARE_REQUEST_PATTERN + "${nodeID}", id = ControlComponentsApplicationBootstrap.CHOICE_CODES_CONTAINER_PREFIX
			+ ".verification.computation.request.queue", autoStartup = "false")
	public void onMessage(final Message message) throws IOException {
		final String correlationId = message.getMessageProperties().getCorrelationId();
		checkNotNull(correlationId, "Correlation Id must not be null");

		final byte[] messageBytes = message.getBody();
		final ConfirmationKeyPayload confirmationKeyPayload = objectMapper.readValue(messageBytes, ConfirmationKeyPayload.class);

		verifyPayloadSignature(confirmationKeyPayload);
		verifyPayloadConsistency(confirmationKeyPayload);

		final ContextIds contextIds = confirmationKeyPayload.getConfirmationKey().getContextIds();
		final String electionEventId = contextIds.getElectionEventId();
		final String verificationCardSetId = contextIds.getVerificationCardSetId();
		final String verificationCardId = contextIds.getVerificationCardId();

		final String contextId = String.join("-", Arrays.asList(electionEventId, verificationCardSetId, verificationCardId, correlationId));

		LOGGER.info("Received create LVCC share request. [contextId: {}, correlationId: {}, nodeId {}]", contextId, correlationId, nodeId);

		// Compute the Long Vote Cast Return Code shares.
		final ExactlyOnceTask exactlyOnceTask = new ExactlyOnceTask.Builder()
				.setCorrelationId(correlationId)
				.setContextId(contextId)
				.setContext(Context.VOTING_RETURN_CODES_CREATE_LVCC_SHARE.toString())
				.setTask(() -> generateLongVoteCastReturnCodesSharePayload(confirmationKeyPayload))
				.setRequestContent(messageBytes)
				.build();
		final byte[] payloadBytes = exactlyOnceProcessor.process(exactlyOnceTask);

		final Message responseMessage = Messages.createMessage(correlationId, payloadBytes);

		rabbitTemplate.send(RABBITMQ_EXCHANGE, responseQueue, responseMessage);
		LOGGER.info("Create LVCC shares response sent. [contextIds: {}]", contextIds);
	}

	private void verifyPayloadSignature(final ConfirmationKeyPayload confirmationKeyPayload) {
		// Currently, we do not have a signing key in the vote-verification.
		final CryptoPrimitivesPayloadSignature signature = confirmationKeyPayload.getSignature();
		if (signature == null) {
			final ContextIds contextIds = confirmationKeyPayload.getConfirmationKey().getContextIds();
			throw new InvalidPayloadSignatureException(ConfirmationKeyPayload.class, String.format("[contextIds: %s]", contextIds));
		}
	}

	private void verifyPayloadConsistency(final ConfirmationKeyPayload confirmationKeyPayload) {
		final ContextIds contextIds = confirmationKeyPayload.getConfirmationKey().getContextIds();
		final String electionEventId = contextIds.getElectionEventId();
		final String verificationCardSetId = contextIds.getVerificationCardSetId();

		final GqGroup ccGqGroup = electionEventService.getEncryptionGroup(electionEventId);

		if (!ccGqGroup.equals(confirmationKeyPayload.getEncryptionGroup())) {
			throw new IllegalStateException(String.format(
					"The confirmation key payload's group is different from the control-component's group. [electionEventId: %s, verificationCardSetId: %s]",
					electionEventId, verificationCardSetId));
		}
	}

	private byte[] generateLongVoteCastReturnCodesSharePayload(final ConfirmationKeyPayload confirmationKeyPayload) {
		final ConfirmationKey confirmationKey = confirmationKeyPayload.getConfirmationKey();
		final ContextIds contextIds = confirmationKey.getContextIds();
		final String electionEventId = contextIds.getElectionEventId();
		final GqGroup gqGroup = confirmationKey.getElement().getGroup();

		// Compute the LVCC shares.
		final LongVoteCastReturnCodesShare longVoteCastReturnCodesShare = lvccShareService.computeLVCCShares(confirmationKey,
				confirmationKeyPayload.getRequestId());
		LOGGER.info("Successfully generated the Long Vote Cast Return Codes Share. [contextIds: {}]", contextIds);

		final LongReturnCodesSharePayload payload = new LongReturnCodesSharePayload(gqGroup, longVoteCastReturnCodesShare);

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
			signedPayload = payloadSignatureService.sign(payload, signingKey, certificateChain);
		} catch (final PayloadSignatureException e) {
			throw new IllegalStateException(String.format("Could not sign payload. [contextIds: %s]", contextIds), e);
		}
		LOGGER.info("Successfully signed Long Return Codes Share payload. [contextIds: {}]", contextIds);

		try {
			return objectMapper.writeValueAsBytes(signedPayload);
		} catch (final JsonProcessingException e) {
			throw new UncheckedIOException("Could not serialize long return codes share payload", e);
		}
	}
}
