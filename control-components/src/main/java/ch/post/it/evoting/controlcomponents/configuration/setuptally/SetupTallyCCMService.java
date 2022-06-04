/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.configuration.setuptally;

import static ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientKeyPair.genKeyPair;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import org.springframework.stereotype.Service;

import ch.post.it.evoting.cryptoprimitives.domain.VotingOptionsConstants;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientKeyPair;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.RandomService;

/**
 * Implements the SetupTallyCCM<sub>j</sub> algorithm.
 */
@Service
public class SetupTallyCCMService {
	private static final int MU = VotingOptionsConstants.MAXIMUM_NUMBER_OF_WRITE_IN_OPTIONS + 1;

	private final RandomService randomService;

	public SetupTallyCCMService(final RandomService randomService) {
		this.randomService = randomService;
	}

	/**
	 * Generates the CCM<sub>j</sub> election key pair (EL<sub>pk,j</sub>, EL<sub>sk,j</sub>).
	 *
	 * @param group the GqGroup in which to generate the keys. Non-null.
	 * @return (EL<sub>pk,j</sub>, EL<sub>sk,j</sub>), the CCM<sub>j</sub> election key pair.
	 * @throws NullPointerException if the provided group is null.
	 * @throws IllegalArgumentException if μ, the maximum number of write-ins + 1, is not strictly positive.
	 */
	public ElGamalMultiRecipientKeyPair setupTallyCCM(final GqGroup group) {
		checkNotNull(group);
		checkArgument(MU > 0, "Mu must be strictly positive.");

		// Operation.
		return genKeyPair(group, MU, randomService);
	}
}
