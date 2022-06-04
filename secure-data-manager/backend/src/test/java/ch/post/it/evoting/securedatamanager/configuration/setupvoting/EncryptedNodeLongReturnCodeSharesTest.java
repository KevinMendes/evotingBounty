/*
 * (c) Copyright 2022. Swiss Post Ltd
 */
package ch.post.it.evoting.securedatamanager.configuration.setupvoting;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.exceptions.FailedValidationException;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.test.tools.data.GroupTestData;
import ch.post.it.evoting.cryptoprimitives.test.tools.generator.ElGamalGenerator;

@DisplayName("EncryptedNodeLongReturnCodeShares")
class EncryptedNodeLongReturnCodeSharesTest {

	private static final String VALID_ID = "1de81acb944d4161a7148a2240edea47";
	private static final String INVALID_ID = "invalid id";
	private static ElGamalMultiRecipientCiphertext ciphertext;
	private static EncryptedSingleNodeLongReturnCodeShares encryptedSingleNodeLongReturnCodeShares;

	@BeforeAll
	static void setup() {
		final GqGroup gqGroup = GroupTestData.getGqGroup();
		final ElGamalGenerator elGamalGenerator = new ElGamalGenerator(gqGroup);
		ciphertext = elGamalGenerator.genRandomCiphertext(1);
		encryptedSingleNodeLongReturnCodeShares = new EncryptedSingleNodeLongReturnCodeShares.Builder()
				.setNodeId(1)
				.setVerificationCardIds(Collections.singletonList(VALID_ID))
				.setExponentiatedEncryptedPartialChoiceReturnCodes(Collections.singletonList(ciphertext))
				.setExponentiatedEncryptedConfirmationKeys(Collections.singletonList(ciphertext))
				.build();
	}

	private static Stream<Arguments> provideNullInputsForEncryptedNodeLongReturnCodeShares() {
		return Stream.of(
				Arguments.of(null, VALID_ID, Collections.singletonList(VALID_ID), Collections.singletonList(encryptedSingleNodeLongReturnCodeShares)),
				Arguments.of(VALID_ID, null, Collections.singletonList(VALID_ID), Collections.singletonList(encryptedSingleNodeLongReturnCodeShares)),
				Arguments.of(VALID_ID, VALID_ID, null, Collections.singletonList(encryptedSingleNodeLongReturnCodeShares)),
				Arguments.of(VALID_ID, VALID_ID, Collections.singletonList(VALID_ID), null)
		);
	}

	@ParameterizedTest
	@MethodSource("provideNullInputsForEncryptedNodeLongReturnCodeShares")
	@DisplayName("calling builder with null values throws NullPointerException")
	void buildWithInvalidValuesThrows(final String electionEventId, final String verificationCardSetId, final List<String> verificationCardIds,
			final List<EncryptedSingleNodeLongReturnCodeShares> nodeReturnCodesValues) {
		final EncryptedNodeLongReturnCodeShares.Builder builder = new EncryptedNodeLongReturnCodeShares.Builder()
				.setElectionEventId(electionEventId)
				.setVerificationCardSetId(verificationCardSetId)
				.setVerificationCardIds(verificationCardIds)
				.setNodeReturnCodesValues(nodeReturnCodesValues);
		assertThrows(NullPointerException.class, builder::build);
	}

	private static Stream<Arguments> provideInvalidUUIDInputsForEncryptedNodeLongReturnCodeShares() {
		return Stream.of(
				Arguments.of(INVALID_ID, VALID_ID, Collections.singletonList(VALID_ID)),
				Arguments.of(VALID_ID, INVALID_ID, Collections.singletonList(VALID_ID)),
				Arguments.of(VALID_ID, VALID_ID, Collections.singletonList(INVALID_ID))
		);
	}

	@ParameterizedTest
	@MethodSource("provideInvalidUUIDInputsForEncryptedNodeLongReturnCodeShares")
	@DisplayName("calling builder with null values throws FailedValidationException")
	void buildWithInvalidUUIDValuesThrows(final String electionEventId, final String verificationCardSetId, final List<String> verificationCardIds) {
		final EncryptedNodeLongReturnCodeShares.Builder builder = new EncryptedNodeLongReturnCodeShares.Builder()
				.setElectionEventId(electionEventId)
				.setVerificationCardSetId(verificationCardSetId)
				.setVerificationCardIds(verificationCardIds);
		assertThrows(FailedValidationException.class, builder::build);
	}

