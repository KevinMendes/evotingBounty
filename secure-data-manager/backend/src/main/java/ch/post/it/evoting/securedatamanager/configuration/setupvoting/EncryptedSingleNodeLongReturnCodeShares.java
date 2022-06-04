/*
 * (c) Copyright 2022. Swiss Post Ltd
 */
package ch.post.it.evoting.securedatamanager.configuration.setupvoting;

import static ch.post.it.evoting.securedatamanager.commons.Constants.NODE_IDS;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.Stream.of;

import java.util.List;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.exceptions.FailedValidationException;
import ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;

/**
 * Encapsulates the flattened (combining all chunks) contributions of a single control component node.
 * <p>
 * All control components generate encrypted long return code shares during the configuration phase. The encrypted long return code shares contain
 * both the exponentiated encrypted partial choice return codes and the exponentiated encrypted confirmation keys.
 */
public class EncryptedSingleNodeLongReturnCodeShares {

	private final int nodeId;
	private final List<String> verificationCardIds;
	private final List<ElGamalMultiRecipientCiphertext> exponentiatedEncryptedPartialChoiceReturnCodes;
	private final List<ElGamalMultiRecipientCiphertext> exponentiatedEncryptedConfirmationKeys;

	private EncryptedSingleNodeLongReturnCodeShares(final int nodeId,
			final List<String> verificationCardIds,
			final List<ElGamalMultiRecipientCiphertext> exponentiatedEncryptedPartialChoiceReturnCodes,
			final List<ElGamalMultiRecipientCiphertext> exponentiatedEncryptedConfirmationKeys) {
		this.nodeId = nodeId;
		this.verificationCardIds = verificationCardIds;
		this.exponentiatedEncryptedPartialChoiceReturnCodes = exponentiatedEncryptedPartialChoiceReturnCodes;
		this.exponentiatedEncryptedConfirmationKeys = exponentiatedEncryptedConfirmationKeys;
	}

	public int getNodeId() {
		return nodeId;
	}

	public List<String> getVerificationCardIds() {
		return verificationCardIds;
	}

	public List<ElGamalMultiRecipientCiphertext> getExponentiatedEncryptedPartialChoiceReturnCodes() {
		return exponentiatedEncryptedPartialChoiceReturnCodes;
	}

	public List<ElGamalMultiRecipientCiphertext> getExponentiatedEncryptedConfirmationKeys() {
		return exponentiatedEncryptedConfirmationKeys;
	}

	public static class Builder {
		private int nodeId;
		private List<String> verificationCardIds;
		private List<ElGamalMultiRecipientCiphertext> exponentiatedEncryptedPartialChoiceReturnCodes;
		private List<ElGamalMultiRecipientCiphertext> exponentiatedEncryptedConfirmationKeys;

		public Builder setNodeId(final int nodeId) {
			this.nodeId = nodeId;
			return this;
		}

		public Builder setVerificationCardIds(final List<String> verificationCardIds) {
			this.verificationCardIds = verificationCardIds;
			return this;
		}

		public Builder setExponentiatedEncryptedPartialChoiceReturnCodes(
				final List<ElGamalMultiRecipientCiphertext> exponentiatedEncryptedPartialChoiceReturnCodes) {
			this.exponentiatedEncryptedPartialChoiceReturnCodes = exponentiatedEncryptedPartialChoiceReturnCodes;
			return this;
		}

		public Builder setExponentiatedEncryptedConfirmationKeys(
				final List<ElGamalMultiRecipientCiphertext> exponentiatedEncryptedConfirmationKeys) {
			this.exponentiatedEncryptedConfirmationKeys = exponentiatedEncryptedConfirmationKeys;
			return this;
		}

		/**
		 * Creates the EncryptedSingleNodeLongReturnCodeShares. All fields must have been set and be non-null.
		 *
		 * @return a new EncryptedNodeLongCodeShares.
		 * @throws NullPointerException      if any of the fields is null.
		 * @throws IllegalArgumentException  if
		 *                                   <ul>
		 *                                       <li>the return codes contributions node ids are invalid (size or values).</li>
		 *                                       <li>the {@code verificationCardIds}, {@code exponentiatedEncryptedPartialChoiceReturnCodes} and {@code exponentiatedEncryptedConfirmationKey} do not have the same size.</li>
		 *                                   </ul>
		 * @throws FailedValidationException if
		 *                                   <ul>
		 *                                       <li>{@code electionEventId} has an invalid UUID format.</li>
		 *                                       <li>{@code verificationCardSetId} has an invalid UUID format.</li>
		 *                                       <li>{@code verificationCardIds} contains an id with an invalid UUID format.</li>
		 *                                   </ul>
		 */
		public EncryptedSingleNodeLongReturnCodeShares build() {
			checkArgument(NODE_IDS.contains(nodeId),
					String.format("Control component node id is incorrect. [required node ids: %s, found: %s]", NODE_IDS, nodeId));
			checkNotNull(verificationCardIds);
			verificationCardIds.forEach(UUIDValidations::validateUUID);
			checkNotNull(exponentiatedEncryptedPartialChoiceReturnCodes);
			checkNotNull(exponentiatedEncryptedConfirmationKeys);

			checkArgument(of(verificationCardIds, exponentiatedEncryptedPartialChoiceReturnCodes, exponentiatedEncryptedConfirmationKeys)
					.map(List::size)
					.collect(toSet())
					.size()
					== 1, "The flattened contributions of a single control component node must the same size.");

			return new EncryptedSingleNodeLongReturnCodeShares(nodeId, verificationCardIds, exponentiatedEncryptedPartialChoiceReturnCodes,
					exponentiatedEncryptedConfirmationKeys);
		}
	}
}
