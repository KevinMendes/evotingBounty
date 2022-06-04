/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.configuration.setuptally;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import ch.post.it.evoting.cryptoprimitives.domain.VotingOptionsConstants;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientKeyPair;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalService;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.RandomService;
import ch.post.it.evoting.cryptoprimitives.test.tools.data.GroupTestData;
import ch.post.it.evoting.cryptoprimitives.test.tools.generator.GqGroupGenerator;

@DisplayName("A SetupTallyEBService calling setupTallyEB with")
class SetupTallyEBServiceTest {
	private static final int MU = VotingOptionsConstants.MAXIMUM_NUMBER_OF_WRITE_IN_OPTIONS + 1;
	private static final int MAX_WRITE_INS_IN_ALL_VERIFICATION_CARD_SETS = 1;
	private static final int DELTA = MAX_WRITE_INS_IN_ALL_VERIFICATION_CARD_SETS + 1;

	private static SetupTallyEBService setupTallyEBService;
	private static GqGroup gqGroup;
	private static GqGroupGenerator gqGroupGenerator;
	private static GroupVector<ElGamalMultiRecipientPublicKey, GqGroup> ccmElectionPublicKeys;

	@BeforeAll
	static void setUpAll() {
		setupTallyEBService = new SetupTallyEBService(new RandomService(), new ElGamalService());
		gqGroup = GroupTestData.getGqGroup();
		gqGroupGenerator = new GqGroupGenerator(gqGroup);

		final ElGamalMultiRecipientPublicKey ccmElectionPublicKey1 =
				new ElGamalMultiRecipientPublicKey(gqGroupGenerator.genRandomGqElementVector(MU));
		final ElGamalMultiRecipientPublicKey ccmElectionPublicKey2 =
				new ElGamalMultiRecipientPublicKey(gqGroupGenerator.genRandomGqElementVector(MU));
		final ElGamalMultiRecipientPublicKey ccmElectionPublicKey3 =
				new ElGamalMultiRecipientPublicKey(gqGroupGenerator.genRandomGqElementVector(MU));
		final ElGamalMultiRecipientPublicKey ccmElectionPublicKey4 =
				new ElGamalMultiRecipientPublicKey(gqGroupGenerator.genRandomGqElementVector(MU));

		ccmElectionPublicKeys = GroupVector.of(ccmElectionPublicKey1, ccmElectionPublicKey2, ccmElectionPublicKey3, ccmElectionPublicKey4);
	}

	@Test
	@DisplayName("a valid SetupTallyEBInput does not throw any Exception.")
	void validParamDoesNotThrow() {
		final SetupTallyEBService.SetupTallyEBInput input =
				new SetupTallyEBService.SetupTallyEBInput(ccmElectionPublicKeys, MAX_WRITE_INS_IN_ALL_VERIFICATION_CARD_SETS);

		assertDoesNotThrow(() -> setupTallyEBService.setupTallyEB(input));
	}

	@Test
	@DisplayName("a valid SetupTallyEBInput returns a non-null SetupTallyEBOutput with expected content.")
	void nonNullOutput() {
		final SetupTallyEBService.SetupTallyEBInput input =
				new SetupTallyEBService.SetupTallyEBInput(ccmElectionPublicKeys, MAX_WRITE_INS_IN_ALL_VERIFICATION_CARD_SETS);

		final SetupTallyEBService.SetupTallyEBOutput output = setupTallyEBService.setupTallyEB(input);
		final ElGamalMultiRecipientPublicKey electionPublicKey = output.getElectionPublicKey();
		final ElGamalMultiRecipientKeyPair electoralBoardKeyPair = output.getElectoralBoardKeyPair();

		assertNotNull(output);
		assertNotNull(electoralBoardKeyPair);
		assertEquals(DELTA, electionPublicKey.size());
		assertEquals(DELTA, electoralBoardKeyPair.getPrivateKey().size());
	}

	@Test
	@DisplayName("a null SetupTallyEBInput throws a NullPointerException.")
	void nullParamThrowsANullPointer() {
		assertThrows(NullPointerException.class, () -> setupTallyEBService.setupTallyEB(null));
	}

