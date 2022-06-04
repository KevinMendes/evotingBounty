/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.tally.mixonline;

import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.mixnet.VerifiableShuffle;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.VerifiableDecryptions;

class MixDecryptOutput {

	private final VerifiableShuffle verifiableShuffle;
	private final VerifiableDecryptions verifiableDecryptions;
	private final ElGamalMultiRecipientPublicKey remainingElectionPublicKey;

	MixDecryptOutput(final VerifiableShuffle verifiableShuffle, final VerifiableDecryptions verifiableDecryptions,
			final ElGamalMultiRecipientPublicKey remainingElectionPublicKey) {
		this.verifiableShuffle = verifiableShuffle;
		this.verifiableDecryptions = verifiableDecryptions;
		this.remainingElectionPublicKey = remainingElectionPublicKey;
	}

	VerifiableShuffle getVerifiableShuffle() {
		return verifiableShuffle;
	}

	VerifiableDecryptions getVerifiableDecryptions() {
		return verifiableDecryptions;
	}

	ElGamalMultiRecipientPublicKey getRemainingElectionPublicKey() {
		return remainingElectionPublicKey;
	}
}
