/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.voting.sendvote;

import static ch.post.it.evoting.cryptoprimitives.utils.ConversionService.integerToByteArray;
import static ch.post.it.evoting.cryptoprimitives.utils.ConversionService.integerToString;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableList;

import ch.post.it.evoting.controlcomponents.VerificationCardSetEntity;
import ch.post.it.evoting.controlcomponents.VerificationCardSetService;
import ch.post.it.evoting.controlcomponents.voting.ReturnCodesNodeContext;
import ch.post.it.evoting.controlcomponents.voting.VerificationCardStateService;
import ch.post.it.evoting.cryptoprimitives.domain.election.CombinedCorrectnessInformation;
import ch.post.it.evoting.cryptoprimitives.hashing.HashService;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableString;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;
import ch.post.it.evoting.cryptoprimitives.utils.KDFService;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ExponentiationProof;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ZeroKnowledgeProof;

/**
 * Implements the CreateLCCShare_j algorithm.
 */
@Service
public class CreateLCCShareService {

	private final KDFService kdfService;
	private final HashService hashService;
	private final ZeroKnowledgeProof zeroKnowledgeProofService;
	private final VerificationCardSetService verificationCardSetService;
	private final VerificationCardStateService verificationCardStateService;

	public CreateLCCShareService(
			final KDFService kdfService,
			final HashService hashService,
			final ZeroKnowledgeProof zeroKnowledgeProofService,
			final VerificationCardSetService verificationCardSetService,
			final VerificationCardStateService verificationCardStateService) {
		this.kdfService = kdfService;
		this.hashService = hashService;
		this.zeroKnowledgeProofService = zeroKnowledgeProofService;
		this.verificationCardSetService = verificationCardSetService;
		this.verificationCardStateService = verificationCardStateService;
	}

	/**
	 * Generates the long Choice Return Codes shares and proves correct exponentiation.
	 *
	 * @param partialChoiceReturnCodes           pCC<sub>id</sub> ∈ G<sub>q</sub><sup>&#x1D713;</sup>, a vector of partial choice return codes. Not
	 *                                           null.
	 * @param ccrjReturnCodesGenerationSecretKey k<sub>j</sub>' ∈ Z<sub>q</sub><sup>k</sup>, CCR<sub>j</sub> return codes generation secret key. Not
	 *                                           null.
	 * @param verificationCardId                 vc<sub>id</sub>, the verification card id. Not null.
	 * @param context                            the control component context, wrapping the election event id, verification card set id and control
	 *                                           component id. Not null.
	 * @return the hashed partial Choice Return Codes, the long Choice Return Code share, the Voter Choice Return Code Generation public key and the
	 * exponentiation proof encapsulated in a {@link CreateLCCShareOutput}.
	 * @throws NullPointerException     if any input parameter is null.
	 * @throws IllegalArgumentException if
	 *                                  <ul>
	 *                                      <li>The partial choice return codes and the CCR_J Return Codes Generation secret key do not have the same group order.</li>
	 *                                      <li>All partial choice return codes are not distinct.</li>
	 *                                      <li>The verification card is not in L_decPCC,j.</li>
	 *                                      <li>The verification card is in L_sentVotes,j</li>
	 *                                  </ul>
	 */
	@SuppressWarnings("java:S117")
	public CreateLCCShareOutput createLCCShare(final GroupVector<GqElement, GqGroup> partialChoiceReturnCodes,
			final ZqElement ccrjReturnCodesGenerationSecretKey, final String verificationCardId, final ReturnCodesNodeContext context) {

		checkNotNull(partialChoiceReturnCodes);
		checkNotNull(ccrjReturnCodesGenerationSecretKey);
		checkNotNull(verificationCardId);
		checkNotNull(context);

		final String electionEventId = context.getElectionEventId();
		final String verificationCardSetId = context.getVerificationCardSetId();
		final int nodeId = context.getNodeId();

		// Cross group check.
		checkArgument(partialChoiceReturnCodes.getGroup().hasSameOrderAs(ccrjReturnCodesGenerationSecretKey.getGroup()),
				"The partial choice return codes and return codes generation secret key must have the same group order.");

		final VerificationCardSetEntity verificationCardSet = verificationCardSetService.getVerificationCardSet(verificationCardSetId);
		final CombinedCorrectnessInformation combinedCorrectnessInformation = verificationCardSet.getCombinedCorrectnessInformation();

		// Check correct number of partial choice return codes.
		final int psi = combinedCorrectnessInformation.getTotalNumberOfSelections();
		checkArgument(partialChoiceReturnCodes.size() == psi,
				String.format("The number of partial choice return codes (%s) must be equal to psi (%s).", partialChoiceReturnCodes.size(), psi));

		// Ensures that all partial Choice Return Codes are distinct.
		final boolean pCCDistinct = partialChoiceReturnCodes.stream()
				.allMatch(ConcurrentHashMap.newKeySet()::add);
		checkArgument(pCCDistinct, "All pCC must be distinct.");

		// Corresponds to vc_id ∈ L_decPCC,j.
		checkArgument(!verificationCardStateService.isLCCShareCreated(verificationCardId),
				String.format("The CCR_j already generated the long Choice Return Code share in a previous attempt for verification card %s.",
						verificationCardId));

		// Variables.
		final String vc_id = verificationCardId;
		final String vcs = context.getVerificationCardSetId();
		final ZqElement k_prime_j = ccrjReturnCodesGenerationSecretKey;
		final GqElement g = partialChoiceReturnCodes.getGroup().getGenerator();
		final String ee = electionEventId;
		final String vcs_id = verificationCardSetId;

		// Retrieve the partial Choice Return Codes allow list for the current verification card set id.
		final List<String> allowList = getAllowList(vcs_id);

		// Operation.
		final byte[] PRK = integerToByteArray(k_prime_j.getValue());
		final ImmutableList<String> info = ImmutableList.of("VoterChoiceReturnCodeGeneration", ee, vcs, vc_id);
		final BigInteger q = k_prime_j.getGroup().getQ();
		final ZqElement k_j_id = kdfService.KDFToZq(PRK, info, q);
		final GqElement K_j_id = g.exponentiate(k_j_id);

		final List<GqElement> hpCC_id_elements = new ArrayList<>();
		final List<GqElement> lCC_j_id_elements = new ArrayList<>();
		for (int i = 0; i < psi; i++) {
			final GqElement pCC_id_i = partialChoiceReturnCodes.get(i);
			final GqElement hpCC_id_i = hashService.hashAndSquare(pCC_id_i.getValue(), pCC_id_i.getGroup());
			final String ci = combinedCorrectnessInformation.getCorrectnessIdForSelectionIndex(i);
			final byte[] lpCC_id_id = hashService.recursiveHash(hpCC_id_i, HashableString.from(vc_id), HashableString.from(ee),
					HashableString.from(ci));
			if (!allowList.contains(Base64.getEncoder().encodeToString(lpCC_id_id))) {
				throw new IllegalArgumentException("The partial Choice Return Codes allow list does not contain the partial Choice Return Code.");
			} else {
				final GqElement lCC_j_id_i = hpCC_id_i.exponentiate(k_j_id);
				lCC_j_id_elements.add(lCC_j_id_i);
			}

			hpCC_id_elements.add(hpCC_id_i);
		}
		final GroupVector<GqElement, GqGroup> hpCC_id = GroupVector.from(hpCC_id_elements);
		final GroupVector<GqElement, GqGroup> lCC_j_id = GroupVector.from(lCC_j_id_elements);

		final List<String> i_aux = Arrays.asList(ee, vc_id, "CreateLCCShare", integerToString(nodeId));
		final GroupVector<GqElement, GqGroup> bases = hpCC_id.prepend(g);
		final GroupVector<GqElement, GqGroup> exponentiations = lCC_j_id.prepend(K_j_id);
		final ExponentiationProof pi_expLCC_j_id = zeroKnowledgeProofService.genExponentiationProof(bases, k_j_id, exponentiations, i_aux);

		verificationCardStateService.setLCCShareCreated(verificationCardId);

		return new CreateLCCShareOutput(hpCC_id, lCC_j_id, K_j_id, pi_expLCC_j_id);
	}

