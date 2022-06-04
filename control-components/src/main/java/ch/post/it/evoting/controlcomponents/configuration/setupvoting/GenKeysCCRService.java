/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.configuration.setupvoting;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;

import org.springframework.stereotype.Service;

import ch.post.it.evoting.cryptoprimitives.CryptoPrimitivesService;
import ch.post.it.evoting.cryptoprimitives.domain.VotingOptionsConstants;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientKeyPair;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.RandomService;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;

/**
 * Implements the GenKeysCCRj algorithm.
 */
@Service
public class GenKeysCCRService {

	private static final int PHI = VotingOptionsConstants.MAXIMUM_NUMBER_OF_SELECTABLE_VOTING_OPTIONS;
	private final RandomService randomService;

	public GenKeysCCRService(final RandomService randomService) {
		this.randomService = randomService;
	}

	/**
	 * Generates the CCR_j Choice Return Codes encryption key pair and the CCR_j Return Codes Generation secret key.
	 *
	 * @param group the GqGroup in which to generate the keys.
	 * @return the CCR_j Choice Return Codes encryption key pair and the CCR_j Return Codes Generation secret key as a {@link GenKeysCCROutput}.
	 * @throws NullPointerException if the provided group is null.
	 */
	@SuppressWarnings("java:S117")
	public GenKeysCCROutput genKeysCCR(final GqGroup group) {
		checkNotNull(group);

		// Variables.
		final BigInteger q = group.getQ();

		// Operation.
		final ElGamalMultiRecipientKeyPair keyPair = ElGamalMultiRecipientKeyPair.genKeyPair(group, PHI, randomService);
		final ZqElement k_prime_j = ZqElement.create(CryptoPrimitivesService.get().genRandomInteger(q), ZqGroup.sameOrderAs(group));

		return new GenKeysCCROutput(keyPair, k_prime_j);
	}

	/**
	 * Holds the output of the GenKeysCCR algorithm.
	 */
	public static class GenKeysCCROutput {

		private final ElGamalMultiRecipientKeyPair ccrjChoiceReturnCodesEncryptionKeyPair;
		private final ZqElement ccrjReturnCodesGenerationSecretKey;

		public GenKeysCCROutput(final ElGamalMultiRecipientKeyPair ccrjChoiceReturnCodesEncryptionKeyPair,
				final ZqElement ccrjReturnCodesGenerationSecretKey) {

			checkNotNull(ccrjChoiceReturnCodesEncryptionKeyPair);
			checkNotNull(ccrjReturnCodesGenerationSecretKey);

			// Size check.
			checkArgument(ccrjChoiceReturnCodesEncryptionKeyPair.size() == PHI,
					"The ccrj Choice Return Codes encryption key pair must be of size phi. [phi: %s]", PHI);

			// Cross group check.
			checkArgument(ccrjChoiceReturnCodesEncryptionKeyPair.getGroup().hasSameOrderAs(ccrjReturnCodesGenerationSecretKey.getGroup()),
					"The ccrj Return Codes generation secret key must have the same order than the ccr Choice Return Codes encryption key pair.");

			this.ccrjChoiceReturnCodesEncryptionKeyPair = ccrjChoiceReturnCodesEncryptionKeyPair;
			this.ccrjReturnCodesGenerationSecretKey = ccrjReturnCodesGenerationSecretKey;
		}

		public ElGamalMultiRecipientKeyPair getCcrjChoiceReturnCodesEncryptionKeyPair() {
			return ccrjChoiceReturnCodesEncryptionKeyPair;
		}

		public ZqElement getCcrjReturnCodesGenerationSecretKey() {
			return ccrjReturnCodesGenerationSecretKey;
		}
	}

}
