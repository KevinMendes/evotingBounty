/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.voting.sendvote;

import static ch.post.it.evoting.cryptoprimitives.utils.Validations.allEqual;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableList;

import ch.post.it.evoting.controlcomponents.VerificationCardSetEntity;
import ch.post.it.evoting.controlcomponents.VerificationCardSetService;
import ch.post.it.evoting.controlcomponents.voting.ReturnCodesNodeContext;
import ch.post.it.evoting.cryptoprimitives.domain.VotingOptionsConstants;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ExponentiationProof;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ZeroKnowledgeProof;

/**
 * Implements the DecryptPCC_j algorithm.
 */
@Service
@SuppressWarnings("java:S1068")
public class DecryptPCCService {

	private final int nodeID;
	private final ZeroKnowledgeProof zeroKnowledgeProof;
	private final VerificationCardSetService verificationCardSetService;

	public DecryptPCCService(
			@Value("${nodeID}")
			final Integer nodeID,
			final ZeroKnowledgeProof zeroKnowledgeProof,
			final VerificationCardSetService verificationCardSetService) {
		this.nodeID = nodeID;
		this.zeroKnowledgeProof = zeroKnowledgeProof;
		this.verificationCardSetService = verificationCardSetService;
	}

	/**
	 * Decrypts the partial choice return codes.
	 *
	 * @param context the {@link ReturnCodesNodeContext} containing necessary ids and group. Non-null.
	 * @param input   the {@link DecryptPPCInput} containing all needed inputs. Non-null.
	 * @return the decrypted partial choice return codes
	 * @throws NullPointerException     if any of the parameters is null
	 * @throws IllegalArgumentException if
	 *                                  <ul>
	 *                                      <li>the context's encryption group is different from the input's group</li>
	 *                                      <li>the size of the input's encrypted partial choice return codes is different from psi</li>
	 *                                  </ul>
	 */
	@SuppressWarnings("java:S117")
	public GroupVector<GqElement, GqGroup> decryptPCC(final ReturnCodesNodeContext context, final DecryptPPCInput input) {
		checkNotNull(context);
		checkNotNull(input);

		checkArgument(context.getEncryptionGroup().equals(input.getGroup()), "The context and input must have the same group.");
		final int psi = getPsi(context.getVerificationCardSetId());
		checkArgument(input.getEncryptedPartialChoiceReturnCodes().size() == psi,
				String.format("There must be psi encrypted partial Choice Return Codes. [psi: %s]", psi));

		// Input variables.
		final GroupVector<GqElement, GqGroup> d_j = input.getExponentiatedGammaElements();
		final GroupVector<GroupVector<GqElement, GqGroup>, GqGroup> d_j_hat = input.getOtherCcrExponentiatedGammaElements();
		final ElGamalMultiRecipientCiphertext E2 = input.getEncryptedPartialChoiceReturnCodes();

		final GroupVector<GqElement, GqGroup> phi_2 = E2.getPhi();

		// Operations.

		// Currently, the control-component does not have the other ccr encryption keys so the proof verification can not be done yet.

		final GroupVector<GqElement, GqGroup> d = IntStream.range(0, psi)
				.mapToObj(i -> d_j.get(i).multiply(d_j_hat.get(0).get(i)).multiply(d_j_hat.get(1).get(i)).multiply(d_j_hat.get(2).get(i)))
				.collect(GroupVector.toGroupVector());

		return IntStream.range(0, psi)
				.mapToObj(i -> phi_2.get(i).multiply(d.get(i).invert()))
				.collect(GroupVector.toGroupVector());
	}

	private int getPsi(final String verificationCardSetId) {
		final VerificationCardSetEntity verificationCardSet = verificationCardSetService.getVerificationCardSet(verificationCardSetId);
		return verificationCardSet.getCombinedCorrectnessInformation().getTotalNumberOfSelections();
	}

	public static class DecryptPPCInput {
		private final String verificationCardId;
		private final GroupVector<GqElement, GqGroup> exponentiatedGammaElements;
		private final GroupVector<GroupVector<GqElement, GqGroup>, GqGroup> otherCcrExponentiatedGammaElements;
		private final GroupVector<GroupVector<ExponentiationProof, ZqGroup>, ZqGroup> otherCcrExponentiationProofs;
		private final GroupVector<ElGamalMultiRecipientPublicKey, GqGroup> otherCcrChoiceReturnCodesEncryptionKeys;
		private final ElGamalMultiRecipientCiphertext encryptedVote;
		private final ElGamalMultiRecipientCiphertext exponentiatedEncryptedVote;
		private final ElGamalMultiRecipientCiphertext encryptedPartialChoiceReturnCodes;

