/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.configuration.setupvoting;

import static ch.post.it.evoting.cryptoprimitives.domain.VotingOptionsConstants.MAXIMUM_NUMBER_OF_VOTING_OPTIONS;
import static com.google.common.base.Preconditions.checkNotNull;

import org.springframework.stereotype.Service;

import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientKeyPair;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.RandomService;

/**
 * Implements the GenSetupEncryptionKeys algorithm.
 */
@Service
public class GenSetupEncryptionKeysService {

	private static final int OMEGA = MAXIMUM_NUMBER_OF_VOTING_OPTIONS;

	private final RandomService randomService;

	public GenSetupEncryptionKeysService(final RandomService randomService) {
		this.randomService = randomService;
	}

	/**
	 * Generates a key pair to encrypt the partial Choice Return Codes pCC<sub>id</sub> during the configuration phase.
	 *
	 * @param group the {@link GqGroup}. Non-null.
	 * @return the generated key pair in a {@link ElGamalMultiRecipientKeyPair}.
	 * @throws NullPointerException if the group parameter is null.
	 */
	@SuppressWarnings("java:S117")
	public ElGamalMultiRecipientKeyPair genSetupEncryptionKeys(final GqGroup group) {
		checkNotNull(group);

		return ElGamalMultiRecipientKeyPair.genKeyPair(group, OMEGA, randomService);
	}

}
