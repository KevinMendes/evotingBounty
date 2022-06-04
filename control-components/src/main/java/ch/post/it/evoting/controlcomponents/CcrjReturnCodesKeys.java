/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientKeyPair;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;

public final class CcrjReturnCodesKeys {

	private final String electionEventId;
	private final ZqElement ccrjReturnCodesGenerationSecretKey;
	private final ElGamalMultiRecipientKeyPair ccrjChoiceReturnCodesEncryptionKeyPair;

	public CcrjReturnCodesKeys(final String electionEventId, final ZqElement ccrjReturnCodesGenerationSecretKey,
			final ElGamalMultiRecipientKeyPair ccrjChoiceReturnCodesEncryptionKeyPair) {

		validateUUID(electionEventId);
		checkNotNull(ccrjReturnCodesGenerationSecretKey);
		checkNotNull(ccrjChoiceReturnCodesEncryptionKeyPair);

		checkArgument(ccrjReturnCodesGenerationSecretKey.getGroup().hasSameOrderAs(ccrjChoiceReturnCodesEncryptionKeyPair.getGroup()),
				"The generation secret key must have the same order as the encryption key pair.");

		this.electionEventId = electionEventId;
		this.ccrjReturnCodesGenerationSecretKey = ccrjReturnCodesGenerationSecretKey;
		this.ccrjChoiceReturnCodesEncryptionKeyPair = ccrjChoiceReturnCodesEncryptionKeyPair;
	}

	public String getElectionEventId() {
		return electionEventId;
	}

	public ZqElement getCcrjReturnCodesGenerationSecretKey() {
		return ccrjReturnCodesGenerationSecretKey;
	}

	public ElGamalMultiRecipientKeyPair getCcrjChoiceReturnCodesEncryptionKeyPair() {
		return ccrjChoiceReturnCodesEncryptionKeyPair;
	}
}
