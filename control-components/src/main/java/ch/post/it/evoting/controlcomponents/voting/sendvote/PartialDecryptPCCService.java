/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.voting.sendvote;

import static ch.post.it.evoting.cryptoprimitives.utils.ConversionService.integerToString;
import static ch.post.it.evoting.cryptoprimitives.utils.Validations.allEqual;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.google.common.collect.Streams;

import ch.post.it.evoting.controlcomponents.VerificationCardSetEntity;
import ch.post.it.evoting.controlcomponents.VerificationCardSetService;
import ch.post.it.evoting.controlcomponents.voting.ReturnCodesNodeContext;
import ch.post.it.evoting.controlcomponents.voting.VerificationCardStateService;
import ch.post.it.evoting.cryptoprimitives.domain.VotingOptionsConstants;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientKeyPair;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPrivateKey;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ExponentiationProof;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ZeroKnowledgeProof;

/**
 * Implements the PartialDecryptPCC_j algorithm.
 */
@Service
public class PartialDecryptPCCService {

	public static final String PARTIAL_DECRYPT_PCC = "PartialDecryptPCC";

	private final ZeroKnowledgeProof zeroKnowledgeProofService;
	private final VerificationCardSetService verificationCardSetService;
	private final VerificationCardStateService verificationCardStateService;

	public PartialDecryptPCCService(
			final ZeroKnowledgeProof zeroKnowledgeProofService,
			final VerificationCardSetService verificationCardSetService,
			final VerificationCardStateService verificationCardStateService) {
		this.zeroKnowledgeProofService = zeroKnowledgeProofService;
		this.verificationCardSetService = verificationCardSetService;
		this.verificationCardStateService = verificationCardStateService;
	}

