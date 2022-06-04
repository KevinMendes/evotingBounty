/*
 * (c) Copyright 2022. Swiss Post Ltd
 */
package ch.post.it.evoting.securedatamanager.configuration.setupvoting;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;
import static ch.post.it.evoting.securedatamanager.commons.Constants.NODE_IDS;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.exceptions.FailedValidationException;
import ch.post.it.evoting.cryptoprimitives.domain.returncodes.ChoiceCodeGenerationDTO;
import ch.post.it.evoting.cryptoprimitives.domain.returncodes.ReturnCodeGenerationResponsePayload;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;

/**
 * Allows to combine the node contributions responses.
 */
@Service
public class EncryptedNodeLongReturnCodeSharesService {

	private static final Logger LOGGER = LoggerFactory.getLogger(EncryptedNodeLongReturnCodeSharesService.class);

	private final NodeContributionsResponsesService nodeContributionsResponsesService;

	public EncryptedNodeLongReturnCodeSharesService(final NodeContributionsResponsesService nodeContributionsResponsesService) {
		this.nodeContributionsResponsesService = nodeContributionsResponsesService;
	}

	/**
	 * Loads all node contributions responses into an {@link EncryptedNodeLongReturnCodeShares} for the given {@code electionEventId} and {@code
	 * verificationCardSetId}.
	 *
	 * @param electionEventId       the node contributions responses' election event id. Must be non-null and a valid UUID.
	 * @param verificationCardSetId the node contributions responses' verification card set id. Must be non-null and a valid UUID.
	 * @return an {@link EncryptedNodeLongReturnCodeShares}
	 * @throws FailedValidationException if {@code electionEventId} or {@code verificationCardSetId} is invalid.
	 */
	public EncryptedNodeLongReturnCodeShares load(final String electionEventId, final String verificationCardSetId) {
		validateUUID(electionEventId);
		validateUUID(verificationCardSetId);

		LOGGER.info("Loading the node contributions. [electionEventId: {}, verificationCardSetId: {}]", electionEventId, verificationCardSetId);

		final List<List<ChoiceCodeGenerationDTO<ReturnCodeGenerationResponsePayload>>> nodeContributionsResponses =
				nodeContributionsResponsesService.load(electionEventId, verificationCardSetId);

		if (nodeContributionsResponses.isEmpty()) {
			throw new IllegalStateException(
					String.format("No node contributions responses. [electionEventId: %s, verificationCardSetId: %s]", electionEventId,
							verificationCardSetId));
		}

		final List<EncryptedSingleNodeLongReturnCodeShares> encryptedSingleNodeLongReturnCodesGenerationValues = NODE_IDS.stream()
				.parallel()
				.map(nodeId -> {
					final List<ReturnCodeGenerationOutput> list = nodeContributionsResponses.stream()
							.flatMap(Collection::stream)
							.map(ChoiceCodeGenerationDTO::getPayload)
							.filter(returnCodeGenerationResponsePayload -> returnCodeGenerationResponsePayload.getNodeId() == nodeId)
							.map(ReturnCodeGenerationResponsePayload::getReturnCodeGenerationOutputs)
							.flatMap(Collection::stream)
							.map(returnCodeGenerationOutput -> new ReturnCodeGenerationOutput(returnCodeGenerationOutput.getVerificationCardId(),
									returnCodeGenerationOutput.getExponentiatedEncryptedPartialChoiceReturnCodes(),
									returnCodeGenerationOutput.getExponentiatedEncryptedConfirmationKey()))
							.collect(Collectors.toList());

					final List<String> verificationCardIds = new ArrayList<>();
					final List<ElGamalMultiRecipientCiphertext> partialChoiceReturnCodes = new ArrayList<>();
					final List<ElGamalMultiRecipientCiphertext> confirmationKeys = new ArrayList<>();

					for (final ReturnCodeGenerationOutput output : list) {
						verificationCardIds.add(output.verificationCardId);
						partialChoiceReturnCodes.add(output.exponentiatedEncryptedPartialChoiceReturnCodes);
						confirmationKeys.add(output.exponentiatedEncryptedConfirmationKey);
					}

					return new EncryptedSingleNodeLongReturnCodeShares.Builder()
							.setNodeId(nodeId)
							.setVerificationCardIds(verificationCardIds)
							.setExponentiatedEncryptedPartialChoiceReturnCodes(partialChoiceReturnCodes)
							.setExponentiatedEncryptedConfirmationKeys(confirmationKeys)
							.build();
				}).collect(Collectors.toList());

		LOGGER.info("Node contributions successfully loaded. [electionEventId: {}, verificationCardSetId: {}]", electionEventId,
				verificationCardSetId);

		return new EncryptedNodeLongReturnCodeShares.Builder()
				.setElectionEventId(electionEventId)
				.setVerificationCardSetId(verificationCardSetId)
				.setVerificationCardIds(encryptedSingleNodeLongReturnCodesGenerationValues.get(0).getVerificationCardIds())
				.setNodeReturnCodesValues(encryptedSingleNodeLongReturnCodesGenerationValues)
				.build();
	}

	private static class ReturnCodeGenerationOutput {
		final String verificationCardId;
		final ElGamalMultiRecipientCiphertext exponentiatedEncryptedPartialChoiceReturnCodes;
		final ElGamalMultiRecipientCiphertext exponentiatedEncryptedConfirmationKey;

		public ReturnCodeGenerationOutput(final String verificationCardId,
				final ElGamalMultiRecipientCiphertext exponentiatedEncryptedPartialChoiceReturnCodes,
				final ElGamalMultiRecipientCiphertext exponentiatedEncryptedConfirmationKey) {
			this.verificationCardId = verificationCardId;
			this.exponentiatedEncryptedPartialChoiceReturnCodes = exponentiatedEncryptedPartialChoiceReturnCodes;
			this.exponentiatedEncryptedConfirmationKey = exponentiatedEncryptedConfirmationKey;
		}
	}

}
