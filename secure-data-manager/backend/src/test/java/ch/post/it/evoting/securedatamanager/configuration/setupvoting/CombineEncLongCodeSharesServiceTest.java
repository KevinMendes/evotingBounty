/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

package ch.post.it.evoting.securedatamanager.configuration.setupvoting;

import static ch.post.it.evoting.cryptoprimitives.domain.VotingOptionsConstants.MAXIMUM_NUMBER_OF_SELECTABLE_VOTING_OPTIONS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.exceptions.FailedValidationException;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPrivateKey;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalService;
import ch.post.it.evoting.cryptoprimitives.hashing.HashService;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupMatrix;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.test.tools.TestGroupSetup;
import ch.post.it.evoting.cryptoprimitives.test.tools.data.GroupTestData;
import ch.post.it.evoting.cryptoprimitives.test.tools.generator.ElGamalGenerator;

@DisplayName("CombineEncLongCodeSharesService")
class CombineEncLongCodeSharesServiceTest extends TestGroupSetup {

	private static final int NUMBER_OF_VOTING_OPTIONS = MAXIMUM_NUMBER_OF_SELECTABLE_VOTING_OPTIONS;
	private static final int CONFIRMATION_KEYS_CIPHERTEXT_SIZE = 1;
	private static final List<String> VERIFICATION_CARD_IDS = ImmutableList.of("e3318008e47d439a92577fcb2c738192", "4f51188102c2421385d250bf48b8b8dd",
			"9b5be5f5068a499d9998d48cb394aee1", "f51188102c2421385d250bf48b845b8a");

	private final GqGroup gqGroup1 = GroupTestData.getGqGroup();
	private final GqGroup gqGroup2 = GroupTestData.getDifferentGqGroup(gqGroup1);
	private final ElGamalGenerator elGamalGeneratorGrp2 = new ElGamalGenerator(gqGroup2);
	private final ElGamalGenerator elGamalGeneratorGrp1 = new ElGamalGenerator(gqGroup1);

	@Nested
	@DisplayName("calling combineEncLongCodeShares with")
	class CombineEncLongCodeSharesTest {
		private final int NUM_ROWS = 4;
		private final int NUM_COLS = 4;
		final GroupMatrix<ElGamalMultiRecipientCiphertext, GqGroup> exponentiatedEncryptedPartialChoiceReturnCodesMatrix =
				elGamalGeneratorGrp1.genRandomCiphertextMatrix(NUM_ROWS, NUM_COLS, NUMBER_OF_VOTING_OPTIONS);
		final GroupMatrix<ElGamalMultiRecipientCiphertext, GqGroup> exponentiatedEncryptedConfirmationKeysMatrix =
				elGamalGeneratorGrp1.genRandomCiphertextMatrix(NUM_ROWS, NUM_COLS, CONFIRMATION_KEYS_CIPHERTEXT_SIZE);
		private final CombineEncLongCodeSharesInput input =
				new CombineEncLongCodeSharesInput.Builder()
						.setExponentiatedEncryptedChoiceReturnCodesMatrix(exponentiatedEncryptedPartialChoiceReturnCodesMatrix)
						.setExponentiatedEncryptedConfirmationKeysMatrix(exponentiatedEncryptedConfirmationKeysMatrix)
						.setVerificationCardIds(VERIFICATION_CARD_IDS)
						.build();
		private final HashService hashService = HashService.getInstance();
		private final ElGamalService elGamalService = new ElGamalService();
		private final CombineEncLongCodeSharesService combineEncLongCodeSharesService = new CombineEncLongCodeSharesService(hashService,
				elGamalService);
		private final String electionEventId = "0123456789abcdef0123456789abcdef";
		private final String verificationCardSetId = "abcdef0123456789abcdef0123456789";
		private final ElGamalMultiRecipientPrivateKey setupSecretKey = elGamalGeneratorGrp1.genRandomPrivateKey(1);
		private final CombineEncLongCodeSharesContext context =
				new CombineEncLongCodeSharesContext.Builder()
						.setElectionEventId(electionEventId)
						.setVerificationCardSetId(verificationCardSetId)
						.setSetupSecretKey(setupSecretKey)
						.build();

