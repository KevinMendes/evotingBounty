/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.tally.mixoffline;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.exceptions.FailedValidationException;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientKeyPair;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.hashing.HashService;
import ch.post.it.evoting.cryptoprimitives.hashing.TestHashService;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.RandomService;
import ch.post.it.evoting.cryptoprimitives.mixnet.MixnetService;
import ch.post.it.evoting.cryptoprimitives.test.tools.TestGroupSetup;
import ch.post.it.evoting.cryptoprimitives.test.tools.data.GroupTestData;
import ch.post.it.evoting.cryptoprimitives.test.tools.generator.ElGamalGenerator;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ZeroKnowledgeProofService;

class MixDecryptServiceTest extends TestGroupSetup {

	private static String ee;
	private static String bb;
	private static MixDecryptService mixingDecryptionService;
	private static ElGamalGenerator elGamalGenerator;
	private static GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> ciphertexts;
	private static ElGamalMultiRecipientPublicKey remainingElectionPublicKey;
	private static ElGamalMultiRecipientKeyPair electoralBoardKeyPair;
	private static int numCiphertexts;
	private static int ciphertextsSize;
	private static int keySize;
	private static MixnetService mixnet;
	private static ZeroKnowledgeProofService zeroKnowledgeProof;

	@BeforeAll
	static void setUpAll() {
		elGamalGenerator = new ElGamalGenerator(TestGroupSetup.gqGroup);
		keySize = TestGroupSetup.secureRandom.nextInt(10) + 3;
		numCiphertexts = TestGroupSetup.secureRandom.nextInt(10) + 1;
		ciphertextsSize = 1;
		ciphertexts = elGamalGenerator.genRandomCiphertextVector(numCiphertexts, ciphertextsSize);
		final ElGamalMultiRecipientKeyPair keyPair = ElGamalMultiRecipientKeyPair.genKeyPair(TestGroupSetup.gqGroup, keySize, new RandomService());
		remainingElectionPublicKey = keyPair.getPublicKey();
		electoralBoardKeyPair = keyPair;
		ee = "0d31a1148f95488fae6827391425dc08";
		bb = "f8ba3dd3844a4815af39c63570c12006";

		final HashService hashService = TestHashService.create(TestGroupSetup.gqGroup.getQ());
		mixnet = new MixnetService(hashService);
		zeroKnowledgeProof = new ZeroKnowledgeProofService(new RandomService(), hashService);
		mixingDecryptionService = new MixDecryptService(mixnet, zeroKnowledgeProof);
	}

	@Test
	void testThatYouCantCreateAMixDecryptServiceWithNulls() {
		assertAll(() -> assertThrows(NullPointerException.class, () -> new MixDecryptService(null, zeroKnowledgeProof)),
				() -> assertThrows(NullPointerException.class, () -> new MixDecryptService(mixnet, null)));
	}

	@Test
	void testThatYouCantMixDecryptWithNulls() {
		assertAll(() -> assertThrows(NullPointerException.class,
						() -> mixingDecryptionService.mixDecryptOffline(null, remainingElectionPublicKey, electoralBoardKeyPair, ee, bb)),
				() -> assertThrows(NullPointerException.class,
						() -> mixingDecryptionService.mixDecryptOffline(ciphertexts, null, electoralBoardKeyPair, ee, bb)),
				() -> assertThrows(NullPointerException.class,
						() -> mixingDecryptionService.mixDecryptOffline(ciphertexts, remainingElectionPublicKey, null, ee, bb)),
				() -> assertThrows(NullPointerException.class,
						() -> mixingDecryptionService.mixDecryptOffline(ciphertexts, remainingElectionPublicKey, electoralBoardKeyPair, null, bb)),
				() -> assertThrows(NullPointerException.class,
						() -> mixingDecryptionService.mixDecryptOffline(ciphertexts, remainingElectionPublicKey, electoralBoardKeyPair, ee, null)));
	}

