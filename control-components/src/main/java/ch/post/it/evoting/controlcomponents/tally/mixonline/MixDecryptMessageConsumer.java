/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.tally.mixonline;

import static ch.post.it.evoting.domain.SharedQueue.MIX_DEC_ONLINE_REQUEST_PATTERN;
import static ch.post.it.evoting.domain.SharedQueue.MIX_DEC_ONLINE_RESPONSE_PATTERN;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.controlcomponents.ControlComponentsApplicationBootstrap;
import ch.post.it.evoting.controlcomponents.CryptolibPayloadSignatureService;
import ch.post.it.evoting.controlcomponents.keymanagement.ElectionSigningKeys;
import ch.post.it.evoting.controlcomponents.keymanagement.ElectionSigningKeysService;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.MixnetInitialPayload;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.MixnetPayload;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.MixnetShufflePayload;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.MixnetState;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientKeyPair;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPrivateKey;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;
import ch.post.it.evoting.domain.election.model.messaging.PayloadSignatureException;
import ch.post.it.evoting.domain.election.model.messaging.PayloadVerificationException;

@Service
public class MixDecryptMessageConsumer {

	private static final Logger LOGGER = LoggerFactory.getLogger(MixDecryptMessageConsumer.class);

	private final ObjectMapper objectMapper;
	private final RabbitTemplate rabbitTemplate;
	private final MixDecryptOnlineService mixDecryptOnlineService;
	private final ElectionSigningKeysService electionSigningKeysService;
	private final CryptolibPayloadSignatureService signatureService;

	@Value("${nodeID}")
	private int nodeId;

	public MixDecryptMessageConsumer(
			final ObjectMapper objectMapper,
			final RabbitTemplate rabbitTemplate,
			final MixDecryptOnlineService mixDecryptOnlineService,
			final ElectionSigningKeysService electionSigningKeysService,
			final CryptolibPayloadSignatureService signatureService) {
		this.objectMapper = objectMapper;
		this.rabbitTemplate = rabbitTemplate;
		this.mixDecryptOnlineService = mixDecryptOnlineService;
		this.electionSigningKeysService = electionSigningKeysService;
		this.signatureService = signatureService;
	}

	private static boolean containsNullFields(final MixnetPayload mixnetPayload) {
		if (mixnetPayload instanceof MixnetInitialPayload) {
			final MixnetInitialPayload mixnetInitialPayload = (MixnetInitialPayload) mixnetPayload;
			return mixnetInitialPayload.getElectionEventId() == null || mixnetInitialPayload.getBallotBoxId() == null
					|| mixnetInitialPayload.getEncryptionGroup() == null || mixnetInitialPayload.getEncryptedVotes() == null
					|| mixnetInitialPayload.getElectionPublicKey() == null;
		} else {
			final MixnetShufflePayload mixnetShufflePayload = (MixnetShufflePayload) mixnetPayload;
			return mixnetShufflePayload.getElectionEventId() == null || mixnetShufflePayload.getBallotBoxId() == null
					|| mixnetShufflePayload.getEncryptionGroup() == null || mixnetShufflePayload.getVerifiableDecryptions() == null
					|| mixnetShufflePayload.getEncryptedVotes() == null || mixnetShufflePayload.getNodeElectionPublicKey() == null
					|| mixnetShufflePayload.getPreviousRemainingElectionPublicKey() == null
					|| mixnetShufflePayload.getRemainingElectionPublicKey() == null;
		}
	}

