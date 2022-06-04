/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.voting.sendvote;

import static ch.post.it.evoting.cryptoprimitives.utils.ConversionService.integerToString;
import static ch.post.it.evoting.cryptoprimitives.utils.Validations.allEqual;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.google.common.collect.Streams;

import ch.post.it.evoting.controlcomponents.VerificationCardSetEntity;
import ch.post.it.evoting.controlcomponents.VerificationCardSetService;
import ch.post.it.evoting.controlcomponents.voting.ReturnCodesNodeContext;
import ch.post.it.evoting.cryptoprimitives.domain.VotingOptionsConstants;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqElement.GqElementFactory;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ExponentiationProof;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.PlaintextEqualityProof;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ZeroKnowledgeProof;

/**
 * Implements the VerifyBallotCCR_j algorithm.
 */
@Service
public class VerifyBallotCCRService {

	private final ZeroKnowledgeProof zeroKnowledgeProofService;
	private final VerificationCardSetService verificationCardSetService;

	public VerifyBallotCCRService(
			final ZeroKnowledgeProof zeroKnowledgeProofService,
			final VerificationCardSetService verificationCardSetService) {
		this.zeroKnowledgeProofService = checkNotNull(zeroKnowledgeProofService);
		this.verificationCardSetService = verificationCardSetService;
	}

	/**
	 * Checks the voting client's encrypted vote by verifying the zero-knowledge proofs.
	 *
	 * @param context the {@link ReturnCodesNodeContext} containing necessary ids and group.
	 * @param input   the {@link VerifyBallotCCRInput} containing all needed inputs. Non-null.
	 * @return {@code true} if the verification is successful, {@code false} otherwise.
	 * @throws NullPointerException     if any input parameter is null.
	 * @throws IllegalArgumentException if
	 *                                  <ul>
	 *                                      <li>The context and input do not have the same group.</li>
	 *                                      <li>There is no combined correctness information for the election event ID and verification card set ID.</li>
	 *                                      <li>There are not psi encrypted partial Choice Return Codes in the {@code input}.</li>
	 *                                      <li>Psi larger than phi.</li>
	 *                                  </ul>
	 */
	@SuppressWarnings("java:S117")
	public boolean verifyBallotCCR(final ReturnCodesNodeContext context, final VerifyBallotCCRInput input) {
		checkNotNull(context);
		checkNotNull(input);

		// Cross group check.
		checkArgument(context.getEncryptionGroup().equals(input.getGroup()), "The context and input must have the same group.");

		// Size check.
		final int psi = getPsi(context.getVerificationCardSetId());
		checkArgument(input.getEncryptedPartialChoiceReturnCodes().size() == psi,
				String.format("There must be psi encrypted partial Choice Return Codes. [psi: %s]", psi));

		// Variables.
		final String ee = context.getElectionEventId();
		final GqGroup gqGroup = context.getEncryptionGroup();
		final String vc_id = input.getVerificationCardId();
		final ElGamalMultiRecipientCiphertext E1 = input.getEncryptedVote();
		final ElGamalMultiRecipientCiphertext E1_tilde = input.getExponentiatedEncryptedVote();
		final ElGamalMultiRecipientCiphertext E2 = input.getEncryptedPartialChoiceReturnCodes();
		final ElGamalMultiRecipientPublicKey EL_pk = input.getElectionPublicKey();
		final ElGamalMultiRecipientPublicKey pk_CCR = input.getChoiceReturnCodesEncryptionPublicKey();
		final ExponentiationProof pi_Exp = input.getExponentiationProof();
		final PlaintextEqualityProof pi_EqEnc = input.getPlaintextEqualityProof();
		final GqElement K_id = input.getVerificationCardPublicKey();
		final GqElement gamma_1 = E1.getGamma();
		final GqElement Phi_1_0 = E1.get(0);
		final GqElement gamma_1_k_id = E1_tilde.getGamma();
		final GqElement Phi_1_0_k_id = E1_tilde.get(0);
		final GqElement gamma_2 = E2.getGamma();
		final int phi = pk_CCR.size();
		final GqElement g = gqGroup.getGenerator();

		// Ensure psi <= phi.
		checkArgument(psi <= phi, String.format("psi must be smaller or equal to phi. [psi: %s, phi: %s]", psi, phi));

		// Operation.
		final GqElement identity = GqElementFactory.fromValue(BigInteger.ONE, gqGroup);

		final GqElement Phi_2 = E2.getPhi().stream().reduce(identity, GqElement::multiply);
		final ElGamalMultiRecipientCiphertext E2_tilde = ElGamalMultiRecipientCiphertext.create(gamma_2, Collections.singletonList(Phi_2));
		final GqElement pk_CCR_tilde = pk_CCR.stream().sequential().limit(psi).reduce(identity, GqElement::multiply);

		final List<String> i_aux = Streams.concat(
				Stream.of(ee, vc_id),
				EL_pk.stream().map(element -> integerToString(element.getValue())),
				Stream.of("CreateVote")
		).collect(Collectors.toList());

		final GroupVector<GqElement, GqGroup> bases = GroupVector.of(g, gamma_1, Phi_1_0);
		final GroupVector<GqElement, GqGroup> exponentiations = GroupVector.of(K_id, gamma_1_k_id, Phi_1_0_k_id);
		final boolean verifExp = zeroKnowledgeProofService.verifyExponentiation(bases, exponentiations, pi_Exp, i_aux);

		final boolean verifEqEnc = zeroKnowledgeProofService.verifyPlaintextEquality(E1_tilde, E2_tilde, EL_pk.get(0), pk_CCR_tilde, pi_EqEnc, i_aux);

		return verifExp && verifEqEnc;
	}