	@Test
	void testThatDifferentGroupsThrow() {
		final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> otherCiphertexts = new ElGamalGenerator(TestGroupSetup.otherGqGroup)
				.genRandomCiphertextVector(numCiphertexts, ciphertextsSize);
		assertThrows(IllegalArgumentException.class,
				() -> mixingDecryptionService.mixDecryptOffline(otherCiphertexts, remainingElectionPublicKey, electoralBoardKeyPair, ee, bb));

		final ElGamalMultiRecipientKeyPair otherKeyPair = ElGamalMultiRecipientKeyPair.genKeyPair(TestGroupSetup.otherGqGroup, keySize,
				new RandomService());
		final ElGamalMultiRecipientPublicKey otherElectionPk = otherKeyPair.getPublicKey();
		assertThrows(IllegalArgumentException.class,
				() -> mixingDecryptionService.mixDecryptOffline(ciphertexts, otherElectionPk, electoralBoardKeyPair, ee, bb));

		assertThrows(IllegalArgumentException.class,
				() -> mixingDecryptionService.mixDecryptOffline(ciphertexts, remainingElectionPublicKey, otherKeyPair, ee, bb));
	}

	@Test
	void testThatDifferentSizesKeysThrows() {
		final ElGamalMultiRecipientPublicKey otherElectionPk = elGamalGenerator.genRandomPublicKey(keySize + 1);
		assertThrows(IllegalArgumentException.class,
				() -> mixingDecryptionService.mixDecryptOffline(ciphertexts, otherElectionPk, electoralBoardKeyPair, ee, bb));
	}

	@Test
	void testThatEmptyCiphertextsThrows() {
		final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> empty = elGamalGenerator.genRandomCiphertextVector(0, ciphertextsSize);
		assertThrows(IllegalArgumentException.class,
				() -> mixingDecryptionService.mixDecryptOffline(empty, remainingElectionPublicKey, electoralBoardKeyPair, ee, bb));
	}

	@Test
	void testThatElectionPublicKeyAndElectoralBoardPublicKeyAreEqual() {
		final ElGamalMultiRecipientPublicKey otherElectionPk = elGamalGenerator.genRandomPublicKey(keySize);
		assertThrows(IllegalArgumentException.class,
				() -> mixingDecryptionService.mixDecryptOffline(ciphertexts, otherElectionPk, electoralBoardKeyPair, ee, bb));
	}

	@Test
	void testThatResultContainsNoVerifiableShuffleIfOnlyOneCiphertext() {
		final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> oneCiphertext = elGamalGenerator.genRandomCiphertextVector(1, ciphertextsSize);
		final MixDecryptService.Result result = mixingDecryptionService
				.mixDecryptOffline(oneCiphertext, remainingElectionPublicKey, electoralBoardKeyPair, ee, bb);
		assertFalse(result.getVerifiableShuffle().isPresent());
		assertNotNull(result.getVerifiablePlaintextDecryption());
		assertNotNull(result.getVerifiablePlaintextDecryption());
	}

	@Test
	void testThatResultContainsVerifiableShuffleForMoreThanOneCiphertext() {
		final GqGroup largeGroup = GroupTestData.getLargeGqGroup();
		final ElGamalGenerator largeElGamalGenerator = new ElGamalGenerator(largeGroup);
		final ElGamalMultiRecipientKeyPair largeElectoralBoardKeyPair = ElGamalMultiRecipientKeyPair.genKeyPair(largeGroup, keySize,
				new RandomService());

		final int numCiphertexts = TestGroupSetup.secureRandom.nextInt(5) + 2;
		final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> moreThanOneCiphertexts = largeElGamalGenerator
				.genRandomCiphertextVector(numCiphertexts, ciphertextsSize);
		final MixDecryptService.Result result = mixingDecryptionService
				.mixDecryptOffline(moreThanOneCiphertexts, largeElectoralBoardKeyPair.getPublicKey(), largeElectoralBoardKeyPair, ee, bb);
		assertTrue(result.getVerifiableShuffle().isPresent());
		assertNotNull(result.getVerifiablePlaintextDecryption());
		assertNotNull(result.getVerifiablePlaintextDecryption());
	}

	@Test
	void testThatInvalidUUIDThrows() {
		final String invalidUUID = "0d31a1148f95488fae6827391425dc0X";
		assertThrows(FailedValidationException.class,
				() -> mixingDecryptionService.mixDecryptOffline(ciphertexts, remainingElectionPublicKey, electoralBoardKeyPair, invalidUUID, bb));
		assertThrows(FailedValidationException.class,
				() -> mixingDecryptionService.mixDecryptOffline(ciphertexts, remainingElectionPublicKey, electoralBoardKeyPair, ee, invalidUUID));
	}
}
