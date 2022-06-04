/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.configuration.setupvoting;

import static ch.post.it.evoting.controlcomponents.configuration.setupvoting.GenKeysCCRService.GenKeysCCROutput;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import com.google.common.base.Throwables;

import ch.post.it.evoting.cryptoprimitives.domain.VotingOptionsConstants;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientKeyPair;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.RandomService;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;
import ch.post.it.evoting.cryptoprimitives.test.tools.data.GroupTestData;
import ch.post.it.evoting.cryptoprimitives.test.tools.generator.ZqGroupGenerator;

@DisplayName("A GenKeysCCRService")
class GenKeysCCRServiceTest {

	private static final int PHI = VotingOptionsConstants.MAXIMUM_NUMBER_OF_SELECTABLE_VOTING_OPTIONS;

	private static GenKeysCCRService genKeysCCRService;
	private static GqGroup group;

	@BeforeAll
	static void setUpAll() {
		genKeysCCRService = new GenKeysCCRService(new RandomService());
		group = GroupTestData.getGqGroup();
	}

	@Test
	@DisplayName("valid parameter does not throw")
	void validParamDoesNotThrow() {
		assertDoesNotThrow(() -> genKeysCCRService.genKeysCCR(group));
	}

	@Test
	@DisplayName("null parameter throws NullPointerException")
	void nullParamThrows() {
		assertThrows(NullPointerException.class, () -> genKeysCCRService.genKeysCCR(null));
	}

	@Nested
	@DisplayName("A GenKeysCCROutput built with")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class GenKeysCCROutputTest {

		private final RandomService randomService = new RandomService();

		private ElGamalMultiRecipientKeyPair keyPair;
		private ZqElement generationSecretKey;

		@BeforeAll
		void setUpAll() {
			keyPair = ElGamalMultiRecipientKeyPair.genKeyPair(group, PHI, randomService);
			generationSecretKey = new ZqGroupGenerator(ZqGroup.sameOrderAs(group)).genRandomZqElementMember();
		}

		@Test
		@DisplayName("valid param gives expected output")
		void expectOutput() {
			final GenKeysCCROutput genKeysCCROutput = new GenKeysCCROutput(keyPair, generationSecretKey);

			final GqGroup keyPairGroup = genKeysCCROutput.getCcrjChoiceReturnCodesEncryptionKeyPair().getGroup();
			final ZqGroup generationKeyGroup = genKeysCCROutput.getCcrjReturnCodesGenerationSecretKey().getGroup();
			assertTrue(keyPairGroup.hasSameOrderAs(generationKeyGroup));
		}

		@Test
		@DisplayName("any null parameter throws NullPointerException")
		void nullParamThrows() {
			assertThrows(NullPointerException.class, () -> new GenKeysCCROutput(null, generationSecretKey));
			assertThrows(NullPointerException.class, () -> new GenKeysCCROutput(keyPair, null));
		}

		@Test
		@DisplayName("wrong size keypair throws IllegalArgumentException")
		void wrongSizeKeyPairThrows() {
			final ElGamalMultiRecipientKeyPair wrongSizeKeyPair = ElGamalMultiRecipientKeyPair.genKeyPair(group, 1, randomService);

			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> new GenKeysCCROutput(wrongSizeKeyPair, generationSecretKey));
			final String message = String.format("The ccrj Choice Return Codes encryption key pair must be of size phi. [phi: %s]", PHI);
			assertEquals(message, Throwables.getRootCause(exception).getMessage());
		}

		@Test
		@DisplayName("wrong groups throws IllegalArgumentException")
		void wrongGroupsThrows() {
			final GqGroup otherGroup = GroupTestData.getDifferentGqGroup(group);
			final ZqElement otherGenerationKey = new ZqGroupGenerator(ZqGroup.sameOrderAs(otherGroup)).genRandomZqElementMember();

			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> new GenKeysCCROutput(keyPair, otherGenerationKey));
			assertEquals("The ccrj Return Codes generation secret key must have the same order than the ccr Choice Return Codes encryption key pair.",
					Throwables.getRootCause(exception).getMessage());
		}

	}

}