/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.configuration.setupvoting;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import com.google.common.base.Throwables;

import ch.post.it.evoting.controlcomponents.VerificationCardService;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.exceptions.FailedValidationException;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientKeyPair;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.RandomService;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;
import ch.post.it.evoting.cryptoprimitives.test.tools.TestGroupSetup;
import ch.post.it.evoting.cryptoprimitives.test.tools.data.GroupTestData;
import ch.post.it.evoting.cryptoprimitives.test.tools.generator.ElGamalGenerator;
import ch.post.it.evoting.cryptoprimitives.utils.KDFService;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ExponentiationProof;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ZeroKnowledgeProof;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ZeroKnowledgeProofService;

@DisplayName("A GenEncLongCodeSharesService")
class GenEncLongCodeSharesServiceTest extends TestGroupSetup {

	private static final int NUM_KEY_ELEMENTS = 5;
	private static final int CIPHERTEXT_SIZE = 1; // Currently, we do not support write-ins.

	private static final String ELECTION_EVENT_ID = "0b88257ec32142bb8ee0ed1bb70f362e";
	private static final String VERIFICATION_CARD_SET_ID = "f0dd956605bb47d589f1bd7b195d6f38";
	private static final List<String> VERIFICATION_CARD_IDS = Arrays.asList("e3318008e47d439a92577fcb2c738192", "4f51188102c2421385d250bf48b8b8dd",
			"9b5be5f5068a499d9998d48cb394aee1");

	private static final GqGroup GQ_GROUP = gqGroup;
	private static final int NODE_ID = 1;

	private static final ElGamalGenerator elGamalGenerator = new ElGamalGenerator(GQ_GROUP);
	private static final ElGamalGenerator elGamalGenerator2 = new ElGamalGenerator(GroupTestData.getDifferentGqGroup(GQ_GROUP));

	private static ZqElement returnCodesGenerationSecretKey;
	private static List<String> verificationCardIDs;
	private static List<ElGamalMultiRecipientPublicKey> verificationCardPublicKeys;
	private static GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> encryptedHashedPartialChoiceReturnCodes;
	private static GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> encryptedHashedConfirmationKeys;

	private final ZeroKnowledgeProof zeroKnowledgeProofService = mock(ZeroKnowledgeProofService.class);
	private final KDFService kdfService = mock(KDFService.class);
	private final VerificationCardService verificationCardService = mock(VerificationCardService.class);
	private final RandomService randomService = new RandomService();
	private final ElGamalMultiRecipientKeyPair ccmKeyPair = ElGamalMultiRecipientKeyPair.genKeyPair(GQ_GROUP, NUM_KEY_ELEMENTS, randomService);

	private GenEncLongCodeSharesService genEncLongCodeSharesService;
	private GenEncLongCodeSharesContext context;
	private GenEncLongCodeSharesInput input;

	@BeforeEach
	void setup() {
		genEncLongCodeSharesService = new GenEncLongCodeSharesService(kdfService, zeroKnowledgeProofService, verificationCardService);

		returnCodesGenerationSecretKey = zqGroupGenerator.genRandomZqElementMember();
		verificationCardIDs = VERIFICATION_CARD_IDS;
		verificationCardPublicKeys = Arrays.asList(ccmKeyPair.getPublicKey(), ccmKeyPair.getPublicKey(), ccmKeyPair.getPublicKey());
		encryptedHashedPartialChoiceReturnCodes = elGamalGenerator.genRandomCiphertextVector(3, 1);
		encryptedHashedConfirmationKeys = elGamalGenerator.genRandomCiphertextVector(3, 1);

		context = new GenEncLongCodeSharesContext.Builder()
				.electionEventId(ELECTION_EVENT_ID)
				.verificationCardSetId(VERIFICATION_CARD_SET_ID)
				.gqGroup(GQ_GROUP)
				.nodeID(NODE_ID)
				.build();

		input = new GenEncLongCodeSharesInput.Builder()
				.returnCodesGenerationSecretKey(returnCodesGenerationSecretKey)
				.verificationCardIDs(verificationCardIDs)
				.verificationCardPublicKeys(verificationCardPublicKeys)
				.encryptedHashedPartialChoiceReturnCodes(encryptedHashedPartialChoiceReturnCodes)
				.encryptedHashedConfirmationKeys(encryptedHashedConfirmationKeys)
				.build();

		when(verificationCardService.exist(any())).thenReturn(false);
		when(kdfService.KDFToZq(any(), any(), any())).thenReturn(zqGroupGenerator.genRandomZqElementMember());
		when(zeroKnowledgeProofService.genExponentiationProof(any(), any(), any(), any()))
				.thenReturn(new ExponentiationProof(zqGroupGenerator.genRandomZqElementMember(), zqGroupGenerator.genRandomZqElementMember()));
	}

