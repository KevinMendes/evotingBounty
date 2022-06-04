/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

package ch.post.it.evoting.securedatamanager.configuration.setupvoting;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;

/**
 * Regroups the output values returned by the CombineEncLongCodeShares algorithm.
 *
 * <ul>
 *     <li>c<sub>pC</sub>, The vector of encrypted pre-Choice Return Codes.</li>
 *     <li>p<sub>VCC</sub>, The vector of pre-Vote Cast Return Codes.</li>
 *     <li>L<sub>lVCC</sub>, The long Vote Cast Return Codes allow list.</li>
 * </ul>
 */
public class CombineEncLongCodeSharesOutput {

	private final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> encryptedPreChoiceReturnCodesVector;
	private final GroupVector<GqElement, GqGroup> preVoteCastReturnCodesVector;
	private final List<String> longVoteCastReturnCodesAllowList;

	public CombineEncLongCodeSharesOutput(final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> encryptedPreChoiceReturnCodesVector,
			final GroupVector<GqElement, GqGroup> preVoteCastReturnCodesVector,
			final List<String> longVoteCastReturnCodesAllowList) {

		this.encryptedPreChoiceReturnCodesVector = encryptedPreChoiceReturnCodesVector;
		this.preVoteCastReturnCodesVector = preVoteCastReturnCodesVector;
		this.longVoteCastReturnCodesAllowList = longVoteCastReturnCodesAllowList;
	}

	public GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> getEncryptedPreChoiceReturnCodesVector() {
		return encryptedPreChoiceReturnCodesVector;
	}

	public List<GqElement> getPreVoteCastReturnCodesVector() {
		return preVoteCastReturnCodesVector;
	}

	public List<String> getLongVoteCastReturnCodesAllowList() {
		return longVoteCastReturnCodesAllowList;
	}

	public static class Builder {
		private GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> encryptedPreChoiceReturnCodesVector;
		private GroupVector<GqElement, GqGroup> preVoteCastReturnCodesVector;
		private List<String> longVoteCastReturnCodesAllowList;

		public Builder setEncryptedPreChoiceReturnCodesVector(
				final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> encryptedPreChoiceReturnCodesVector) {
			this.encryptedPreChoiceReturnCodesVector = encryptedPreChoiceReturnCodesVector;
			return this;
		}

		public Builder setPreVoteCastReturnCodesVector(final GroupVector<GqElement, GqGroup> preVoteCastReturnCodesVector) {
			this.preVoteCastReturnCodesVector = preVoteCastReturnCodesVector;
			return this;
		}

		public Builder setLongVoteCastReturnCodesAllowList(final List<String> longVoteCastReturnCodesAllowList) {
			this.longVoteCastReturnCodesAllowList = longVoteCastReturnCodesAllowList;
			return this;
		}

		/**
		 * Creates the CombineEncLongCodeSharesOutput. All fields must have been set and be non-null.
		 *
		 * @return a new CombineEncLongCodeSharesOutput.
		 * @throws NullPointerException     if any of the fields is null.
		 * @throws IllegalArgumentException if
		 *                                  <ul>
		 *                                      <li>All lists/vectors do not have the exactly same size.</li>
		 *                                      <li>The vector of exponentiated, encrypted, hashed partial Choice Return Codes and the
		 *                                      vector of exponentiated, encrypted, hashed Confirmation Keys do not have the same group order.</li>
		 *                                  </ul>
		 */
		public CombineEncLongCodeSharesOutput build() {

			checkNotNull(encryptedPreChoiceReturnCodesVector, "The vector of encrypted pre-Choice Return Codes must not be null.");
			checkNotNull(preVoteCastReturnCodesVector, "The vector of pre-Vote Cast Return Codes must not be null.");
			checkNotNull(longVoteCastReturnCodesAllowList, "The long Vote Cast Return Codes allow list must not be null.");

			// Size checks.

			checkArgument(!encryptedPreChoiceReturnCodesVector.isEmpty(),
					"The vector of encrypted pre-Choice Return Codes must have more than zero elements.");

			final int N_E = encryptedPreChoiceReturnCodesVector.size();

			checkArgument(preVoteCastReturnCodesVector.size() == N_E,
					"The vector of pre-Vote Cast Return Codes is of incorrect size [size: expected: %s, actual: %s].",
					N_E, preVoteCastReturnCodesVector.size());

			checkArgument(longVoteCastReturnCodesAllowList.size() == N_E,
					"The long Vote Cast Return Codes allow list is of incorrect size [size: expected: %s, actual: %s].",
					N_E, longVoteCastReturnCodesAllowList.size());

			// Cross group checks.
			checkArgument(preVoteCastReturnCodesVector.getGroup().equals(encryptedPreChoiceReturnCodesVector.getGroup()),
					"The vector of encrypted pre-Choice Return Codes and the vector of pre-Vote Cast Return Codes do not have the same group order.");

			return new CombineEncLongCodeSharesOutput(encryptedPreChoiceReturnCodesVector, preVoteCastReturnCodesVector,
					longVoteCastReturnCodesAllowList);
		}
	}
}
