/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.voting.confirmvote;

import static ch.post.it.evoting.cryptoprimitives.utils.ConversionService.integerToByteArray;
import static ch.post.it.evoting.cryptoprimitives.utils.ConversionService.integerToString;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

import ch.post.it.evoting.controlcomponents.voting.ReturnCodesNodeContext;
import ch.post.it.evoting.controlcomponents.voting.VerificationCardStateService;
import ch.post.it.evoting.cryptoprimitives.hashing.HashService;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableList;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableString;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;
import ch.post.it.evoting.cryptoprimitives.utils.KDFService;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ExponentiationProof;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ZeroKnowledgeProof;

/**
 * Implements the CreateLVCCShare_j algorithm.
 */
@Service
public class CreateLVCCShareService {

	@VisibleForTesting
	static final int MAX_CONFIRMATION_ATTEMPTS = 5;

	private final int nodeId;
	private final KDFService kdfService;
	private final HashService hashService;
	private final ZeroKnowledgeProof zeroKnowledgeProofService;
	private final VerificationCardStateService verificationCardStateService;

	public CreateLVCCShareService(
			@Value("${nodeID}")
			final int nodeId,
			final KDFService kdfService,
			final HashService hashService,
			final ZeroKnowledgeProof zeroKnowledgeProofService,
			final VerificationCardStateService verificationCardStateService) {
		this.nodeId = nodeId;
		this.kdfService = kdfService;
		this.hashService = hashService;
		this.zeroKnowledgeProofService = zeroKnowledgeProofService;
		this.verificationCardStateService = verificationCardStateService;
	}

	/**
	 * Generates the long Vote Cast Return Code shares.
	 *
	 * @param confirmationKey                    CK<sub>id</sub>, the voter confirmation key.
	 * @param ccrjReturnCodesGenerationSecretKey k'<sub>j</sub>∈ Z<sub>q</sub>, CCR<sub>j</sub> return codes generation secret key. Not null.
	 * @param verificationCardId                 vc<sub>id</sub>, the verification card id. Not null.
	 * @param context                            the control component context, wrapping the election event id, verification card set id and control
	 *                                           component id. Not null.
	 * @return the long vote cast return code share, voter vote cast return code generation public key and exponentiation proof encapsulated in a
	 * {@link CreateLVCCShareOutput}.
	 * @throws NullPointerException     if any input parameter is null.
	 * @throws IllegalArgumentException if
	 *                                  <ul>
	 *                                      <li>The confirmation and secret keys do not have the same group order.</li>
	 *                                      <li>The verification card is not in L_sentVotes,j.</li>
	 *                                  </ul>
	 */
	@SuppressWarnings("java:S117")
	public CreateLVCCShareOutput createLVCCShare(final GqElement confirmationKey, final ZqElement ccrjReturnCodesGenerationSecretKey,
			final String verificationCardId, final ReturnCodesNodeContext context) {

		checkNotNull(confirmationKey);
		checkNotNull(ccrjReturnCodesGenerationSecretKey);
		checkNotNull(verificationCardId);
		checkNotNull(context);

		// Cross group checks.
		checkArgument(confirmationKey.getGroup().hasSameOrderAs(ccrjReturnCodesGenerationSecretKey.getGroup()),
				"Confirmation key and CCR_j Return Codes Generation secret key must have the same group order.");

		final String electionEventId = context.getElectionEventId();
		final String verificationCardSetId = context.getVerificationCardSetId();

		// Ensure vc_id ∈ L_sentVotes,j.
		checkArgument(verificationCardStateService.isLCCShareCreated(verificationCardId),
				String.format("The CCR_j did not compute the long Choice Return Code shares for verification card %s.", verificationCardId));
		// Ensure vc_id ∉ L_confirmedVotes,j
		checkArgument(verificationCardStateService.isNotConfirmed(verificationCardId),
				String.format("The CCR_j did already confirm the long Choice Return Code shares for verification card %s.", verificationCardId));

		// Variables.
		final GqElement g = confirmationKey.getGroup().getGenerator();
		final String vc_id = verificationCardId;
		final GqElement CK_id = confirmationKey;
		final String ee = electionEventId;
		final String vcs = verificationCardSetId;
		final ZqElement k_prime_j = ccrjReturnCodesGenerationSecretKey;

		// Operation.
		final int attempts_id = verificationCardStateService.getConfirmationAttempts(verificationCardId);
		checkArgument(attempts_id < MAX_CONFIRMATION_ATTEMPTS, String.format("Max confirmation attempts of %s exceeded.", MAX_CONFIRMATION_ATTEMPTS));

		final byte[] PRK = integerToByteArray(k_prime_j.getValue());
		final ImmutableList<String> info_CK = ImmutableList.of("VoterVoteCastReturnCodeGeneration", ee, vcs, vc_id);
		final BigInteger q = k_prime_j.getGroup().getQ();
		final ZqElement kc_j_id = kdfService.KDFToZq(PRK, info_CK, q);
		final GqElement Kc_j_id = g.exponentiate(kc_j_id);
		final GqElement hCK_id = hashService.hashAndSquare(CK_id.getValue(), CK_id.getGroup());
		final GqElement lVCC_id_j = hCK_id.exponentiate(kc_j_id);
		final List<String> i_aux_1 = ImmutableList.of("CreateLVCCShare", ee, vcs, vc_id, integerToString(nodeId));
		final HashableList i_aux_1_hashable = HashableList.from(i_aux_1.stream().map(HashableString::from).collect(Collectors.toList()));
		final String hlVCC_id_j = Base64.getEncoder().encodeToString(hashService.recursiveHash(i_aux_1_hashable, lVCC_id_j));
		final List<String> i_aux_2 = Stream.concat(i_aux_1.stream(), Stream.of(integerToString(attempts_id)))
				.collect(ImmutableList.toImmutableList());
		final GroupVector<GqElement, GqGroup> bases = GroupVector.of(g, hCK_id);
		final GroupVector<GqElement, GqGroup> exponentiations = GroupVector.of(Kc_j_id, lVCC_id_j);
		final ExponentiationProof pi_expLVCC_j_id = zeroKnowledgeProofService.genExponentiationProof(bases, kc_j_id, exponentiations, i_aux_2);

		verificationCardStateService.incrementConfirmationAttempts(verificationCardId);

		return new CreateLVCCShareOutput(attempts_id, hCK_id, lVCC_id_j, hlVCC_id_j, Kc_j_id, pi_expLVCC_j_id);
	}

