/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

package ch.post.it.evoting.controlcomponents.configuration.setupvoting;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.HashSet;
import java.util.List;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.exceptions.FailedValidationException;
import ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;

/**
 * Regroups the input values needed by the GenEncLongCodeShares algorithm.
 *
 * <ul>
 * <li>k<sup>'</sup><sub>j</sub> the CCR<sub>j</sub> Return Codes Generation secret key. Not null.</li>
 * <li>vc a vector of verification card IDs. Not null.</li>
 * <li>c<sub>pCC</sub> a vector of encrypted, hashed partial Choice Return Codes. Not null.</li>
 * <li>c<sub>ck</sub> a vector of encrypted, hashed Confirmation Keys. Not null.</li>
 * </ul>
 */
public class GenEncLongCodeSharesInput {
	private final ZqElement returnCodesGenerationSecretKey;
	private final List<String> verificationCardIDs;
	private final List<ElGamalMultiRecipientPublicKey> verificationCardPublicKeys;
	private final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> encryptedHashedPartialChoiceReturnCodes;
	private final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> encryptedHashedConfirmationKeys;

	private GenEncLongCodeSharesInput(
			final ZqElement returnCodesGenerationSecretKey,
			final List<String> verificationCardIDs,
			final List<ElGamalMultiRecipientPublicKey> verificationCardPublicKeys,
			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> encryptedHashedPartialChoiceReturnCodes,
			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> encryptedHashedConfirmationKeys) {
		this.returnCodesGenerationSecretKey = returnCodesGenerationSecretKey;
		this.verificationCardIDs = verificationCardIDs;
		this.verificationCardPublicKeys = verificationCardPublicKeys;
		this.encryptedHashedPartialChoiceReturnCodes = encryptedHashedPartialChoiceReturnCodes;
		this.encryptedHashedConfirmationKeys = encryptedHashedConfirmationKeys;
	}

	ZqElement getReturnCodesGenerationSecretKey() {
		return returnCodesGenerationSecretKey;
	}

	public List<String> getVerificationCardIDs() {
		return verificationCardIDs;
	}

	List<ElGamalMultiRecipientPublicKey> getVerificationCardPublicKeys() {
		return verificationCardPublicKeys;
	}

	List<ElGamalMultiRecipientCiphertext> getEncryptedHashedPartialChoiceReturnCodes() {
		return encryptedHashedPartialChoiceReturnCodes;
	}

	List<ElGamalMultiRecipientCiphertext> getEncryptedHashedConfirmationKeys() {
		return encryptedHashedConfirmationKeys;
	}

	/**
	 * Builder performing input validations and cross-validations before constructing a {@link GenEncLongCodeSharesInput}.
	 */
	public static class Builder {

		private ZqElement returnCodesGenerationSecretKey;
		private List<String> verificationCardIDs;
		private List<ElGamalMultiRecipientPublicKey> verificationCardPublicKeys;
		private GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> encryptedHashedPartialChoiceReturnCodes;
		private GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> encryptedHashedConfirmationKeys;

		public Builder returnCodesGenerationSecretKey(final ZqElement returnCodesGenerationSecretKey) {
			this.returnCodesGenerationSecretKey = returnCodesGenerationSecretKey;
			return this;
		}

		public Builder verificationCardIDs(final List<String> verificationCardIDs) {
			this.verificationCardIDs = verificationCardIDs;
			return this;
		}

		public Builder verificationCardPublicKeys(final List<ElGamalMultiRecipientPublicKey> verificationCardPublicKeys) {
			this.verificationCardPublicKeys = verificationCardPublicKeys;
			return this;
		}

		public Builder encryptedHashedPartialChoiceReturnCodes(
				final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> encryptedHashedPartialChoiceReturnCodes) {
			this.encryptedHashedPartialChoiceReturnCodes = encryptedHashedPartialChoiceReturnCodes;
			return this;
		}

		public Builder encryptedHashedConfirmationKeys(
				final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> encryptedHashedConfirmationKeys) {
			this.encryptedHashedConfirmationKeys = encryptedHashedConfirmationKeys;
			return this;
		}

		/**
		 * Creates the GenEncLongCodeSharesInput. All fields must have been set and be non-null.
		 *
		 * @return a new GenEncLongCodeSharesInput.
		 * @throws NullPointerException      if any of the fields is null.
		 * @throws FailedValidationException if any of the verification card IDs do not comply with the required UUID format
		 * @throws IllegalArgumentException  if
		 *                                   <ul>
		 *                                       <li>All list/vectors do not have the exactly same size.</li>
		 *                                       <li>The partial Choice Return Codes and Confirmation Keys do not have the same group order.</li>
		 *                                       <li>The verification card IDs contains duplicated values.</li>
		 *                                   </ul>
		 */
		public GenEncLongCodeSharesInput build() {

			checkNotNull(returnCodesGenerationSecretKey, "The CCRj Return Codes Generation Secret Key is null.");
			checkNotNull(verificationCardIDs, "The vector verification Card IDs is null.");
			checkNotNull(verificationCardPublicKeys, "The vector verification Card Public Keys is null.");
			checkNotNull(encryptedHashedPartialChoiceReturnCodes, "The vector encrypted, hashed partial Choice Return Codes is null.");
			checkNotNull(encryptedHashedConfirmationKeys, "The vector encrypted, hashed Confirmation Keys is null.");

			verificationCardIDs.forEach(UUIDValidations::validateUUID);

			// Size checks.
			final int N_E = verificationCardIDs.size();
			checkArgument(verificationCardPublicKeys.size() == N_E,
					"The vector verification Card Public Keys is of incorrect size [size: expected: %s, actual: %s]",
					N_E, verificationCardPublicKeys.size());
			checkArgument(encryptedHashedPartialChoiceReturnCodes.size() == N_E,
					"The vector encrypted, hashed partial Choice Return Codes is of incorrect size [size: expected: %s, actual: %s]",
					N_E, encryptedHashedPartialChoiceReturnCodes.size());
			checkArgument(encryptedHashedConfirmationKeys.size() == N_E,
					"The vector encrypted, hashed Confirmation Keys is of incorrect size [size: expected: %s, actual: %s]",
					N_E, encryptedHashedConfirmationKeys.size());

			// Cross group checks.
			checkArgument(encryptedHashedPartialChoiceReturnCodes.getGroup().hasSameOrderAs(encryptedHashedConfirmationKeys.getGroup()),
					"The Vector of exponentiated, encrypted, hashed partial Choice Return Codes and the Vector of "
							+ "exponentiated, encrypted, hashed Confirmation Keys do not have the same group order.");

			// The verificationCardIds must be unique
			checkArgument(new HashSet<>(verificationCardIDs).size() == verificationCardIDs.size(),
					"The Vector of verification card IDs contains duplicated values.");

			return new GenEncLongCodeSharesInput(returnCodesGenerationSecretKey, verificationCardIDs, verificationCardPublicKeys,
					encryptedHashedPartialChoiceReturnCodes, encryptedHashedConfirmationKeys);
		}
	}
}
