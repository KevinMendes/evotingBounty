/*
 *  (c) Copyright 2022 Swiss Post Ltd.
 */

package ch.post.it.evoting.securedatamanager.tally.mixoffline;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.VerifiablePlaintextDecryption;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientMessage;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;
import ch.post.it.evoting.cryptoprimitives.mixnet.VerifiableShuffle;
import ch.post.it.evoting.cryptoprimitives.mixnet.VerifiableShuffleGenerator;
import ch.post.it.evoting.cryptoprimitives.test.tools.generator.ElGamalGenerator;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.DecryptionProof;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.DecryptionProofGenerator;

public class MixDecryptResultGenerator {

	private final GqGroup group;

	MixDecryptResultGenerator(final GqGroup group) {
		this.group = group;
	}

	MixDecryptService.Result genMixDecryptResult(final int numCiphertexts, final int ciphertextSize) {
		final VerifiableShuffle shuffle = new VerifiableShuffleGenerator(group).genVerifiableShuffle(numCiphertexts, ciphertextSize);
		final VerifiablePlaintextDecryption plaintextDecryption = genVerifiablePlaintextDecryption(numCiphertexts, ciphertextSize);
		return new MixDecryptService.Result(shuffle, plaintextDecryption.getDecryptedVotes(), plaintextDecryption.getDecryptionProofs());
	}

	VerifiablePlaintextDecryption genVerifiablePlaintextDecryption(final int numMessages, final int messageSize) {
		final GroupVector<ElGamalMultiRecipientMessage, GqGroup> decryptedVotes = new ElGamalGenerator(group)
				.genRandomMessageVector(numMessages, messageSize);
		final GroupVector<DecryptionProof, ZqGroup> decryptionProofs = new DecryptionProofGenerator(ZqGroup.sameOrderAs(group))
				.genDecryptionProofVector(numMessages, messageSize);
		return new VerifiablePlaintextDecryption(decryptedVotes, decryptionProofs);
	}

}
