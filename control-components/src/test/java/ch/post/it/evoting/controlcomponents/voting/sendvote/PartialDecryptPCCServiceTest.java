/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.voting.sendvote;

import static ch.post.it.evoting.controlcomponents.voting.sendvote.PartialDecryptPCCService.PartialDecryptPCCInput;
import static ch.post.it.evoting.controlcomponents.voting.sendvote.PartialDecryptPCCService.PartialDecryptPCCInputBuilder;
import static ch.post.it.evoting.controlcomponents.voting.sendvote.PartialDecryptPCCService.PartialDecryptPCCOutput;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.google.common.base.Throwables;

import ch.post.it.evoting.controlcomponents.VerificationCardSetEntity;
import ch.post.it.evoting.controlcomponents.VerificationCardSetService;
import ch.post.it.evoting.controlcomponents.voting.ReturnCodesNodeContext;
import ch.post.it.evoting.controlcomponents.voting.VerificationCardStateService;
import ch.post.it.evoting.cryptoprimitives.domain.VotingOptionsConstants;
import ch.post.it.evoting.cryptoprimitives.domain.election.CombinedCorrectnessInformation;
import ch.post.it.evoting.cryptoprimitives.domain.election.CorrectnessInformation;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientKeyPair;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPrivateKey;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.RandomService;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;
import ch.post.it.evoting.cryptoprimitives.test.tools.TestGroupSetup;
import ch.post.it.evoting.cryptoprimitives.test.tools.generator.ElGamalGenerator;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ExponentiationProof;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ZeroKnowledgeProof;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ZeroKnowledgeProofService;

@DisplayName("PartialDecryptPCCService")
public class PartialDecryptPCCServiceTest extends TestGroupSetup {

	private static final int NODE_ID = 1;
	private static final int PSI = 5;
	private static final int PHI = VotingOptionsConstants.MAXIMUM_NUMBER_OF_SELECTABLE_VOTING_OPTIONS;
	private static final int l_ID = 32;
	private static final String CORRECTNESS_ID = "1";
	private static final ZeroKnowledgeProof zeroKnowledgeProofService = spy(ZeroKnowledgeProofService.class);
	private static final VerificationCardSetService verificationCardSetServiceMock = mock(VerificationCardSetService.class);
	private static final VerificationCardStateService verificationCardStateService = mock(VerificationCardStateService.class);

	private static PartialDecryptPCCService partialDecryptPCCService;

	@BeforeAll
	static void setUpAll() {
		partialDecryptPCCService = new PartialDecryptPCCService(zeroKnowledgeProofService, verificationCardSetServiceMock,
				verificationCardStateService);
	}

	@Nested
	@DisplayName("calling partialDecryptPCC with")
	class PartialDecryptPCCTest {

		private final RandomService randomService = new RandomService();
		private final ElGamalGenerator elGamalGenerator = new ElGamalGenerator(gqGroup);

		private PartialDecryptPCCInput input;
		private ReturnCodesNodeContext context;
		private String electionEventId;
		private String verificationCardSetId;

		@BeforeEach
		void setUp() {
			final PartialDecryptPCCInputBuilder builder = new PartialDecryptPCCInputBuilder();
			final String verificationCardId = randomService.genRandomBase16String(l_ID).toLowerCase();
			final ElGamalMultiRecipientCiphertext encryptedVote = elGamalGenerator.genRandomCiphertext(1);
			final ZqElement k_id = zqGroupGenerator.genRandomZqElementMember();
			final ElGamalMultiRecipientCiphertext exponentiatedEncryptedVote = encryptedVote.exponentiate(k_id);
			final ElGamalMultiRecipientCiphertext encryptedPartialChoiceReturnCodes = elGamalGenerator.genRandomCiphertext(PSI);
			final ElGamalMultiRecipientKeyPair keyPair = ElGamalMultiRecipientKeyPair.genKeyPair(gqGroup, PHI, randomService);

			input = builder.setVerificationCardId(verificationCardId)
					.setEncryptedVote(encryptedVote)
					.setExponentiatedEncryptedVote(exponentiatedEncryptedVote)
					.setEncryptedPartialChoiceReturnCodes(encryptedPartialChoiceReturnCodes)
					.setCcrjChoiceReturnCodesEncryptionSecretKey(keyPair.getPrivateKey())
					.setCcrjChoiceReturnCodesEncryptionPublicKey(keyPair.getPublicKey())
					.createPartialDecryptPCCInput();
			electionEventId = randomService.genRandomBase16String(l_ID).toLowerCase();
			verificationCardSetId = randomService.genRandomBase16String(l_ID).toLowerCase();
			context = new ReturnCodesNodeContext(NODE_ID, electionEventId, verificationCardSetId, gqGroup);

			reset(verificationCardSetServiceMock);
		}