	private int getPsi(final String verificationCardSetId) {
		final VerificationCardSetEntity verificationCardSet = verificationCardSetService.getVerificationCardSet(verificationCardSetId);
		return verificationCardSet.getCombinedCorrectnessInformation().getTotalNumberOfSelections();
	}

	/**
	 * Regroups the inputs needed by the VerifyBallotCCR_j algorithm.
	 */
	public static class VerifyBallotCCRInput {

		private final String verificationCardId;
		private final ElGamalMultiRecipientCiphertext encryptedVote;
		private final ElGamalMultiRecipientCiphertext exponentiatedEncryptedVote;
		private final ElGamalMultiRecipientCiphertext encryptedPartialChoiceReturnCodes;
		private final GqElement verificationCardPublicKey;
		private final ElGamalMultiRecipientPublicKey electionPublicKey;
		private final ElGamalMultiRecipientPublicKey choiceReturnCodesEncryptionPublicKey;
		private final ExponentiationProof exponentiationProof;
		private final PlaintextEqualityProof plaintextEqualityProof;

		private VerifyBallotCCRInput(final String verificationCardId,
				final ElGamalMultiRecipientCiphertext encryptedVote,
				final ElGamalMultiRecipientCiphertext exponentiatedEncryptedVote,
				final ElGamalMultiRecipientCiphertext encryptedPartialChoiceReturnCodes,
				final GqElement verificationCardPublicKey,
				final ElGamalMultiRecipientPublicKey electionPublicKey,
				final ElGamalMultiRecipientPublicKey choiceReturnCodesEncryptionPublicKey,
				final ExponentiationProof exponentiationProof,
				final PlaintextEqualityProof plaintextEqualityProof) {
			this.verificationCardId = verificationCardId;
			this.encryptedVote = encryptedVote;
			this.exponentiatedEncryptedVote = exponentiatedEncryptedVote;
			this.encryptedPartialChoiceReturnCodes = encryptedPartialChoiceReturnCodes;
			this.verificationCardPublicKey = verificationCardPublicKey;
			this.electionPublicKey = electionPublicKey;
			this.choiceReturnCodesEncryptionPublicKey = choiceReturnCodesEncryptionPublicKey;
			this.exponentiationProof = exponentiationProof;
			this.plaintextEqualityProof = plaintextEqualityProof;
		}

		public String getVerificationCardId() {
			return verificationCardId;
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

		public GqElement getVerificationCardPublicKey() {
			return verificationCardPublicKey;
		}

		public ElGamalMultiRecipientPublicKey getElectionPublicKey() {
			return electionPublicKey;
		}

		public ElGamalMultiRecipientPublicKey getChoiceReturnCodesEncryptionPublicKey() {
			return choiceReturnCodesEncryptionPublicKey;
		}

		public ExponentiationProof getExponentiationProof() {
			return exponentiationProof;
		}

		public PlaintextEqualityProof getPlaintextEqualityProof() {
			return plaintextEqualityProof;
		}

		public GqGroup getGroup() {
			return encryptedVote.getGroup();
		}
	}

	/**
	 * Builder performing input validations and cross-validations before constructing a {@link VerifyBallotCCRInput}.
	 */
	public static class VerifyBallotCCRInputBuilder {

		private String verificationCardId;
		private ElGamalMultiRecipientCiphertext encryptedVote;
		private ElGamalMultiRecipientCiphertext exponentiatedEncryptedVote;
		private ElGamalMultiRecipientCiphertext encryptedPartialChoiceReturnCodes;
		private GqElement verificationCardPublicKey;
		private ElGamalMultiRecipientPublicKey electionPublicKey;
		private ElGamalMultiRecipientPublicKey choiceReturnCodesEncryptionPublicKey;
		private ExponentiationProof exponentiationProof;
		private PlaintextEqualityProof plaintextEqualityProof;

		public VerifyBallotCCRInputBuilder setVerificationCardId(final String verificationCardId) {
			this.verificationCardId = verificationCardId;
			return this;
		}

		public VerifyBallotCCRInputBuilder setEncryptedVote(final ElGamalMultiRecipientCiphertext encryptedVote) {
			this.encryptedVote = encryptedVote;
			return this;
		}

