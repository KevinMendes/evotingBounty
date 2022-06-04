/*
 * (c) Copyright 2022. Swiss Post Ltd
 */
package ch.post.it.evoting.securedatamanager.configuration.setupvoting;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;
import static ch.post.it.evoting.securedatamanager.commons.Constants.NODE_IDS;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;
import java.util.stream.Collectors;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.exceptions.FailedValidationException;
import ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations;

/**
 * Encapsulates the flattened (combining all chunks) control component node contributions.
 * <p>
 * All control components generate encrypted long return code shares during the configuration phase. The encrypted long return code shares contain
 * both the exponentiated encrypted partial choice return codes and the exponentiated encrypted confirmation keys.
 */
public class EncryptedNodeLongReturnCodeShares {

	private final String electionEventId;
	private final String verificationCardSetId;
	private final List<String> verificationCardIds;
	private final List<EncryptedSingleNodeLongReturnCodeShares> nodeReturnCodesValues;

	private EncryptedNodeLongReturnCodeShares(final String electionEventId, final String verificationCardSetId,
			final List<String> verificationCardIds,
			final List<EncryptedSingleNodeLongReturnCodeShares> nodeReturnCodesValues) {
		this.electionEventId = electionEventId;
		this.verificationCardSetId = verificationCardSetId;
		this.verificationCardIds = verificationCardIds;
		this.nodeReturnCodesValues = nodeReturnCodesValues;
	}

	public String getElectionEventId() {
		return electionEventId;
	}

	public String getVerificationCardSetId() {
		return verificationCardSetId;
	}

	public List<String> getVerificationCardIds() {
		return verificationCardIds;
	}

	public List<EncryptedSingleNodeLongReturnCodeShares> getNodeReturnCodesValues() {
		return nodeReturnCodesValues;
	}

	public static class Builder {

		private String electionEventId;
		private String verificationCardSetId;
		private List<String> verificationCardIds;
		private List<EncryptedSingleNodeLongReturnCodeShares> nodeReturnCodesValues;

		public Builder setElectionEventId(final String electionEventId) {
			this.electionEventId = electionEventId;
			return this;
		}

		public Builder setVerificationCardSetId(final String verificationCardSetId) {
			this.verificationCardSetId = verificationCardSetId;
			return this;
		}

		public Builder setVerificationCardIds(final List<String> verificationCardIds) {
			this.verificationCardIds = verificationCardIds;
			return this;
		}

		public Builder setNodeReturnCodesValues(final List<EncryptedSingleNodeLongReturnCodeShares> nodeReturnCodesValues) {
			this.nodeReturnCodesValues = nodeReturnCodesValues;
			return this;
		}

		/**
		 * Creates the EncryptedNodeLongCodeShares. All fields must have been set and be non-null.
		 *
		 * @return a new EncryptedNodeLongCodeShares.
		 * @throws NullPointerException      if any of the fields is null.
		 * @throws IllegalArgumentException  if the return codes contributions node ids are invalid (size and values).
		 * @throws FailedValidationException if
		 *                                   <ul>
		 *                                       <li>{@code electionEventId} has an invalid UUID format.</li>
		 *                                       <li>{@code verificationCardSetId} has an invalid UUID format.</li>
		 *                                       <li>{@code verificationCardIds} contains an id with an invalid UUID format.</li>
		 *                                   </ul>
		 */
		public EncryptedNodeLongReturnCodeShares build() {
			validateUUID(electionEventId);
			validateUUID(verificationCardSetId);
			verificationCardIds.forEach(UUIDValidations::validateUUID);
			checkNotNull(nodeReturnCodesValues);

			final List<Integer> nodeReturnCodesValuesNodeIds = nodeReturnCodesValues.stream()
					.map(EncryptedSingleNodeLongReturnCodeShares::getNodeId)
					.collect(Collectors.toList());
			checkArgument(NODE_IDS.size() == nodeReturnCodesValuesNodeIds.size() && nodeReturnCodesValuesNodeIds.containsAll(NODE_IDS),
					String.format("Wrong number or invalid values of return codes contributions node ids. [required node ids: %s, found: %s]",
							NODE_IDS,
							nodeReturnCodesValuesNodeIds));

			return new EncryptedNodeLongReturnCodeShares(electionEventId, verificationCardSetId, verificationCardIds, nodeReturnCodesValues);
		}
	}
}