	public static class CreateLVCCShareOutput {

		private final int confirmationAttempts;
		private final GqElement hashedSquaredConfirmationKey;
		private final GqElement longVoteCastReturnCodeShare;
		private final String hashedLongVoteCasteReturnCodeShare;
		private final GqElement voterVoteCastReturnCodeGenerationPublicKey;
		private final ExponentiationProof exponentiationProof;

		CreateLVCCShareOutput(final int confirmationAttempts, final GqElement hashedSquaredConfirmationKey,
				final GqElement longVoteCastReturnCodeShare, final String hashedLongVoteCastReturnCodeShare,
				final GqElement voterVoteCastReturnCodeGenerationPublicKey,
				final ExponentiationProof exponentiationProof) {

			checkArgument(confirmationAttempts >= 0);
			checkArgument(confirmationAttempts < MAX_CONFIRMATION_ATTEMPTS);
			checkNotNull(hashedSquaredConfirmationKey);
			checkNotNull(longVoteCastReturnCodeShare);
			checkNotNull(hashedLongVoteCastReturnCodeShare);
			checkNotNull(voterVoteCastReturnCodeGenerationPublicKey);
			checkNotNull(exponentiationProof);

			// Cross group checks.
			checkArgument(hashedSquaredConfirmationKey.getGroup().equals(longVoteCastReturnCodeShare.getGroup()),
					"Confirmation key and long vote cast return code share must have the same group.");
			checkArgument(hashedSquaredConfirmationKey.getGroup().equals(voterVoteCastReturnCodeGenerationPublicKey.getGroup()),
					"Confirmation key and Voter Vote Cast Return Code Generation public key must have the same group.");
			checkArgument(hashedSquaredConfirmationKey.getGroup().hasSameOrderAs(exponentiationProof.getGroup()),
					"Confirmation key and exponentiation proof must have the same group order.");

			this.confirmationAttempts = confirmationAttempts;
			this.hashedSquaredConfirmationKey = hashedSquaredConfirmationKey;
			this.longVoteCastReturnCodeShare = longVoteCastReturnCodeShare;
			this.hashedLongVoteCasteReturnCodeShare = hashedLongVoteCastReturnCodeShare;
			this.voterVoteCastReturnCodeGenerationPublicKey = voterVoteCastReturnCodeGenerationPublicKey;
			this.exponentiationProof = exponentiationProof;
		}

		public int getConfirmationAttempts() {
			return confirmationAttempts;
		}

		public GqElement getHashedSquaredConfirmationKey() {
			return hashedSquaredConfirmationKey;
		}

		public GqElement getLongVoteCastReturnCodeShare() {
			return longVoteCastReturnCodeShare;
		}

		public String getHashedLongVoteCasteReturnCodeShare() {
			return hashedLongVoteCasteReturnCodeShare;
		}

		public GqElement getVoterVoteCastReturnCodeGenerationPublicKey() {
			return voterVoteCastReturnCodeGenerationPublicKey;
		}

		public ExponentiationProof getExponentiationProof() {
			return exponentiationProof;
		}
	}
}
