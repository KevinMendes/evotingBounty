/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.tally.mixonline;

import static ch.post.it.evoting.cryptoprimitives.utils.ConversionService.integerToString;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientKeyPair;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPrivateKey;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.mixnet.Mixnet;
import ch.post.it.evoting.cryptoprimitives.mixnet.VerifiableShuffle;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.VerifiableDecryptions;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ZeroKnowledgeProof;

@Service
public class MixDecryptOnlineService {

	private final Mixnet mixnet;
	private final ZeroKnowledgeProof zeroKnowledgeProof;
	private final Integer nodeID;

	public MixDecryptOnlineService(final Mixnet mixnet, final ZeroKnowledgeProof zeroKnowledgeProof,
			@Value("${nodeID}")
			final Integer nodeID) {
		this.mixnet = mixnet;
		this.zeroKnowledgeProof = zeroKnowledgeProof;
		this.nodeID = nodeID;
	}

	/**
	 * Mixes and partially decrypts ciphertexts, providing proofs of knowledge for the shuffle and the decryption.
	 *
	 * @param ciphertexts                c<sub>dec,j-1</sub>, a list of input ciphertexts
	 * @param remainingElectionPublicKey ELbar<sub>pk,j-1</sub>, the remaining election public key after mixing and decryption by CCM<sub>j-1</sub>
	 * @param ccmElectionKeyPair         (EL<sub>pk,j-1</sub>, EL<sub>sk,j-1</sub>), control component j's key pair
	 * @return the output of the mixing and decryption as a list of mixed ciphertexts, a shuffle proof, a list of partially decrypted ciphertexts, a
	 * list of decryption proofs and the remaining election public key
	 */
	MixDecryptOutput mixDecOnline(final String electionEventID, final String balloBoxID, final List<ElGamalMultiRecipientCiphertext> ciphertexts,
			final ElGamalMultiRecipientPublicKey remainingElectionPublicKey, final ElGamalMultiRecipientKeyPair ccmElectionKeyPair) {
		final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> cDec = GroupVector.from(ciphertexts);
		final ElGamalMultiRecipientPublicKey ELbarPk = checkNotNull(remainingElectionPublicKey);
		checkNotNull(ccmElectionKeyPair);
		final ElGamalMultiRecipientPrivateKey ELsk = ccmElectionKeyPair.getPrivateKey();

		final int Nc = cDec.size();
		final int l = cDec.getElementSize();
		final int deltaHat = 1; // Currently, we do not support write-ins.
		final int delta = ELbarPk.size();
		final int mu = ELsk.size();
		checkArgument(Nc >= 1, "There must be at least one ciphertext.");
		checkArgument(l == deltaHat, "The ciphertexts size must be equal to the number of allowed write-ins + 1.");
		checkArgument(l <= delta, "The ciphertexts must not be longer than the remaining election public key.");
		checkArgument(delta <= mu, "The remaining election public key must not be longer than the control component public key.");
		checkArgument(cDec.getGroup().equals(ELbarPk.getGroup()),
				"The ciphertexts to be decrypted must have the same group as the remaining election public key.");
		checkArgument(ELbarPk.getGroup().equals(ccmElectionKeyPair.getPublicKey().getGroup()),
				"The remaining election public key and the ccm election key must have the same group.");

		// Algorithm.
		final List<String> iAux = Arrays.asList(electionEventID, balloBoxID, "MixDecOnline", integerToString(nodeID));
		VerifiableShuffle shuffle = null;
		final VerifiableDecryptions decryptions;
		if (Nc > 1) {
			shuffle = mixnet.genVerifiableShuffle(cDec, ELbarPk);
			decryptions = zeroKnowledgeProof.genVerifiableDecryptions(shuffle.getShuffledCiphertexts(), ccmElectionKeyPair, iAux);
		} else {
			decryptions = zeroKnowledgeProof.genVerifiableDecryptions(cDec, ccmElectionKeyPair, iAux);
		}
		final ElGamalMultiRecipientPublicKey ELprimePk = ccmElectionKeyPair.getPublicKey();
		final ElGamalMultiRecipientPublicKey newRemainingElectionPublicKey = IntStream.range(0, ELbarPk.size())
				.mapToObj(i -> ELbarPk.get(i).multiply(ELprimePk.get(i).invert()))
				.collect(Collectors.collectingAndThen(Collectors.toList(), ElGamalMultiRecipientPublicKey::new));

		return new MixDecryptOutput(shuffle, decryptions, newRemainingElectionPublicKey);
	}
}