	@Test
	@DisplayName("valid parameters does not throw")
	void validParamDoesNotThrow() {
		assertDoesNotThrow(() -> genEncLongCodeSharesService.genEncLongCodeShares(context, input));

		verify(verificationCardService, /*atLeastOnce()*/times(1)).exist(any());
		verify(verificationCardService, times(1)).saveAll(any());
	}

	@Test
	@DisplayName("null parameter throws NullPointerException")
	void nullParamThrows() {
		assertThrows(NullPointerException.class, () -> genEncLongCodeSharesService.genEncLongCodeShares(null, null));
		assertThrows(NullPointerException.class, () -> genEncLongCodeSharesService.genEncLongCodeShares(context, null));
		assertThrows(NullPointerException.class, () -> genEncLongCodeSharesService.genEncLongCodeShares(null, input));
	}

	@Test
	@DisplayName("parameters already been generated voting cards IllegalArgumentException")
	void alreadyGeneratedVotingCardThrow() {
		when(verificationCardService.exist(any())).thenReturn(true);
		IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> genEncLongCodeSharesService.genEncLongCodeShares(context, input));
		assertEquals("Voting cards have already been generated.", Throwables.getRootCause(exception).getMessage());

	}

	@Nested
	@DisplayName("A GenEncLongCodeSharesContext built with")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class GenEncLongCodeSharesContextTest {

		@Test
		@DisplayName("null electionEventId parameter throws NullPointerException")
		void electionEventId_nullParamThrows() {
			final GenEncLongCodeSharesContext.Builder builder = new GenEncLongCodeSharesContext.Builder()
					.electionEventId(null)
					.verificationCardSetId(VERIFICATION_CARD_SET_ID)
					.gqGroup(GQ_GROUP)
					.nodeID(NODE_ID);

			assertThrows(NullPointerException.class, builder::build);
		}

		@Test
		@DisplayName("invalid electionEventId parameter throws FailedValidationException")
		void electionEventId_invalidParamThrows() {
			GenEncLongCodeSharesContext.Builder builder = new GenEncLongCodeSharesContext.Builder()
					.electionEventId("")
					.verificationCardSetId(VERIFICATION_CARD_SET_ID)
					.gqGroup(GQ_GROUP)
					.nodeID(NODE_ID);

			assertThrows(FailedValidationException.class, builder::build);

			builder = builder.electionEventId("0b88257ec32142b");
			assertThrows(FailedValidationException.class, builder::build);
		}

		@Test
		@DisplayName("null verificationCardSetId parameter throws NullPointerException")
		void verificationCardSetId_nullParamThrows() {
			final GenEncLongCodeSharesContext.Builder builder = new GenEncLongCodeSharesContext.Builder()
					.electionEventId(ELECTION_EVENT_ID)
					.verificationCardSetId(null)
					.gqGroup(GQ_GROUP)
					.nodeID(NODE_ID);

			assertThrows(NullPointerException.class, builder::build);
		}

		@Test
		@DisplayName("invalid verificationCardSetId parameter throws FailedValidationException")
		void verificationCardSetId_invalidParamThrows() {
			GenEncLongCodeSharesContext.Builder builder = new GenEncLongCodeSharesContext.Builder()
					.electionEventId(ELECTION_EVENT_ID)
					.verificationCardSetId("")
					.gqGroup(GQ_GROUP)
					.nodeID(NODE_ID);

			assertThrows(FailedValidationException.class, builder::build);

			builder = builder.verificationCardSetId("f1bd7b195d6f38");

			assertThrows(FailedValidationException.class, builder::build);
		}

		@Test
		@DisplayName("null gqGroup parameter throws NullPointerException")
		void gqGroup_nullParamThrows() {
			final GenEncLongCodeSharesContext.Builder builder = new GenEncLongCodeSharesContext.Builder()
					.electionEventId(ELECTION_EVENT_ID)
					.verificationCardSetId(VERIFICATION_CARD_SET_ID)
					.gqGroup(null)
					.nodeID(NODE_ID);

			assertThrows(NullPointerException.class, builder::build);
		}

		@Test
		@DisplayName("valid param gives expected context")
		void expectInput() {
			GenEncLongCodeSharesContext.Builder builder = new GenEncLongCodeSharesContext.Builder()
					.electionEventId(ELECTION_EVENT_ID)
					.verificationCardSetId(VERIFICATION_CARD_SET_ID)
					.gqGroup(GQ_GROUP)
					.nodeID(NODE_ID);

			assertDoesNotThrow(builder::build);
		}
	}

	@Nested
	@DisplayName("A GenEncLongCodeSharesInput built with")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class GenEncLongCodeSharesInputTest {

		@Test
		@DisplayName("null input parameter throws NullPointerException")
		void nullInputParams_Throws() {
			GenEncLongCodeSharesInput.Builder builder =
					new GenEncLongCodeSharesInput.Builder()
							.returnCodesGenerationSecretKey(null)
							.verificationCardIDs(null)
							.verificationCardPublicKeys(null)
							.encryptedHashedPartialChoiceReturnCodes(null)
							.encryptedHashedConfirmationKeys(null);

			NullPointerException exception = assertThrows(NullPointerException.class, builder::build);

			assertEquals("The CCRj Return Codes Generation Secret Key is null.",
					Throwables.getRootCause(exception).getMessage());

			builder = builder.returnCodesGenerationSecretKey(returnCodesGenerationSecretKey);

			exception = assertThrows(NullPointerException.class, builder::build);

			assertEquals("The vector verification Card IDs is null.",
					Throwables.getRootCause(exception).getMessage());

			builder = builder.verificationCardIDs(verificationCardIDs);

			exception = assertThrows(NullPointerException.class, builder::build);

			assertEquals("The vector verification Card Public Keys is null.",
					Throwables.getRootCause(exception).getMessage());

			builder = builder.verificationCardPublicKeys(verificationCardPublicKeys);

			exception = assertThrows(NullPointerException.class, builder::build);

			assertEquals("The vector encrypted, hashed partial Choice Return Codes is null.",
					Throwables.getRootCause(exception).getMessage());

			builder = builder.encryptedHashedPartialChoiceReturnCodes(encryptedHashedPartialChoiceReturnCodes);

			exception = assertThrows(NullPointerException.class, builder::build);

			assertEquals("The vector encrypted, hashed Confirmation Keys is null.",
					Throwables.getRootCause(exception).getMessage());
		}

		@Test
		@DisplayName("check sizes throws IllegalArgumentException")
		void checkSizes_Throws() {
			final int N_E = verificationCardIDs.size();
			final List<ElGamalMultiRecipientPublicKey> verificationCardPublicKeys_plus1 =
					Arrays.asList(ccmKeyPair.getPublicKey(), ccmKeyPair.getPublicKey(), ccmKeyPair.getPublicKey(), ccmKeyPair.getPublicKey());

			GenEncLongCodeSharesInput.Builder builder =
					new GenEncLongCodeSharesInput.Builder()
							.returnCodesGenerationSecretKey(returnCodesGenerationSecretKey)
							.verificationCardIDs(verificationCardIDs)
							.verificationCardPublicKeys(verificationCardPublicKeys_plus1)
							.encryptedHashedPartialChoiceReturnCodes(encryptedHashedPartialChoiceReturnCodes)
							.encryptedHashedConfirmationKeys(encryptedHashedConfirmationKeys);

			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, builder::build);

			String message = String.format("The vector verification Card Public Keys is of incorrect size [size: expected: %S, actual: %S]",
					N_E, verificationCardPublicKeys_plus1.size());
			assertEquals(message, Throwables.getRootCause(exception).getMessage());

			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> encryptedHashedPartialChoiceReturnCodes_plus1 =
					elGamalGenerator.genRandomCiphertextVector(4, 1);

			builder = builder.verificationCardPublicKeys(verificationCardPublicKeys)
					.encryptedHashedPartialChoiceReturnCodes(encryptedHashedPartialChoiceReturnCodes_plus1);

			exception = assertThrows(IllegalArgumentException.class, builder::build);

			message = String.format("The vector encrypted, hashed partial Choice Return Codes is of incorrect size [size: expected: %S, actual: %S]",
					verificationCardIDs.size(), verificationCardPublicKeys_plus1.size());
			assertEquals(message, Throwables.getRootCause(exception).getMessage());

			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> encryptedHashedConfirmationKeys_plus1 =
					elGamalGenerator.genRandomCiphertextVector(4, 1);

			builder = builder.encryptedHashedPartialChoiceReturnCodes(encryptedHashedPartialChoiceReturnCodes)
					.encryptedHashedConfirmationKeys(encryptedHashedConfirmationKeys_plus1);

			exception = assertThrows(IllegalArgumentException.class, builder::build);

			message = String.format("The vector encrypted, hashed Confirmation Keys is of incorrect size [size: expected: %S, actual: %S]",
					N_E, encryptedHashedConfirmationKeys_plus1.size());
			assertEquals(message, Throwables.getRootCause(exception).getMessage());
		}

		@Test
		@DisplayName("cross group checks throws IllegalArgumentException")
		void crossGroupChecks_Throws() {

			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> encryptedHashedConfirmationKeys_grp2 =
					elGamalGenerator2.genRandomCiphertextVector(3, 1);

			final GenEncLongCodeSharesInput.Builder builder =
					new GenEncLongCodeSharesInput.Builder()
							.returnCodesGenerationSecretKey(returnCodesGenerationSecretKey)
							.verificationCardIDs(verificationCardIDs)
							.verificationCardPublicKeys(verificationCardPublicKeys)
							.encryptedHashedPartialChoiceReturnCodes(encryptedHashedPartialChoiceReturnCodes)
							.encryptedHashedConfirmationKeys(encryptedHashedConfirmationKeys_grp2);

			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, builder::build);

			final String message = "The Vector of exponentiated, encrypted, hashed partial Choice Return Codes and the Vector of exponentiated, "
					+ "encrypted, hashed Confirmation Keys do not have the same group order.";
			assertEquals(message, Throwables.getRootCause(exception).getMessage());
		}

		@Test
		@DisplayName("verificationCardIds must be unique throws IllegalArgumentException")
		void verificationCardIdsUnique_Throws() {
			final List<String> verificationCardIDsWithDouble = Arrays.asList(VERIFICATION_CARD_IDS.get(0), VERIFICATION_CARD_IDS.get(1),
					VERIFICATION_CARD_IDS.get(0));

			final GenEncLongCodeSharesInput.Builder builder =
					new GenEncLongCodeSharesInput.Builder()
							.returnCodesGenerationSecretKey(returnCodesGenerationSecretKey)
							.verificationCardIDs(verificationCardIDsWithDouble)
							.verificationCardPublicKeys(verificationCardPublicKeys)
							.encryptedHashedPartialChoiceReturnCodes(encryptedHashedPartialChoiceReturnCodes)
							.encryptedHashedConfirmationKeys(encryptedHashedConfirmationKeys);

			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, builder::build);

			final String message = "The Vector of verification card IDs contains duplicated values.";
			assertEquals(message, Throwables.getRootCause(exception).getMessage());
		}

		@Test
		@DisplayName("valid param gives expected input")
		void expectInput() {
			final GenEncLongCodeSharesInput.Builder builder =
					new GenEncLongCodeSharesInput.Builder()
							.returnCodesGenerationSecretKey(returnCodesGenerationSecretKey)
							.verificationCardIDs(verificationCardIDs)
							.verificationCardPublicKeys(verificationCardPublicKeys)
							.encryptedHashedPartialChoiceReturnCodes(encryptedHashedPartialChoiceReturnCodes)
							.encryptedHashedConfirmationKeys(encryptedHashedConfirmationKeys);

			assertDoesNotThrow(builder::build);
		}
	}

	@Nested
	@DisplayName("A GenEncLongCodeSharesOutput built with")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class GenEncLongCodeSharesOutputTest {

		private final int SIZE_10 = 10;

		private final GqGroup generator = GroupTestData.getDifferentGqGroup(GQ_GROUP);
		private final GqGroup generator2 = GroupTestData.getDifferentGqGroup(GQ_GROUP);

		private final List<GqElement> K_j_id = Stream.generate(generator::getGenerator).limit(SIZE_10).collect(Collectors.toList());
		private final List<GqElement> Kc_j_id = Stream.generate(generator2::getGenerator).limit(SIZE_10).collect(Collectors.toList());
		private final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> c_expPCC_j_id = elGamalGenerator.genRandomCiphertextVector(10,
				CIPHERTEXT_SIZE);
		private final List<ExponentiationProof> pi_expPCC_j_id =
				Stream.generate(
								() -> new ExponentiationProof(zqGroupGenerator.genRandomZqElementMember(), zqGroupGenerator.genRandomZqElementMember()))
						.limit(SIZE_10)
						.collect(Collectors.toList());

		private final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> c_expCK_j_id = elGamalGenerator.genRandomCiphertextVector(10,
				CIPHERTEXT_SIZE);
		private final List<ExponentiationProof> pi_expCK_j_id =
				Stream.generate(
								() -> new ExponentiationProof(zqGroupGenerator.genRandomZqElementMember(), zqGroupGenerator.genRandomZqElementMember()))
						.limit(SIZE_10)
						.collect(Collectors.toList());

		@Test
		@DisplayName("valid param gives expected output")
		void expectOutput() {
			final GenEncLongCodeSharesOutput output =
					new GenEncLongCodeSharesOutput.Builder()
							.setVoterChoiceReturnCodeGenerationPublicKeys(K_j_id)
							.setVoterVoteCastReturnCodeGenerationPublicKeys(Kc_j_id)
							.setExponentiatedEncryptedHashedPartialChoiceReturnCodes(c_expPCC_j_id)
							.setProofsCorrectExponentiationPartialChoiceReturnCodes(pi_expPCC_j_id)
							.setExponentiatedEncryptedHashedConfirmationKeys(c_expCK_j_id)
							.setProofsCorrectExponentiationConfirmationKeys(pi_expCK_j_id)
							.build();

			final GqGroup confirmationKeysGroup = output.getExponentiatedEncryptedHashedConfirmationKeys().getGroup();
			final GqGroup partialChoiceReturnCodesGroup = output.getExponentiatedEncryptedHashedPartialChoiceReturnCodes().getGroup();

			assertTrue(confirmationKeysGroup.hasSameOrderAs(partialChoiceReturnCodesGroup));
		}

		@Test
		@DisplayName("any null output parameter throws NullPointerException")
		void nullOutputParam_Throws() {
			GenEncLongCodeSharesOutput.Builder builder = new GenEncLongCodeSharesOutput.Builder()
					.setVoterChoiceReturnCodeGenerationPublicKeys(K_j_id)
					.setVoterVoteCastReturnCodeGenerationPublicKeys(Kc_j_id)
					.setExponentiatedEncryptedHashedPartialChoiceReturnCodes(c_expPCC_j_id)
					.setProofsCorrectExponentiationPartialChoiceReturnCodes(pi_expPCC_j_id)
					.setExponentiatedEncryptedHashedConfirmationKeys(c_expCK_j_id)
					.setProofsCorrectExponentiationConfirmationKeys(pi_expCK_j_id);

			builder = builder.setVoterChoiceReturnCodeGenerationPublicKeys(null);
			Exception exception = assertThrows(NullPointerException.class, builder::build);

			assertEquals("The Vector of Voter Choice Return Code Generation public keys is null.",
					Throwables.getRootCause(exception).getMessage());

			builder = builder.setVoterChoiceReturnCodeGenerationPublicKeys(K_j_id)
					.setVoterVoteCastReturnCodeGenerationPublicKeys(null);
			exception = assertThrows(NullPointerException.class, builder::build);

			assertEquals("The Vector of Voter Vote Cast Return Code Generation public keys is null.",
					Throwables.getRootCause(exception).getMessage());

			builder = builder.setVoterVoteCastReturnCodeGenerationPublicKeys(Kc_j_id)
					.setExponentiatedEncryptedHashedPartialChoiceReturnCodes(null);
			exception = assertThrows(NullPointerException.class, builder::build);

			assertEquals("The Vector of exponentiated, encrypted, hashed partial Choice Return Codes is null.",
					Throwables.getRootCause(exception).getMessage());

			builder = builder.setExponentiatedEncryptedHashedPartialChoiceReturnCodes(c_expPCC_j_id)
					.setProofsCorrectExponentiationPartialChoiceReturnCodes(null);
			exception = assertThrows(NullPointerException.class, builder::build);

			assertEquals("The Proofs of correct exponentiation of the partial Choice Return Codes is null.",
					Throwables.getRootCause(exception).getMessage());

			builder = builder.setProofsCorrectExponentiationPartialChoiceReturnCodes(pi_expPCC_j_id)
					.setExponentiatedEncryptedHashedConfirmationKeys(null);
			exception = assertThrows(NullPointerException.class, builder::build);

			assertEquals("The Vector of exponentiated, encrypted, hashed Confirmation Keys is null.",
					Throwables.getRootCause(exception).getMessage());

			builder = builder.setExponentiatedEncryptedHashedConfirmationKeys(c_expCK_j_id).setProofsCorrectExponentiationConfirmationKeys(null);
			exception = assertThrows(NullPointerException.class, builder::build);

			assertEquals("The Proofs of correct exponentiation of the Confirmation Keys is null.",
					Throwables.getRootCause(exception).getMessage());

		}

		@Test
		@DisplayName("empty vectors throws IllegalArgumentException")
		void emptyVectorsChecks() {
			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> emptyVector = GroupVector.of();

			final List<GqElement> emptyGqElementList = Collections.emptyList();
			GenEncLongCodeSharesOutput.Builder builder = new GenEncLongCodeSharesOutput.Builder()
					.setVoterChoiceReturnCodeGenerationPublicKeys(K_j_id)
					.setVoterVoteCastReturnCodeGenerationPublicKeys(Kc_j_id)
					.setExponentiatedEncryptedHashedPartialChoiceReturnCodes(c_expPCC_j_id)
					.setProofsCorrectExponentiationPartialChoiceReturnCodes(pi_expPCC_j_id)
					.setExponentiatedEncryptedHashedConfirmationKeys(c_expCK_j_id)
					.setProofsCorrectExponentiationConfirmationKeys(pi_expCK_j_id);

			builder = builder.setVoterChoiceReturnCodeGenerationPublicKeys(emptyGqElementList);
			Exception exception = assertThrows(IllegalArgumentException.class, builder::build);

			assertEquals("The Vector of Voter Choice Return Code Generation public keys must have more than zero elements.",
					Throwables.getRootCause(exception).getMessage());

			builder = builder.setVoterChoiceReturnCodeGenerationPublicKeys(K_j_id).setVoterVoteCastReturnCodeGenerationPublicKeys(emptyGqElementList);
			exception = assertThrows(IllegalArgumentException.class, builder::build);

			assertEquals("The Vector of Voter Vote Cast Return Code Generation public keys must have more than zero elements.",
					Throwables.getRootCause(exception).getMessage());

			builder = builder.setVoterVoteCastReturnCodeGenerationPublicKeys(Kc_j_id)
					.setExponentiatedEncryptedHashedPartialChoiceReturnCodes(emptyVector);
			exception = assertThrows(IllegalArgumentException.class, builder::build);

			assertEquals("The Vector of exponentiated, encrypted, hashed partial Choice Return Codes must have more than zero elements.",
					Throwables.getRootCause(exception).getMessage());

			final List<ExponentiationProof> emptyExponentiationProofList = Collections.emptyList();

			builder = builder.setExponentiatedEncryptedHashedPartialChoiceReturnCodes(c_expPCC_j_id)
					.setProofsCorrectExponentiationPartialChoiceReturnCodes(emptyExponentiationProofList);
			exception = assertThrows(IllegalArgumentException.class, builder::build);

			assertEquals("The Proofs of correct exponentiation of the partial Choice Return Codes must have more than zero elements.",
					Throwables.getRootCause(exception).getMessage());

			builder = builder.setProofsCorrectExponentiationPartialChoiceReturnCodes(pi_expPCC_j_id)
					.setExponentiatedEncryptedHashedConfirmationKeys(emptyVector);
			exception = assertThrows(IllegalArgumentException.class, builder::build);

			assertEquals("The Vector of exponentiated, encrypted, hashed Confirmation Keys must have more than zero elements.",
					Throwables.getRootCause(exception).getMessage());

			builder = builder.setExponentiatedEncryptedHashedConfirmationKeys(c_expCK_j_id)
					.setProofsCorrectExponentiationConfirmationKeys(emptyExponentiationProofList);
			exception = assertThrows(IllegalArgumentException.class, builder::build);

			assertEquals("The Proofs of correct exponentiation of the Confirmation Keys must have more than zero elements.",
					Throwables.getRootCause(exception).getMessage());
		}

		@Test
		@DisplayName("Cross group checks throws IllegalArgumentException")
		void inconsistentVectorsSizeThrows() {
			final List<GqElement> Kc_j_id_1 = Stream.generate(generator2::getGenerator).limit(SIZE_10 + 1).collect(Collectors.toList());
			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> c_expPCC_j_id_1 = elGamalGenerator.genRandomCiphertextVector(SIZE_10 + 1,
					CIPHERTEXT_SIZE);
			final List<ExponentiationProof> pi_expPCC_j_id_1 =
					Stream.generate(
									() -> new ExponentiationProof(zqGroupGenerator.genRandomZqElementMember(), zqGroupGenerator.genRandomZqElementMember()))
							.limit(SIZE_10 + 1)
							.collect(Collectors.toList());

			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> c_expCK_j_id_1 = elGamalGenerator.genRandomCiphertextVector(SIZE_10 + 1,
					CIPHERTEXT_SIZE);
			final List<ExponentiationProof> pi_expCK_j_id_1 =
					Stream.generate(
									() -> new ExponentiationProof(zqGroupGenerator.genRandomZqElementMember(), zqGroupGenerator.genRandomZqElementMember()))
							.limit(SIZE_10 + 1)
							.collect(Collectors.toList());

			final int N_E = K_j_id.size();
			GenEncLongCodeSharesOutput.Builder builder = new GenEncLongCodeSharesOutput.Builder()
					.setVoterChoiceReturnCodeGenerationPublicKeys(K_j_id)
					.setVoterVoteCastReturnCodeGenerationPublicKeys(Kc_j_id)
					.setExponentiatedEncryptedHashedPartialChoiceReturnCodes(c_expPCC_j_id)
					.setProofsCorrectExponentiationPartialChoiceReturnCodes(pi_expPCC_j_id)
					.setExponentiatedEncryptedHashedConfirmationKeys(c_expCK_j_id)
					.setProofsCorrectExponentiationConfirmationKeys(pi_expCK_j_id);

			builder = builder.setVoterVoteCastReturnCodeGenerationPublicKeys(Kc_j_id_1);
			Exception exception = assertThrows(IllegalArgumentException.class, builder::build);

			assertEquals(String.format("The Vector of Voter Vote Cast Return Code Generation public keys is of incorrect size "
									+ "[size: expected: %s, actual: %s].",
							N_E, Kc_j_id_1.size()),
					Throwables.getRootCause(exception).getMessage());

			builder = builder.setVoterVoteCastReturnCodeGenerationPublicKeys(Kc_j_id)
					.setExponentiatedEncryptedHashedPartialChoiceReturnCodes(c_expPCC_j_id_1);
			exception = assertThrows(IllegalArgumentException.class, builder::build);

			assertEquals(String.format("The Vector of exponentiated, encrypted, hashed partial Choice Return Codes is of incorrect size "
							+ "[size: expected: %s, actual: %s].", N_E, c_expPCC_j_id_1.size()),
					Throwables.getRootCause(exception).getMessage());

			builder = builder.setExponentiatedEncryptedHashedPartialChoiceReturnCodes(c_expPCC_j_id)
					.setProofsCorrectExponentiationPartialChoiceReturnCodes(pi_expPCC_j_id_1);
			exception = assertThrows(IllegalArgumentException.class, builder::build);

			assertEquals(String.format("The Proofs of correct exponentiation of the partial Choice Return Codes is of incorrect size "
							+ "[size: expected: %s, actual: %s].", N_E, c_expPCC_j_id_1.size()),
					Throwables.getRootCause(exception).getMessage());

			builder = builder.setProofsCorrectExponentiationPartialChoiceReturnCodes(pi_expPCC_j_id)
					.setExponentiatedEncryptedHashedConfirmationKeys(c_expCK_j_id_1);
			exception = assertThrows(IllegalArgumentException.class, builder::build);

			assertEquals(String.format("The Vector of exponentiated, encrypted, hashed Confirmation Keys is of incorrect size "
							+ "[size: expected: %s, actual: %s].", N_E, c_expPCC_j_id_1.size()),
					Throwables.getRootCause(exception).getMessage());

			builder = builder.setExponentiatedEncryptedHashedConfirmationKeys(c_expCK_j_id)
					.setProofsCorrectExponentiationConfirmationKeys(pi_expCK_j_id_1);
			exception = assertThrows(IllegalArgumentException.class, builder::build);

			assertEquals(String.format("The Proofs of correct exponentiation of the Confirmation Keys is of incorrect size "
							+ "[size: expected: %s, actual: %s].", N_E, c_expPCC_j_id_1.size()),
					Throwables.getRootCause(exception).getMessage());
		}

		@Test
		@DisplayName("valid param gives expected output")
		void crossGroupChecksThrows() {
			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> c_expPCC_j_id_q1 = elGamalGenerator2.genRandomCiphertextVector(SIZE_10,
					CIPHERTEXT_SIZE);
			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> c_expCK_j_id_q1 = elGamalGenerator2.genRandomCiphertextVector(SIZE_10,
					CIPHERTEXT_SIZE);

			GenEncLongCodeSharesOutput.Builder builder = new GenEncLongCodeSharesOutput.Builder()
					.setVoterChoiceReturnCodeGenerationPublicKeys(K_j_id)
					.setVoterVoteCastReturnCodeGenerationPublicKeys(Kc_j_id)
					.setExponentiatedEncryptedHashedPartialChoiceReturnCodes(c_expPCC_j_id)
					.setProofsCorrectExponentiationPartialChoiceReturnCodes(pi_expPCC_j_id)
					.setExponentiatedEncryptedHashedConfirmationKeys(c_expCK_j_id)
					.setProofsCorrectExponentiationConfirmationKeys(pi_expCK_j_id);

			builder = builder.setExponentiatedEncryptedHashedPartialChoiceReturnCodes(c_expPCC_j_id_q1);
			Exception exception = assertThrows(IllegalArgumentException.class, builder::build);

			assertEquals("The Vector of exponentiated, encrypted, hashed partial Choice Return Codes and the Vector of exponentiated, encrypted, "
							+ "hashed Confirmation Keys do not have the same group order.",
					Throwables.getRootCause(exception).getMessage());

			builder = builder.setExponentiatedEncryptedHashedPartialChoiceReturnCodes(c_expPCC_j_id)
					.setExponentiatedEncryptedHashedConfirmationKeys(c_expCK_j_id_q1);
			exception = assertThrows(IllegalArgumentException.class, builder::build);

			assertEquals("The Vector of exponentiated, encrypted, hashed partial Choice Return Codes and the Vector of exponentiated, encrypted, "
							+ "hashed Confirmation Keys do not have the same group order.",
					Throwables.getRootCause(exception).getMessage());
		}
	}
}