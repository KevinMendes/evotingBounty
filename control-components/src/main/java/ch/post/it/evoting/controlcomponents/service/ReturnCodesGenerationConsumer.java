/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.service;

import static ch.post.it.evoting.domain.SharedQueue.GEN_ENC_LONG_CODE_SHARES_REQUEST_PATTERN;
import static ch.post.it.evoting.domain.SharedQueue.GEN_ENC_LONG_CODE_SHARES_RESPONSE_PATTERN;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.controlcomponents.CcrjReturnCodesKeys;
import ch.post.it.evoting.controlcomponents.CcrjReturnCodesKeysService;
import ch.post.it.evoting.controlcomponents.ControlComponentsApplicationBootstrap;
import ch.post.it.evoting.controlcomponents.CryptolibPayloadSignatureService;
import ch.post.it.evoting.controlcomponents.VerificationCardSetService;
import ch.post.it.evoting.controlcomponents.configuration.setupvoting.GenEncLongCodeSharesContext;
import ch.post.it.evoting.controlcomponents.configuration.setupvoting.GenEncLongCodeSharesInput;
import ch.post.it.evoting.controlcomponents.configuration.setupvoting.GenEncLongCodeSharesOutput;
import ch.post.it.evoting.controlcomponents.configuration.setupvoting.GenEncLongCodeSharesService;
import ch.post.it.evoting.controlcomponents.keymanagement.ElectionSigningKeys;
import ch.post.it.evoting.controlcomponents.keymanagement.ElectionSigningKeysService;
import ch.post.it.evoting.controlcomponents.keymanagement.KeysManager;
import ch.post.it.evoting.controlcomponents.service.exception.MissingSignatureException;
import ch.post.it.evoting.cryptoprimitives.domain.returncodes.ChoiceCodeGenerationDTO;
import ch.post.it.evoting.cryptoprimitives.domain.returncodes.ReturnCodeGenerationInput;
import ch.post.it.evoting.cryptoprimitives.domain.returncodes.ReturnCodeGenerationOutput;
import ch.post.it.evoting.cryptoprimitives.domain.returncodes.ReturnCodeGenerationRequestPayload;
import ch.post.it.evoting.cryptoprimitives.domain.returncodes.ReturnCodeGenerationResponsePayload;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ExponentiationProof;
import ch.post.it.evoting.domain.election.model.messaging.InvalidSignatureException;
import ch.post.it.evoting.domain.election.model.messaging.PayloadSignatureException;
import ch.post.it.evoting.domain.election.model.messaging.PayloadVerificationException;

/**
 * Consumer class for the generation of Return Codes in the configuration phase of the Swiss Post Voting Protocol.
 */
