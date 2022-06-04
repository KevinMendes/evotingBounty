/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.configuration.setupvoting;

import static ch.post.it.evoting.cryptoprimitives.math.GroupVector.toGroupVector;
import static ch.post.it.evoting.cryptoprimitives.utils.ConversionService.integerToByteArray;
import static ch.post.it.evoting.cryptoprimitives.utils.ConversionService.integerToString;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Streams;

import ch.post.it.evoting.controlcomponents.VerificationCard;
import ch.post.it.evoting.controlcomponents.VerificationCardService;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;
import ch.post.it.evoting.cryptoprimitives.utils.KDFService;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ExponentiationProof;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ZeroKnowledgeProof;

@SuppressWarnings("java:S117")
@Service
public class GenEncLongCodeSharesService {

	private final KDFService kdfService;
	private final ZeroKnowledgeProof zeroKnowledgeProofService;
	private final VerificationCardService verificationCardService;

	public GenEncLongCodeSharesService(
			final KDFService kdfService,
			final ZeroKnowledgeProof zeroKnowledgeProofService,
			final VerificationCardService verificationCardService) {
		this.kdfService = kdfService;
		this.zeroKnowledgeProofService = zeroKnowledgeProofService;
		this.verificationCardService = verificationCardService;
	}

	/**
	 * Generates the encrypted CCR_j long return code shares.
	 *
	 * @param context the {@link GenEncLongCodeSharesContext} containing necessary ids, keys and group. Non-null.
	 * @param input   the {@link GenEncLongCodeSharesInput} input, contains the verification card id, the encrypted hashed partial choice return codes
	 *                and the encrypted hashed confirmation key of a specific voter. Non-null.
	 * @return output the {@link GenEncLongCodeSharesOutput}, contains the output for each verification card.
	 * @throws NullPointerException     if any of the context or input are null.
	 * @throws IllegalArgumentException if any of the voting cards has already been generated.
	 */
	public GenEncLongCodeSharesOutput genEncLongCodeShares(final GenEncLongCodeSharesContext context, final GenEncLongCodeSharesInput input) {

		checkNotNull(context);
		checkNotNull(input);

		// Context
		final int j = context.getNodeID();
		final String ee = context.getElectionEventId();
		final String vcs = context.getVerificationCardSetId();
		final GqElement g = context.getGqGroup().getGenerator();

		// Input
		final ZqElement k_prime_j = input.getReturnCodesGenerationSecretKey();
		final List<String> vc = input.getVerificationCardIDs();
		final List<ElGamalMultiRecipientCiphertext> c_pCC = input.getEncryptedHashedPartialChoiceReturnCodes();
		final List<ElGamalMultiRecipientCiphertext> c_ck = input.getEncryptedHashedConfirmationKeys();

		final int N_E = vc.size();
		final BigInteger q = g.getGroup().getQ();
		final List<GqElement> K_j = new ArrayList<>();
		final List<GqElement> Kc_j = new ArrayList<>();
		final List<ElGamalMultiRecipientCiphertext> c_expPCC_j = new ArrayList<>();
		final List<ExponentiationProof> pi_expPCC_j = new ArrayList<>();
		final List<ElGamalMultiRecipientCiphertext> c_expCK_j = new ArrayList<>();
		final List<ExponentiationProof> pi_expCK_j = new ArrayList<>();
		final List<VerificationCard> L_genVC_j = new ArrayList<>();

		final List<ElGamalMultiRecipientPublicKey> vcPublicKeys = input.getVerificationCardPublicKeys();
		final List<String> ids = IntStream.range(0, N_E)
				.mapToObj(vc::get)
				.collect(Collectors.toList());

		// Require
		checkArgument(!verificationCardService.exist(ids), "Voting cards have already been generated.");

		// Algorithm.
		final byte[] PRK = integerToByteArray(k_prime_j.getValue());

		for (int id = 0; id < N_E; id++) {

			final String vc_id = vc.get(id);

			final ImmutableList<String> info = ImmutableList.of("VoterChoiceReturnCodeGeneration", ee, vcs, vc_id);

			final ZqElement k_j_id = kdfService.KDFToZq(PRK, info, q);

			final GqElement K_j_id = g.exponentiate(k_j_id);

			final ImmutableList<String> info_CK = ImmutableList.of("VoterVoteCastReturnCodeGeneration", ee, vcs, vc_id);

			final ZqElement kc_j_id = kdfService.KDFToZq(PRK, info_CK, q);

			final GqElement Kc_j_id = g.exponentiate(kc_j_id);

			final ElGamalMultiRecipientCiphertext c_pCC_id = c_pCC.get(id);
			final ElGamalMultiRecipientCiphertext c_expPCC_j_id = c_pCC_id.exponentiate(k_j_id);

			final List<String> i_aux = Arrays.asList(ee, vc_id, "GenEncLongCodeShares", integerToString(j));

			final GroupVector<GqElement, GqGroup> basesPCC = Streams.concat(Stream.of(g), c_pCC_id.stream()).collect(toGroupVector());
			List<GqElement> exponentiations = Streams.concat(Stream.of(K_j_id), c_expPCC_j_id.stream()).collect(Collectors.toList());
			final GroupVector<GqElement, GqGroup> exponentiationsPCC = GroupVector.from(exponentiations);
			final ExponentiationProof pi_expPCC_j_id = zeroKnowledgeProofService.genExponentiationProof(basesPCC, k_j_id, exponentiationsPCC, i_aux);

			final ElGamalMultiRecipientCiphertext c_ck_id = c_ck.get(id);
			final ElGamalMultiRecipientCiphertext c_expCK_j_id = c_ck_id.exponentiate(kc_j_id);

			final GroupVector<GqElement, GqGroup> basesCK = Streams.concat(Stream.of(g), c_ck_id.stream()).collect(toGroupVector());
			exponentiations = Streams.concat(Stream.of(Kc_j_id), c_expCK_j_id.stream()).collect(Collectors.toList());
			final GroupVector<GqElement, GqGroup> exponentiationsCK = GroupVector.from(exponentiations);
			final ExponentiationProof pi_expCK_j_id = zeroKnowledgeProofService.genExponentiationProof(basesCK, kc_j_id, exponentiationsCK, i_aux);

			L_genVC_j.add(new VerificationCard(vc_id, vcs, vcPublicKeys.get(id)));

			// Output.
			K_j.add(K_j_id);
			Kc_j.add(Kc_j_id);
			c_expPCC_j.add(c_expPCC_j_id);
			pi_expPCC_j.add(pi_expPCC_j_id);
			c_expCK_j.add(c_expCK_j_id);
			pi_expCK_j.add(pi_expCK_j_id);
		}
		verificationCardService.saveAll(L_genVC_j);

		return new GenEncLongCodeSharesOutput.Builder()
				.setVoterChoiceReturnCodeGenerationPublicKeys(K_j)
				.setVoterVoteCastReturnCodeGenerationPublicKeys(Kc_j)
				.setExponentiatedEncryptedHashedPartialChoiceReturnCodes(GroupVector.from(c_expPCC_j))
				.setProofsCorrectExponentiationPartialChoiceReturnCodes(pi_expPCC_j)
				.setExponentiatedEncryptedHashedConfirmationKeys(GroupVector.from(c_expCK_j))
				.setProofsCorrectExponentiationConfirmationKeys(pi_expCK_j)
				.build();
	}

}