		private DecryptPPCInput(final String verificationCardId,
				final GroupVector<GqElement, GqGroup> exponentiatedGammaElements,
				final GroupVector<GroupVector<GqElement, GqGroup>, GqGroup> otherCcrExponentiatedGammaElements,
				final GroupVector<GroupVector<ExponentiationProof, ZqGroup>, ZqGroup> otherCcrExponentiationProofs,
				final GroupVector<ElGamalMultiRecipientPublicKey, GqGroup> otherCcrChoiceReturnCodesEncryptionKeys,
				final ElGamalMultiRecipientCiphertext encryptedVote, final ElGamalMultiRecipientCiphertext exponentiatedEncryptedVote,
				final ElGamalMultiRecipientCiphertext encryptedPartialChoiceReturnCodes) {
			this.verificationCardId = verificationCardId;
			this.exponentiatedGammaElements = exponentiatedGammaElements;
			this.otherCcrExponentiatedGammaElements = otherCcrExponentiatedGammaElements;
			this.otherCcrExponentiationProofs = otherCcrExponentiationProofs;
			this.otherCcrChoiceReturnCodesEncryptionKeys = otherCcrChoiceReturnCodesEncryptionKeys;
			this.encryptedVote = encryptedVote;
			this.exponentiatedEncryptedVote = exponentiatedEncryptedVote;
			this.encryptedPartialChoiceReturnCodes = encryptedPartialChoiceReturnCodes;
		}

		public String getVerificationCardId() {
			return verificationCardId;
		}

		public GroupVector<GqElement, GqGroup> getExponentiatedGammaElements() {
			return exponentiatedGammaElements;
		}

		public GroupVector<GroupVector<GqElement, GqGroup>, GqGroup> getOtherCcrExponentiatedGammaElements() {
			return otherCcrExponentiatedGammaElements;
		}

		public GroupVector<GroupVector<ExponentiationProof, ZqGroup>, ZqGroup> getOtherCcrExponentiationProofs() {
			return otherCcrExponentiationProofs;
		}

		public GroupVector<ElGamalMultiRecipientPublicKey, GqGroup> getOtherCcrChoiceReturnCodesEncryptionKeys() {
			return otherCcrChoiceReturnCodesEncryptionKeys;
		}

		public ElGamalMultiRecipientCiphertext getEncryptedVote() {
			return encryptedVote;
		}

		public ElGamalMultiRecipientCiphertext getExponentiatedEncryptedVote() {
			return exponentiatedEncryptedVote;
		}

		public ElGamalMultiRecipientCiphertext getEncryptedPartialChoiceReturnCodes() {
			return encryptedPartialChoiceReturnCodes;
		}

		public GqGroup getGroup() {
			return encryptedVote.getGroup();
		}

		public static class Builder {
			private String verificationCardId;
			private GroupVector<GqElement, GqGroup> exponentiatedGammaElements;
			private GroupVector<GroupVector<GqElement, GqGroup>, GqGroup> otherCcrExponentiatedGammaElements;
			private GroupVector<GroupVector<ExponentiationProof, ZqGroup>, ZqGroup> otherCcrExponentiationProofs;
			private GroupVector<ElGamalMultiRecipientPublicKey, GqGroup> otherCcrChoiceReturnCodesEncryptionKeys;
			private ElGamalMultiRecipientCiphertext encryptedVote;
			private ElGamalMultiRecipientCiphertext exponentiatedEncryptedVote;
			private ElGamalMultiRecipientCiphertext encryptedPartialChoiceReturnCodes;

			public Builder addVerificationCardId(final String verificationCardId) {
				this.verificationCardId = verificationCardId;
				return this;
			}

			public Builder addExponentiatedGammaElements(final GroupVector<GqElement, GqGroup> exponentiatedGammaElements) {
				this.exponentiatedGammaElements = exponentiatedGammaElements;
				return this;
			}

			public Builder addOtherCcrExponentiatedGammaElements(
					final GroupVector<GroupVector<GqElement, GqGroup>, GqGroup> otherCcrExponentiatedGammaElements) {
				this.otherCcrExponentiatedGammaElements = otherCcrExponentiatedGammaElements;
				return this;
			}

			public Builder addOtherCcrExponentiationProofs(
					final GroupVector<GroupVector<ExponentiationProof, ZqGroup>, ZqGroup> otherCcrExponentiationProofs) {
				this.otherCcrExponentiationProofs = otherCcrExponentiationProofs;
				return this;
			}