		@Test
		@DisplayName("valid parameters does not throw")
		void validParams() {
			final String verificationCardId = input.getVerificationCardId();

			final CombinedCorrectnessInformation combinedCorrectnessInformation = new CombinedCorrectnessInformation(
					Collections.singletonList(new CorrectnessInformation(CORRECTNESS_ID, PSI, PSI)));
			final VerificationCardSetEntity verificationCardSetEntity = new VerificationCardSetEntity();
			verificationCardSetEntity.setCombinedCorrectnessInformation(combinedCorrectnessInformation);
			when(verificationCardSetServiceMock.getVerificationCardSet(verificationCardSetId)).thenReturn(verificationCardSetEntity);

			when(verificationCardStateService.isNotPartiallyDecrypted(verificationCardId)).thenReturn(true);

			final ExponentiationProof exponentiationProof = new ExponentiationProof(zqGroupGenerator.genRandomZqElementMember(),
					zqGroupGenerator.genRandomZqElementMember());
			doReturn(exponentiationProof).when(zeroKnowledgeProofService).genExponentiationProof(any(), any(), any(), any());

			final PartialDecryptPCCOutput output = partialDecryptPCCService.partialDecryptPCC(context, input);
			assertEquals(PSI, output.getExponentiatedGammas().size());
			assertEquals(PSI, output.getExponentiationProofs().size());
			assertEquals(gqGroup, output.getExponentiatedGammas().getGroup());
			assertTrue(gqGroup.hasSameOrderAs(output.getExponentiationProofs().getGroup()));
		}

		@Test
		@DisplayName("any null parameters throws NullPointerException")
		void nullParams() {
			assertAll(
					() -> assertThrows(NullPointerException.class, () -> partialDecryptPCCService.partialDecryptPCC(context, null)),
					() -> assertThrows(NullPointerException.class, () -> partialDecryptPCCService.partialDecryptPCC(null, input))
			);
		}

		@Test
		@DisplayName("context and input with different groups throws IllegalArgumentException")
		void differentGroupContextInput() {
			final ReturnCodesNodeContext otherContext = new ReturnCodesNodeContext(NODE_ID, electionEventId, verificationCardSetId, otherGqGroup);

			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> partialDecryptPCCService.partialDecryptPCC(otherContext, input));
			assertEquals("The context and input must have the same group.", Throwables.getRootCause(exception).getMessage());
		}

