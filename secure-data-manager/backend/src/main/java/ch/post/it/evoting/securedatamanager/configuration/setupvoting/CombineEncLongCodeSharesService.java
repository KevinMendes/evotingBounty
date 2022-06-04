/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

package ch.post.it.evoting.securedatamanager.configuration.setupvoting;

import static ch.post.it.evoting.cryptoprimitives.hashing.HashableString.from;
import static ch.post.it.evoting.cryptoprimitives.utils.ConversionService.integerToString;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientMessage;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPrivateKey;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalService;
import ch.post.it.evoting.cryptoprimitives.hashing.HashService;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableList;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableString;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupMatrix;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;

/**
 * Implements the CombineEncLongCodeShares algorithm.
 */
@Service
public class CombineEncLongCodeSharesService {

	private final HashService hashService;
	private final ElGamalService elGamalService;

	public CombineEncLongCodeSharesService(
			@Qualifier("cryptoPrimitivesHashService")
			final HashService hashService,
			final ElGamalService elGamalService) {
		this.hashService = hashService;
		this.elGamalService = elGamalService;
	}

	/**
	 * Combines the control components’ encrypted long return code shares.
	 *
	 * @param input the {@link CombineEncLongCodeSharesInput} containing all needed inputs. Non-null.
	 * @return the combined control components’ encrypted long return code shares in a {@link CombineEncLongCodeSharesOutput}.
	 * @throws NullPointerException if the input parameter is null.
	 */
	@SuppressWarnings("java:S117")
	public CombineEncLongCodeSharesOutput combineEncLongCodeShares(final CombineEncLongCodeSharesContext context,
			final CombineEncLongCodeSharesInput input) {
		checkNotNull(context, "The context cannot be null.");
		checkNotNull(input, "The input cannot be null.");

		// Context
		final String ee = context.getElectionEventId();
		final String vcs = context.getVerificationCardSetId();
		final ElGamalMultiRecipientPrivateKey sk_setup = context.getSetupSecretKey();

		// Input
		final GroupMatrix<ElGamalMultiRecipientCiphertext, GqGroup> C_expPCC = input.getExponentiatedEncryptedHashedPartialChoiceReturnCodesMatrix();
		final GroupMatrix<ElGamalMultiRecipientCiphertext, GqGroup> C_expCK = input.getExponentiatedEncryptedHashedConfirmationKeysMatrix();
		final List<String> vc = input.getVerificationCardIds();

		final int N_E = C_expPCC.numRows();
		final int n = C_expPCC.get(0, 0).size();
		final GqGroup group = C_expPCC.getGroup();
		final List<ElGamalMultiRecipientCiphertext> c_pC = new ArrayList<>();
		final List<GqElement> pVCC = new ArrayList<>();

		// Algorithm
		final List<String> L_lVCC = new ArrayList<>();

		for (int id = 0; id < N_E; id++) {

			ElGamalMultiRecipientCiphertext c_pC_id = ElGamalMultiRecipientCiphertext.neutralElement(n, group);
			final String vc_id = vc.get(id);

			final List<GqElement> lVCC_id = new ArrayList<>();
			final List<HashableString> hlVCC_id = new ArrayList<>();

			for (int j = 0; j < 4; j++) {
				c_pC_id = c_pC_id.multiply(C_expPCC.get(id, j));

				final ElGamalMultiRecipientCiphertext C_expCK_j_id = C_expCK.get(id, j);

				final GqElement lVCC_id_j = getMessage(C_expCK_j_id, sk_setup);

				final HashableList i_aux_1 = HashableList.of(from("CreateLVCCShare"), from(ee), from(vcs), HashableString.from(vc_id),
						from(integerToString(j)));

				final HashableString hlVCC_id_j = from(Base64.getEncoder().encodeToString(hashService.recursiveHash(i_aux_1, lVCC_id_j)));

				lVCC_id.add(lVCC_id_j);
				hlVCC_id.add(hlVCC_id_j);
			}

			final GqElement pVCC_id = lVCC_id.stream().reduce(group.getIdentity(), GqElement::multiply);

			final HashableList i_aux_2 = HashableList.of(from("VerifyLVCCHash"), from(ee), from(vcs), from(vc_id));

			final String hhlVCC_id = Base64.getEncoder().encodeToString(hashService.recursiveHash(i_aux_2, HashableList.from(hlVCC_id)));

			c_pC.add(c_pC_id);
			pVCC.add(pVCC_id);
			L_lVCC.add(hhlVCC_id);
		}

		return new CombineEncLongCodeSharesOutput.Builder()
				.setEncryptedPreChoiceReturnCodesVector(GroupVector.from(c_pC))
				.setPreVoteCastReturnCodesVector(GroupVector.from(pVCC))
				.setLongVoteCastReturnCodesAllowList(L_lVCC)
				.build();
	}

	private GqElement getMessage(final ElGamalMultiRecipientCiphertext ciphertext, final ElGamalMultiRecipientPrivateKey secretKey) {
		final ElGamalMultiRecipientMessage message = elGamalService.getMessage(ciphertext, secretKey);

		checkArgument(message.getElements().size() == 1, "The message must have only one element.");

		return message.getElements().get(0);
	}

}