		@Test()
		@DisplayName("The context is null")
		void testWithNullContext() {
			final Exception exception = assertThrows(NullPointerException.class, () ->
					combineEncLongCodeSharesService.combineEncLongCodeShares(null, input)
			);

			final String expectedMessage = "The context cannot be null";
			final String actualMessage = exception.getMessage();

			assertTrue(actualMessage.contains(expectedMessage), actualMessage);
		}

		@Test()
		@DisplayName("The input is null")
		void testWithNullInput() {
			final Exception exception = assertThrows(NullPointerException.class, () ->
					combineEncLongCodeSharesService.combineEncLongCodeShares(context, null)
			);

			final String expectedMessage = "The input cannot be null";
			final String actualMessage = exception.getMessage();

			assertTrue(actualMessage.contains(expectedMessage), actualMessage);
		}

		@Test()
		@DisplayName("The context and input are not null")
		void testWithNonNullContextAndInput() {
			final CombineEncLongCodeSharesOutput output = combineEncLongCodeSharesService.combineEncLongCodeShares(context, input);

			assertNotNull(output);

			final int N_E = input.getExponentiatedEncryptedHashedPartialChoiceReturnCodesMatrix().numRows();

			final List<Integer> sizes = Arrays.asList(
					input.getExponentiatedEncryptedHashedConfirmationKeysMatrix().numRows(),
					input.getExponentiatedEncryptedHashedPartialChoiceReturnCodesMatrix().numRows(),
					input.getVerificationCardIds().size());

			assertTrue(sizes.stream().allMatch(size -> size == N_E));
		}
	}

	@Nested
	@DisplayName("CombineEncLongCodeSharesContext with")
	class CombineEncLongCodeSharesContextTest {

		private final String electionEventId = "0123456789abcdef0123456789abcdef";
		private final String verificationCardSetId = "abcdef0123456789abcdef0123456789";
		private final ElGamalMultiRecipientPrivateKey setupSecretKey = elGamalGeneratorGrp1.genRandomPrivateKey(1);

		@Test()
		@DisplayName("The Election Event Id null")
		void contextWithVector_electionEventId_is_null() {
			final CombineEncLongCodeSharesContext.Builder builder = new CombineEncLongCodeSharesContext.Builder()
					.setElectionEventId(null)
					.setVerificationCardSetId(verificationCardSetId)
					.setSetupSecretKey(setupSecretKey);

			assertThrows(NullPointerException.class, builder::build);
		}

		@Test()
		@DisplayName("The Verification Card Set Id null")
		void contextWithVector_verificationCardSetId_is_null() {
			final CombineEncLongCodeSharesContext.Builder builder = new CombineEncLongCodeSharesContext.Builder()
					.setElectionEventId(electionEventId)
					.setVerificationCardSetId(null)
					.setSetupSecretKey(setupSecretKey);

			assertThrows(NullPointerException.class, builder::build);
		}

		@Test()
		@DisplayName("The Setup Secret Key null")
		void contextWithVector_setupSecretKey_is_null() {
			final CombineEncLongCodeSharesContext.Builder builder = new CombineEncLongCodeSharesContext.Builder()
					.setElectionEventId(electionEventId)
					.setVerificationCardSetId(verificationCardSetId)
					.setSetupSecretKey(null);

			final Exception exception = assertThrows(NullPointerException.class, builder::build);

			final String expectedMessage = "The Setup Secret Key must not be null.";
			final String actualMessage = exception.getMessage();

			assertEquals(expectedMessage, actualMessage);
		}

		@Test()
		@DisplayName("The Election Event Id invalid UUID")
		void contextWithVector_electionEventId_invalidUUID() {
			final String invalidElectionEventId = "zdiauzdi134";

			final CombineEncLongCodeSharesContext.Builder builder = new CombineEncLongCodeSharesContext.Builder()
					.setElectionEventId(invalidElectionEventId)
					.setVerificationCardSetId(verificationCardSetId)
					.setSetupSecretKey(setupSecretKey);

			final FailedValidationException exception = assertThrows(FailedValidationException.class, builder::build);

			final String expectedMessage = String.format(
					"The given string does not comply with the required UUID format. [string: %s, format: ^[0123456789abcdef]{32}$].",
					invalidElectionEventId);
			final String actualMessage = exception.getMessage();

			assertEquals(expectedMessage, actualMessage);
		}