	@RabbitListener(queues = MIX_DEC_ONLINE_REQUEST_PATTERN + "${nodeID}", id = ControlComponentsApplicationBootstrap.MIXING_CONTAINER_PREFIX
			+ ".partialMixingDecryptionRequestQueue", autoStartup = "false")
	public void onMessage(final Message message) throws IOException, KeyManagementException, PayloadSignatureException {
		final byte[] messageBody = message.getBody();
		final byte[] mixnetStateBytes = new byte[messageBody.length - 1];
		System.arraycopy(messageBody, 1, mixnetStateBytes, 0, messageBody.length - 1);

		MixnetState mixnetState = objectMapper.readValue(mixnetStateBytes, MixnetState.class);

		final List<String> validationErrors = validateData(mixnetState);
		if (!validationErrors.isEmpty()) {
			sendWithError(mixnetState, "The following fields present validation errors: " + validationErrors);
			return;
		}
		final List<ElGamalMultiRecipientCiphertext> ciphertexts = mixnetState.getPayload().getEncryptedVotes();
		final ElGamalMultiRecipientPublicKey remainingPublicKey = mixnetState.getPayload().getRemainingElectionPublicKey();
		final String ballotBoxId = mixnetState.getPayload().getBallotBoxId();
		final String electionEventId = mixnetState.getPayload().getElectionEventId();

		LOGGER.info("Received ballot box {} for election event {} from {} for mixing and decrypting", ballotBoxId, electionEventId, message);

		final GqGroup gqGroup = remainingPublicKey.getGroup();
		final ZqElement zeroZqElement = ZqElement.create(BigInteger.ZERO, ZqGroup.sameOrderAs(gqGroup));
		final ElGamalMultiRecipientPrivateKey zeroPrivateKey = new ElGamalMultiRecipientPrivateKey(Collections.singletonList(zeroZqElement));

		final ElGamalMultiRecipientKeyPair ccmjElectionKeyPair = ElGamalMultiRecipientKeyPair.from(zeroPrivateKey, gqGroup.getGenerator());

		LOGGER.info("Mixing and decryption of {} from control component node {} finished, sending results to {}{}.", ballotBoxId,
				mixnetState.getNodeToVisit(), MIX_DEC_ONLINE_RESPONSE_PATTERN, nodeId);

		final MixDecryptOutput mixDecryptOutput;
		try {
			mixDecryptOutput = mixDecryptOnlineService.mixDecOnline(electionEventId, ballotBoxId, ciphertexts, remainingPublicKey,
					ccmjElectionKeyPair);
		} catch (final IllegalArgumentException e) {
			sendWithError(mixnetState, "Incompatible input arguments: " + e.getMessage());
			return;
		} catch (final NullPointerException e) {
			sendWithError(mixnetState, "The payload contains null objects.");
			return;
		}

		LOGGER.info("Mixing and decryption of {} from control component node {} finished, preparing shuffle payload.", ballotBoxId,
				mixnetState.getNodeToVisit());

		// Pack result back into a MixnetState
		final MixnetShufflePayload payload = new MixnetShufflePayload(electionEventId, ballotBoxId, ccmjElectionKeyPair.getGroup(),
				mixDecryptOutput.getVerifiableDecryptions(),
				mixDecryptOutput.getVerifiableShuffle(),
				mixDecryptOutput.getRemainingElectionPublicKey(),
				remainingPublicKey,
				ccmjElectionKeyPair.getPublicKey(),
				mixnetState.getNodeToVisit());

		LOGGER.info("Signing mixnet payload...");

		final ElectionSigningKeys electionSigningKeys = electionSigningKeysService.getElectionSigningKeys(electionEventId);
		final PrivateKey ccnSigningKey = electionSigningKeys.privateKey();
		final X509Certificate[] ccnCertificateChain = electionSigningKeys.certificateChain();

		MixnetShufflePayload signedPayload = signatureService.sign(payload, ccnSigningKey, ccnCertificateChain);

		LOGGER.info("Payload signed, sending mixnet state to queue {}{}.", MIX_DEC_ONLINE_RESPONSE_PATTERN, nodeId);

		mixnetState = new MixnetState(mixnetState.getNodeToVisit(), signedPayload, mixnetState.getRetryCount(), null);
		send(mixnetState);
	}

	/**
	 * Checks that the fields of a given MixnetState object are set correctly.
	 *
	 * @param mixnetState the MixnetState object to be validated.
	 * @return a list of error messages, if there are any
	 */
	private List<String> validateData(final MixnetState mixnetState) throws KeyManagementException {
		final List<String> errors = new ArrayList<>();

		final MixnetPayload payload = mixnetState.getPayload();

		if (mixnetState.getNodeToVisit() != nodeId) {
			final String errorMessage = String.format("Node to visit is expected to be %d, but was %d", nodeId, mixnetState.getNodeToVisit());
			errors.add(errorMessage);
			LOGGER.error(errorMessage);
		}
		if (payload == null) {
			final String errorMessage = "No payload provided";
			errors.add(errorMessage);
			LOGGER.error(errorMessage);
		} else if (containsNullFields(payload)) {
			final String errorMessage = "The payload contains null objects.";
			errors.add(errorMessage);
			LOGGER.error(errorMessage);
		} else {
			LOGGER.info("Verifying signature...");
			final String electionEventId = mixnetState.getPayload().getElectionEventId();
			final ElectionSigningKeys electionSigningKeys = electionSigningKeysService.getElectionSigningKeys(electionEventId);
			final X509Certificate[] certificateChain = electionSigningKeys.certificateChain();
			final X509Certificate platformRootCertificate = certificateChain[certificateChain.length - 1];
			try {
				final boolean validSignature = signatureService.verify(payload, platformRootCertificate);

				if (!validSignature) {
					final String errorMessage = "Invalid signature.";
					errors.add(errorMessage);
					LOGGER.error(errorMessage);
				} else {
					LOGGER.info("The signature is valid.");
				}
			} catch (final PayloadVerificationException e) {
				final String errorMessage = "Signature verification failed.";
				errors.add(errorMessage);
				LOGGER.error(errorMessage);
			}
		}

		return errors;
	}

	/**
	 * Sends the provided MixnetState object to the node's response queue
	 *
	 * @param mixnetState the MixnetState object to be send
	 */
	private void send(final MixnetState mixnetState) {
		try {
			final String outputMixnetStateJson = objectMapper.writeValueAsString(mixnetState);

			final byte[] serializedOutputMixnetState = outputMixnetStateJson.getBytes(StandardCharsets.UTF_8);
			final byte[] byteContent = new byte[serializedOutputMixnetState.length + 1];
			byteContent[0] = 0;
			System.arraycopy(serializedOutputMixnetState, 0, byteContent, 1, serializedOutputMixnetState.length);

			rabbitTemplate.convertAndSend(MIX_DEC_ONLINE_RESPONSE_PATTERN + nodeId, byteContent);
		} catch (final IOException e) {
			throw new UncheckedIOException("Failed to send the mixing DTO", e);
		}
	}

	/**
	 * Sets the provided MixnetState object's error message and sends it to the node's response queue.
	 *
	 * @param mixnetState  the MixnetState object to be send
	 * @param errorMessage the error message to be set
	 */
	private void sendWithError(final MixnetState mixnetState, final String errorMessage) {
		mixnetState.setMixnetError(errorMessage);
		send(mixnetState);
	}
}
