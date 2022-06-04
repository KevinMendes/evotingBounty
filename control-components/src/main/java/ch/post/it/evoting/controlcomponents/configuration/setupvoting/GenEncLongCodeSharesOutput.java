/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

package ch.post.it.evoting.controlcomponents.configuration.setupvoting;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import com.google.common.collect.ImmutableList;

import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ExponentiationProof;

/**
 * Regroups the output values returned by the GenEncLongCodeShares algorithm.
 *
 * <ul>
 * <li>K<sub>j</sub> the vector of Voter Choice Return Code Generation public keys.</li>
 * <li>Kc<sub>j</sub> the vector of Voter Vote Cast Return Code Generation public keys.</li>
 * <li>C<sub>expPCC,j</sub> the vector of exponentiated, encrypted, hashed partial Choice Return Codes key.</li>
 * <li>&pi;<sub>expPCC,j</sub> the proofs of correct exponentiation of the partial Choice Return Codes.</li>
 * <li>C<sub>expCK,j</sub> the vector of exponentiated, encrypted, hashed Confirmation Keys.</li>
 * <li>&pi;<sub>expCK,j</sub> the proofs of  correct exponentiation of the Confirmation Keys.</li>
 * </ul>
 */
public class GenEncLongCodeSharesOutput {
	private final List<GqElement> voterChoiceReturnCodeGenerationPublicKeys;
	private final List<GqElement> voterVoteCastReturnCodeGenerationPublicKeys;
	private final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> exponentiatedEncryptedHashedPartialChoiceReturnCodes;
	private final List<ExponentiationProof> proofsCorrectExponentiationPartialChoiceReturnCodes;
	private final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> exponentiatedEncryptedHashedConfirmationKeys;
	private final List<ExponentiationProof> proofsCorrectExponentiationConfirmationKeys;

	private GenEncLongCodeSharesOutput(
			final List<GqElement> voterChoiceReturnCodeGenerationPublicKeys,
			final List<GqElement> voterVoteCastReturnCodeGenerationPublicKeys,
			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> exponentiatedEncryptedHashedPartialChoiceReturnCodes,
			final List<ExponentiationProof> proofsCorrectExponentiationPartialChoiceReturnCodes,
			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> exponentiatedEncryptedHashedConfirmationKeys,
			final List<ExponentiationProof> proofsCorrectExponentiationConfirmationKeys) {

		this.voterChoiceReturnCodeGenerationPublicKeys = ImmutableList.copyOf(voterChoiceReturnCodeGenerationPublicKeys);
		this.voterVoteCastReturnCodeGenerationPublicKeys = ImmutableList.copyOf(voterVoteCastReturnCodeGenerationPublicKeys);
		this.exponentiatedEncryptedHashedPartialChoiceReturnCodes = GroupVector.from(exponentiatedEncryptedHashedPartialChoiceReturnCodes);
		this.proofsCorrectExponentiationPartialChoiceReturnCodes = ImmutableList.copyOf(proofsCorrectExponentiationPartialChoiceReturnCodes);
		this.exponentiatedEncryptedHashedConfirmationKeys = GroupVector.from(exponentiatedEncryptedHashedConfirmationKeys);
		this.proofsCorrectExponentiationConfirmationKeys = ImmutableList.copyOf(proofsCorrectExponentiationConfirmationKeys);
	}

	public final List<GqElement> getVoterChoiceReturnCodeGenerationPublicKeys() {
		return voterChoiceReturnCodeGenerationPublicKeys;
	}

	public final List<GqElement> getVoterVoteCastReturnCodeGenerationPublicKeys() {
		return voterVoteCastReturnCodeGenerationPublicKeys;
	}

	public final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> getExponentiatedEncryptedHashedPartialChoiceReturnCodes() {
		return exponentiatedEncryptedHashedPartialChoiceReturnCodes;
	}

	public final List<ExponentiationProof> getProofsCorrectExponentiationPartialChoiceReturnCodes() {
		return proofsCorrectExponentiationPartialChoiceReturnCodes;
	}