		@Test()
		@DisplayName("The Verification Card Set Id invalid UUID")
		void contextWithVector_verificationCardSetId_invalidUUID() {
			final String invalidVerificationCardSetId = "zdiauzdi134";

			final CombineEncLongCodeSharesContext.Builder builder = new CombineEncLongCodeSharesContext.Builder()
					.setElectionEventId(electionEventId)
					.setVerificationCardSetId(invalidVerificationCardSetId)
					.setSetupSecretKey(setupSecretKey);

			final FailedValidationException exception = assertThrows(FailedValidationException.class, builder::build);

			final String expectedMessage = String.format(
					"The given string does not comply with the required UUID format. [string: %s, format: ^[0123456789abcdef]{32}$].",
					invalidVerificationCardSetId);
			final String actualMessage = exception.getMessage();

			assertEquals(expectedMessage, actualMessage);
		}

		@Test()
		@DisplayName("output for input with same size and Gq group")
		void contextWithCorrectValues() {
			final CombineEncLongCodeSharesContext context =
					new CombineEncLongCodeSharesContext.Builder()
							.setElectionEventId(electionEventId)
							.setVerificationCardSetId(verificationCardSetId)
							.setSetupSecretKey(setupSecretKey)
							.build();

			assertEquals(electionEventId, context.getElectionEventId(), "electionEventId expected");
			assertEquals(verificationCardSetId, context.getVerificationCardSetId(), "verificationCardSetId expected");
			assertEquals(setupSecretKey, context.getSetupSecretKey(), "setupSecretKey expected");
		}
	}

	@Nested
	@DisplayName("CombineEncLongCodeSharesInput with")
	class CombineEncLongCodeSharesInputTest {

		private GroupMatrix<ElGamalMultiRecipientCiphertext, GqGroup> exponentiatedEncryptedPartialChoiceReturnCodesMatrix_2x2;
		private GroupMatrix<ElGamalMultiRecipientCiphertext, GqGroup> exponentiatedEncryptedPartialChoiceReturnCodesMatrix_3x4;
		private GroupMatrix<ElGamalMultiRecipientCiphertext, GqGroup> exponentiatedEncryptedPartialChoiceReturnCodesMatrix_4x4;
		private GroupMatrix<ElGamalMultiRecipientCiphertext, GqGroup> exponentiatedEncryptedPartialConfirmationKeysMatrix_3x2;
		private GroupMatrix<ElGamalMultiRecipientCiphertext, GqGroup> exponentiatedEncryptedPartialConfirmationKeysMatrix_4x4_grp_1;
		private GroupMatrix<ElGamalMultiRecipientCiphertext, GqGroup> exponentiatedEncryptedPartialConfirmationKeysMatrix_4x4_grp_2;
		private List<String> verificationCardIds_4;
		private List<String> verificationCardIds_3;

		@BeforeEach
		void setUp() {
			exponentiatedEncryptedPartialChoiceReturnCodesMatrix_2x2 = elGamalGeneratorGrp1.genRandomCiphertextMatrix(2, 2, NUMBER_OF_VOTING_OPTIONS);
			exponentiatedEncryptedPartialChoiceReturnCodesMatrix_3x4 = elGamalGeneratorGrp1.genRandomCiphertextMatrix(3, 4, NUMBER_OF_VOTING_OPTIONS);
			exponentiatedEncryptedPartialChoiceReturnCodesMatrix_4x4 = elGamalGeneratorGrp1.genRandomCiphertextMatrix(4, 4, NUMBER_OF_VOTING_OPTIONS);
			exponentiatedEncryptedPartialConfirmationKeysMatrix_3x2 = elGamalGeneratorGrp1.genRandomCiphertextMatrix(3, 2,
					CONFIRMATION_KEYS_CIPHERTEXT_SIZE);
			exponentiatedEncryptedPartialConfirmationKeysMatrix_4x4_grp_1 = elGamalGeneratorGrp1.genRandomCiphertextMatrix(4, 4,
					CONFIRMATION_KEYS_CIPHERTEXT_SIZE);
			exponentiatedEncryptedPartialConfirmationKeysMatrix_4x4_grp_2 = elGamalGeneratorGrp2.genRandomCiphertextMatrix(4, 4,
					CONFIRMATION_KEYS_CIPHERTEXT_SIZE);
			verificationCardIds_4 = VERIFICATION_CARD_IDS;
			verificationCardIds_3 = VERIFICATION_CARD_IDS.subList(0, 2);
		}

