/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

package ch.post.it.evoting.securedatamanager.configuration.setupvoting;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.List;

import com.google.common.collect.ImmutableList;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.exceptions.FailedValidationException;
import ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupMatrix;

/**
 * Regroups the inputs needed by the CombineEncLongCodeShares algorithm.
 *
 * <ul>
 *    <li>C<sub>expPCC</sub>, The Matrix of exponentiated, encrypted, hashed partial Choice Return Codes.</li>
 *    <li>C<sub>expCK</sub>, The Matrix of exponentiated, encrypted, hashed Confirmation Keys.</li>
 *    <li>vc, The List of Verification card ids.</li>
 * </ul>
 **/
public class CombineEncLongCodeSharesInput {

	private final GroupMatrix<ElGamalMultiRecipientCiphertext, GqGroup> exponentiatedEncryptedHashedPartialChoiceReturnCodesMatrix;
	private final GroupMatrix<ElGamalMultiRecipientCiphertext, GqGroup> exponentiatedEncryptedHashedConfirmationKeysMatrix;
	private final List<String> verificationCardIds;

	private CombineEncLongCodeSharesInput(
			final GroupMatrix<ElGamalMultiRecipientCiphertext, GqGroup> exponentiatedEncryptedHashedPartialChoiceReturnCodesMatrix,
			final GroupMatrix<ElGamalMultiRecipientCiphertext, GqGroup> exponentiatedEncryptedHashedConfirmationKeysMatrix,
			final List<String> verificationCardIds) {
		this.exponentiatedEncryptedHashedPartialChoiceReturnCodesMatrix = exponentiatedEncryptedHashedPartialChoiceReturnCodesMatrix;
		this.exponentiatedEncryptedHashedConfirmationKeysMatrix = exponentiatedEncryptedHashedConfirmationKeysMatrix;
		this.verificationCardIds = verificationCardIds;
	}

	public GroupMatrix<ElGamalMultiRecipientCiphertext, GqGroup> getExponentiatedEncryptedHashedPartialChoiceReturnCodesMatrix() {
		return this.exponentiatedEncryptedHashedPartialChoiceReturnCodesMatrix;
	}

	public GroupMatrix<ElGamalMultiRecipientCiphertext, GqGroup> getExponentiatedEncryptedHashedConfirmationKeysMatrix() {
		return this.exponentiatedEncryptedHashedConfirmationKeysMatrix;
	}

	public List<String> getVerificationCardIds() {
		return this.verificationCardIds;
	}

	public static class Builder {
		private GroupMatrix<ElGamalMultiRecipientCiphertext, GqGroup> exponentiatedEncryptedHashedPartialChoiceReturnCodesMatrix;
		private GroupMatrix<ElGamalMultiRecipientCiphertext, GqGroup> exponentiatedEncryptedHashedConfirmationKeysMatrix;
		private List<String> verificationCardIds;

		public Builder setExponentiatedEncryptedChoiceReturnCodesMatrix(
				final GroupMatrix<ElGamalMultiRecipientCiphertext, GqGroup> exponentiatedEncryptedChoiceReturnCodesMatrix) {
			this.exponentiatedEncryptedHashedPartialChoiceReturnCodesMatrix = exponentiatedEncryptedChoiceReturnCodesMatrix;
			return this;
		}

		public Builder setExponentiatedEncryptedConfirmationKeysMatrix(
				final GroupMatrix<ElGamalMultiRecipientCiphertext, GqGroup> exponentiatedEncryptedConfirmationKeysMatrix) {
			this.exponentiatedEncryptedHashedConfirmationKeysMatrix = exponentiatedEncryptedConfirmationKeysMatrix;
			return this;
		}

		public Builder setVerificationCardIds(final List<String> verificationCardIds) {
			this.verificationCardIds = verificationCardIds != null ? ImmutableList.copyOf(verificationCardIds) : null;
			return this;
		}