			public Builder addOtherCcrChoiceReturnCodesEncryptionKeys(
					final GroupVector<ElGamalMultiRecipientPublicKey, GqGroup> otherCcrChoiceReturnCodesEncryptionKeys) {
				this.otherCcrChoiceReturnCodesEncryptionKeys = otherCcrChoiceReturnCodesEncryptionKeys;
				return this;
			}

			public Builder addEncryptedVote(final ElGamalMultiRecipientCiphertext encryptedVote) {
				this.encryptedVote = encryptedVote;
				return this;
			}

			public Builder addExponentiatedEncryptedVote(final ElGamalMultiRecipientCiphertext exponentiatedEncryptedVote) {
				this.exponentiatedEncryptedVote = exponentiatedEncryptedVote;
				return this;
			}

			public Builder addEncryptedPartialChoiceReturnCodes(final ElGamalMultiRecipientCiphertext encryptedPartialChoiceReturnCodes) {
				this.encryptedPartialChoiceReturnCodes = encryptedPartialChoiceReturnCodes;
				return this;
			}

			public DecryptPPCInput build() {
				checkNotNull(this.verificationCardId);
				checkNotNull(this.exponentiatedGammaElements);
				checkNotNull(this.otherCcrExponentiatedGammaElements);
				checkNotNull(this.otherCcrExponentiationProofs);
				checkNotNull(this.otherCcrChoiceReturnCodesEncryptionKeys);
				checkNotNull(this.encryptedVote);
				checkNotNull(this.exponentiatedEncryptedVote);
				checkNotNull(this.encryptedPartialChoiceReturnCodes);

				// Check sizes
				final ImmutableList<Integer> sizes = ImmutableList.of(exponentiatedGammaElements.size(),
						otherCcrExponentiatedGammaElements.get(0).size(), otherCcrExponentiationProofs.get(0).size());
				checkArgument(allEqual(sizes.stream(), Function.identity()),
						"The exponentiated gamma elements, the other CCR's exponentiated gamma elements and the other CCR's exponentiation proofs must have the same size");

				checkArgument(otherCcrExponentiatedGammaElements.size() == 3,
						"There must be exactly 3 vectors of other CCR's exponentiated gamma elements");
				checkArgument(otherCcrExponentiatedGammaElements.allEqual(GroupVector::size),
						"All other CCR's exponentiated gamma elements must have the same size");

				checkArgument(otherCcrExponentiationProofs.size() == 3, "There must be exactly 3 vectors of other CCR's exponentiation proofs");
				checkArgument(otherCcrExponentiationProofs.allEqual(GroupVector::size),
						"All other CCR's exponentiation proof vectors must have the same size");

				final int phi = VotingOptionsConstants.MAXIMUM_NUMBER_OF_SELECTABLE_VOTING_OPTIONS;
				checkArgument(otherCcrChoiceReturnCodesEncryptionKeys.size() == 3,
						"There must be exactly 3 vectors of other CCR's Choice Return Codes encryption keys");
				checkArgument(otherCcrChoiceReturnCodesEncryptionKeys.allEqual(ElGamalMultiRecipientPublicKey::size),
						"All other CCR's Choice Return Codes encryption keys must have the same size");
				checkArgument(otherCcrChoiceReturnCodesEncryptionKeys.get(0).size() == phi,
						String.format("The other CCR's Choice Return Codes encryption keys must be of size phi. [phi: %s]", phi));

				checkArgument(encryptedVote.size() == 1, "The encrypted vote must have exactly one phi element");
				checkArgument(exponentiatedEncryptedVote.size() == 1, "The exponentiated encrypted votes must have exactly one phi element.");

				// Cross-group checks
				final List<GqGroup> gqGroups = Arrays.asList(
						exponentiatedGammaElements.getGroup(), otherCcrExponentiatedGammaElements.getGroup(),
						otherCcrChoiceReturnCodesEncryptionKeys.getGroup(), encryptedVote.getGroup(), exponentiatedEncryptedVote.getGroup(),
						encryptedPartialChoiceReturnCodes.getGroup());
				checkArgument(allEqual(gqGroups.stream(), Function.identity()), "All input Gq groups must be the same.");
				checkArgument(otherCcrExponentiationProofs.getGroup().hasSameOrderAs(encryptedVote.getGroup()),
						"The other CCR's exponentiation proofs' group must have the same order as the encrypted vote's group.");

				return new DecryptPPCInput(this.verificationCardId, this.exponentiatedGammaElements,
						this.otherCcrExponentiatedGammaElements, this.otherCcrExponentiationProofs, this.otherCcrChoiceReturnCodesEncryptionKeys,
						this.encryptedVote, this.exponentiatedEncryptedVote, this.encryptedPartialChoiceReturnCodes);
			}
		}
	}
}
