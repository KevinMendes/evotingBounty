/*
 * (c) Copyright 2022. Swiss Post Ltd
 */
package ch.post.it.evoting.securedatamanager.configuration.setupvoting;

import static ch.post.it.evoting.securedatamanager.commons.Constants.NODE_IDS;
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

@DisplayName("EncryptedSingleNodeLongReturnCodeShares")
class EncryptedSingleNodeLongReturnCodeSharesTest {

	private static final String VERIFICATION_CARD_ID = "1de81acb944d4161a7148a2240edea47";
	private static ElGamalMultiRecipientCiphertext ciphertext;

	@BeforeAll
	static void setup() {
		final GqGroup gqGroup = GroupTestData.getGqGroup();
		final ElGamalGenerator elGamalGenerator = new ElGamalGenerator(gqGroup);
		ciphertext = elGamalGenerator.genRandomCiphertext(1);
	}

	@Test
	@DisplayName("calling builder with invalid node id values throws IllegalArgumentException")
	void buildWithInvalidNodeIdThrows() {
		final EncryptedSingleNodeLongReturnCodeShares.Builder builder_0 = new EncryptedSingleNodeLongReturnCodeShares.Builder()
				.setNodeId(0);
		final EncryptedSingleNodeLongReturnCodeShares.Builder builder_N_plus_1 = new EncryptedSingleNodeLongReturnCodeShares.Builder()
				.setNodeId(NODE_IDS.size() + 1);
		assertAll(
				() -> assertThrows(IllegalArgumentException.class, builder_0::build),
				() -> assertThrows(IllegalArgumentException.class, builder_N_plus_1::build)
		);
	}

	private static Stream<Arguments> provideNullInputsForEncryptedSingleNodeLongReturnCodeShares() {
		return Stream.of(
				Arguments.of(null, Collections.singletonList(ciphertext), Collections.singletonList(ciphertext)),
				Arguments.of(Collections.singletonList(VERIFICATION_CARD_ID), null, Collections.singletonList(ciphertext)),
				Arguments.of(Collections.singletonList(VERIFICATION_CARD_ID), Collections.singletonList(ciphertext), null)
		);
	}

	@ParameterizedTest
	@MethodSource("provideNullInputsForEncryptedSingleNodeLongReturnCodeShares")
	@DisplayName("calling builder with null values throws NullPointerException")
	void buildWithInvalidValuesThrows(final List<String> verificationCardIds, final List<ElGamalMultiRecipientCiphertext> partialChoiceReturnCodes,
			final List<ElGamalMultiRecipientCiphertext> confirmationKeys) {
		final EncryptedSingleNodeLongReturnCodeShares.Builder builder = new EncryptedSingleNodeLongReturnCodeShares.Builder()
				.setNodeId(1)
				.setVerificationCardIds(verificationCardIds)
				.setExponentiatedEncryptedPartialChoiceReturnCodes(partialChoiceReturnCodes)
				.setExponentiatedEncryptedConfirmationKeys(confirmationKeys);
		assertThrows(NullPointerException.class, builder::build);
	}

	@Test
	@DisplayName("calling builder with invalid verification card ids values throws FailedValidationException")
	void buildWithInvalidVerificationCardIdsThrows() {
		final EncryptedSingleNodeLongReturnCodeShares.Builder builder = new EncryptedSingleNodeLongReturnCodeShares.Builder()
				.setNodeId(1)
				.setVerificationCardIds(Arrays.asList(VERIFICATION_CARD_ID, "invalid verification card id"));
		assertThrows(FailedValidationException.class, builder::build);
	}

	private static Stream<Arguments> provideWrongSizeListInputsForEncryptedSingleNodeLongReturnCodeShares() {
		return Stream.of(
				Arguments.of(
						Collections.singletonList(VERIFICATION_CARD_ID),
						Arrays.asList(ciphertext, ciphertext),
						Arrays.asList(ciphertext, ciphertext)
				),
				Arguments.of(
						Arrays.asList(VERIFICATION_CARD_ID, VERIFICATION_CARD_ID),
						Collections.singletonList(ciphertext),
						Arrays.asList(ciphertext, ciphertext)

				),
				Arguments.of(
						Arrays.asList(VERIFICATION_CARD_ID, VERIFICATION_CARD_ID),
						Arrays.asList(ciphertext, ciphertext),
						Collections.singletonList(ciphertext)
				)
		);
	}

	@ParameterizedTest
	@MethodSource("provideWrongSizeListInputsForEncryptedSingleNodeLongReturnCodeShares")
	@DisplayName("calling builder with wrong size list throws IllegalArgumentException")
	void buildWithWrongSizeListInputsThrows(final List<String> verificationCardIds,
			final List<ElGamalMultiRecipientCiphertext> partialChoiceReturnCodes,
			final List<ElGamalMultiRecipientCiphertext> confirmationKeys) {
		final EncryptedSingleNodeLongReturnCodeShares.Builder builder = new EncryptedSingleNodeLongReturnCodeShares.Builder()
				.setNodeId(1)
				.setVerificationCardIds(verificationCardIds)
				.setExponentiatedEncryptedPartialChoiceReturnCodes(partialChoiceReturnCodes)
				.setExponentiatedEncryptedConfirmationKeys(confirmationKeys);
		assertThrows(IllegalArgumentException.class, builder::build);
	}

	@Test
	@DisplayName("calling builder with valid values does not throw")
	void buildWithValidValues() {
		final EncryptedSingleNodeLongReturnCodeShares.Builder builder = new EncryptedSingleNodeLongReturnCodeShares.Builder()
				.setNodeId(1)
				.setVerificationCardIds(Arrays.asList(VERIFICATION_CARD_ID, VERIFICATION_CARD_ID))
				.setExponentiatedEncryptedPartialChoiceReturnCodes(Arrays.asList(ciphertext, ciphertext))
				.setExponentiatedEncryptedConfirmationKeys(Arrays.asList(ciphertext, ciphertext));
		assertDoesNotThrow(builder::build);
	}

}