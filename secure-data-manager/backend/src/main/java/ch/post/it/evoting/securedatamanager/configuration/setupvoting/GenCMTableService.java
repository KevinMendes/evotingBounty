/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.configuration.setupvoting;

import static ch.post.it.evoting.cryptoprimitives.domain.VotingOptionsConstants.MAXIMUM_NUMBER_OF_VOTING_OPTIONS;
import static ch.post.it.evoting.cryptoprimitives.utils.ConversionService.stringToByteArray;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.google.common.collect.ImmutableList;
import com.google.common.primitives.Bytes;

import ch.post.it.evoting.cryptoprimitives.domain.election.Ballot;
import ch.post.it.evoting.cryptoprimitives.domain.election.CombinedCorrectnessInformation;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientMessage;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPrivateKey;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalService;
import ch.post.it.evoting.cryptoprimitives.hashing.HashService;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableByteArray;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableString;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.RandomService;
import ch.post.it.evoting.cryptoprimitives.symmetric.SymmetricCiphertext;
import ch.post.it.evoting.cryptoprimitives.symmetric.SymmetricService;
import ch.post.it.evoting.cryptoprimitives.utils.KDFService;
import ch.post.it.evoting.securedatamanager.services.application.service.BallotService;

/**
 * Implements the GenCMTable algorithm.
 */
@Service
public class GenCMTableService {

	static final int OMEGA = MAXIMUM_NUMBER_OF_VOTING_OPTIONS;
	static final int CHOICE_RETURN_CODES_LENGTH = 4;
	static final int VOTE_CAST_RETURN_CODE_LENGTH = 8;
	static final int BASE64_ENCODE_HASH_OUTPUT_LENGTH = 44;

	private static final int KEY_DERIVATION_BYTE_LENGTH = 32;

	private final KDFService kdfService;
	private final HashService hashService;
	private final BallotService ballotService;
	private final RandomService randomService;
	private final ElGamalService elGamalService;
	private final SymmetricService symmetricService;

	public GenCMTableService(final KDFService kdfService,
			@Qualifier("cryptoPrimitivesHashService")
			final HashService hashService,
			final BallotService ballotService,
			final RandomService randomService,
			final ElGamalService elGamalService,
			final SymmetricService symmetricService) {
		this.kdfService = kdfService;
		this.hashService = hashService;
		this.ballotService = ballotService;
		this.randomService = randomService;
		this.elGamalService = elGamalService;
		this.symmetricService = symmetricService;
	}