		/**
		 * Creates the CombineEncLongCodeSharesInput. All fields must have been set and be non-null.
		 *
		 * @throws NullPointerException      if any of the fields is null.
		 * @throws IllegalArgumentException  if
		 *                                   <ul>
		 *                                     <li>The Matrix of exponentiated, encrypted, hashed partial Choice Return Codes does not have exactly four elements.</li>
		 *                                     <li>The Matrix of exponentiated, encrypted, hashed Confirmation Keys does not have exactly four elements.</li>
		 *                                     <li>The Matrix of exponentiated, encrypted, hashed partial Choice Return Codes does not have any rows.</li>
		 *                                     <li>The Matrix of exponentiated, encrypted, hashed Confirmation Keys does not have any rows.</li>
		 *                                     <li>The exponentiated, encrypted, hashed partial Choice Return Codes Matrix and exponentiated, encrypted, hashed Confirmation Keys Matrix
		 *                                     do not have the same number of rows.</li>
		 *                                     <li>The Vector of verification card ids does not have the same size as the number of rows of the matrix's</li>
		 *                                     <li>All inputs do not have the same Gq group.</li>
		 *                                   </ul>
		 * @throws FailedValidationException if any of the Vector of verification card ids does not comply with the required UUID format.
		 */
		public CombineEncLongCodeSharesInput build() {

			final int NUM_COLUMNS = 4;
			final int EXPONENTIATED_ENCRYPTED_CONFIRMATION_KEYS_CIPHERTEXT_SIZE = 1;

			checkNotNull(exponentiatedEncryptedHashedPartialChoiceReturnCodesMatrix,
					"The Matrix of exponentiated, encrypted, hashed partial Choice Return Codes must not be null.");
			checkNotNull(exponentiatedEncryptedHashedConfirmationKeysMatrix,
					"The Matrix of exponentiated, encrypted, hashed Confirmation Keys must not be null.");
			checkNotNull(verificationCardIds,
					"The Vector of verification card ids must not be null.");

			verificationCardIds.forEach(UUIDValidations::validateUUID);

			// Size checks.
			checkArgument(exponentiatedEncryptedHashedPartialChoiceReturnCodesMatrix.numColumns() == NUM_COLUMNS,
					"The Matrix of exponentiated, encrypted, hashed partial Choice Return Codes must have exactly %s columns. [cols: %s].",
					NUM_COLUMNS, exponentiatedEncryptedHashedPartialChoiceReturnCodesMatrix.numColumns());
			checkArgument(exponentiatedEncryptedHashedConfirmationKeysMatrix.numColumns() == NUM_COLUMNS,
					"The Matrix of exponentiated, encrypted, hashed Confirmation Keys must have exactly %s columns. [cols: %s].",
					NUM_COLUMNS, exponentiatedEncryptedHashedConfirmationKeysMatrix.numColumns());

			checkArgument(exponentiatedEncryptedHashedPartialChoiceReturnCodesMatrix.numRows() > 0,
					"The Matrix of exponentiated, encrypted, hashed partial Choice Return Codes must have more than zero rows.");
			checkArgument(exponentiatedEncryptedHashedConfirmationKeysMatrix.numRows() > 0,
					"The Matrix of exponentiated, encrypted, hashed Confirmation Keys must have more than zero rows.");
			checkArgument(!verificationCardIds.isEmpty(),
					"The Vector of verification card Ids must have more than zero elements.");

			checkArgument(
					exponentiatedEncryptedHashedPartialChoiceReturnCodesMatrix.numRows()
							== exponentiatedEncryptedHashedConfirmationKeysMatrix.numRows(),
					"The Matrix of exponentiated, encrypted, hashed partial Choice Return Codes and Matrix of exponentiated, encrypted, hashed "
							+ "Confirmation Keys must have the same number of rows. [rows: 1): %s, 2): %s]",
					exponentiatedEncryptedHashedPartialChoiceReturnCodesMatrix.numRows(),
					exponentiatedEncryptedHashedConfirmationKeysMatrix.numRows());
			checkArgument(
					exponentiatedEncryptedHashedPartialChoiceReturnCodesMatrix.numRows() == verificationCardIds.size(),
					"The Matrix of exponentiated, encrypted, hashed partial Choice Return Codes and Vector of Verification Card Ids must have the "
							+ "same number of rows. [rows: 1): %s, 2): %s]",
					exponentiatedEncryptedHashedPartialChoiceReturnCodesMatrix.numRows(),
					verificationCardIds.size());

			final int ciphertextSize = exponentiatedEncryptedHashedPartialChoiceReturnCodesMatrix.get(0, 0).size();

			checkArgument(exponentiatedEncryptedHashedPartialChoiceReturnCodesMatrix.rowStream()
							.noneMatch(cols -> cols.stream().anyMatch(c -> c.getPhi().size() != ciphertextSize)),
					"The size of each of the ciphertexts in the Matrix of exponentiated, encrypted, hashed partial Choice Return Codes must be the same.");

			checkArgument(exponentiatedEncryptedHashedConfirmationKeysMatrix.rowStream()
							.noneMatch(cols -> cols.stream().anyMatch(c -> c.getPhi().size() != EXPONENTIATED_ENCRYPTED_CONFIRMATION_KEYS_CIPHERTEXT_SIZE)),
					"The size of each of the ciphertexts in the Matrix of exponentiated, encrypted, hashed Confirmation Keys must be %s.",
					EXPONENTIATED_ENCRYPTED_CONFIRMATION_KEYS_CIPHERTEXT_SIZE);

			// Cross group checks.
			checkArgument(exponentiatedEncryptedHashedPartialChoiceReturnCodesMatrix.getGroup()
					.equals(exponentiatedEncryptedHashedConfirmationKeysMatrix.getGroup()), "All input must have the same Gq group.");

			return new CombineEncLongCodeSharesInput(exponentiatedEncryptedHashedPartialChoiceReturnCodesMatrix,
					exponentiatedEncryptedHashedConfirmationKeysMatrix,
					verificationCardIds);
		}
	}
}