@Service
public class ReturnCodesGenerationConsumer {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReturnCodesGenerationConsumer.class);

	@SuppressWarnings("java:S115")
	private static final int l_HB64 = 44;

	private final RabbitTemplate rabbitTemplate;
	private final KeysManager keysManager;
	private final CryptolibPayloadSignatureService payloadSignatureService;
	private final ObjectMapper objectMapper;
	private final ElectionSigningKeysService electionSigningKeysService;
	private final CcrjReturnCodesKeysService ccrjReturnCodesKeysService;
	private final GenEncLongCodeSharesService genEncLongCodeSharesService;
	private final VerificationCardSetService verificationCardSetService;

	@Value("${keys.nodeId:defCcxId}")
	private String controlComponentId;

	@Value("${nodeID}")
	private int nodeId;

	public ReturnCodesGenerationConsumer(
			final RabbitTemplate rabbitTemplate,
			final KeysManager keysManager,
			final CryptolibPayloadSignatureService payloadSignatureService,
			final ObjectMapper objectMapper,
			final ElectionSigningKeysService electionSigningKeysService,
			final CcrjReturnCodesKeysService ccrjReturnCodesKeysService,
			final GenEncLongCodeSharesService genEncLongCodeSharesService,
			final VerificationCardSetService verificationCardSetService) {

		this.rabbitTemplate = rabbitTemplate;
		this.keysManager = keysManager;
		this.payloadSignatureService = payloadSignatureService;
		this.objectMapper = objectMapper;
		this.electionSigningKeysService = electionSigningKeysService;
		this.ccrjReturnCodesKeysService = ccrjReturnCodesKeysService;
		this.genEncLongCodeSharesService = genEncLongCodeSharesService;
		this.verificationCardSetService = verificationCardSetService;
	}

	@RabbitListener(queues = GEN_ENC_LONG_CODE_SHARES_REQUEST_PATTERN + "${nodeID}", id =
			ControlComponentsApplicationBootstrap.CHOICE_CODES_CONTAINER_PREFIX + ".generation.computation.request.queue", autoStartup = "false")
	public void onMessage(final Message message) throws IOException, TimeoutException {
		checkNotNull(message);

		final byte[] messageBody = message.getBody();
		final byte[] dtoBytes = new byte[messageBody.length - 1];
		System.arraycopy(messageBody, 1, dtoBytes, 0, messageBody.length - 1);

		final ChoiceCodeGenerationDTO<ReturnCodeGenerationRequestPayload> choiceCodeGenerationDTO = objectMapper
				.readValue(dtoBytes, new TypeReference<ChoiceCodeGenerationDTO<ReturnCodeGenerationRequestPayload>>() {
				});

		final ReturnCodeGenerationRequestPayload payload = choiceCodeGenerationDTO.getPayload();

		try {
			validateSignature(payload);
			final String electionEventId = payload.getElectionEventId();
			final String verificationCardSetId = payload.getVerificationCardSetId();
			final CcrjReturnCodesKeys ccrjReturnCodesKeys = ccrjReturnCodesKeysService.getCcrjReturnCodesKeys(electionEventId);
			final GqGroup gqGroup = ccrjReturnCodesKeys.getCcrjChoiceReturnCodesEncryptionKeyPair().getGroup();

			// Sanity check the partial Choice Return Codes allow list before saving.
			final List<String> payloadAllowList = validateAllowList(payload);

			verificationCardSetService.updateAllowList(electionEventId, verificationCardSetId, payloadAllowList,
					payload.getCombinedCorrectnessInformation());

			final GenEncLongCodeSharesContext context = new GenEncLongCodeSharesContext.Builder()
					.electionEventId(electionEventId)
					.verificationCardSetId(verificationCardSetId)
					.gqGroup(gqGroup)
					.nodeID(nodeId)
					.build();

			final GenEncLongCodeSharesInput input = new GenEncLongCodeSharesInput.Builder()
					.returnCodesGenerationSecretKey(ccrjReturnCodesKeys.getCcrjReturnCodesGenerationSecretKey())
					.verificationCardIDs(getVerificationCardIDs(payload))
					.verificationCardPublicKeys(getVerificationCardPublicKeys(payload))
					.encryptedHashedPartialChoiceReturnCodes(getEncryptedHashedPartialChoiceReturnCodes(payload))
					.encryptedHashedConfirmationKeys(getEncryptedHashedConfirmationKeys(payload))
					.build();

			final GenEncLongCodeSharesOutput output = genEncLongCodeSharesService.genEncLongCodeShares(context, input);

			final List<ReturnCodeGenerationOutput> returnCodeGenerationOutputs = toReturnCodeGenerationOutput(payload, output);

			sendResponse(electionEventId, verificationCardSetId, gqGroup, choiceCodeGenerationDTO, returnCodeGenerationOutputs);
		} catch (final KeyManagementException | MessagingException e) {
			LOGGER.error("Failed to handle the return code generation request.", e);
		} catch (final PayloadSignatureException e) {
			LOGGER.error("Failed to sign the return code generation response.", e);
		} catch (final MissingSignatureException e) {
			LOGGER.error("Failed to find a signature in the received request.", e);
		} catch (final InvalidSignatureException e) {
			LOGGER.error("The signature of the received request is invalid.", e);
		} catch (final PayloadVerificationException e) {
			LOGGER.error("Exception while trying to verify the signature of the received request.", e);
		}

	}

	private List<ReturnCodeGenerationOutput> toReturnCodeGenerationOutput(final ReturnCodeGenerationRequestPayload payload,
			final GenEncLongCodeSharesOutput output) {
		final List<String> vc = getVerificationCardIDs(payload);
		final List<ReturnCodeGenerationOutput> returnCodeGenerationOutputs = new ArrayList<>();

		checkArgument(vc.size() == output.getExponentiatedEncryptedHashedPartialChoiceReturnCodes().size());

		for (int id = 0; id < output.getExponentiatedEncryptedHashedPartialChoiceReturnCodes().size(); id++) {
			final String vc_id = vc.get(id);
			final GqElement voterChoiceReturnCodeGenerationPublicKeys = output.getVoterChoiceReturnCodeGenerationPublicKeys().get(id);
			final GqElement voterVoteCastReturnCodeGenerationPublicKeys = output.getVoterVoteCastReturnCodeGenerationPublicKeys().get(id);
			final ElGamalMultiRecipientCiphertext exponentiatedEncryptedHashedPartialChoiceReturnCodes =
					output.getExponentiatedEncryptedHashedPartialChoiceReturnCodes().get(id);
			final ExponentiationProof proofsCorrectExponentiationPartialChoiceReturnCodes =
					output.getProofsCorrectExponentiationPartialChoiceReturnCodes().get(id);
			final ElGamalMultiRecipientCiphertext exponentiatedEncryptedHashedConfirmationKeys =
					output.getExponentiatedEncryptedHashedConfirmationKeys().get(id);
			final ExponentiationProof proofsCorrectExponentiationConfirmationKeys =
					output.getProofsCorrectExponentiationConfirmationKeys().get(id);

			final ReturnCodeGenerationOutput returnCodeGenerationOutput = new ReturnCodeGenerationOutput(
					vc_id,
					new ElGamalMultiRecipientPublicKey(Collections.singletonList(voterChoiceReturnCodeGenerationPublicKeys)),
					new ElGamalMultiRecipientPublicKey(Collections.singletonList(voterVoteCastReturnCodeGenerationPublicKeys)),
					exponentiatedEncryptedHashedPartialChoiceReturnCodes,
					proofsCorrectExponentiationPartialChoiceReturnCodes,
					exponentiatedEncryptedHashedConfirmationKeys,
					proofsCorrectExponentiationConfirmationKeys);

			returnCodeGenerationOutputs.add(returnCodeGenerationOutput);
		}

		return returnCodeGenerationOutputs;
	}

	private List<String> getVerificationCardIDs(final ReturnCodeGenerationRequestPayload payload) {
		final List<ReturnCodeGenerationInput> returnCodeGenerationInputs = payload.getReturnCodeGenerationInputs();

		return returnCodeGenerationInputs.stream().map(ReturnCodeGenerationInput::getVerificationCardId).collect(Collectors.toList());
	}

	private List<ElGamalMultiRecipientPublicKey> getVerificationCardPublicKeys(final ReturnCodeGenerationRequestPayload payload) {
		final List<ReturnCodeGenerationInput> returnCodeGenerationInputs = payload.getReturnCodeGenerationInputs();

		return returnCodeGenerationInputs.stream().map(ReturnCodeGenerationInput::getVerificationCardPublicKey).collect(Collectors.toList());
	}

	private GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> getEncryptedHashedPartialChoiceReturnCodes(
			final ReturnCodeGenerationRequestPayload payload) {
		final List<ReturnCodeGenerationInput> returnCodeGenerationInputs = payload.getReturnCodeGenerationInputs();

		return returnCodeGenerationInputs.stream().map(ReturnCodeGenerationInput::getEncryptedHashedSquaredPartialChoiceReturnCodes)
				.collect(GroupVector.toGroupVector());
	}

	private GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> getEncryptedHashedConfirmationKeys(
			final ReturnCodeGenerationRequestPayload payload) {
		final List<ReturnCodeGenerationInput> returnCodeGenerationInputs = payload.getReturnCodeGenerationInputs();

		return returnCodeGenerationInputs.stream().map(ReturnCodeGenerationInput::getEncryptedHashedSquaredConfirmationKey)
				.collect(GroupVector.toGroupVector());
	}

	private void validateSignature(final ReturnCodeGenerationRequestPayload payload)
			throws MissingSignatureException, InvalidSignatureException, PayloadVerificationException {

		final String payloadId = String.format("[electionEventId:%s, verificationCardSetId:%s, chunkID:%s]", payload.getElectionEventId(),
				payload.getVerificationCardSetId(), payload.getChunkId());

		LOGGER.info("Checking the signature of payload {}...", payloadId);

		if (payload.getSignature() == null) {
			LOGGER.warn("REJECTED payload {} because it is not signed", payloadId);
			throw new MissingSignatureException(payloadId);
		}

		final boolean isPayloadSignatureValid = payloadSignatureService.verify(payload, keysManager.getPlatformCACertificate());

		if (!isPayloadSignatureValid) {
			LOGGER.warn("REJECTED payload {} because the signature is not valid.", payloadId);
			throw new InvalidSignatureException(controlComponentId, payloadId);
		}

		LOGGER.info("Signature of payload {} accepted for generation.", payloadId);
	}

	private List<String> validateAllowList(final ReturnCodeGenerationRequestPayload payload) {
		final List<String> payloadAllowList = payload.getPartialChoiceReturnCodesAllowList();
		final int totalNumberOfVotingOptions = payload.getCombinedCorrectnessInformation().getTotalNumberOfVotingOptions();
		final int numberOfVotingCards = payload.getReturnCodeGenerationInputs().size();
		checkArgument(totalNumberOfVotingOptions * numberOfVotingCards == payloadAllowList.size(), String.format(
				"The total number of voting options times the number of voting cards must be equal to the size of the partial Choice Return Codes allow list. [voting options: %s, voting cards: %s, allow list: %s]",
				totalNumberOfVotingOptions, numberOfVotingCards, payloadAllowList.size()));
		payloadAllowList.forEach(element -> checkArgument(element.length() == l_HB64, String.format(
				"At least one element in the partial Choice Return Codes allow list has incorrect length. [element: %s, allowed length: %s]", element,
				l_HB64)));
		final ArrayList<String> payloadAllowListCopy = new ArrayList<>(payloadAllowList);
		Collections.sort(payloadAllowListCopy);
		checkArgument(payloadAllowList.equals(payloadAllowListCopy), "The allow list is not lexicographically sorted.");

		return payloadAllowList;
	}

	private void sendResponse(final String electionEventId, final String verificationCardSetId, final GqGroup gqGroup,
			final ChoiceCodeGenerationDTO<ReturnCodeGenerationRequestPayload> choiceCodeGenerationDTO,
			final List<ReturnCodeGenerationOutput> returnCodeGenerationOutputList) throws KeyManagementException, PayloadSignatureException {

		final int chunkId = choiceCodeGenerationDTO.getPayload().getChunkId();

		final ReturnCodeGenerationResponsePayload responsePayload = new ReturnCodeGenerationResponsePayload(
				choiceCodeGenerationDTO.getPayload().getTenantId(), electionEventId, verificationCardSetId, chunkId, gqGroup,
				returnCodeGenerationOutputList, nodeId);

		// Sign response payload.
		final ElectionSigningKeys electionSigningKeys = electionSigningKeysService.getElectionSigningKeys(electionEventId);
		final ReturnCodeGenerationResponsePayload signedPayload = payloadSignatureService.sign(responsePayload, electionSigningKeys.privateKey(),
				electionSigningKeys.certificateChain());

		LOGGER.info(
				"Successfully signed the payload after the Return Code generation for electionEventId {}, verificationCardSetId {} and chunkID {}.",
				electionEventId, verificationCardSetId, chunkId);

		final ChoiceCodeGenerationDTO<ReturnCodeGenerationResponsePayload> choiceCodeGenerationDTOResponse =
				new ChoiceCodeGenerationDTO<>(choiceCodeGenerationDTO.getCorrelationId(), choiceCodeGenerationDTO.getRequestId(), signedPayload);

		final String responseJson;
		try {
			responseJson = objectMapper.writeValueAsString(choiceCodeGenerationDTOResponse);
		} catch (final JsonProcessingException e) {
			throw new UncheckedIOException("Failed to serialize response ChoiceCodeGenerationDTO.", e);
		}
		final byte[] serializedResponsePayloadBytes = responseJson.getBytes(StandardCharsets.UTF_8);

		// The MessagingService in the voting-server expects the first byte to be the type.
		final byte[] byteContent = new byte[serializedResponsePayloadBytes.length + 1];
		byteContent[0] = 0;
		System.arraycopy(serializedResponsePayloadBytes, 0, byteContent, 1, serializedResponsePayloadBytes.length);

		rabbitTemplate.convertAndSend(GEN_ENC_LONG_CODE_SHARES_RESPONSE_PATTERN + nodeId, byteContent);
	}

}
