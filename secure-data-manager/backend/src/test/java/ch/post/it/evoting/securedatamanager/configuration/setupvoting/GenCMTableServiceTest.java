/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.configuration.setupvoting;

import static ch.post.it.evoting.securedatamanager.configuration.setupvoting.GenCMTableService.BASE64_ENCODE_HASH_OUTPUT_LENGTH;
import static ch.post.it.evoting.securedatamanager.configuration.setupvoting.GenCMTableService.CHOICE_RETURN_CODES_LENGTH;
import static ch.post.it.evoting.securedatamanager.configuration.setupvoting.GenCMTableService.OMEGA;
import static ch.post.it.evoting.securedatamanager.configuration.setupvoting.GenCMTableService.VOTE_CAST_RETURN_CODE_LENGTH;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.election.Ballot;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.exceptions.FailedValidationException;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPrivateKey;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalService;
import ch.post.it.evoting.cryptoprimitives.hashing.HashService;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.RandomService;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;
import ch.post.it.evoting.cryptoprimitives.symmetric.SymmetricService;
import ch.post.it.evoting.cryptoprimitives.test.tools.data.GroupTestData;
import ch.post.it.evoting.cryptoprimitives.test.tools.generator.ElGamalGenerator;
import ch.post.it.evoting.cryptoprimitives.utils.KDFService;
import ch.post.it.evoting.securedatamanager.services.application.service.BallotService;

@DisplayName("GenCMTableService")
class GenCMTableServiceTest {

	private static final String ELECTION_EVENT_ID = "200e63a610bf4574b289abf8854d4d34";
	private static final String BALLOT_ID = "a3fad1c257634f07b284dc857629b070";
	private static final String VERIFICATION_CARD_SET_ID = "1de81acb944d4161a7148a2240edea47";
	private static final List<String> VERIFICATION_CARD_IDS = Arrays.asList("e3318008e47d439a92577fcb2c738192", "4f51188102c2421385d250bf48b8b8dd",
			"9b5be5f5068a499d9998d48cb394aee1");
	private static final RandomService randomService = new RandomService();
	private static GqGroup group;
	private static GqGroup otherGroup;
	private static ZqElement zqElement;
	private static ElGamalMultiRecipientPrivateKey setupSecretKey;
	private static ElGamalGenerator elGamalGenerator;
	private static ElGamalGenerator otherElGamalGenerator;
	private static GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> preChoiceReturnCodesVector;
	private static GroupVector<GqElement, GqGroup> preVoteCastReturnCodesVector;
	private static Map<String, String> CMtable;
	private static List<List<String>> shortCC;
	private static List<String> shortVCC;
	private static GenCMTableContext genCMTableContext;
	private static GenCMTableInput genCMTableInput;