	@Test
	@DisplayName("calling builder with invalid node id values throws IllegalArgumentException")
	void buildWithInvalidNodeIdValues() {
		final EncryptedSingleNodeLongReturnCodeShares.Builder encryptedSingleNodeLongReturnCodeSharesBuilder = new EncryptedSingleNodeLongReturnCodeShares.Builder()
				.setVerificationCardIds(Arrays.asList(VALID_ID, VALID_ID))
				.setExponentiatedEncryptedPartialChoiceReturnCodes(Arrays.asList(ciphertext, ciphertext))
				.setExponentiatedEncryptedConfirmationKeys(Arrays.asList(ciphertext, ciphertext));

		// Wrong size node return codes values
		final List<EncryptedSingleNodeLongReturnCodeShares> wrongSizeNodeReturnCodesValues = Arrays.asList(
				encryptedSingleNodeLongReturnCodeSharesBuilder.setNodeId(1).build(),
				encryptedSingleNodeLongReturnCodeSharesBuilder.setNodeId(2).build(),
				encryptedSingleNodeLongReturnCodeSharesBuilder.setNodeId(3).build()
		);
		final EncryptedNodeLongReturnCodeShares.Builder wrongSizeBuilder = new EncryptedNodeLongReturnCodeShares.Builder()
				.setElectionEventId(VALID_ID)
				.setVerificationCardSetId(VALID_ID)
				.setVerificationCardIds(Arrays.asList(VALID_ID, VALID_ID))
				.setNodeReturnCodesValues(wrongSizeNodeReturnCodesValues);

		// Missing node id return codes values
		final List<EncryptedSingleNodeLongReturnCodeShares> missingNodeIdReturnCodesValues = Arrays.asList(
				encryptedSingleNodeLongReturnCodeSharesBuilder.setNodeId(1).build(),
				encryptedSingleNodeLongReturnCodeSharesBuilder.setNodeId(2).build(),
				encryptedSingleNodeLongReturnCodeSharesBuilder.setNodeId(3).build(),
				encryptedSingleNodeLongReturnCodeSharesBuilder.setNodeId(3).build()
		);
		final EncryptedNodeLongReturnCodeShares.Builder missingNodeIdBuilder = new EncryptedNodeLongReturnCodeShares.Builder()
				.setElectionEventId(VALID_ID)
				.setVerificationCardSetId(VALID_ID)
				.setVerificationCardIds(Arrays.asList(VALID_ID, VALID_ID))
				.setNodeReturnCodesValues(missingNodeIdReturnCodesValues);

		// Wrong node id return codes values
		final List<EncryptedSingleNodeLongReturnCodeShares> wrongNodeIdReturnCodesValues = Arrays.asList(
				encryptedSingleNodeLongReturnCodeSharesBuilder.setNodeId(1).build(),
				encryptedSingleNodeLongReturnCodeSharesBuilder.setNodeId(2).build(),
				encryptedSingleNodeLongReturnCodeSharesBuilder.setNodeId(3).build(),
				encryptedSingleNodeLongReturnCodeSharesBuilder.setNodeId(3).build()
		);
		final EncryptedNodeLongReturnCodeShares.Builder wrongNodeIdBuilder = new EncryptedNodeLongReturnCodeShares.Builder()
				.setElectionEventId(VALID_ID)
				.setVerificationCardSetId(VALID_ID)
				.setVerificationCardIds(Arrays.asList(VALID_ID, VALID_ID))
				.setNodeReturnCodesValues(wrongNodeIdReturnCodesValues);

		assertAll(
				() -> assertThrows(IllegalArgumentException.class, wrongSizeBuilder::build),
				() -> assertThrows(IllegalArgumentException.class, missingNodeIdBuilder::build),
				() -> assertThrows(IllegalArgumentException.class, wrongNodeIdBuilder::build)
		);

	}

	@Test
	@DisplayName("calling builder with valid values does not throw")
	void buildWithValidValues() {
		final EncryptedSingleNodeLongReturnCodeShares.Builder encryptedSingleNodeLongReturnCodeSharesBuilder = new EncryptedSingleNodeLongReturnCodeShares.Builder()
				.setVerificationCardIds(Arrays.asList(VALID_ID, VALID_ID))
				.setExponentiatedEncryptedPartialChoiceReturnCodes(Arrays.asList(ciphertext, ciphertext))
				.setExponentiatedEncryptedConfirmationKeys(Arrays.asList(ciphertext, ciphertext));
		final List<EncryptedSingleNodeLongReturnCodeShares> nodeReturnCodesValues = Arrays.asList(
				encryptedSingleNodeLongReturnCodeSharesBuilder.setNodeId(1).build(),
				encryptedSingleNodeLongReturnCodeSharesBuilder.setNodeId(2).build(),
				encryptedSingleNodeLongReturnCodeSharesBuilder.setNodeId(3).build(),
				encryptedSingleNodeLongReturnCodeSharesBuilder.setNodeId(4).build()
		);
		final EncryptedNodeLongReturnCodeShares.Builder builder = new EncryptedNodeLongReturnCodeShares.Builder()
				.setElectionEventId(VALID_ID)
				.setVerificationCardSetId(VALID_ID)
				.setVerificationCardIds(Arrays.asList(VALID_ID, VALID_ID))
				.setNodeReturnCodesValues(nodeReturnCodesValues);
		assertDoesNotThrow(builder::build);
	}

}