		public VerifyBallotCCRInputBuilder setExponentiatedEncryptedVote(final ElGamalMultiRecipientCiphertext exponentiatedEncryptedVote) {
			this.exponentiatedEncryptedVote = exponentiatedEncryptedVote;
			return this;
		}

		public VerifyBallotCCRInputBuilder setEncryptedPartialChoiceReturnCodes(
				final ElGamalMultiRecipientCiphertext encryptedPartialChoiceReturnCodes) {
			this.encryptedPartialChoiceReturnCodes = encryptedPartialChoiceReturnCodes;
			return this;
		}

		public VerifyBallotCCRInputBuilder setVerificationCardPublicKey(final GqElement verificationCardPublicKey) {
			this.verificationCardPublicKey = verificationCardPublicKey;
			return this;
		}

		public VerifyBallotCCRInputBuilder setElectionPublicKey(final ElGamalMultiRecipientPublicKey electionPublicKey) {
			this.electionPublicKey = electionPublicKey;
			return this;
		}

		public VerifyBallotCCRInputBuilder setChoiceReturnCodesEncryptionPublicKey(
				final ElGamalMultiRecipientPublicKey choiceReturnCodesEncryptionPublicKey) {
			this.choiceReturnCodesEncryptionPublicKey = choiceReturnCodesEncryptionPublicKey;
			return this;
		}

		public VerifyBallotCCRInputBuilder setExponentiationProof(final ExponentiationProof exponentiationProof) {
			this.exponentiationProof = exponentiationProof;
			return this;
		}

		public VerifyBallotCCRInputBuilder setPlaintextEqualityProof(final PlaintextEqualityProof plaintextEqualityProof) {
			this.plaintextEqualityProof = plaintextEqualityProof;
			return this;
		}

		/**
		 * Creates the VerifyBallotCCRInput. All fields must have been set and be non-null.
		 *
		 * @return a new VerifyBallotCCRInput.
		 * @throws NullPointerException     if any of the fields is null.
		 * @throws IllegalArgumentException if
		 *                                  <ul>
		 *                                      <li>The encrypted vote does not have exactly one phi.</li>
		 *                                      <li>The exponentiated encrypted vote does not have exactly one phi.</li>
		 *                                      <li>The election public key is not of size delta.</li>
		 *                                      <li>The choice return codes encryption public key is not of size phi.</li>
		 *                                      <li>Not all inputs have the same Gq group.</li>
		 *                                      <li>The exponentiation proof does not have the same group order as the other inputs.</li>
		 *                                      <li>The plaintext equality proof does not have the same group order as the other inputs.</li>
		 *                                  </ul>
		 */
		public VerifyBallotCCRService.VerifyBallotCCRInput build() {
			checkNotNull(verificationCardId);
			checkNotNull(encryptedVote);
			checkNotNull(exponentiatedEncryptedVote);
			checkNotNull(encryptedPartialChoiceReturnCodes);
			checkNotNull(verificationCardPublicKey);
			checkNotNull(electionPublicKey);
			checkNotNull(choiceReturnCodesEncryptionPublicKey);
			checkNotNull(exponentiationProof);
			checkNotNull(plaintextEqualityProof);

			// Size checks.
			checkArgument(encryptedVote.size() == 1, "The encrypted vote must have exactly 1 phi.");
			checkArgument(exponentiatedEncryptedVote.size() == 1, "The exponentiated encrypted vote must have exactly 1 phi.");

			final int delta = 1; // Currently, we do not support write-ins.
			checkArgument(electionPublicKey.size() == delta, String.format("The election public key must be of size delta. [delta: %s]", delta));

			final int phi = VotingOptionsConstants.MAXIMUM_NUMBER_OF_SELECTABLE_VOTING_OPTIONS;
			checkArgument(choiceReturnCodesEncryptionPublicKey.size() == phi,
					String.format("The choice return codes encryption public key must be of size phi. [phi: %s]", phi));

			// Cross group checks.
			final List<GqGroup> gqGroups = Arrays.asList(encryptedVote.getGroup(), exponentiatedEncryptedVote.getGroup(),
					encryptedPartialChoiceReturnCodes.getGroup(), verificationCardPublicKey.getGroup(), electionPublicKey.getGroup(),
					choiceReturnCodesEncryptionPublicKey.getGroup());
			checkArgument(allEqual(gqGroups.stream(), Function.identity()), "All input Gq groups must be the same.");

			checkArgument(gqGroups.get(0).hasSameOrderAs(exponentiationProof.getGroup()),
					"The exponentiation proof must have the same group order than the other inputs.");
			checkArgument(gqGroups.get(0).hasSameOrderAs(plaintextEqualityProof.getGroup()),
					"The plaintext equality proof must have the same group order than the other inputs.");

			return new VerifyBallotCCRService.VerifyBallotCCRInput(verificationCardId, encryptedVote, exponentiatedEncryptedVote,
					encryptedPartialChoiceReturnCodes, verificationCardPublicKey, electionPublicKey, choiceReturnCodesEncryptionPublicKey,
					exponentiationProof, plaintextEqualityProof);
		}
	}

}
