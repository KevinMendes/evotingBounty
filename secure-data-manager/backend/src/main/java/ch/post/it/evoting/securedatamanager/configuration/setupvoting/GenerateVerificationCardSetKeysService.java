/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.configuration.setupvoting;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.springframework.stereotype.Service;

import ch.post.it.evoting.cryptoprimitives.domain.VotingOptionsConstants;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalService;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;

/**
 * Implements the GenVerCardSetKeys algorithm.
 */
@Service
public class GenerateVerificationCardSetKeysService {

	private final ElGamalService elGamalService;

	public GenerateVerificationCardSetKeysService(final ElGamalService elGamalService) {
		this.elGamalService = elGamalService;
	}

	/**
	 * Generates the verification card set keys by combining the CCR<sub>j</sub> Choice Return Codes encryption public keys pk<sub>CCR_j</sub>.
	 *
	 * @param ccrjChoiceReturnCodesEncryptionPublicKeys the Choice Return Codes encryption public keys (pk<sub>CCR_1</sub>, pk<sub>CCR_2</sub>,
	 *                                                  pk<sub>CCR_3</sub>, pk<sub>CCR_4</sub>) as a {@link GroupVector}. Must be non-null.
	 * @return the combined Choice Return Codes encryption public key <b>pk</b><sub>CCR</sub>
	 * @throws NullPointerException     if the ccrjChoiceReturnCodesEncryptionPublicKeys input parameter is null.
	 * @throws IllegalArgumentException if the number of ccrjChoiceReturnCodesEncryptionPublicKeys is different from 4.
	 */
	@SuppressWarnings("java:S117")
	public ElGamalMultiRecipientPublicKey genVerCardSetKeys(
			final GroupVector<ElGamalMultiRecipientPublicKey, GqGroup> ccrjChoiceReturnCodesEncryptionPublicKeys) {
		checkNotNull(ccrjChoiceReturnCodesEncryptionPublicKeys);

		checkArgument(ccrjChoiceReturnCodesEncryptionPublicKeys.size() == 4,
				"There must be exactly 4 CCR_j Choice Return Codes encryption public keys.");

		final int phi = VotingOptionsConstants.MAXIMUM_NUMBER_OF_SELECTABLE_VOTING_OPTIONS;
		checkArgument(ccrjChoiceReturnCodesEncryptionPublicKeys.getElementSize() == phi,
				"The CCR_j Choice Return Codes encryption public keys must be of size %s", phi);
		final GroupVector<ElGamalMultiRecipientPublicKey, GqGroup> pk_CCR_vector = ccrjChoiceReturnCodesEncryptionPublicKeys;

		return elGamalService.combinePublicKeys(pk_CCR_vector);
	}
}