	/**
	 * Strips the partial Choice Return Codes' encryption layer.
	 *
	 * @param context the {@link ReturnCodesNodeContext} containing necessary ids and group.
	 * @param input   the {@link PartialDecryptPCCInput} containing all needed inputs. Non-null.
	 * @return the exponentiated gamma elements and exponentiation proofs encapsulated in a {@link PartialDecryptPCCOutput}.
	 * @throws NullPointerException     if any input parameter is null.
	 * @throws IllegalArgumentException if
	 *                                  <ul>
	 *                                      <li>The context and input do not have the same group.</li>
	 *                                      <li>There is no combined correctness information for the election event ID and verification card set ID.</li>
	 *                                      <li>There are not psi encrypted partial Choice Return Codes in the {@code input}.</li>
	 *                                      <li>The partial Choice Return Codes have already been partially decrypted.</li>
	 *                                  </ul>
	 */
	@SuppressWarnings("java:S117")
	public PartialDecryptPCCOutput partialDecryptPCC(final ReturnCodesNodeContext context, final PartialDecryptPCCInput input) {
		checkNotNull(context);
		checkNotNull(input);

		// Cross group check.
		checkArgument(context.getEncryptionGroup().equals(input.getGroup()), "The context and input must have the same group.");

		final String vcs_id = context.getVerificationCardSetId();
		final int psi = getPsi(vcs_id);
		checkArgument(input.getEncryptedPartialChoiceReturnCodes().size() == psi,
				String.format("There must be psi encrypted partial Choice Return Codes. [psi: %s]", psi));

		// Variables.
		final int j = context.getNodeId();
		final String ee = context.getElectionEventId();
		final String vc_id = input.getVerificationCardId();
		final ElGamalMultiRecipientCiphertext E1 = input.getEncryptedVote();
		final ElGamalMultiRecipientCiphertext E1_tilde = input.getExponentiatedEncryptedVote();
		final ElGamalMultiRecipientCiphertext E2 = input.getEncryptedPartialChoiceReturnCodes();
		final GqElement gamma_1 = E1.getGamma();
		final GqElement Phi_1_0 = E1.get(0);
		final GqElement gamma_1_k_id = E1_tilde.getGamma();
		final GqElement Phi_1_0_k_id = E1_tilde.get(0);
		final GqElement gamma_2 = E2.getGamma();
		final ElGamalMultiRecipientPrivateKey sk_CCR_j = input.getCcrjChoiceReturnCodesEncryptionSecretKey();
		final ElGamalMultiRecipientPublicKey pk_CCR_j = input.getCcrjChoiceReturnCodesEncryptionPublicKey();
		final GqElement g = context.getEncryptionGroup().getGenerator();

		// Ensure vc_id ∉ L_decPCC,j.
		checkArgument(verificationCardStateService.isNotPartiallyDecrypted(vc_id),
				"The partial Choice Return Code has already been partially decrypted.");

		// Operation.
		final List<String> i_aux = Streams.concat(
				Stream.of(ee, vc_id),
				E2.getPhi().stream().map(phi_2_k -> integerToString(phi_2_k.getValue())),
				Stream.of(integerToString(gamma_1.getValue())),
				Stream.of(integerToString(gamma_1_k_id.getValue()), integerToString(Phi_1_0_k_id.getValue())),
				Stream.of(integerToString(Phi_1_0.getValue()), PARTIAL_DECRYPT_PCC, integerToString(BigInteger.valueOf(j)))
		).collect(Collectors.toList());

		final List<GqElement> d_j = new ArrayList<>();
		final List<ExponentiationProof> pi_decPCC_j = new ArrayList<>();
		for (int i = 0; i < psi; i++) {
			final GqElement d_j_i = gamma_2.exponentiate(sk_CCR_j.get(i));

			final GroupVector<GqElement, GqGroup> bases = GroupVector.of(g, gamma_2);
			final GroupVector<GqElement, GqGroup> exponentiations = GroupVector.of(pk_CCR_j.get(i), d_j_i);
			final ExponentiationProof pi_decPCC_j_i = zeroKnowledgeProofService.genExponentiationProof(bases, sk_CCR_j.get(i), exponentiations,
					i_aux);

			d_j.add(d_j_i);
			pi_decPCC_j.add(pi_decPCC_j_i);
		}
		// Corresponds to L_decPCC,j = L_decPCC,j ∪ vc_id.
		verificationCardStateService.setPartiallyDecrypted(vc_id);

		return new PartialDecryptPCCOutput(GroupVector.from(d_j), GroupVector.from(pi_decPCC_j));
	}

	private int getPsi(final String verificationCardSetId) {
		final VerificationCardSetEntity verificationCardSet = verificationCardSetService.getVerificationCardSet(verificationCardSetId);
		return verificationCardSet.getCombinedCorrectnessInformation().getTotalNumberOfSelections();
	}

	/**
	 * Regroups the inputs needed by the PartialDecryptPCC_j algorithm.
	 */
	public static class PartialDecryptPCCInput {

		private final String verificationCardId;
		private final ElGamalMultiRecipientCiphertext encryptedVote;
		private final ElGamalMultiRecipientCiphertext exponentiatedEncryptedVote;
		private final ElGamalMultiRecipientCiphertext encryptedPartialChoiceReturnCodes;
		private final ElGamalMultiRecipientPrivateKey ccrjChoiceReturnCodesEncryptionSecretKey;
		private final ElGamalMultiRecipientPublicKey ccrjChoiceReturnCodesEncryptionPublicKey;

