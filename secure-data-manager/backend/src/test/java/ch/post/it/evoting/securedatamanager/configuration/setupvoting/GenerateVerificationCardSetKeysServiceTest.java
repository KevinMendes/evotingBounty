/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.configuration.setupvoting;

import static ch.post.it.evoting.cryptoprimitives.math.GqElement.GqElementFactory;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.common.base.Throwables;

import ch.post.it.evoting.cryptoprimitives.domain.VotingOptionsConstants;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalService;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.test.tools.data.GroupTestData;
import ch.post.it.evoting.cryptoprimitives.test.tools.generator.ElGamalGenerator;

@DisplayName("A GenerateVerificationCardSetKeysService")
class GenerateVerificationCardSetKeysServiceTest {

	private static final int PHI = VotingOptionsConstants.MAXIMUM_NUMBER_OF_SELECTABLE_VOTING_OPTIONS;
	private static final SecureRandom secureRandom = new SecureRandom();
	private static GenerateVerificationCardSetKeysService generateVerificationCardSetKeysService;

	@BeforeAll
	static void setup() {
		generateVerificationCardSetKeysService = new GenerateVerificationCardSetKeysService(new ElGamalService());
	}

	@Test
	@DisplayName("calling genVerCardSetKeys with null argument throws a NullPointerException")
	void testGenVerCardSetKeysWithNullArgumentThrows() {
		assertThrows(NullPointerException.class, () -> generateVerificationCardSetKeysService.genVerCardSetKeys(null));
	}