	public final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> getExponentiatedEncryptedHashedConfirmationKeys() {
		return exponentiatedEncryptedHashedConfirmationKeys;
	}

	public final List<ExponentiationProof> getProofsCorrectExponentiationConfirmationKeys() {
		return proofsCorrectExponentiationConfirmationKeys;
	}

	/**
	 * Builder performing input validations and cross-validations before constructing a {@link GenEncLongCodeSharesOutput}.
	 */
	public static class Builder {
		private List<GqElement> voterChoiceReturnCodeGenerationPublicKeys;
		private List<GqElement> voterVoteCastReturnCodeGenerationPublicKeys;
		private GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> exponentiatedEncryptedHashedPartialChoiceReturnCodes;
		private List<ExponentiationProof> proofsCorrectExponentiationPartialChoiceReturnCodes;
		private GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> exponentiatedEncryptedHashedConfirmationKeys;
		private List<ExponentiationProof> proofsCorrectExponentiationConfirmationKeys;

		public Builder setVoterChoiceReturnCodeGenerationPublicKeys(final List<GqElement> voterChoiceReturnCodeGenerationPublicKeys) {
			this.voterChoiceReturnCodeGenerationPublicKeys = voterChoiceReturnCodeGenerationPublicKeys;
			return this;
		}

		public Builder setVoterVoteCastReturnCodeGenerationPublicKeys(final List<GqElement> voterVoteCastReturnCodeGenerationPublicKeys) {
			this.voterVoteCastReturnCodeGenerationPublicKeys = voterVoteCastReturnCodeGenerationPublicKeys;
			return this;
		}

		public Builder setExponentiatedEncryptedHashedPartialChoiceReturnCodes(
				final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> exponentiatedEncryptedHashedPartialChoiceReturnCodes) {
			this.exponentiatedEncryptedHashedPartialChoiceReturnCodes = exponentiatedEncryptedHashedPartialChoiceReturnCodes;
			return this;
		}

		public Builder setProofsCorrectExponentiationPartialChoiceReturnCodes(
				final List<ExponentiationProof> proofsCorrectExponentiationPartialChoiceReturnCodes) {
			this.proofsCorrectExponentiationPartialChoiceReturnCodes = proofsCorrectExponentiationPartialChoiceReturnCodes;
			return this;
		}

		public Builder setExponentiatedEncryptedHashedConfirmationKeys(
				final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> exponentiatedEncryptedHashedConfirmationKeys) {
			this.exponentiatedEncryptedHashedConfirmationKeys = exponentiatedEncryptedHashedConfirmationKeys;
			return this;
		}

		public Builder setProofsCorrectExponentiationConfirmationKeys(final List<ExponentiationProof> proofsCorrectExponentiationConfirmationKeys) {
			this.proofsCorrectExponentiationConfirmationKeys = proofsCorrectExponentiationConfirmationKeys;
			return this;
		}