	/**
	 * Generates the Return Codes Mapping table CMtable that allows the voting server to retrieve the short Choice Return Codes and the short Vote
	 * Cast Return Code.
	 *
	 * @param context the {@link GenCMTableContext} containing necessary ids, keys and group. Non-null.
	 * @param input   the {@link GenCMTableInput} containing all needed inputs. Non-null.
	 * @return the Return Codes Mapping table, the short Choice Return Codes and the short Vote Cast Return Codes encapsulated in the {@link
	 * GenCMTableOutput}.
	 * @throws NullPointerException     if context or input parameters are null.
	 * @throws IllegalArgumentException if
	 *                                  <ul>
	 *                                      <li>The context and input do not have the same group.</li>
	 *                                      <li>There are less secret key elements than pre-Choice Return codes elements.</li>
	 *                                  </ul>
	 */
	@SuppressWarnings("java:S117")
	public GenCMTableOutput genCMTable(final GenCMTableContext context, final GenCMTableInput input) {

		checkNotNull(context);
		checkNotNull(input);

		// Cross group check.
		checkArgument(context.getEncryptionGroup().equals(input.getGroup()), "The context and input must have the same group.");

		// Variables.
		final String ee = context.getElectionEventId();
		final ElGamalMultiRecipientPrivateKey sk_setup = context.getSetupSecretKey();
		final int L_CC = CHOICE_RETURN_CODES_LENGTH;
		final int L_VCC = VOTE_CAST_RETURN_CODE_LENGTH;

		final List<String> vc = input.getVerificationCardIds();
		final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> c_pC = input.getEncryptedPreChoiceReturnCodes();
		final GroupVector<GqElement, GqGroup> pVCC = input.getPreVoteCastReturnCodes();

		final int N_E = input.getEncryptedPreChoiceReturnCodes().size();
		final int n = input.getEncryptedPreChoiceReturnCodes().get(0).size();
		final List<List<String>> CC = new ArrayList<>();
		final List<String> VCC = new ArrayList<>();
		final int omega = sk_setup.size();

		// Require n ≤ ω.
		checkArgument(n <= omega, "There cannot be more encrypted pre-Choice Return codes elements than secret key elements.");

		final CombinedCorrectnessInformation combinedCorrectnessInformation = getCombinedCorrectnessInformation(context.getElectionEventId(),
				context.getBallotId());

		// Operation.
		final Map<String, String> CMtable = new HashMap<>();

		for (int id = 0; id < N_E; id++) {

			final List<String> CC_id = randomService.genUniqueDecimalStrings(L_CC, n);
			final ElGamalMultiRecipientMessage pC_id = elGamalService.getMessage(c_pC.get(id), sk_setup);

			final String vc_id = vc.get(id);

			for (int k = 0; k < n; k++) {
				final GqElement pC_id_k = pC_id.get(k);
				final byte[] lCC_id_k = hashService.recursiveHash(pC_id_k, HashableString.from(vc_id), HashableString.from(ee),
						HashableString.from(combinedCorrectnessInformation.getCorrectnessIdForVotingOptionIndex(k)));

				final byte[] skcc_id_k = kdfService.KDF(lCC_id_k, ImmutableList.of(), KEY_DERIVATION_BYTE_LENGTH);

				final SymmetricCiphertext ctCC_id_k = symmetricService.genCiphertextSymmetric(skcc_id_k, stringToByteArray(CC_id.get(k)),
						Collections.emptyList());

				final String lCC_id_k_HB64 = Base64.getEncoder().encodeToString(hashService.recursiveHash(HashableByteArray.from(lCC_id_k)));
				final String ctCC_id_k_B64 = Base64.getEncoder().encodeToString(Bytes.concat(ctCC_id_k.getCiphertext(), ctCC_id_k.getNonce()));
				CMtable.put(lCC_id_k_HB64, ctCC_id_k_B64);
			}

			final GqElement pVCC_id = pVCC.get(id);

			final byte[] lVCC_id = hashService.recursiveHash(pVCC_id, HashableString.from(vc_id), HashableString.from(ee));

			final String VCC_id = randomService.genUniqueDecimalStrings(L_VCC, 1).get(0);

			final byte[] skvcc_id = kdfService.KDF(lVCC_id, ImmutableList.of(), KEY_DERIVATION_BYTE_LENGTH);

			final SymmetricCiphertext ctVCC_id = symmetricService.genCiphertextSymmetric(skvcc_id, stringToByteArray(VCC_id),
					Collections.emptyList());

			final String lVCC_id_HB64 = Base64.getEncoder().encodeToString(hashService.recursiveHash(HashableByteArray.from(lVCC_id)));
			final String ctVCC_id_B64 = Base64.getEncoder().encodeToString(Bytes.concat(ctVCC_id.getCiphertext(), ctVCC_id.getNonce()));
			CMtable.put(lVCC_id_HB64, ctVCC_id_B64);

			CC.add(CC_id);
			VCC.add(VCC_id);
		}

		// Order(CMtable, 1). The TreeMap reorders the entries by their key to ensure that the original order of insertion is completely lost.
		final Map<String, String> ordered_CMtable = new TreeMap<>(CMtable);

		return new GenCMTableOutput.Builder()
				.setReturnCodesMappingTable(ordered_CMtable)
				.setShortChoiceReturnCodes(CC)
				.setShortVoteCastReturnCodes(VCC)
				.build();
	}

	private CombinedCorrectnessInformation getCombinedCorrectnessInformation(final String electionEventId, final String ballotId) {
		final Ballot ballot = ballotService.getBallot(electionEventId, ballotId);
		return new CombinedCorrectnessInformation(ballot);
	}

}