		@Test()
		@DisplayName("Constructor with Matrix Choice Return Codes null")
		void inputWithChoiceReturnCodesMatrixNull() {
			final CombineEncLongCodeSharesInput.Builder builder = new CombineEncLongCodeSharesInput.Builder()
					.setExponentiatedEncryptedChoiceReturnCodesMatrix(null)
					.setExponentiatedEncryptedConfirmationKeysMatrix(exponentiatedEncryptedPartialConfirmationKeysMatrix_4x4_grp_1)
					.setVerificationCardIds(verificationCardIds_4);

			final Exception exception = assertThrows(NullPointerException.class, builder::build);

			final String expectedMessage = "The Matrix of exponentiated, encrypted, hashed partial Choice Return Codes must not be null.";
			final String actualMessage = exception.getMessage();

			assertEquals(expectedMessage, actualMessage);
		}

		@Test()
		@DisplayName("Constructor with Matrix of Confirmation Keys null")
		void inputWithNullMatrixConfirmationKeys() {
			final CombineEncLongCodeSharesInput.Builder builder = new CombineEncLongCodeSharesInput.Builder()
					.setExponentiatedEncryptedChoiceReturnCodesMatrix(exponentiatedEncryptedPartialChoiceReturnCodesMatrix_2x2)
					.setExponentiatedEncryptedConfirmationKeysMatrix(null)
					.setVerificationCardIds(verificationCardIds_4);

			final Exception exception = assertThrows(NullPointerException.class, builder::build);

			final String expectedMessage = "The Matrix of exponentiated, encrypted, hashed Confirmation Keys must not be null.";
			final String actualMessage = exception.getMessage();

			assertEquals(expectedMessage, actualMessage);
		}

		@Test()
		@DisplayName("Constructor with Vector of Verification Card Ids null")
		void inputWithNullVectorVerificationCardIds() {
			final CombineEncLongCodeSharesInput.Builder builder = new CombineEncLongCodeSharesInput.Builder()
					.setExponentiatedEncryptedChoiceReturnCodesMatrix(exponentiatedEncryptedPartialChoiceReturnCodesMatrix_2x2)
					.setExponentiatedEncryptedConfirmationKeysMatrix(exponentiatedEncryptedPartialConfirmationKeysMatrix_4x4_grp_1)
					.setVerificationCardIds(null);

			final Exception exception = assertThrows(NullPointerException.class, builder::build);

			final String expectedMessage = "The Vector of verification card ids must not be null.";
			final String actualMessage = exception.getMessage();

			assertEquals(expectedMessage, actualMessage);
		}

		@Test()
		@DisplayName("Constructor with Vector of Verification Card Ids of invalid UUID format")
		void inputWithVectorVerificationCardIds_invalidUUIDFormat() {
			final String verificationCardId_invalid = "f51188102c2421385zS";
			final CombineEncLongCodeSharesInput.Builder builder = new CombineEncLongCodeSharesInput.Builder()
					.setExponentiatedEncryptedChoiceReturnCodesMatrix(exponentiatedEncryptedPartialChoiceReturnCodesMatrix_2x2)
					.setExponentiatedEncryptedConfirmationKeysMatrix(exponentiatedEncryptedPartialConfirmationKeysMatrix_4x4_grp_1)
					.setVerificationCardIds(Collections.singletonList(verificationCardId_invalid));

			final FailedValidationException exception = assertThrows(FailedValidationException.class, builder::build);

			final String expectedMessage = String.format(
					"The given string does not comply with the required UUID format. [string: %s, format: ^[0123456789abcdef]{32}$].",
					verificationCardId_invalid);
			final String actualMessage = exception.getMessage();

			assertEquals(expectedMessage, actualMessage);
		}