		/**
		 * Creates the GenEncLongCodeSharesOutput. All fields must have been set and be non-null.
		 *
		 * @return a new GenEncLongCodeSharesOutput.
		 * @throws NullPointerException     if any of the fields is null.
		 * @throws IllegalArgumentException if
		 *                                  <ul>
		 *                                      <li>All list/vectors do not have the exactly same size.</li>
		 *                                      <li>The vector exponentiated, encrypted, hashed partial Choice Return Codes and the
		 *                                      vector of exponentiated, encrypted, hashed Confirmation Keys do not have the same group order.</li>
		 *                                  </ul>
		 */
		public GenEncLongCodeSharesOutput build() {

			checkNotNull(voterChoiceReturnCodeGenerationPublicKeys, "The Vector of Voter Choice Return Code Generation public keys is null.");
			checkNotNull(voterVoteCastReturnCodeGenerationPublicKeys, "The Vector of Voter Vote Cast Return Code Generation public keys is null.");
			checkNotNull(exponentiatedEncryptedHashedPartialChoiceReturnCodes,
					"The Vector of exponentiated, encrypted, hashed partial Choice Return Codes is null.");
			checkNotNull(proofsCorrectExponentiationPartialChoiceReturnCodes,
					"The Proofs of correct exponentiation of the partial Choice Return Codes is null.");
			checkNotNull(exponentiatedEncryptedHashedConfirmationKeys, "The Vector of exponentiated, encrypted, hashed Confirmation Keys is null.");
			checkNotNull(proofsCorrectExponentiationConfirmationKeys, "The Proofs of correct exponentiation of the Confirmation Keys is null.");

			// Size checks.

			checkArgument(!voterChoiceReturnCodeGenerationPublicKeys.isEmpty(),
					"The Vector of Voter Choice Return Code Generation public keys must have more than zero elements.");
			checkArgument(!voterVoteCastReturnCodeGenerationPublicKeys.isEmpty(),
					"The Vector of Voter Vote Cast Return Code Generation public keys must have more than zero elements.");
			checkArgument(!exponentiatedEncryptedHashedPartialChoiceReturnCodes.isEmpty(),
					"The Vector of exponentiated, encrypted, hashed partial Choice Return Codes must have more than zero elements.");
			checkArgument(!proofsCorrectExponentiationPartialChoiceReturnCodes.isEmpty(),
					"The Proofs of correct exponentiation of the partial Choice Return Codes must have more than zero elements.");
			checkArgument(!exponentiatedEncryptedHashedConfirmationKeys.isEmpty(),
					"The Vector of exponentiated, encrypted, hashed Confirmation Keys must have more than zero elements.");
			checkArgument(!proofsCorrectExponentiationConfirmationKeys.isEmpty(),
					"The Proofs of correct exponentiation of the Confirmation Keys must have more than zero elements.");

			final int N_E = voterChoiceReturnCodeGenerationPublicKeys.size();

			checkArgument(voterVoteCastReturnCodeGenerationPublicKeys.size() == N_E,
					"The Vector of Voter Vote Cast Return Code Generation public keys is of incorrect size [size: expected: %s, actual: %s].",
					N_E, voterVoteCastReturnCodeGenerationPublicKeys.size());
			checkArgument(exponentiatedEncryptedHashedPartialChoiceReturnCodes.size() == N_E,
					"The Vector of exponentiated, encrypted, hashed partial Choice Return Codes is of incorrect size [size: expected: %s, actual: %s].",
					N_E, exponentiatedEncryptedHashedPartialChoiceReturnCodes.size());
			checkArgument(proofsCorrectExponentiationPartialChoiceReturnCodes.size() == N_E,
					"The Proofs of correct exponentiation of the partial Choice Return Codes is of incorrect size [size: expected: %s, actual: %s].",
					N_E, proofsCorrectExponentiationPartialChoiceReturnCodes.size());
			checkArgument(exponentiatedEncryptedHashedConfirmationKeys.size() == N_E,
					"The Vector of exponentiated, encrypted, hashed Confirmation Keys is of incorrect size [size: expected: %s, actual: %s].",
					N_E, exponentiatedEncryptedHashedConfirmationKeys.size());
			checkArgument(proofsCorrectExponentiationConfirmationKeys.size() == N_E,
					"The Proofs of correct exponentiation of the Confirmation Keys is of incorrect size [size: expected: %s, actual: %s].",
					N_E, proofsCorrectExponentiationConfirmationKeys.size());

			// Cross group checks.
			checkArgument(exponentiatedEncryptedHashedPartialChoiceReturnCodes.getGroup()
							.hasSameOrderAs(exponentiatedEncryptedHashedConfirmationKeys.getGroup()),
					"The Vector of exponentiated, encrypted, hashed partial Choice Return Codes and the Vector of exponentiated, encrypted, "
							+ "hashed Confirmation Keys do not have the same group order.");
			return new GenEncLongCodeSharesOutput(voterChoiceReturnCodeGenerationPublicKeys, voterVoteCastReturnCodeGenerationPublicKeys,
					exponentiatedEncryptedHashedPartialChoiceReturnCodes, proofsCorrectExponentiationPartialChoiceReturnCodes,
					exponentiatedEncryptedHashedConfirmationKeys, proofsCorrectExponentiationConfirmationKeys);
		}
	}
}
