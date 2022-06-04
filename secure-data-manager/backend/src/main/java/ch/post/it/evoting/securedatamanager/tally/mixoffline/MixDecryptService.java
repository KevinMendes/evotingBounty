/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.tally.mixoffline;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;
import static ch.post.it.evoting.cryptoprimitives.math.GroupVector.toGroupVector;
import static ch.post.it.evoting.cryptoprimitives.utils.Validations.allEqual;
import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Optional;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.VerifiablePlaintextDecryption;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientKeyPair;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientMessage;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;
import ch.post.it.evoting.cryptoprimitives.mixnet.Mixnet;
import ch.post.it.evoting.cryptoprimitives.mixnet.VerifiableShuffle;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.DecryptionProof;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.VerifiableDecryptions;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ZeroKnowledgeProof;

/**
 * Service to mix and decrypt a collection of encrypted votes.
 */
@Service
class MixDecryptService {

	private final Mixnet mixnet;
	private final ZeroKnowledgeProof zeroKnowledgeProof;

	@Autowired
	MixDecryptService(final Mixnet mixnet, final ZeroKnowledgeProof zeroKnowledgeProof) {
		checkNotNull(mixnet);
		checkNotNull(zeroKnowledgeProof);
		this.mixnet = mixnet;
		this.zeroKnowledgeProof = zeroKnowledgeProof;
	}

	/**
	 * Re-encrypts and shuffles the provided ciphertexts and performs the final decryption.
	 * <p>
	 * Corresponds to algorithm MixDecOffline.
	 *
	 * <p>
	 * Arguments must abide by the additional cross argument conditions:
	 * <ul>
	 *     <li>all arguments must come from the same group</li>
	 *     <li>the {@code remainingElectionPublicKey} and {@code electoralBoardKeyPair} must be the same size</li>
	 *     <li>the {@code remainingElectionPublicKey} and {@code electoralBoardKeyPair} public key must be equal</li>
	 * </ul>
	 *
	 * @param ciphertexts                c<sub>dec,3</sub>, the ciphertexts to shuffle and decrypt. Not null, not empty.
	 * @param remainingElectionPublicKey EL<sub>pk,3</sub>, the public key to use for re-encrypting during shuffling. Not null.
	 * @param electoralBoardKeyPair      (EB<sub>pk</sub>, EB<sub>sk</sub>) the key pair to use to decrypt the votes. Not null.
	 * @param electionEventId            ee, the election event id. Not null. Must be a valid UUID.
	 * @param ballotBoxId                bb, the ballot box id. Not null. Must be a valid UUID.
	 * @return a {@link Result} with the results
	 */
	Result mixDecryptOffline(final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> ciphertexts,
			final ElGamalMultiRecipientPublicKey remainingElectionPublicKey, final ElGamalMultiRecipientKeyPair electoralBoardKeyPair,
			final String electionEventId, final String ballotBoxId) {

		final int Nc = ciphertexts.size();
		final int l = ciphertexts.getElementSize();
		final int deltaHat = 1; // Currently, we do not support write-ins.
		final int delta = remainingElectionPublicKey.size();

		checkNotNull(ciphertexts);
		checkNotNull(remainingElectionPublicKey);
		checkNotNull(electoralBoardKeyPair);
		checkNotNull(electionEventId);
		checkNotNull(ballotBoxId);

		//Ensure statements
		//These are done before to exclude the case where the ciphertexts are empty
		checkArgument(Nc >= 1, "There must be at least one partially decrypted vote.");
		checkArgument(remainingElectionPublicKey.equals(electoralBoardKeyPair.getPublicKey()),
				"The remaining public key must be equal to the electoral board public key.");
		checkArgument(l == deltaHat, "The ciphertexts size must be equal to the number of allowed write-ins + 1.");
		checkArgument(l <= delta, "Ciphertexts must be smaller than the key size.");

		//Group checks
		final ImmutableList<GqGroup> groups = ImmutableList
				.of(ciphertexts.getGroup(), remainingElectionPublicKey.getGroup(), electoralBoardKeyPair.getGroup());
		checkArgument(allEqual(groups.stream(), Function.identity()),
				"The partially decrypted votes, remaining public key and electoral board key must belong to the same encryption group.");

		//Size checks
		checkArgument(delta == electoralBoardKeyPair.size(), "Remaining public key and electoral board key must have the same size.");
		checkArgument(electionEventId.length() == ballotBoxId.length(), "The election event id and ballot box id must have the same length.");

		//Validate UUIDs
		validateUUID(electionEventId);
		validateUUID(ballotBoxId);

		//Algorithm
		final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> c = ciphertexts;
		final ElGamalMultiRecipientPublicKey EL = remainingElectionPublicKey;
		final ElGamalMultiRecipientKeyPair EB = electoralBoardKeyPair;
		final String ee = electionEventId;
		final String bb = ballotBoxId;

		final ImmutableList<String> iAux = ImmutableList.of(ee, bb, "MixDecOffline");

		final VerifiableShuffle verifiableShuffle;
		final VerifiableDecryptions verifiableDecryptions;
		if (Nc > 1) {
			verifiableShuffle = mixnet.genVerifiableShuffle(c, EL);
			verifiableDecryptions = zeroKnowledgeProof.genVerifiableDecryptions(verifiableShuffle.getShuffledCiphertexts(), EB, iAux);
		} else {
			verifiableShuffle = null;
			verifiableDecryptions = zeroKnowledgeProof.genVerifiableDecryptions(c, EB, iAux);
		}

		final GroupVector<ElGamalMultiRecipientMessage, GqGroup> m = verifiableDecryptions.getCiphertexts()
				.stream()
				.map(ElGamalMultiRecipientCiphertext::getPhi)
				.map(ElGamalMultiRecipientMessage::new)
				.collect(toGroupVector());

		return new Result(verifiableShuffle, m, verifiableDecryptions.getDecryptionProofs());

	}

	/**
	 * Value class containing the result of a re-encrypting shuffle and decryption. It is composed of:
	 * <ul>
	 * 		<li>a verifiable shuffle. This is absent if there was one or less votes to shuffle</li>
	 * 		<li>a verifiable plaintext decryption.</li>
	 * </ul>
	 */
	static class Result {

		private final VerifiableShuffle verifiableShuffle;
		private final VerifiablePlaintextDecryption verifiablePlaintextDecryption;

		@VisibleForTesting
		Result(final VerifiableShuffle verifiableShuffle, final GroupVector<ElGamalMultiRecipientMessage, GqGroup> decryptedVotes,
				final GroupVector<DecryptionProof, ZqGroup> decryptionProofs) {
			this.verifiableShuffle = verifiableShuffle;
			this.verifiablePlaintextDecryption = new VerifiablePlaintextDecryption(decryptedVotes, decryptionProofs);
		}

		public Optional<VerifiableShuffle> getVerifiableShuffle() {
			return Optional.ofNullable(verifiableShuffle);
		}

		public VerifiablePlaintextDecryption getVerifiablePlaintextDecryption() {
			return verifiablePlaintextDecryption;
		}
	}
}