	@BeforeAll
	static void setup() {
		group = GroupTestData.getGqGroup();
		otherGroup = GroupTestData.getDifferentGqGroup(group);
		zqElement = ZqElement.create(BigInteger.ONE, ZqGroup.sameOrderAs(group));
		setupSecretKey = new ElGamalMultiRecipientPrivateKey(Collections.nCopies(OMEGA, zqElement));

		elGamalGenerator = new ElGamalGenerator(group);
		otherElGamalGenerator = new ElGamalGenerator(otherGroup);

		preChoiceReturnCodesVector = elGamalGenerator.genRandomCiphertextVector(3, 1);
		preVoteCastReturnCodesVector = GroupVector.from(elGamalGenerator.genRandomCiphertextVector(3, 1)
				.stream().map(ElGamalMultiRecipientCiphertext::getGamma).collect(Collectors.toList()));

		CMtable = new HashMap<>();
		CMtable.put(randomService.genRandomBase64String(BASE64_ENCODE_HASH_OUTPUT_LENGTH), "hash1");
		CMtable.put(randomService.genRandomBase64String(BASE64_ENCODE_HASH_OUTPUT_LENGTH), "hash2");
		CMtable.put(randomService.genRandomBase64String(BASE64_ENCODE_HASH_OUTPUT_LENGTH), "hash3");
		CMtable.put(randomService.genRandomBase64String(BASE64_ENCODE_HASH_OUTPUT_LENGTH), "hash4");
		CMtable.put(randomService.genRandomBase64String(BASE64_ENCODE_HASH_OUTPUT_LENGTH), "hash5");
		CMtable.put(randomService.genRandomBase64String(BASE64_ENCODE_HASH_OUTPUT_LENGTH), "hash6");

		final List<String> choiceCodes = Arrays.asList(randomService.genRandomBase64String(CHOICE_RETURN_CODES_LENGTH),
				randomService.genRandomBase64String(CHOICE_RETURN_CODES_LENGTH));
		shortCC = Arrays.asList(choiceCodes, choiceCodes);

		shortVCC = Arrays.asList(randomService.genRandomBase64String(VOTE_CAST_RETURN_CODE_LENGTH),
				randomService.genRandomBase64String(VOTE_CAST_RETURN_CODE_LENGTH));

		genCMTableContext = new GenCMTableContext.Builder().setEncryptionGroup(group).setElectionEventId(ELECTION_EVENT_ID).setBallotId(BALLOT_ID)
				.setVerificationCardSetId(VERIFICATION_CARD_SET_ID).setSetupSecretKey(setupSecretKey).build();
		genCMTableInput = new GenCMTableInput.Builder().setVerificationCardIds(VERIFICATION_CARD_IDS)
				.setEncryptedPreChoiceReturnCodes(preChoiceReturnCodesVector).setPreVoteCastReturnCodes(preVoteCastReturnCodesVector)
				.build();
	}

	@Nested
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class GenCMTableContextTest {

		private Stream<Arguments> provideNullInputsForGenCMTableContext() {
			return Stream.of(
					Arguments.of(null, ELECTION_EVENT_ID, BALLOT_ID, VERIFICATION_CARD_SET_ID, setupSecretKey),
					Arguments.of(group, null, BALLOT_ID, VERIFICATION_CARD_SET_ID, setupSecretKey),
					Arguments.of(group, ELECTION_EVENT_ID, null, VERIFICATION_CARD_SET_ID, setupSecretKey),
					Arguments.of(group, ELECTION_EVENT_ID, BALLOT_ID, null, setupSecretKey),
					Arguments.of(group, ELECTION_EVENT_ID, BALLOT_ID, VERIFICATION_CARD_SET_ID, null)
			);
		}

		@ParameterizedTest
		@MethodSource("provideNullInputsForGenCMTableContext")
		@DisplayName("calling GenCMTableContext constructor with null values throws NullPointerException")
		void genCMTableContextWithNullValuesThrows(final GqGroup encryptionGroup, final String electionEventId, final String ballotId,
				final String verificationCardSetId, final ElGamalMultiRecipientPrivateKey setupSecretKey) {
			final GenCMTableContext.Builder builder = new GenCMTableContext.Builder().setEncryptionGroup(encryptionGroup)
					.setElectionEventId(electionEventId)
					.setBallotId(ballotId).setVerificationCardSetId(verificationCardSetId).setSetupSecretKey(setupSecretKey);
			assertThrows(NullPointerException.class, builder::build);
		}

		private Stream<Arguments> provideInvalidUUIDInputsForGenCMTableContext() {
			return Stream.of(
					Arguments.of(group, "invalidUUID", BALLOT_ID, VERIFICATION_CARD_SET_ID, setupSecretKey),
					Arguments.of(group, ELECTION_EVENT_ID, "invalidUUID", VERIFICATION_CARD_SET_ID, setupSecretKey),
					Arguments.of(group, ELECTION_EVENT_ID, BALLOT_ID, "invalidUUID", setupSecretKey)
			);
		}

		@ParameterizedTest
		@MethodSource("provideInvalidUUIDInputsForGenCMTableContext")
		@DisplayName("calling GenCMTableContext constructor with invalid UUID values throws FailedValidationException")
		void genCMTableContextWithInvalidUUIDValuesThrows(final GqGroup encryptionGroup, final String electionEventId, final String ballotId,
				final String verificationCardSetId, final ElGamalMultiRecipientPrivateKey setupSecretKey) {
			final GenCMTableContext.Builder builder = new GenCMTableContext.Builder().setEncryptionGroup(encryptionGroup)
					.setElectionEventId(electionEventId)
					.setBallotId(ballotId).setVerificationCardSetId(verificationCardSetId).setSetupSecretKey(setupSecretKey);
			assertThrows(FailedValidationException.class, builder::build);
		}

		@Test
		@DisplayName("calling GenCMTableContext constructor with incorrect group throws IllegalArgumentException")
		void genCMTableContextWithIncorrectGroupThrows() {
			final GenCMTableContext.Builder builder = new GenCMTableContext.Builder().setEncryptionGroup(otherGroup)
					.setElectionEventId(ELECTION_EVENT_ID).setBallotId(BALLOT_ID).setVerificationCardSetId(VERIFICATION_CARD_SET_ID)
					.setSetupSecretKey(setupSecretKey);
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, builder::build);
			assertEquals("The setup secret key must have the same group order than the encryption group.", exception.getMessage());
		}