	@Test
	@DisplayName("calling genVerCardSetKeys with a too short Choice Return Codes encryption public key vector throws")
	void testGenVerCardSetKeysWithTooShortChoiceReturnCodesEncryptionPublicKeysVectorThrows() {
		final GqGroup gqGroup = GroupTestData.getGqGroup();
		final GroupVector<ElGamalMultiRecipientPublicKey, GqGroup> ccrPublicKeys = genEncryptionPublicKeysVector(gqGroup, PHI, 3);
		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> generateVerificationCardSetKeysService.genVerCardSetKeys(ccrPublicKeys));
		assertEquals("There must be exactly 4 CCR_j Choice Return Codes encryption public keys.", Throwables.getRootCause(exception).getMessage());
	}

	@Test
	@DisplayName("calling genVerCardSetKeys with a too long Choice Return Codes encryption public key vector throws")
	void testGenVerCardSetKeysWithTooLongChoiceReturnCodesEncryptionPublicKeysVectorThrows() {
		final GqGroup gqGroup = GroupTestData.getGqGroup();
		final GroupVector<ElGamalMultiRecipientPublicKey, GqGroup> ccrPublicKeys = genEncryptionPublicKeysVector(gqGroup, PHI, 5);
		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> generateVerificationCardSetKeysService.genVerCardSetKeys(ccrPublicKeys));
		assertEquals("There must be exactly 4 CCR_j Choice Return Codes encryption public keys.", Throwables.getRootCause(exception).getMessage());
	}

	@Test
	@DisplayName("calling genVerCardSetKeys with specific values gives the expected result")
	void testGenVerCardSetKeysWithSpecificValuesGivesExpectedOutput() {
		final GqGroup groupP59 = GroupTestData.getGroupP59();

		final GqElement one = GqElementFactory.fromValue(BigInteger.ONE, groupP59);
		final GqElement three = GqElementFactory.fromValue(BigInteger.valueOf(3), groupP59);
		final GqElement four = GqElementFactory.fromValue(BigInteger.valueOf(4), groupP59);
		final GqElement five = GqElementFactory.fromValue(BigInteger.valueOf(5), groupP59);
		final GqElement seven = GqElementFactory.fromValue(BigInteger.valueOf(7), groupP59);
		final GqElement nine = GqElementFactory.fromValue(BigInteger.valueOf(9), groupP59);
		final GqElement sixteen = GqElementFactory.fromValue(BigInteger.valueOf(16), groupP59);
		final GqElement twenty = GqElementFactory.fromValue(BigInteger.valueOf(20), groupP59);
		final GqElement twentyNine = GqElementFactory.fromValue(BigInteger.valueOf(29), groupP59);
		final GqElement thirtySix = GqElementFactory.fromValue(BigInteger.valueOf(36), groupP59);

		final List<GqElement> pk1BaseElements = Arrays.asList(four, nine, five, three);
		final List<GqElement> pk1Elements = IntStream.range(0, PHI).mapToObj(i -> pk1BaseElements.get(i % 4)).collect(Collectors.toList());
		final List<GqElement> pk2BaseElements = Arrays.asList(one, four, five, sixteen);
		final List<GqElement> pk2Elements = IntStream.range(0, PHI).mapToObj(i -> pk2BaseElements.get(i % 4)).collect(Collectors.toList());
		final List<GqElement> pk3BaseElements = Arrays.asList(four, three, sixteen, nine);
		final List<GqElement> pk3Elements = IntStream.range(0, PHI).mapToObj(i -> pk3BaseElements.get(i % 4)).collect(Collectors.toList());
		final List<GqElement> pk4BaseElements = Arrays.asList(sixteen, three, four, five);
		final List<GqElement> pk4Elements = IntStream.range(0, PHI).mapToObj(i -> pk4BaseElements.get(i % 4)).collect(Collectors.toList());

		final ElGamalMultiRecipientPublicKey pk1 = new ElGamalMultiRecipientPublicKey(pk1Elements);
		final ElGamalMultiRecipientPublicKey pk2 = new ElGamalMultiRecipientPublicKey(pk2Elements);
		final ElGamalMultiRecipientPublicKey pk3 = new ElGamalMultiRecipientPublicKey(pk3Elements);
		final ElGamalMultiRecipientPublicKey pk4 = new ElGamalMultiRecipientPublicKey(pk4Elements);
		final GroupVector<ElGamalMultiRecipientPublicKey, GqGroup> encryptionPublicKeys = GroupVector.of(pk1, pk2, pk3, pk4);

		final List<GqElement> expectedBaseElements = Arrays.asList(twenty, twentyNine, seven, thirtySix);
		final List<GqElement> expectedElements = IntStream.range(0, PHI).mapToObj(i -> expectedBaseElements.get(i % 4)).collect(Collectors.toList());
		final ElGamalMultiRecipientPublicKey expected = new ElGamalMultiRecipientPublicKey(expectedElements);

		assertEquals(expected, generateVerificationCardSetKeysService.genVerCardSetKeys(encryptionPublicKeys));
	}

	@Test
	@DisplayName("calling genVerCardSetKeys with a too few Choice Return Codes encryption public key elements throws")
	void testGenVerCardSetKeysWithTooFewKeyElementsThrows() {
		final GqGroup gqGroup = GroupTestData.getGqGroup();
		final GroupVector<ElGamalMultiRecipientPublicKey, GqGroup> ccrPublicKeys = genEncryptionPublicKeysVector(gqGroup, PHI - 1, 4);
		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> generateVerificationCardSetKeysService.genVerCardSetKeys(ccrPublicKeys));
		final String expectedErrorMessage = String.format("The CCR_j Choice Return Codes encryption public keys must be of size %d", PHI);
		assertEquals(expectedErrorMessage, Throwables.getRootCause(exception).getMessage());
	}

	@Test
	@DisplayName("calling genVerCardSetKeys with a too many Choice Return Codes encryption public key elements throws")
	void testGenVerCardSetKeysWithTooManyKeyElementsThrows() {
		final GqGroup gqGroup = GroupTestData.getGqGroup();
		final GroupVector<ElGamalMultiRecipientPublicKey, GqGroup> ccrPublicKeys = genEncryptionPublicKeysVector(gqGroup, PHI + 1, 4);
		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> generateVerificationCardSetKeysService.genVerCardSetKeys(ccrPublicKeys));
		final String expectedErrorMessage = String.format("The CCR_j Choice Return Codes encryption public keys must be of size %d", PHI);
		assertEquals(expectedErrorMessage, Throwables.getRootCause(exception).getMessage());
	}

	private GroupVector<ElGamalMultiRecipientPublicKey, GqGroup> genEncryptionPublicKeysVector(final GqGroup gqGroup, final int keyElementsSize, final int vectorSize) {
		final ElGamalGenerator elGamalGenerator = new ElGamalGenerator(gqGroup);
		return Stream.generate(() -> elGamalGenerator.genRandomPublicKey(keyElementsSize)).limit(vectorSize)
				.collect(GroupVector.toGroupVector());
	}

}