		private PartialDecryptPCCInput(final String verificationCardId,
				final ElGamalMultiRecipientCiphertext encryptedVote,
				final ElGamalMultiRecipientCiphertext exponentiatedEncryptedVote,
				final ElGamalMultiRecipientCiphertext encryptedPartialChoiceReturnCodes,
				final ElGamalMultiRecipientPrivateKey ccrjChoiceReturnCodesEncryptionSecretKey,
				final ElGamalMultiRecipientPublicKey ccrjChoiceReturnCodesEncryptionPublicKey) {
			this.verificationCardId = verificationCardId;
			this.encryptedVote = encryptedVote;
			this.exponentiatedEncryptedVote = exponentiatedEncryptedVote;
			this.encryptedPartialChoiceReturnCodes = encryptedPartialChoiceReturnCodes;
			this.ccrjChoiceReturnCodesEncryptionSecretKey = ccrjChoiceReturnCodesEncryptionSecretKey;
			this.ccrjChoiceReturnCodesEncryptionPublicKey = ccrjChoiceReturnCodesEncryptionPublicKey;
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

		public ElGamalMultiRecipientPrivateKey getCcrjChoiceReturnCodesEncryptionSecretKey() {
			return ccrjChoiceReturnCodesEncryptionSecretKey;
		}

		public ElGamalMultiRecipientPublicKey getCcrjChoiceReturnCodesEncryptionPublicKey() {
			return ccrjChoiceReturnCodesEncryptionPublicKey;
		}

		public GqGroup getGroup() {
			return encryptedVote.getGroup();
		}

	}

	/**
	 * Builder performing input validations and cross-validations before constructing a {@link PartialDecryptPCCInput}.
	 */
	public static class PartialDecryptPCCInputBuilder {

		private String verificationCardId;
		private ElGamalMultiRecipientCiphertext encryptedVote;
		private ElGamalMultiRecipientCiphertext exponentiatedEncryptedVote;
		private ElGamalMultiRecipientCiphertext encryptedPartialChoiceReturnCodes;
		private ElGamalMultiRecipientPrivateKey ccrjChoiceReturnCodesEncryptionSecretKey;
		private ElGamalMultiRecipientPublicKey ccrjChoiceReturnCodesEncryptionPublicKey;

		public PartialDecryptPCCInputBuilder setVerificationCardId(final String verificationCardId) {
			this.verificationCardId = verificationCardId;
			return this;
		}

		public PartialDecryptPCCInputBuilder setEncryptedVote(final ElGamalMultiRecipientCiphertext encryptedVote) {
			this.encryptedVote = encryptedVote;
			return this;
		}

		public PartialDecryptPCCInputBuilder setExponentiatedEncryptedVote(final ElGamalMultiRecipientCiphertext exponentiatedEncryptedVote) {
			this.exponentiatedEncryptedVote = exponentiatedEncryptedVote;
			return this;
		}

		public PartialDecryptPCCInputBuilder setEncryptedPartialChoiceReturnCodes(
				final ElGamalMultiRecipientCiphertext encryptedPartialChoiceReturnCodes) {
			this.encryptedPartialChoiceReturnCodes = encryptedPartialChoiceReturnCodes;
			return this;
		}

		public PartialDecryptPCCInputBuilder setCcrjChoiceReturnCodesEncryptionSecretKey(
				final ElGamalMultiRecipientPrivateKey ccrjChoiceReturnCodesEncryptionSecretKey) {
			this.ccrjChoiceReturnCodesEncryptionSecretKey = ccrjChoiceReturnCodesEncryptionSecretKey;
			return this;
		}

		public PartialDecryptPCCInputBuilder setCcrjChoiceReturnCodesEncryptionPublicKey(
				final ElGamalMultiRecipientPublicKey ccrjChoiceReturnCodesEncryptionPublicKey) {
			this.ccrjChoiceReturnCodesEncryptionPublicKey = ccrjChoiceReturnCodesEncryptionPublicKey;
			return this;
		}