	@Nested
	@DisplayName("a SetupTallyEBInput built with")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class SetupTallyEBInputTest {

		@Test
		@DisplayName("null election public keys throws a NullPointerException.")
		void nullEPKThrowsANullPointer() {
			assertThrows(NullPointerException.class,
					() -> new SetupTallyEBService.SetupTallyEBInput(null, MAX_WRITE_INS_IN_ALL_VERIFICATION_CARD_SETS));
		}

		@Test
		@DisplayName("election public keys of size different than μ throws an IllegalArgumentException.")
		void nonValidSizeEPKThrowsAnIllegalArgument() {

			final GqGroup otherGqGroup = GroupTestData.getDifferentGqGroup(gqGroup);
			final GqGroupGenerator otherGqGroupGenerator = new GqGroupGenerator(otherGqGroup);
			final int otherSize = MU + 1;

			final GroupVector<ElGamalMultiRecipientPublicKey, GqGroup> ccmElectionPublicKeysOtherSize = GroupVector.of(
					new ElGamalMultiRecipientPublicKey(otherGqGroupGenerator.genRandomGqElementVector(otherSize)),
					new ElGamalMultiRecipientPublicKey(otherGqGroupGenerator.genRandomGqElementVector(otherSize)),
					new ElGamalMultiRecipientPublicKey(otherGqGroupGenerator.genRandomGqElementVector(otherSize)));

			assertThrows(IllegalArgumentException.class,
					() -> new SetupTallyEBService.SetupTallyEBInput(ccmElectionPublicKeysOtherSize, MAX_WRITE_INS_IN_ALL_VERIFICATION_CARD_SETS));
		}

		@Test
		@DisplayName("a number election public keys different than 4 throws an IllegalArgumentException.")
		void nonValidNumberOfEPKThrowsAnIllegalArgumentr() {

			final ElGamalMultiRecipientPublicKey ccmElectionPublicKey1 =
					new ElGamalMultiRecipientPublicKey(gqGroupGenerator.genRandomGqElementVector(MU));
			final ElGamalMultiRecipientPublicKey ccmElectionPublicKey2 =
					new ElGamalMultiRecipientPublicKey(gqGroupGenerator.genRandomGqElementVector(MU));
			final ElGamalMultiRecipientPublicKey ccmElectionPublicKey3 =
					new ElGamalMultiRecipientPublicKey(gqGroupGenerator.genRandomGqElementVector(MU));
			final GroupVector<ElGamalMultiRecipientPublicKey, GqGroup> smallCcmElectionPublicKeys =
					GroupVector.of(ccmElectionPublicKey1, ccmElectionPublicKey2, ccmElectionPublicKey3);

			assertThrows(IllegalArgumentException.class,
					() -> new SetupTallyEBService.SetupTallyEBInput(smallCcmElectionPublicKeys, MAX_WRITE_INS_IN_ALL_VERIFICATION_CARD_SETS));

			final ElGamalMultiRecipientPublicKey ccmElectionPublicKey4 =
					new ElGamalMultiRecipientPublicKey(gqGroupGenerator.genRandomGqElementVector(MU));
			final ElGamalMultiRecipientPublicKey ccmElectionPublicKey5 =
					new ElGamalMultiRecipientPublicKey(gqGroupGenerator.genRandomGqElementVector(MU));

			final GroupVector<ElGamalMultiRecipientPublicKey, GqGroup> bigCcmElectionPublicKeys =
					GroupVector.of(ccmElectionPublicKey1, ccmElectionPublicKey2, ccmElectionPublicKey3, ccmElectionPublicKey4, ccmElectionPublicKey5);

			assertThrows(IllegalArgumentException.class,
					() -> new SetupTallyEBService.SetupTallyEBInput(bigCcmElectionPublicKeys, MAX_WRITE_INS_IN_ALL_VERIFICATION_CARD_SETS));
		}

		@Test
		@DisplayName("a negative maximum number of write-ins in all verification card sets throws an IllegalArgumentException.")
		void nonValidMaxThrowsANullPointer() {

			final int nonValidMaxWriteInsInAllVerificationCardSets = -1;

			assertThrows(IllegalArgumentException.class,
					() -> new SetupTallyEBService.SetupTallyEBInput(ccmElectionPublicKeys, nonValidMaxWriteInsInAllVerificationCardSets));
		}
	}

}