		@Test
		@DisplayName("calling GenCMTableContext constructor with invalid setup secret key size throws IllegalArgumentException")
		void genCMTableContextWithInvalidSetupSecretKeySizeThrows() {
			final ElGamalMultiRecipientPrivateKey invalidSetupSecretKey = new ElGamalMultiRecipientPrivateKey(Collections.singletonList(zqElement));
			final GenCMTableContext.Builder builder = new GenCMTableContext.Builder().setEncryptionGroup(group).setElectionEventId(ELECTION_EVENT_ID)
					.setBallotId(BALLOT_ID).setVerificationCardSetId(VERIFICATION_CARD_SET_ID).setSetupSecretKey(invalidSetupSecretKey);
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, builder::build);
			assertEquals("The setup secret key must have omega elements.", exception.getMessage());
		}

		@Test
		@DisplayName("calling GenCMTableContext constructor with valid inputs does not throw")
		void genCMTableContextWithValidValues() {
			assertDoesNotThrow(() -> new GenCMTableContext.Builder().setEncryptionGroup(group).setElectionEventId(ELECTION_EVENT_ID)
					.setBallotId(BALLOT_ID).setVerificationCardSetId(VERIFICATION_CARD_SET_ID).setSetupSecretKey(setupSecretKey)
					.build());
		}

		@Test
		@DisplayName("calling GenCMTableContext getters")
		void genCMTableContextGetters() {
			final GenCMTableContext genCMTableContext = new GenCMTableContext.Builder().setEncryptionGroup(group)
					.setElectionEventId(ELECTION_EVENT_ID).setBallotId(BALLOT_ID).setVerificationCardSetId(VERIFICATION_CARD_SET_ID)
					.setSetupSecretKey(setupSecretKey).build();
			assertEquals(group, genCMTableContext.getEncryptionGroup());
			assertEquals(ELECTION_EVENT_ID, genCMTableContext.getElectionEventId());
			assertEquals(BALLOT_ID, genCMTableContext.getBallotId());
			assertEquals(VERIFICATION_CARD_SET_ID, genCMTableContext.getVerificationCardSetId());
			assertEquals(setupSecretKey, genCMTableContext.getSetupSecretKey());
		}
	}

	@Nested
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class GenCMTableInputTest {

		private Stream<Arguments> provideNullInputsForGenCMTableInput() {
			return Stream.of(
					Arguments.of(null, preChoiceReturnCodesVector, preVoteCastReturnCodesVector),
					Arguments.of(Collections.emptyList(), null, preVoteCastReturnCodesVector),
					Arguments.of(Collections.emptyList(), preChoiceReturnCodesVector, null)
			);
		}

		@ParameterizedTest
		@MethodSource("provideNullInputsForGenCMTableInput")
		@DisplayName("calling GenCMTableInput constructor with null values throws NullPointerException")
		void genCMTableInputWithNullValuesThrows(final List<String> verificationCardIds,
				final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> preChoiceReturnCodes,
				final GroupVector<GqElement, GqGroup> preVoteCastReturnCodes) {
			final GenCMTableInput.Builder builder = new GenCMTableInput.Builder().setVerificationCardIds(verificationCardIds)
					.setEncryptedPreChoiceReturnCodes(preChoiceReturnCodes).setPreVoteCastReturnCodes(preVoteCastReturnCodes);
			assertThrows(NullPointerException.class, builder::build);
		}

		@Test
		@DisplayName("calling GenCMTableInput constructor with invalid UUID values throws FailedValidationException")
		void genCMTableInputWithInvalidValuesThrows() {
			final List<String> invalidVerificationCardIds = Collections.singletonList("invalidUUID");
			final GenCMTableInput.Builder builder = new GenCMTableInput.Builder().setVerificationCardIds(invalidVerificationCardIds)
					.setEncryptedPreChoiceReturnCodes(preChoiceReturnCodesVector).setPreVoteCastReturnCodes(preVoteCastReturnCodesVector);
			assertThrows(FailedValidationException.class, builder::build);
		}

		private Stream<Arguments> provideEmptyElementSizeForGenCMTableInput() {
			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> emptyVector = elGamalGenerator.genRandomCiphertextVector(0, 0);
			return Stream.of(
					Arguments.of(Collections.emptyList(), preChoiceReturnCodesVector, preVoteCastReturnCodesVector),
					Arguments.of(VERIFICATION_CARD_IDS, emptyVector, preVoteCastReturnCodesVector),
					Arguments.of(VERIFICATION_CARD_IDS, preChoiceReturnCodesVector, emptyVector)
			);
		}

		@ParameterizedTest
		@MethodSource("provideEmptyElementSizeForGenCMTableInput")
		@DisplayName("calling GenCMTableInput constructor with empty elements size throws IllegalArgumentException")
		void genCMTableInputWithEmptyElementSizeThrows(final List<String> verificationCardIds,
				final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> preChoiceReturnCodes,
				final GroupVector<GqElement, GqGroup> preVoteCastReturnCodes) {

			final GenCMTableInput.Builder builder = new GenCMTableInput.Builder().setVerificationCardIds(verificationCardIds)
					.setEncryptedPreChoiceReturnCodes(preChoiceReturnCodes).setPreVoteCastReturnCodes(preVoteCastReturnCodes);
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, builder::build);

			assertEquals("All inputs must not be empty.", exception.getMessage());
		}

		private Stream<Arguments> provideIncorrectElementSizeForGenCMTableInput() {
			final List<String> invalidVerificationCardIds = Arrays.asList("e3318008e47d439a92577fcb2c738192", "4f51188102c2421385d250bf48b8b8dd");
			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> invalidPreVoteCastReturnCodesVector = elGamalGenerator.genRandomCiphertextVector(
					2, 1);
			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> invalidPreChoiceReturnCodesVector = elGamalGenerator.genRandomCiphertextVector(
					2, 1);
			return Stream.of(
					Arguments.of(invalidVerificationCardIds, preChoiceReturnCodesVector, preVoteCastReturnCodesVector),
					Arguments.of(VERIFICATION_CARD_IDS, invalidPreVoteCastReturnCodesVector, preVoteCastReturnCodesVector),
					Arguments.of(VERIFICATION_CARD_IDS, preChoiceReturnCodesVector, invalidPreChoiceReturnCodesVector)
			);
		}

		@ParameterizedTest
		@MethodSource("provideIncorrectElementSizeForGenCMTableInput")
		@DisplayName("calling GenCMTableInput constructor with incorrect elements size throws IllegalArgumentException")
		void genCMTableInputWithIncorrectElementSizeThrows(final List<String> verificationCardIds,
				final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> preChoiceReturnCodes,
				final GroupVector<GqElement, GqGroup> preVoteCastReturnCodes) {
			final GenCMTableInput.Builder builder = new GenCMTableInput.Builder().setVerificationCardIds(verificationCardIds)
					.setEncryptedPreChoiceReturnCodes(preChoiceReturnCodes).setPreVoteCastReturnCodes(preVoteCastReturnCodes);
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, builder::build);

			assertEquals("All inputs sizes must be the same.", exception.getMessage());
		}

		@Test
		@DisplayName("calling GenCMTableInput constructor with incorrect group throws IllegalArgumentException")
		void genCMTableInputWithIncorrectGroupThrows() {
			final GroupVector<GqElement, GqGroup> otherVector = GroupVector.from(otherElGamalGenerator.genRandomCiphertextVector(3, 1)
					.stream().map(ElGamalMultiRecipientCiphertext::getGamma).collect(Collectors.toList()));
			final GenCMTableInput.Builder builder = new GenCMTableInput.Builder().setVerificationCardIds(VERIFICATION_CARD_IDS)
					.setEncryptedPreChoiceReturnCodes(preChoiceReturnCodesVector).setPreVoteCastReturnCodes(otherVector);
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, builder::build);
			assertEquals("All inputs must have the same Gq group.", exception.getMessage());
		}

		@Test
		@DisplayName("calling GenCMTableInput constructor with valid inputs does not throw")
		void genCMTableInputWithValidValues() {
			assertDoesNotThrow(
					() -> new GenCMTableInput.Builder().setVerificationCardIds(VERIFICATION_CARD_IDS)
							.setEncryptedPreChoiceReturnCodes(preChoiceReturnCodesVector)
							.setPreVoteCastReturnCodes(preVoteCastReturnCodesVector).build());
		}

		@Test
		@DisplayName("calling GenCMTableInput getters")
		void genCMTableInputGetter() {
			final GenCMTableInput genCMTableInput = new GenCMTableInput.Builder().setVerificationCardIds(VERIFICATION_CARD_IDS)
					.setEncryptedPreChoiceReturnCodes(preChoiceReturnCodesVector).setPreVoteCastReturnCodes(preVoteCastReturnCodesVector)
					.build();
			assertEquals(VERIFICATION_CARD_IDS, genCMTableInput.getVerificationCardIds());
			assertEquals(preChoiceReturnCodesVector, genCMTableInput.getEncryptedPreChoiceReturnCodes());
			assertEquals(preVoteCastReturnCodesVector, genCMTableInput.getPreVoteCastReturnCodes());
			assertEquals(preChoiceReturnCodesVector.getGroup(), genCMTableInput.getGroup());
		}
	}

	@Nested
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class GenCMTableOutputTest {

		private Stream<Arguments> provideNullInputsForGenCMTableOutput() {
			return Stream.of(
					Arguments.of(null, Collections.emptyList(), Collections.emptyList()),
					Arguments.of(new HashMap<>(), null, Collections.emptyList()),
					Arguments.of(new HashMap<>(), Collections.emptyList(), null)
			);
		}

		@ParameterizedTest
		@MethodSource("provideNullInputsForGenCMTableOutput")
		@DisplayName("calling GenCMTableOutput constructor with null values throws NullPointerException")
		void genCMTableOutputWithNullValuesThrows(final Map<String, String> returnCodesMappingTable, final List<List<String>> shortChoiceReturnCodes,
				final List<String> shortVoteCastReturnCodes) {
			final GenCMTableOutput.Builder builder = new GenCMTableOutput.Builder().setReturnCodesMappingTable(returnCodesMappingTable)
					.setShortChoiceReturnCodes(shortChoiceReturnCodes).setShortVoteCastReturnCodes(shortVoteCastReturnCodes);
			assertThrows(NullPointerException.class, builder::build);
		}

		@Test
		@DisplayName("calling GenCMTableOutput constructor with empty values throws IllegalArgumentException")
		void genCMTableOutputWithEmptyValuesThrows() {
			assertAll(
					() -> {
						final HashMap<String, String> returnCodesMappingTable = new HashMap<>();
						final GenCMTableOutput.Builder builder = new GenCMTableOutput.Builder().setReturnCodesMappingTable(returnCodesMappingTable)
								.setShortChoiceReturnCodes(shortCC).setShortVoteCastReturnCodes(shortVCC);
						final IllegalArgumentException returnCodesMappingTableException = assertThrows(IllegalArgumentException.class,
								builder::build);
						assertEquals("Return Codes Mapping table must not be empty.", returnCodesMappingTableException.getMessage());
					},
					() -> {
						final List<List<String>> shortChoiceReturnCodes = Collections.emptyList();
						final GenCMTableOutput.Builder builder = new GenCMTableOutput.Builder().setReturnCodesMappingTable(CMtable)
								.setShortChoiceReturnCodes(shortChoiceReturnCodes).setShortVoteCastReturnCodes(shortVCC);
						final IllegalArgumentException shortChoiceReturnCodesException = assertThrows(IllegalArgumentException.class, builder::build);
						assertEquals("Short Choice Return Codes must not be empty.", shortChoiceReturnCodesException.getMessage());
					},
					() -> {
						final List<List<String>> shortChoiceReturnCodes = Collections.singletonList(Collections.emptyList());
						final GenCMTableOutput.Builder builder = new GenCMTableOutput.Builder().setReturnCodesMappingTable(CMtable)
								.setShortChoiceReturnCodes(shortChoiceReturnCodes).setShortVoteCastReturnCodes(shortVCC);
						final IllegalArgumentException shortChoiceReturnCodesElementsException = assertThrows(IllegalArgumentException.class,
								builder::build);
						assertEquals("Short Choice Return Codes must not contain empty lists.", shortChoiceReturnCodesElementsException.getMessage());
					},
					() -> {
						final List<String> shortVoteCastReturnCodes = Collections.emptyList();
						final GenCMTableOutput.Builder builder = new GenCMTableOutput.Builder().setReturnCodesMappingTable(CMtable)
								.setShortChoiceReturnCodes(shortCC).setShortVoteCastReturnCodes(shortVoteCastReturnCodes);
						final IllegalArgumentException shortVoteCastReturnCodesException = assertThrows(IllegalArgumentException.class,
								builder::build);
						assertEquals("Vote Cast Return Codes must not be empty.", shortVoteCastReturnCodesException.getMessage());
					}
			);
		}

		@Test
		@DisplayName("calling GenCMTableOutput constructor with invalid key length throws IllegalArgumentException")
		void genCMTableOutputWithInvalidKeyLengthThrows() {
			final HashMap<String, String> invalidCMtable = new HashMap<>();
			invalidCMtable.put(randomService.genRandomBase64String(BASE64_ENCODE_HASH_OUTPUT_LENGTH), "hash");
			invalidCMtable.put(randomService.genRandomBase64String(BASE64_ENCODE_HASH_OUTPUT_LENGTH + 1), "hash");
			final GenCMTableOutput.Builder builder = new GenCMTableOutput.Builder().setReturnCodesMappingTable(invalidCMtable)
					.setShortChoiceReturnCodes(shortCC).setShortVoteCastReturnCodes(shortVCC);
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, builder::build);
			assertEquals(String.format("Return Codes Mapping table keys must have a length of %s.", BASE64_ENCODE_HASH_OUTPUT_LENGTH),
					exception.getMessage());
		}

		@Test
		@DisplayName("calling GenCMTableOutput constructor with invalid choice code length throws IllegalArgumentException")
		void genCMTableOutputWithInvalidChoiceCodeLengthThrows() {
			final List<String> choiceCodes = Arrays.asList(randomService.genRandomBase64String(CHOICE_RETURN_CODES_LENGTH),
					randomService.genRandomBase64String(CHOICE_RETURN_CODES_LENGTH + 1));
			final List<List<String>> invalidShortCC = Arrays.asList(choiceCodes, choiceCodes);
			final GenCMTableOutput.Builder builder = new GenCMTableOutput.Builder().setReturnCodesMappingTable(CMtable)
					.setShortChoiceReturnCodes(invalidShortCC).setShortVoteCastReturnCodes(shortVCC);
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, builder::build);
			assertEquals(String.format("Short Choice Return Codes values must have a length of %s.", CHOICE_RETURN_CODES_LENGTH),
					exception.getMessage());
		}

		@Test
		@DisplayName("calling GenCMTableOutput constructor with invalid vote cast code length throws IllegalArgumentException")
		void genCMTableOutputWithInvalidVoteCastCodeLengthThrows() {
			final List<String> invalidShortVCC = Arrays.asList(randomService.genRandomBase64String(VOTE_CAST_RETURN_CODE_LENGTH),
					randomService.genRandomBase64String(VOTE_CAST_RETURN_CODE_LENGTH + 1));
			final GenCMTableOutput.Builder builder = new GenCMTableOutput.Builder().setReturnCodesMappingTable(CMtable)
					.setShortChoiceReturnCodes(shortCC).setShortVoteCastReturnCodes(invalidShortVCC);
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, builder::build);
			assertEquals(String.format("Short Vote Cast Return Codes values must have a length of %s.", VOTE_CAST_RETURN_CODE_LENGTH),
					exception.getMessage());
		}

		@Test
		@DisplayName("calling GenCMTableOutput constructor with invalid short code list size throws IllegalArgumentException")
		void genCMTableOutputWithInvalidShortCodeListSizeThrows() {
			final List<String> invalidShortVCC = new ArrayList<>(shortVCC);
			invalidShortVCC.add(randomService.genRandomBase64String(VOTE_CAST_RETURN_CODE_LENGTH));
			final GenCMTableOutput.Builder builder = new GenCMTableOutput.Builder().setReturnCodesMappingTable(CMtable)
					.setShortChoiceReturnCodes(shortCC).setShortVoteCastReturnCodes(invalidShortVCC);
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, builder::build);
			assertEquals("Short Choice Return Codes and short Vote Cast Return Codes must have the same number of elements.", exception.getMessage());
		}

		@Test
		@DisplayName("calling GenCMTableOutput constructor with invalid code mapping table size throws IllegalArgumentException")
		void genCMTableOutputWithInvalidCodeMappingTableSizeThrows() {
			final HashMap<String, String> invalidCMtable = new HashMap<>(CMtable);
			invalidCMtable.put(randomService.genRandomBase64String(BASE64_ENCODE_HASH_OUTPUT_LENGTH), "hash7");
			final int expectedReturnCodesMappingTableSize = shortCC.size() * (shortCC.get(0).size() + 1);
			final GenCMTableOutput.Builder builder = new GenCMTableOutput.Builder().setReturnCodesMappingTable(invalidCMtable)
					.setShortChoiceReturnCodes(shortCC).setShortVoteCastReturnCodes(shortVCC);
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, builder::build);
			assertEquals(String.format("Return Codes Mapping table must have a size of %s.", expectedReturnCodesMappingTableSize),
					exception.getMessage());
		}

		@Test
		@DisplayName("calling GenCMTableOutput constructor with valid input does not throw")
		void genCMTableOutputWithValidValues() {
			assertDoesNotThrow(() -> new GenCMTableOutput.Builder().setReturnCodesMappingTable(CMtable).setShortChoiceReturnCodes(shortCC)
					.setShortVoteCastReturnCodes(shortVCC).build());
		}

		@Test
		@DisplayName("calling GenCMTableOutput getters")
		void genCMTableOutputGetters() {
			final GenCMTableOutput genCMTableOutput = new GenCMTableOutput.Builder().setReturnCodesMappingTable(CMtable)
					.setShortChoiceReturnCodes(shortCC).setShortVoteCastReturnCodes(shortVCC).build();
			assertEquals(CMtable, genCMTableOutput.getReturnCodesMappingTable());
			assertEquals(shortCC, genCMTableOutput.getShortChoiceReturnCodes());
			assertEquals(shortVCC, genCMTableOutput.getShortVoteCastReturnCodes());
		}
	}

	@Nested
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class GenCMTableTest {

		private GenCMTableService genCMTableService;
		private BallotService ballotService;

		@BeforeAll
		void setup() {
			final KDFService kdfService = KDFService.getInstance();
			final HashService hashService = HashService.getInstance();
			final ElGamalService elGamalService = new ElGamalService();
			final SymmetricService symmetricService = new SymmetricService();

			ballotService = mock(BallotService.class);

			genCMTableService = new GenCMTableService(kdfService, hashService, ballotService, randomService, elGamalService, symmetricService);
		}

		@Test
		@DisplayName("calling genCMTable algorithm with null values throws NullPointerException")
		void genCMTableWithNullValuesThrows() {
			assertThrows(NullPointerException.class, () -> genCMTableService.genCMTable(null, genCMTableInput));
			assertThrows(NullPointerException.class, () -> genCMTableService.genCMTable(genCMTableContext, null));
		}

		@Test
		@DisplayName("calling genCMTable algorithm with inconsistent group throws IllegalArgumentException")
		void genCMTableWithInconsistentGroupThrows() {
			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> otherPreChoiceReturnCodesVector = otherElGamalGenerator.genRandomCiphertextVector(
					3, 1);
			final GroupVector<GqElement, GqGroup> otherPreVoteCastReturnCodesVector =
					GroupVector.from(otherElGamalGenerator.genRandomCiphertextVector(3, 1)
							.stream().map(ElGamalMultiRecipientCiphertext::getGamma).collect(Collectors.toList()));
			final GenCMTableInput otherGenCMTableInput = new GenCMTableInput.Builder().setVerificationCardIds(VERIFICATION_CARD_IDS)
					.setEncryptedPreChoiceReturnCodes(otherPreChoiceReturnCodesVector)
					.setPreVoteCastReturnCodes(otherPreVoteCastReturnCodesVector).build();

			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> genCMTableService.genCMTable(genCMTableContext, otherGenCMTableInput));
			assertEquals("The context and input must have the same group.", exception.getMessage());
		}

		@Test
		@DisplayName("calling genCMTable algorithm with too many pre-Choice Return Codes throws IllegalArgumentException")
		void genCMTableWithTooManyChoiceReturnCodesThrows() {
			final GroupVector<ElGamalMultiRecipientCiphertext, GqGroup> otherPreChoiceReturnCodesVector = elGamalGenerator.genRandomCiphertextVector(
					3, OMEGA + 1);
			final GenCMTableInput otherGenCMTableInput = new GenCMTableInput.Builder().setVerificationCardIds(VERIFICATION_CARD_IDS)
					.setEncryptedPreChoiceReturnCodes(otherPreChoiceReturnCodesVector)
					.setPreVoteCastReturnCodes(preVoteCastReturnCodesVector).build();

			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> genCMTableService.genCMTable(genCMTableContext, otherGenCMTableInput));
			assertEquals("There cannot be more encrypted pre-Choice Return codes elements than secret key elements.", exception.getMessage());
		}

		@Test
		@DisplayName("calling genCMTable algorithm with valid input does not throw")
		void genCMTableWithValidValues() throws IOException {
			when(ballotService.getBallot(anyString(), anyString())).thenReturn(getBallot());
			assertDoesNotThrow(() -> genCMTableService.genCMTable(genCMTableContext, genCMTableInput));
		}

		@Test
		@DisplayName("calling genCMTable algorithm orders the CMTable by keys")
		void genCMTableOrdersCMTable() throws IOException {
			when(ballotService.getBallot(anyString(), anyString())).thenReturn(getBallot());
			final Set<String> keySetCMTable = genCMTableService.genCMTable(genCMTableContext, genCMTableInput).getReturnCodesMappingTable().keySet();
			final List<String> sortedKeyListCMTable = keySetCMTable.stream().sorted().collect(Collectors.toList());
			assertEquals(sortedKeyListCMTable, new ArrayList<>(keySetCMTable));
		}

		private Ballot getBallot() throws IOException {
			final URL resource = GenCMTableTest.class.getClassLoader().getResource("GenCMTableServiceTest/ballot.json");
			return new ObjectMapper().readValue(resource, Ballot.class);
		}
	}
}