		/**
		 * Creates the PartialDecryptPCCInput. All fields must have been set and be non-null.
		 *
		 * @return a new PartialDecryptPCCInput.
		 * @throws NullPointerException     if any of the fields is null.
		 * @throws IllegalArgumentException if
		 *                                  <ul>
		 *                                      <li>The encrypted vote does not have exactly one phi.</li>
		 *                                      <li>The exponentiated encrypted vote does not have exactly one phi.</li>
		 *                                      <li>The secret key does not have phi elements.</li>
		 *                                      <li>The public key does not have phi elements.</li>
		 *                                      <li>Not all inputs have the same Gq group.</li>
		 *                                      <li>The secret key has a different group order.</li>
		 *                                      <li>The secret and public key do not match.</li>
		 *                                  </ul>
		 */
		public PartialDecryptPCCInput createPartialDecryptPCCInput() {
			checkNotNull(verificationCardId);
			checkNotNull(encryptedVote);
			checkNotNull(exponentiatedEncryptedVote);
			checkNotNull(encryptedPartialChoiceReturnCodes);
			checkNotNull(ccrjChoiceReturnCodesEncryptionSecretKey);
			checkNotNull(ccrjChoiceReturnCodesEncryptionPublicKey);

			// Size checks.
			checkArgument(encryptedVote.size() == 1, "The encrypted vote must have exactly 1 phi.");
			checkArgument(exponentiatedEncryptedVote.size() == 1, "The exponentiated encrypted vote must have exactly 1 phi.");

			final int phi = VotingOptionsConstants.MAXIMUM_NUMBER_OF_SELECTABLE_VOTING_OPTIONS;
			checkArgument(ccrjChoiceReturnCodesEncryptionSecretKey.size() == phi,
					String.format("The secret key must be of size phi. [phi: %s]", phi));
			checkArgument(ccrjChoiceReturnCodesEncryptionPublicKey.size() == phi,
					String.format("The public key must be of size phi. [phi: %s]", phi));

			// Cross group checks.
			final List<GqGroup> gqGroups = Arrays.asList(encryptedVote.getGroup(), exponentiatedEncryptedVote.getGroup(),
					encryptedPartialChoiceReturnCodes.getGroup(), ccrjChoiceReturnCodesEncryptionPublicKey.getGroup());
			checkArgument(allEqual(gqGroups.stream(), Function.identity()), "All input Gq groups must be the same.");

			checkArgument(gqGroups.get(0).hasSameOrderAs(ccrjChoiceReturnCodesEncryptionSecretKey.getGroup()),
					"The secret key must have the same group order than the other inputs.");

			// Keypair validation.
			final ElGamalMultiRecipientKeyPair keyPair = ElGamalMultiRecipientKeyPair.from(ccrjChoiceReturnCodesEncryptionSecretKey,
					ccrjChoiceReturnCodesEncryptionPublicKey.getGroup().getGenerator());
			checkArgument(keyPair.getPublicKey().equals(ccrjChoiceReturnCodesEncryptionPublicKey), "The secret and public keys do not match.");

			return new PartialDecryptPCCService.PartialDecryptPCCInput(verificationCardId, encryptedVote, exponentiatedEncryptedVote,
					encryptedPartialChoiceReturnCodes, ccrjChoiceReturnCodesEncryptionSecretKey, ccrjChoiceReturnCodesEncryptionPublicKey);
		}
	}

	/**
	 * Holds the output of the PartialDecryptPCC_j algorithm.
	 */
	public static class PartialDecryptPCCOutput {

		private final GroupVector<GqElement, GqGroup> exponentiatedGammas;
		private final GroupVector<ExponentiationProof, ZqGroup> exponentiationProofs;

		public PartialDecryptPCCOutput(final GroupVector<GqElement, GqGroup> exponentiatedGammas,
				final GroupVector<ExponentiationProof, ZqGroup> exponentiationProofs) {

			checkNotNull(exponentiatedGammas);
			checkNotNull(exponentiationProofs);

			// Size checks.
			checkArgument(exponentiatedGammas.size() == exponentiationProofs.size(),
					"There must be as many exponentiated gammas as there are exponentiation proofs.");

			// Cross group checks.
			checkArgument(exponentiatedGammas.getGroup().hasSameOrderAs(exponentiationProofs.getGroup()),
					"The exponentiated gammas and exponentiation proofs do not have the same group order.");

			this.exponentiatedGammas = exponentiatedGammas;
			this.exponentiationProofs = exponentiationProofs;
		}

		public GroupVector<GqElement, GqGroup> getExponentiatedGammas() {
			return exponentiatedGammas;
		}

		public GroupVector<ExponentiationProof, ZqGroup> getExponentiationProofs() {
			return exponentiationProofs;
		}
	}
}