		@Test()
		@DisplayName("Constructor with Matrix of partial Choice Return Codes with columns != 4")
		void inputWithMatrixChoiceReturnCodes_ofColsSizeDifferentThan4() {
			final CombineEncLongCodeSharesInput.Builder builder = new CombineEncLongCodeSharesInput.Builder()
					.setExponentiatedEncryptedChoiceReturnCodesMatrix(exponentiatedEncryptedPartialChoiceReturnCodesMatrix_2x2)
					.setExponentiatedEncryptedConfirmationKeysMatrix(exponentiatedEncryptedPartialConfirmationKeysMatrix_4x4_grp_1)
					.setVerificationCardIds(verificationCardIds_4);

			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, builder::build);

			final String expectedMessage = String.format("The Matrix of exponentiated, encrypted, hashed partial Choice Return Codes must have "
					+ "exactly 4 columns. [cols: %s].", exponentiatedEncryptedPartialChoiceReturnCodesMatrix_2x2.numRows());
			final String actualMessage = exception.getMessage();

			assertEquals(expectedMessage, actualMessage);
		}

		@Test()
		@DisplayName("Constructor with Matrix of Confirmation Keys with columns != 4")
		void inputWithMatrixConfirmationKeys_ofDifferentRowSize() {
			final CombineEncLongCodeSharesInput.Builder builder = new CombineEncLongCodeSharesInput.Builder()
					.setExponentiatedEncryptedChoiceReturnCodesMatrix(exponentiatedEncryptedPartialChoiceReturnCodesMatrix_4x4)
					.setExponentiatedEncryptedConfirmationKeysMatrix(exponentiatedEncryptedPartialConfirmationKeysMatrix_3x2)
					.setVerificationCardIds(verificationCardIds_4);

			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, builder::build);

			final String expectedMessage = String.format("The Matrix of exponentiated, encrypted, hashed Confirmation Keys must have exactly 4 "
					+ "columns. [cols: %s].", exponentiatedEncryptedPartialConfirmationKeysMatrix_3x2.numColumns());
			final String actualMessage = exception.getMessage();

