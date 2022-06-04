/*
 * (c) Copyright 2022. Swiss Post Ltd
 */
package ch.post.it.evoting.securedatamanager.configuration.setupvoting;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashSet;
import java.util.List;

import com.google.common.collect.ImmutableList;

import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientKeyPair;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;

/**
 * Holds the output of the genVerDat algorithm.
 */
@SuppressWarnings("java:S115")
public class GenVerDatOutput {

	private static final int l_HB64 = 44;

	private final int size;
	private final GqGroup gqGroup;

	private final List<String> verificationCardIds;
	private final List<ElGamalMultiRecipientKeyPair> verificationCardKeyPairs;
	private final List<String> partialChoiceReturnCodesAllowList;
	private final List<String> ballotCastingKeys;
	private final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> encryptedHashedPartialChoiceReturnCodes;
	private final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> encryptedHashedConfirmationKeys;

	private GenVerDatOutput(final List<String> verificationCardIds, final List<ElGamalMultiRecipientKeyPair> verificationCardKeyPairs,
			final List<String> partialChoiceReturnCodesAllowList, final List<String> ballotCastingKeys,
			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> encryptedHashedPartialChoiceReturnCodes,
			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> encryptedHashedConfirmationKeys) {

		checkNotNull(verificationCardIds);
		checkNotNull(verificationCardKeyPairs);
		checkNotNull(partialChoiceReturnCodesAllowList);
		checkNotNull(ballotCastingKeys);
		checkNotNull(encryptedHashedConfirmationKeys);
		final List<String> verificationCardIdsCopy = ImmutableList.copyOf(verificationCardIds);
		final List<ElGamalMultiRecipientKeyPair> verificationCardKeyPairsCopy = ImmutableList.copyOf(verificationCardKeyPairs);
		final List<String> partialChoiceReturnCodesAllowListCopy = ImmutableList.copyOf(partialChoiceReturnCodesAllowList);
		final List<String> ballotCastingKeysCopy = ImmutableList.copyOf(ballotCastingKeys);

		checkArgument(!verificationCardIdsCopy.isEmpty(), "The output must not be empty.");

		this.size = verificationCardIdsCopy.size();
		checkArgument(size() == verificationCardKeyPairsCopy.size() && this.size == ballotCastingKeysCopy.size()
						&& this.size == encryptedHashedPartialChoiceReturnCodes.size() && this.size == encryptedHashedConfirmationKeys.size(),
				"All vectors must have the same size.");

		final int n = encryptedHashedPartialChoiceReturnCodes.getElementSize();
		checkArgument(partialChoiceReturnCodesAllowListCopy.size() == n * this.size,
				String.format("There must be %d elements in the allow list.", n * this.size));

		partialChoiceReturnCodesAllowListCopy.forEach(element -> checkArgument(element.length() == l_HB64,
				String.format("Elements in allowList must be of length %s.", l_HB64)));

		final GqGroup group = verificationCardKeyPairsCopy.get(0).getGroup();
		checkArgument(group.equals(encryptedHashedPartialChoiceReturnCodes.get(0).getGroup()) && group
				.equals(encryptedHashedConfirmationKeys.get(0).getGroup()), "All vectors must belong to the same group.");

		if (new HashSet<>(verificationCardIdsCopy).size() != this.size) {
			throw new IllegalStateException("The verificationCardId is duplicated.");
		}

		this.gqGroup = group;
		this.verificationCardIds = verificationCardIdsCopy;
		this.verificationCardKeyPairs = verificationCardKeyPairsCopy;
		this.partialChoiceReturnCodesAllowList = partialChoiceReturnCodesAllowListCopy;
		this.ballotCastingKeys = ballotCastingKeysCopy;
		this.encryptedHashedPartialChoiceReturnCodes = encryptedHashedPartialChoiceReturnCodes;
		this.encryptedHashedConfirmationKeys = encryptedHashedConfirmationKeys;
	}

	public List<String> getVerificationCardIds() {
		return verificationCardIds;
	}

	public List<ElGamalMultiRecipientKeyPair> getVerificationCardKeyPairs() {
		return verificationCardKeyPairs;
	}

	public List<String> getPartialChoiceReturnCodesAllowList() {
		return partialChoiceReturnCodesAllowList;
	}

	public List<String> getBallotCastingKeys() {
		return ballotCastingKeys;
	}

	public List<ElGamalMultiRecipientCiphertext> getEncryptedHashedPartialChoiceReturnCodes() {
		return encryptedHashedPartialChoiceReturnCodes;
	}

	public List<ElGamalMultiRecipientCiphertext> getEncryptedHashedConfirmationKeys() {
		return encryptedHashedConfirmationKeys;
	}

	public int size() {
		return this.size;
	}

	public GqGroup getGroup() {
		return this.gqGroup;
	}

	public static class Builder {
		private List<String> verificationCardIds;
		private List<ElGamalMultiRecipientKeyPair> verificationCardKeyPairs;
		private List<String> partialChoiceReturnCodesAllowList;
		private List<String> ballotCastingKeys;
		private GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> encryptedHashedPartialChoiceReturnCodes;
		private GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> encryptedHashedConfirmationKeys;

		public Builder setVerificationCardIds(List<String> verificationCardIds) {
			this.verificationCardIds = verificationCardIds;
			return this;
		}

		public Builder setVerificationCardKeyPairs(List<ElGamalMultiRecipientKeyPair> verificationCardKeyPairs) {
			this.verificationCardKeyPairs = verificationCardKeyPairs;
			return this;
		}

		public Builder setPartialChoiceReturnCodesAllowList(List<String> partialChoiceReturnCodesAllowList) {
			this.partialChoiceReturnCodesAllowList = partialChoiceReturnCodesAllowList;
			return this;
		}

		public Builder setBallotCastingKeys(List<String> ballotCastingKeys) {
			this.ballotCastingKeys = ballotCastingKeys;
			return this;
		}

		public Builder setEncryptedHashedPartialChoiceReturnCodes(
				GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> encryptedHashedPartialChoiceReturnCodes) {
			this.encryptedHashedPartialChoiceReturnCodes = encryptedHashedPartialChoiceReturnCodes;
			return this;
		}

		public Builder setEncryptedHashedConfirmationKeys(
				GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> encryptedHashedConfirmationKeys) {
			this.encryptedHashedConfirmationKeys = encryptedHashedConfirmationKeys;
			return this;
		}

		public GenVerDatOutput build() {
			return new GenVerDatOutput(verificationCardIds, verificationCardKeyPairs, partialChoiceReturnCodesAllowList, ballotCastingKeys,
					encryptedHashedPartialChoiceReturnCodes, encryptedHashedConfirmationKeys);
		}
	}

}
