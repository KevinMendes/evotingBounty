/*
 * (c) Copyright 2022. Swiss Post Ltd
 */
package ch.post.it.evoting.securedatamanager.configuration.setupvoting;

import static ch.post.it.evoting.cryptoprimitives.utils.Validations.allEqual;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.exceptions.FailedValidationException;
import ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;

/**
 * Regroups the inputs of the GenCMTable algorithm.
 */
public class GenCMTableInput {
	private final ImmutableList<String> verificationCardIds;
	private final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> encryptedPreChoiceReturnCodes;
	private final GroupVector<GqElement, GqGroup> preVoteCastReturnCodes;

	/**
	 * @throws NullPointerException      if any of the fields is null or {@code verificationCardIds} contains any null value.
	 * @throws IllegalArgumentException  if
	 *                                   <ul>
	 *                                       <li>Any of the {@code verificationCardIds}, {@code encryptedPreChoiceReturnCodes} and {@code encryptedPreVoteCastReturnCodes} is empty.</li>
	 *                                       <li>The sizes of {@code verificationCardIds}, {@code encryptedPreChoiceReturnCodes} and {@code encryptedPreVoteCastReturnCodes} are not equal.</li>
	 *                                       <li>The GqGroup of {@code encryptedPreChoiceReturnCodes} and {@code encryptedPreVoteCastReturnCodes} are not equal.</li>
	 *                                   </ul>
	 * @throws FailedValidationException if the {@code verificationCardIds} values do not comply with the UUID format.
	 */
	private GenCMTableInput(final List<String> verificationCardIds,
			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> encryptedPreChoiceReturnCodes,
			final GroupVector<GqElement, GqGroup> preVoteCastReturnCodes) {

		checkNotNull(verificationCardIds);
		checkNotNull(encryptedPreChoiceReturnCodes);
		checkNotNull(preVoteCastReturnCodes);

		final ImmutableList<String> verificationCardIdsCopy = ImmutableList.copyOf(verificationCardIds);
		verificationCardIdsCopy.forEach(UUIDValidations::validateUUID);

		// Input size checks.
		final List<Integer> inputsSize = Arrays.asList(verificationCardIdsCopy.size(), encryptedPreChoiceReturnCodes.size(),
				preVoteCastReturnCodes.size());
		checkArgument(inputsSize.stream().allMatch(size -> size > 0), "All inputs must not be empty.");
		checkArgument(allEqual(inputsSize.stream(), Function.identity()), "All inputs sizes must be the same.");

		// Cross group checks.
		checkArgument(encryptedPreChoiceReturnCodes.getGroup().equals(preVoteCastReturnCodes.getGroup()),
				"All inputs must have the same Gq group.");

		// Ensure that all verificationCardIds are unique
		checkArgument(verificationCardIds.stream().distinct().count() == verificationCardIds.size(),
				"All verificationCardIds must be unique.");

		this.verificationCardIds = verificationCardIdsCopy;
		this.encryptedPreChoiceReturnCodes = encryptedPreChoiceReturnCodes;
		this.preVoteCastReturnCodes = preVoteCastReturnCodes;
	}

	public List<String> getVerificationCardIds() {
		return verificationCardIds;
	}

	public GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> getEncryptedPreChoiceReturnCodes() {
		return encryptedPreChoiceReturnCodes;
	}

	public GroupVector<GqElement, GqGroup> getPreVoteCastReturnCodes() {
		return preVoteCastReturnCodes;
	}

	public GqGroup getGroup() {
		return this.encryptedPreChoiceReturnCodes.getGroup();
	}

	public static class Builder {
		private List<String> verificationCardIds;
		private GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> encryptedPreChoiceReturnCodes;
		private GroupVector<GqElement, GqGroup> preVoteCastReturnCodes;

		public Builder setVerificationCardIds(final List<String> verificationCardIds) {
			this.verificationCardIds = verificationCardIds;
			return this;
		}

		public Builder setEncryptedPreChoiceReturnCodes(
				final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> encryptedPreChoiceReturnCodes) {
			this.encryptedPreChoiceReturnCodes = encryptedPreChoiceReturnCodes;
			return this;
		}

		public Builder setPreVoteCastReturnCodes(
				final GroupVector<GqElement, GqGroup> encryptedPreVoteCastReturnCodes) {
			this.preVoteCastReturnCodes = encryptedPreVoteCastReturnCodes;
			return this;
		}

		public GenCMTableInput build() {
			return new GenCMTableInput(verificationCardIds, encryptedPreChoiceReturnCodes, preVoteCastReturnCodes);
		}
	}
}