	private List<String> getAllowList(final String verificationCardSetId) {
		final VerificationCardSetEntity verificationCardSetEntity = verificationCardSetService.getVerificationCardSet(verificationCardSetId);
		final List<String> allowList = verificationCardSetEntity.getAllowList();

		checkState(!allowList.isEmpty(),
				"The partial Choice Return Codes allow list must exist for verification card set. [verificationCardSetId: %s]",
				verificationCardSetId);

		return allowList;
	}

	/**
	 * Holds the output of the CreateLCCShare_j algorithm.
	 */
	public static class CreateLCCShareOutput {

		private final GroupVector<GqElement, GqGroup> hashedPartialChoiceReturnCodes;
		private final GroupVector<GqElement, GqGroup> longChoiceReturnCodeShare;
		private final GqElement voterChoiceReturnCodeGenerationPublicKey;
		private final ExponentiationProof exponentiationProof;

		CreateLCCShareOutput(final GroupVector<GqElement, GqGroup> hashedPartialChoiceReturnCodes,
				final GroupVector<GqElement, GqGroup> longChoiceReturnCodeShare, final GqElement voterChoiceReturnCodeGenerationPublicKey,
				final ExponentiationProof exponentiationProof) {

			checkNotNull(hashedPartialChoiceReturnCodes);
			checkNotNull(longChoiceReturnCodeShare);
			checkNotNull(voterChoiceReturnCodeGenerationPublicKey);
			checkNotNull(exponentiationProof);

			// Cross group checks.
			checkArgument(hashedPartialChoiceReturnCodes.getGroup().equals(longChoiceReturnCodeShare.getGroup()),
					"The hashed partial Choice Return Codes and the long Choice Return Code shares must have the same group.");
			checkArgument(longChoiceReturnCodeShare.getGroup().equals(voterChoiceReturnCodeGenerationPublicKey.getGroup()),
					"The long Choice Return Code shares and the voter Choice Return Code Generation public key must have the same group.");
			checkArgument(longChoiceReturnCodeShare.getGroup().hasSameOrderAs(exponentiationProof.getGroup()),
					"The long Choice Return Code shares and exponentiation proof must have the same group order.");

			this.hashedPartialChoiceReturnCodes = hashedPartialChoiceReturnCodes;
			this.longChoiceReturnCodeShare = longChoiceReturnCodeShare;
			this.voterChoiceReturnCodeGenerationPublicKey = voterChoiceReturnCodeGenerationPublicKey;
			this.exponentiationProof = exponentiationProof;
		}

		public GroupVector<GqElement, GqGroup> getHashedPartialChoiceReturnCodes() {
			return hashedPartialChoiceReturnCodes;
		}

		public GroupVector<GqElement, GqGroup> getLongChoiceReturnCodeShare() {
			return longChoiceReturnCodeShare;
		}

		public GqElement getVoterChoiceReturnCodeGenerationPublicKey() {
			return voterChoiceReturnCodeGenerationPublicKey;
		}

		public ExponentiationProof getExponentiationProof() {
			return exponentiationProof;
		}
	}

}