		@Test
		@DisplayName("wrong size encrypted partial Choice Return Codes")
		void wrongSizeEncryptedPartialChoiceReturnCodes() {
			final CombinedCorrectnessInformation combinedCorrectnessInformation = new CombinedCorrectnessInformation(
					Collections.singletonList(new CorrectnessInformation(CORRECTNESS_ID, 4, PSI)));
			final VerificationCardSetEntity verificationCardSetEntity = new VerificationCardSetEntity();
			verificationCardSetEntity.setCombinedCorrectnessInformation(combinedCorrectnessInformation);
			when(verificationCardSetServiceMock.getVerificationCardSet(verificationCardSetId)).thenReturn(verificationCardSetEntity);

			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> partialDecryptPCCService.partialDecryptPCC(context, input));
			assertEquals(String.format("There must be psi encrypted partial Choice Return Codes. [psi: %s]", 4),
					Throwables.getRootCause(exception).getMessage());

		}

		@Test
		@DisplayName("already partially decrypted pCC throws IllegalArgumentException")
		void alreadyPartiallyDecryptedPCC() {
			final String verificationCardId = input.getVerificationCardId();

			final CombinedCorrectnessInformation combinedCorrectnessInformation = new CombinedCorrectnessInformation(
					Collections.singletonList(new CorrectnessInformation(CORRECTNESS_ID, PSI, PSI)));
			final VerificationCardSetEntity verificationCardSetEntity = new VerificationCardSetEntity();
			verificationCardSetEntity.setCombinedCorrectnessInformation(combinedCorrectnessInformation);
			when(verificationCardSetServiceMock.getVerificationCardSet(verificationCardSetId)).thenReturn(verificationCardSetEntity);

			when(verificationCardStateService.isNotPartiallyDecrypted(verificationCardId)).thenReturn(false);

			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> partialDecryptPCCService.partialDecryptPCC(context, input));
			assertEquals("The partial Choice Return Code has already been partially decrypted.", Throwables.getRootCause(exception).getMessage());
		}

	}

	@Nested
	@DisplayName("PartialDecryptPCCInputBuilder with")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class PartialDecryptPCCInputBuilderTest {

		private final RandomService randomService = new RandomService();
		private final ElGamalGenerator elGamalGenerator = new ElGamalGenerator(gqGroup);
		private final ElGamalGenerator otherElGamalGenerator = new ElGamalGenerator(otherGqGroup);

		private String verificationCardId;
		private ElGamalMultiRecipientCiphertext encryptedVote;
		private ElGamalMultiRecipientCiphertext exponentiatedEncryptedVote;
		private ElGamalMultiRecipientCiphertext encryptedPartialChoiceReturnCodes;
		private ElGamalMultiRecipientPrivateKey secretKey;
		private ElGamalMultiRecipientPublicKey publicKey;

		@BeforeEach
		void setUp() {
			verificationCardId = randomService.genRandomBase16String(l_ID).toLowerCase();
			encryptedVote = elGamalGenerator.genRandomCiphertext(1);
			final ZqElement k_id = zqGroupGenerator.genRandomZqElementMember();
			exponentiatedEncryptedVote = encryptedVote.exponentiate(k_id);
			encryptedPartialChoiceReturnCodes = elGamalGenerator.genRandomCiphertext(PSI);

			final ElGamalMultiRecipientKeyPair keyPair = ElGamalMultiRecipientKeyPair.genKeyPair(gqGroup, PHI, randomService);
			secretKey = keyPair.getPrivateKey();
			publicKey = keyPair.getPublicKey();
		}

		@Test
		@DisplayName("valid parameters does not throw")
		void validParams() {
			final PartialDecryptPCCInputBuilder builder = new PartialDecryptPCCInputBuilder()
					.setEncryptedVote(encryptedVote)
					.setVerificationCardId(verificationCardId)
					.setExponentiatedEncryptedVote(exponentiatedEncryptedVote)
					.setEncryptedPartialChoiceReturnCodes(encryptedPartialChoiceReturnCodes)
					.setCcrjChoiceReturnCodesEncryptionSecretKey(secretKey)
					.setCcrjChoiceReturnCodesEncryptionPublicKey(publicKey);

			assertDoesNotThrow(builder::createPartialDecryptPCCInput);
		}

		private Stream<Arguments> nullArgumentProvider() {
			return Stream.of(
					Arguments.of(null, encryptedVote, exponentiatedEncryptedVote, encryptedPartialChoiceReturnCodes, secretKey, publicKey),
					Arguments.of(verificationCardId, null, exponentiatedEncryptedVote, encryptedPartialChoiceReturnCodes, secretKey, publicKey),
					Arguments.of(verificationCardId, encryptedVote, null, encryptedPartialChoiceReturnCodes, secretKey, publicKey),
					Arguments.of(verificationCardId, encryptedVote, exponentiatedEncryptedVote, null, secretKey, publicKey),
					Arguments.of(verificationCardId, encryptedVote, exponentiatedEncryptedVote, encryptedPartialChoiceReturnCodes, null, publicKey),
					Arguments.of(verificationCardId, encryptedVote, exponentiatedEncryptedVote, encryptedPartialChoiceReturnCodes, secretKey, null)
			);
		}

		@ParameterizedTest
		@MethodSource("nullArgumentProvider")
		@DisplayName("any null parameter throws NullPointerException")
		void nullParams(final String verificationCardId, final ElGamalMultiRecipientCiphertext encryptedVote,
				final ElGamalMultiRecipientCiphertext exponentiatedEncryptedVote,
				final ElGamalMultiRecipientCiphertext encryptedPartialChoiceReturnCodes, final ElGamalMultiRecipientPrivateKey secretKey,
				final ElGamalMultiRecipientPublicKey publicKey) {

			final PartialDecryptPCCInputBuilder builder = new PartialDecryptPCCInputBuilder()
					.setVerificationCardId(verificationCardId)
					.setEncryptedVote(encryptedVote)
					.setExponentiatedEncryptedVote(exponentiatedEncryptedVote)
					.setEncryptedPartialChoiceReturnCodes(encryptedPartialChoiceReturnCodes)
					.setCcrjChoiceReturnCodesEncryptionSecretKey(secretKey)
					.setCcrjChoiceReturnCodesEncryptionPublicKey(publicKey);

			assertThrows(NullPointerException.class, builder::createPartialDecryptPCCInput);
		}

		@Test
		@DisplayName("wrong size encrypted vote throws IllegalArgumentException")
		void wrongSizeEncryptedVote() {
			final ElGamalMultiRecipientCiphertext wrongEncryptedVote = elGamalGenerator.genRandomCiphertext(2);
			final PartialDecryptPCCInputBuilder builder = new PartialDecryptPCCInputBuilder()
					.setVerificationCardId(verificationCardId)
					.setEncryptedVote(wrongEncryptedVote)
					.setExponentiatedEncryptedVote(exponentiatedEncryptedVote)
					.setEncryptedPartialChoiceReturnCodes(encryptedPartialChoiceReturnCodes)
					.setCcrjChoiceReturnCodesEncryptionSecretKey(secretKey)
					.setCcrjChoiceReturnCodesEncryptionPublicKey(publicKey);

			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, builder::createPartialDecryptPCCInput);
			assertEquals("The encrypted vote must have exactly 1 phi.", Throwables.getRootCause(exception).getMessage());
		}

		@Test
		@DisplayName("wrong size exponentiated encrypted vote throws IllegalArgumentException")
		void wrongSizeExponentiatedEncryptedVote() {
			final ElGamalMultiRecipientCiphertext wrongExponentiatedEncryptedVote = elGamalGenerator.genRandomCiphertext(2);
			final PartialDecryptPCCInputBuilder builder = new PartialDecryptPCCInputBuilder()
					.setVerificationCardId(verificationCardId)
					.setEncryptedVote(encryptedVote)
					.setExponentiatedEncryptedVote(wrongExponentiatedEncryptedVote)
					.setEncryptedPartialChoiceReturnCodes(encryptedPartialChoiceReturnCodes)
					.setCcrjChoiceReturnCodesEncryptionSecretKey(secretKey)
					.setCcrjChoiceReturnCodesEncryptionPublicKey(publicKey);

			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, builder::createPartialDecryptPCCInput);
			assertEquals("The exponentiated encrypted vote must have exactly 1 phi.", Throwables.getRootCause(exception).getMessage());
		}

		@Test
		@DisplayName("wrong size secret key throws IllegalArgumentException")
		void wrongSizeSecretKey() {
			final ElGamalMultiRecipientKeyPair longKeyPair = ElGamalMultiRecipientKeyPair.genKeyPair(gqGroup, PHI + 1, randomService);
			final PartialDecryptPCCInputBuilder longBuilder = new PartialDecryptPCCInputBuilder()
					.setVerificationCardId(verificationCardId)
					.setEncryptedVote(encryptedVote)
					.setExponentiatedEncryptedVote(exponentiatedEncryptedVote)
					.setEncryptedPartialChoiceReturnCodes(encryptedPartialChoiceReturnCodes)
					.setCcrjChoiceReturnCodesEncryptionSecretKey(longKeyPair.getPrivateKey())
					.setCcrjChoiceReturnCodesEncryptionPublicKey(publicKey);

			final IllegalArgumentException longException = assertThrows(IllegalArgumentException.class, longBuilder::createPartialDecryptPCCInput);
			assertEquals(String.format("The secret key must be of size phi. [phi: %s]", PHI), Throwables.getRootCause(longException).getMessage());

			final ElGamalMultiRecipientKeyPair shortKeyPair = ElGamalMultiRecipientKeyPair.genKeyPair(gqGroup, PHI - 1, randomService);
			final PartialDecryptPCCInputBuilder shortBuilder = new PartialDecryptPCCInputBuilder()
					.setVerificationCardId(verificationCardId)
					.setEncryptedVote(encryptedVote)
					.setExponentiatedEncryptedVote(exponentiatedEncryptedVote)
					.setEncryptedPartialChoiceReturnCodes(encryptedPartialChoiceReturnCodes)
					.setCcrjChoiceReturnCodesEncryptionSecretKey(shortKeyPair.getPrivateKey())
					.setCcrjChoiceReturnCodesEncryptionPublicKey(publicKey);

			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, shortBuilder::createPartialDecryptPCCInput);
			assertEquals(String.format("The secret key must be of size phi. [phi: %s]", PHI), Throwables.getRootCause(exception).getMessage());
		}

		@Test
		@DisplayName("wrong size public key throws IllegalArgumentException")
		void wrongSizePublicKey() {
			final ElGamalMultiRecipientKeyPair longKeyPair = ElGamalMultiRecipientKeyPair.genKeyPair(gqGroup, PHI + 1, randomService);
			final PartialDecryptPCCInputBuilder longBuilder = new PartialDecryptPCCInputBuilder()
					.setVerificationCardId(verificationCardId)
					.setEncryptedVote(encryptedVote)
					.setExponentiatedEncryptedVote(exponentiatedEncryptedVote)
					.setEncryptedPartialChoiceReturnCodes(encryptedPartialChoiceReturnCodes)
					.setCcrjChoiceReturnCodesEncryptionSecretKey(secretKey)
					.setCcrjChoiceReturnCodesEncryptionPublicKey(longKeyPair.getPublicKey());

			final IllegalArgumentException longException = assertThrows(IllegalArgumentException.class, longBuilder::createPartialDecryptPCCInput);
			assertEquals(String.format("The public key must be of size phi. [phi: %s]", PHI), Throwables.getRootCause(longException).getMessage());

			final ElGamalMultiRecipientKeyPair shortKeyPair = ElGamalMultiRecipientKeyPair.genKeyPair(gqGroup, PHI - 1, randomService);
			final PartialDecryptPCCInputBuilder shortBuilder = new PartialDecryptPCCInputBuilder()
					.setVerificationCardId(verificationCardId)
					.setEncryptedVote(encryptedVote)
					.setExponentiatedEncryptedVote(exponentiatedEncryptedVote)
					.setEncryptedPartialChoiceReturnCodes(encryptedPartialChoiceReturnCodes)
					.setCcrjChoiceReturnCodesEncryptionSecretKey(secretKey)
					.setCcrjChoiceReturnCodesEncryptionPublicKey(shortKeyPair.getPublicKey());

			final IllegalArgumentException shortException = assertThrows(IllegalArgumentException.class, shortBuilder::createPartialDecryptPCCInput);
			assertEquals(String.format("The public key must be of size phi. [phi: %s]", PHI), Throwables.getRootCause(shortException).getMessage());
		}

		private Stream<Arguments> differentGroupArgumentProvider() {
			final ElGamalMultiRecipientCiphertext otherEncryptedVote = otherElGamalGenerator.genRandomCiphertext(1);
			final ZqElement k_id = otherZqGroupGenerator.genRandomZqElementMember();
			final ElGamalMultiRecipientCiphertext otherExponentiatedEncryptedVote = otherEncryptedVote.exponentiate(k_id);
			final ElGamalMultiRecipientCiphertext otherEncryptedPartialChoiceReturnCodes = otherElGamalGenerator.genRandomCiphertext(PSI);
			final ElGamalMultiRecipientKeyPair otherKeyPair = ElGamalMultiRecipientKeyPair.genKeyPair(otherGqGroup, PHI, randomService);

			return Stream.of(
					Arguments.of(otherEncryptedVote, exponentiatedEncryptedVote, encryptedPartialChoiceReturnCodes, publicKey),
					Arguments.of(encryptedVote, otherExponentiatedEncryptedVote, encryptedPartialChoiceReturnCodes, publicKey),
					Arguments.of(encryptedVote, exponentiatedEncryptedVote, otherEncryptedPartialChoiceReturnCodes, publicKey),
					Arguments.of(encryptedVote, exponentiatedEncryptedVote, encryptedPartialChoiceReturnCodes, otherKeyPair.getPublicKey())
			);
		}

		@ParameterizedTest
		@MethodSource("differentGroupArgumentProvider")
		@DisplayName("different group parameters throws IllegalArgumentException")
		void differentGroup(final ElGamalMultiRecipientCiphertext encryptedVote, final ElGamalMultiRecipientCiphertext exponentiatedEncryptedVote,
				final ElGamalMultiRecipientCiphertext encryptedPartialChoiceReturnCodes, final ElGamalMultiRecipientPublicKey publicKey) {

			final PartialDecryptPCCInputBuilder builder = new PartialDecryptPCCInputBuilder()
					.setVerificationCardId(verificationCardId)
					.setEncryptedVote(encryptedVote)
					.setExponentiatedEncryptedVote(exponentiatedEncryptedVote)
					.setEncryptedPartialChoiceReturnCodes(encryptedPartialChoiceReturnCodes)
					.setCcrjChoiceReturnCodesEncryptionSecretKey(secretKey)
					.setCcrjChoiceReturnCodesEncryptionPublicKey(publicKey);

			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, builder::createPartialDecryptPCCInput);
			assertEquals("All input Gq groups must be the same.", Throwables.getRootCause(exception).getMessage());
		}

		@Test
		@DisplayName("different group order secret key")
		void differentOrderSecretKey() {
			final ElGamalMultiRecipientKeyPair otherKeyPair = ElGamalMultiRecipientKeyPair.genKeyPair(otherGqGroup, PHI, randomService);
			final PartialDecryptPCCInputBuilder builder = new PartialDecryptPCCInputBuilder()
					.setVerificationCardId(verificationCardId)
					.setEncryptedVote(encryptedVote)
					.setExponentiatedEncryptedVote(exponentiatedEncryptedVote)
					.setEncryptedPartialChoiceReturnCodes(encryptedPartialChoiceReturnCodes)
					.setCcrjChoiceReturnCodesEncryptionSecretKey(otherKeyPair.getPrivateKey())
					.setCcrjChoiceReturnCodesEncryptionPublicKey(publicKey);

			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, builder::createPartialDecryptPCCInput);
			assertEquals("The secret key must have the same group order than the other inputs.", Throwables.getRootCause(exception).getMessage());
		}

		@Test
		@DisplayName("not matching keys")
		void notMatchingKeys() {
			final ElGamalMultiRecipientKeyPair keyPair = ElGamalMultiRecipientKeyPair.genKeyPair(gqGroup, PHI, randomService);
			final PartialDecryptPCCInputBuilder builder = new PartialDecryptPCCInputBuilder()
					.setVerificationCardId(verificationCardId)
					.setEncryptedVote(encryptedVote)
					.setExponentiatedEncryptedVote(exponentiatedEncryptedVote)
					.setEncryptedPartialChoiceReturnCodes(encryptedPartialChoiceReturnCodes)
					.setCcrjChoiceReturnCodesEncryptionSecretKey(keyPair.getPrivateKey())
					.setCcrjChoiceReturnCodesEncryptionPublicKey(publicKey);

			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, builder::createPartialDecryptPCCInput);
			assertEquals("The secret and public keys do not match.", Throwables.getRootCause(exception).getMessage());
		}
	}

	@Nested
	@DisplayName("PartialDecryptPCCOutput with")
	class PartialDecryptPCCOutputTest {

		private GroupVector<GqElement, GqGroup> exponentiatedGammas;
		private GroupVector<ExponentiationProof, ZqGroup> exponentiationProofs;

		@BeforeEach
		void setUp() {
			exponentiatedGammas = gqGroupGenerator.genRandomGqElementVector(PHI);

			final ZqElement e = zqGroupGenerator.genRandomZqElementMember();
			final ZqElement z = zqGroupGenerator.genRandomZqElementMember();
			exponentiationProofs = Stream.generate(() -> new ExponentiationProof(e, z)).limit(PHI).collect(GroupVector.toGroupVector());
		}

		@Test
		@DisplayName("valid parameters does not throw")
		void validParams() {
			assertDoesNotThrow(() -> new PartialDecryptPCCOutput(exponentiatedGammas, exponentiationProofs));
		}

		@Test
		@DisplayName("any null parameter throws NullPointerException")
		void nullParams() {
			assertThrows(NullPointerException.class, () -> new PartialDecryptPCCOutput(null, exponentiationProofs));
			assertThrows(NullPointerException.class, () -> new PartialDecryptPCCOutput(exponentiatedGammas, null));
		}

		@Test
		@DisplayName("exponentiated gammas and exponentiation proofs of different size throws IllegalArgumentException")
		void differentSizeGammasAndExponentiationProofs() {
			final GroupVector<GqElement, GqGroup> tooLongGammas = gqGroupGenerator.genRandomGqElementVector(PHI + 1);
			final IllegalArgumentException tooLongGammasException = assertThrows(IllegalArgumentException.class,
					() -> new PartialDecryptPCCOutput(tooLongGammas, exponentiationProofs));
			assertEquals("There must be as many exponentiated gammas as there are exponentiation proofs.",
					Throwables.getRootCause(tooLongGammasException).getMessage());

			final GroupVector<GqElement, GqGroup> tooShortGammas = gqGroupGenerator.genRandomGqElementVector(PHI - 1);
			final IllegalArgumentException tooShortGammasException = assertThrows(IllegalArgumentException.class,
					() -> new PartialDecryptPCCOutput(tooShortGammas, exponentiationProofs));
			assertEquals("There must be as many exponentiated gammas as there are exponentiation proofs.",
					Throwables.getRootCause(tooShortGammasException).getMessage());
		}

		@Test
		@DisplayName("different group order throws IllegalArgumentException")
		void differentGroupOrder() {
			final GroupVector<GqElement, GqGroup> otherGammas = otherGqGroupGenerator.genRandomGqElementVector(PHI);

			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> new PartialDecryptPCCOutput(otherGammas, exponentiationProofs));
			assertEquals("The exponentiated gammas and exponentiation proofs do not have the same group order.",
					Throwables.getRootCause(exception).getMessage());
		}

	}

}