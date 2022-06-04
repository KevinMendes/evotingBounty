/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;
import static com.google.common.base.Preconditions.checkNotNull;

import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;

public class VerificationCard {

	private final String verificationCardId;
	private final String verificationCardSetId;
	private final ElGamalMultiRecipientPublicKey verificationCardPublicKey;

	public VerificationCard(final String verificationCardId, final String verificationCardSetId,
			final ElGamalMultiRecipientPublicKey verificationCardPublicKey) {
		this.verificationCardId = validateUUID(verificationCardId);
		this.verificationCardSetId = verificationCardSetId;
		this.verificationCardPublicKey = checkNotNull(verificationCardPublicKey);
	}

	public String getVerificationCardId() {
		return verificationCardId;
	}

	public String getVerificationCardSetId() {
		return verificationCardSetId;
	}

	public ElGamalMultiRecipientPublicKey getVerificationCardPublicKey() {
		return verificationCardPublicKey;
	}

}