			assertEquals(expectedMessage, actualMessage);
		}

		@Test()
		@DisplayName("Constructor with matrix with different row numbers")
		void inputWithMatrix_ofDifferentSize() {
			final CombineEncLongCodeSharesInput.Builder builder = new CombineEncLongCodeSharesInput.Builder()
					.setExponentiatedEncryptedChoiceReturnCodesMatrix(exponentiatedEncryptedPartialChoiceReturnCodesMatrix_3x4)
					.setExponentiatedEncryptedConfirmationKeysMatrix(exponentiatedEncryptedPartialConfirmationKeysMatrix_4x4_grp_1)
					.setVerificationCardIds(verificationCardIds_4);

			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, builder::build);

			final String expectedMessage = String.format("The Matrix of exponentiated, encrypted, hashed partial Choice Return Codes and Matrix of "
							+ "exponentiated, encrypted, hashed Confirmation Keys must have the same number of rows. [rows: 1): %s, 2): %s]",
					exponentiatedEncryptedPartialChoiceReturnCodesMatrix_3x4.numRows(),
					exponentiatedEncryptedPartialConfirmationKeysMatrix_4x4_grp_1.numRows());
			final String actualMessage = exception.getMessage();

			assertEquals(expectedMessage, actualMessage);
		}

		@Test()
		@DisplayName("Constructor with Vector of Verification Card Ids with different columns")
		void inputWithVectorVerificationCardIds_ofDifferentSize() {
			final CombineEncLongCodeSharesInput.Builder builder = new CombineEncLongCodeSharesInput.Builder()
					.setExponentiatedEncryptedChoiceReturnCodesMatrix(exponentiatedEncryptedPartialChoiceReturnCodesMatrix_4x4)
					.setExponentiatedEncryptedConfirmationKeysMatrix(exponentiatedEncryptedPartialConfirmationKeysMatrix_4x4_grp_2)
					.setVerificationCardIds(verificationCardIds_3);

			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, builder::build);

			final String expectedMessage = String.format("The Matrix of exponentiated, encrypted, hashed partial Choice Return Codes and "
							+ "Vector of Verification Card Ids must have the same number of rows. [rows: 1): %s, 2): %s]",
					exponentiatedEncryptedPartialChoiceReturnCodesMatrix_4x4.numRows(),
					verificationCardIds_3.size());
			final String actualMessage = exception.getMessage();

			assertEquals(expectedMessage, actualMessage);
		}

		@Test()
		@DisplayName("Constructor with matrix with equal number of rows and columns")
		void inputWithMatrix_ofEqualSizeRowsColumns() {
			final CombineEncLongCodeSharesInput input =
					new CombineEncLongCodeSharesInput.Builder()
							.setExponentiatedEncryptedChoiceReturnCodesMatrix(exponentiatedEncryptedPartialChoiceReturnCodesMatrix_4x4)
							.setExponentiatedEncryptedConfirmationKeysMatrix(exponentiatedEncryptedPartialConfirmationKeysMatrix_4x4_grp_1)
							.setVerificationCardIds(VERIFICATION_CARD_IDS)
							.build();

			assertNotNull(input);

			assertEquals(exponentiatedEncryptedPartialChoiceReturnCodesMatrix_4x4,
					input.getExponentiatedEncryptedHashedPartialChoiceReturnCodesMatrix(),
					"choiceReturnCodesMatrix expected");
			assertEquals(exponentiatedEncryptedPartialConfirmationKeysMatrix_4x4_grp_1, input.getExponentiatedEncryptedHashedConfirmationKeysMatrix(),
					"confirmationKeysMatrix expected");
			assertEquals(VERIFICATION_CARD_IDS, input.getVerificationCardIds(),
					"verificationCardIds expected");
		}
	}

	@Nested
	@DisplayName("CombineEncLongCodeSharesOutput with")
	class CombineEncLongCodeSharesOutputTest {

		private GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> encryptedPreChoiceReturnCodesVector_4;
		private GroupVector<GqElement, GqGroup> preVoteCastReturnCodes4G1;
		private GroupVector<GqElement, GqGroup> preVoteCastReturnCodes3G1;
		private GroupVector<GqElement, GqGroup> preVoteCastReturnCodes4G2;
		private List<String> longVoteCastReturnCodesVector_4;
		private List<String> longVoteCastReturnCodesVector_3;

		@BeforeEach
		void setUp() {
			encryptedPreChoiceReturnCodesVector_4 = GroupVector.of(ElGamalMultiRecipientCiphertext.neutralElement(NUMBER_OF_VOTING_OPTIONS, gqGroup1),
					ElGamalMultiRecipientCiphertext.neutralElement(NUMBER_OF_VOTING_OPTIONS, gqGroup1),
					ElGamalMultiRecipientCiphertext.neutralElement(NUMBER_OF_VOTING_OPTIONS, gqGroup1),
					ElGamalMultiRecipientCiphertext.neutralElement(NUMBER_OF_VOTING_OPTIONS, gqGroup1));

			preVoteCastReturnCodes4G1 = GroupVector.of(gqGroup1.getGenerator(), gqGroup1.getGenerator(), gqGroup1.getGenerator(),
					gqGroup1.getGenerator());

			preVoteCastReturnCodes3G1 = GroupVector.of(gqGroup1.getGenerator(), gqGroup1.getGenerator(), gqGroup1.getGenerator());

			preVoteCastReturnCodes4G2 = GroupVector.of(gqGroup2.getGenerator(), gqGroup2.getGenerator(), gqGroup2.getGenerator(),
					gqGroup2.getGenerator());

			longVoteCastReturnCodesVector_4 = Arrays.asList("lVCC1", "lVCC2", "lVCC3", "longVCC4");

			longVoteCastReturnCodesVector_3 = Arrays.asList("lVCC1", "lVCC2", "lVCC3");
		}

		@Test()
		@DisplayName("The Vector encrypted pre-Choice Return Codes null")
		void outputWithVector_encryptedPreChoiceReturnCodes_is_null() {
			final CombineEncLongCodeSharesOutput.Builder builder = new CombineEncLongCodeSharesOutput.Builder()
					.setEncryptedPreChoiceReturnCodesVector(null)
					.setPreVoteCastReturnCodesVector(preVoteCastReturnCodes4G1)
					.setLongVoteCastReturnCodesAllowList(longVoteCastReturnCodesVector_4);

			final Exception exception = assertThrows(NullPointerException.class, builder::build);

			final String expectedMessage = "The vector of encrypted pre-Choice Return Codes must not be null.";
			final String actualMessage = exception.getMessage();

			assertEquals(expectedMessage, actualMessage);
		}

		@Test()
		@DisplayName("The Vector pre-Choice Return Codes null")
		void outputWithVector_preVoteCastReturnCodes_is_null() {
			final CombineEncLongCodeSharesOutput.Builder builder = new CombineEncLongCodeSharesOutput.Builder()
					.setEncryptedPreChoiceReturnCodesVector(encryptedPreChoiceReturnCodesVector_4)
					.setPreVoteCastReturnCodesVector(null)
					.setLongVoteCastReturnCodesAllowList(longVoteCastReturnCodesVector_4);

			final Exception exception = assertThrows(NullPointerException.class, builder::build);

			final String expectedMessage = "The vector of pre-Vote Cast Return Codes must not be null.";
			final String actualMessage = exception.getMessage();

			assertEquals(expectedMessage, actualMessage);
		}

		@Test()
		@DisplayName("The Vector long Vote Cast Return Codes null")
		void outputWithVector_longVoteCastReturnCodesVector_is_null() {
			final CombineEncLongCodeSharesOutput.Builder builder = new CombineEncLongCodeSharesOutput.Builder()
					.setEncryptedPreChoiceReturnCodesVector(encryptedPreChoiceReturnCodesVector_4)
					.setPreVoteCastReturnCodesVector(preVoteCastReturnCodes4G1)
					.setLongVoteCastReturnCodesAllowList(null);
			final Exception exception = assertThrows(NullPointerException.class, builder::build);

			final String expectedMessage = "The long Vote Cast Return Codes allow list must not be null.";
			final String actualMessage = exception.getMessage();

			assertEquals(expectedMessage, actualMessage);
		}

		@Test()
		@DisplayName("The Vector Encrypted Pre-Choice Return Codes empty")
		void outputWithVector_preChoiceReturnCodes_empty() {
			final CombineEncLongCodeSharesOutput.Builder builder = new CombineEncLongCodeSharesOutput.Builder()
					.setEncryptedPreChoiceReturnCodesVector(GroupVector.from(Collections.emptyList()))
					.setPreVoteCastReturnCodesVector(preVoteCastReturnCodes4G1)
					.setLongVoteCastReturnCodesAllowList(longVoteCastReturnCodesVector_3);

			final Exception exception = assertThrows(IllegalArgumentException.class, builder::build);

			final String expectedMessage = "The vector of encrypted pre-Choice Return Codes must have more than zero elements.";
			final String actualMessage = exception.getMessage();

			assertEquals(expectedMessage, actualMessage);
		}

		@Test()
		@DisplayName("The Vector of pre-Vote Cast Return Codes empty")
		void outputWithVector_preVoteCastReturnCodes_empty() {
			final CombineEncLongCodeSharesOutput.Builder builder = new CombineEncLongCodeSharesOutput.Builder()
					.setEncryptedPreChoiceReturnCodesVector(encryptedPreChoiceReturnCodesVector_4)
					.setPreVoteCastReturnCodesVector(GroupVector.from(Collections.emptyList()))
					.setLongVoteCastReturnCodesAllowList(longVoteCastReturnCodesVector_3);

			final Exception exception = assertThrows(IllegalArgumentException.class, builder::build);

			final String expectedMessage = String.format(
					"The vector of pre-Vote Cast Return Codes is of incorrect size [size: expected: %s, actual: %s].",
					encryptedPreChoiceReturnCodesVector_4.size(), 0);
			final String actualMessage = exception.getMessage();

			assertEquals(expectedMessage, actualMessage);
		}

		@Test()
		@DisplayName("Vector of long Vote Cast Return Codes empty")
		void outputWithVector_longVoteCastReturnCodesVector_empty() {
			final CombineEncLongCodeSharesOutput.Builder builder = new CombineEncLongCodeSharesOutput.Builder()
					.setEncryptedPreChoiceReturnCodesVector(encryptedPreChoiceReturnCodesVector_4)
					.setPreVoteCastReturnCodesVector(preVoteCastReturnCodes4G1)
					.setLongVoteCastReturnCodesAllowList(Collections.emptyList());

			final Exception exception = assertThrows(IllegalArgumentException.class, builder::build);

			final String expectedMessage = String.format(
					"The long Vote Cast Return Codes allow list is of incorrect size [size: expected: %s, "
							+ "actual: %s].", encryptedPreChoiceReturnCodesVector_4.size(), 0);
			final String actualMessage = exception.getMessage();

			assertEquals(expectedMessage, actualMessage);
		}

		@Test()
		@DisplayName("Vector of pre-Vote Cast Return Codes is of incorrect size")
		void buildInputWithChoiceReturnCodesMatrix_colsDiff4() {
			final CombineEncLongCodeSharesOutput.Builder builder = new CombineEncLongCodeSharesOutput.Builder()
					.setEncryptedPreChoiceReturnCodesVector(encryptedPreChoiceReturnCodesVector_4)
					.setPreVoteCastReturnCodesVector(preVoteCastReturnCodes3G1)
					.setLongVoteCastReturnCodesAllowList(longVoteCastReturnCodesVector_4);

			final Exception exception = assertThrows(IllegalArgumentException.class, builder::build);

			final String expectedMessage = String.format(
					"The vector of pre-Vote Cast Return Codes is of incorrect size [size: expected: %s, actual: %s].",
					encryptedPreChoiceReturnCodesVector_4.size(), preVoteCastReturnCodes3G1.size());
			final String actualMessage = exception.getMessage();

			assertEquals(expectedMessage, actualMessage);
		}

		@Test()
		@DisplayName("Vector of long Vote Cast Return Codes is of incorrect size")
		void buildInputWithLongVoteCastReturnCodesVector_colsDiff4() {
			final CombineEncLongCodeSharesOutput.Builder builder = new CombineEncLongCodeSharesOutput.Builder()
					.setEncryptedPreChoiceReturnCodesVector(encryptedPreChoiceReturnCodesVector_4)
					.setPreVoteCastReturnCodesVector(preVoteCastReturnCodes4G1)
					.setLongVoteCastReturnCodesAllowList(longVoteCastReturnCodesVector_3);

			final Exception exception = assertThrows(IllegalArgumentException.class, builder::build);

			final String expectedMessage = String.format(
					"The long Vote Cast Return Codes allow list is of incorrect size [size: expected: %s, actual: %s].",
					encryptedPreChoiceReturnCodesVector_4.size(), longVoteCastReturnCodesVector_3.size());
			final String actualMessage = exception.getMessage();

			assertEquals(expectedMessage, actualMessage);
		}

		@Test()
		@DisplayName("the Vector of pre-Choice Return Codes and the Vector of pre-Vote Cast Return Codes have different Gd groups")
		void buildInputWithConfirmationKeysMatrix_rowSize() {
			final CombineEncLongCodeSharesOutput.Builder builder = new CombineEncLongCodeSharesOutput.Builder()
					.setEncryptedPreChoiceReturnCodesVector(encryptedPreChoiceReturnCodesVector_4)
					.setPreVoteCastReturnCodesVector(preVoteCastReturnCodes4G2)
					.setLongVoteCastReturnCodesAllowList(longVoteCastReturnCodesVector_4);

			final Exception exception = assertThrows(IllegalArgumentException.class, builder::build);

			final String expectedMessage = "The vector of encrypted pre-Choice Return Codes and the vector of pre-Vote Cast Return Codes do not have the same group order.";
			final String actualMessage = exception.getMessage();

			assertEquals(expectedMessage, actualMessage);
		}

		@Test()
		@DisplayName("output for input with same size and Gd group")
		void outputWithCorrectValues() {
			final CombineEncLongCodeSharesOutput.Builder builder = new CombineEncLongCodeSharesOutput.Builder()
					.setEncryptedPreChoiceReturnCodesVector(encryptedPreChoiceReturnCodesVector_4)
					.setPreVoteCastReturnCodesVector(preVoteCastReturnCodes4G1)
					.setLongVoteCastReturnCodesAllowList(longVoteCastReturnCodesVector_4);

			final CombineEncLongCodeSharesOutput output = builder.build();

			assertEquals(encryptedPreChoiceReturnCodesVector_4, output.getEncryptedPreChoiceReturnCodesVector(),
					"EncryptedPreChoiceReturnCodesVector expected");
			assertEquals(preVoteCastReturnCodes4G1, output.getPreVoteCastReturnCodesVector(),
					"PreVoteCastReturnCodesVector expected");
			assertEquals(longVoteCastReturnCodesVector_4, output.getLongVoteCastReturnCodesAllowList(),
					"LongVoteCastReturnCodesVector expected");
		}
	}
}
