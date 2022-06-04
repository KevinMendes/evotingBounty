/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.voting.sendvote;

import static ch.post.it.evoting.controlcomponents.voting.sendvote.VerifyBallotCCRService.VerifyBallotCCRInput;
import static ch.post.it.evoting.controlcomponents.voting.sendvote.VerifyBallotCCRService.VerifyBallotCCRInputBuilder;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
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
import ch.post.it.evoting.cryptoprimitives.domain.VotingOptionsConstants;
import ch.post.it.evoting.cryptoprimitives.domain.election.CombinedCorrectnessInformation;
import ch.post.it.evoting.cryptoprimitives.domain.election.CorrectnessInformation;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.RandomService;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;
import ch.post.it.evoting.cryptoprimitives.test.tools.TestGroupSetup;
import ch.post.it.evoting.cryptoprimitives.test.tools.generator.ElGamalGenerator;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ExponentiationProof;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.PlaintextEqualityProof;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ZeroKnowledgeProof;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ZeroKnowledgeProofService;

@DisplayName("VerifyBallotCCRService")
public class VerifyBallotCCRServiceTest extends TestGroupSetup {

	private static final int NODE_ID = 1;
	private static final int PSI = 5;
	private static final int PHI = VotingOptionsConstants.MAXIMUM_NUMBER_OF_SELECTABLE_VOTING_OPTIONS;
	private static final int DELTA = 1;
	private static final int l_ID = 32;
	private static final String CORRECTNESS_ID = "1";
	private static final ZeroKnowledgeProof zeroKnowledgeProofService = mock(ZeroKnowledgeProofService.class);
	private static final VerificationCardSetService verificationCardSetServiceMock = mock(VerificationCardSetService.class);

	private static VerifyBallotCCRService verifyBallotCCRService;

	@BeforeAll
	static void setUpAll() {
		verifyBallotCCRService = new VerifyBallotCCRService(zeroKnowledgeProofService, verificationCardSetServiceMock);
	}

	@Nested
	@DisplayName("calling verifyBallotCCR with")
	class VerifyBallotCCRTest {

		private final RandomService randomService = new RandomService();
		private final ElGamalGenerator elGamalGenerator = new ElGamalGenerator(gqGroup);

		private ReturnCodesNodeContext context;
		private VerifyBallotCCRInput input;
		private String electionEventId;
		private String verificationCardSetId;
		private VerifyBallotCCRInputBuilder builder;

		@BeforeEach
		void setUp() {
			electionEventId = randomService.genRandomBase16String(l_ID).toLowerCase();
			verificationCardSetId = randomService.genRandomBase16String(l_ID).toLowerCase();
			context = new ReturnCodesNodeContext(NODE_ID, electionEventId, verificationCardSetId, gqGroup);

			builder = new VerifyBallotCCRInputBuilder();
			final String verificationCardId = randomService.genRandomBase16String(l_ID).toLowerCase();
			final ElGamalMultiRecipientCiphertext encryptedVote = elGamalGenerator.genRandomCiphertext(1);
			final ZqElement k_id = zqGroupGenerator.genRandomZqElementMember();
			final ElGamalMultiRecipientCiphertext exponentiatedEncryptedVote = encryptedVote.exponentiate(k_id);
			final ElGamalMultiRecipientCiphertext encryptedPartialChoiceReturnCodes = elGamalGenerator.genRandomCiphertext(PSI);
			final GqElement verificationCardPublicKey = gqGroupGenerator.genMember();
			final ElGamalMultiRecipientPublicKey electionPublicKey = new ElGamalMultiRecipientPublicKey(
					gqGroupGenerator.genRandomGqElementVector(DELTA));
			final ElGamalMultiRecipientPublicKey choiceReturnCodesEncryptionPublicKey = new ElGamalMultiRecipientPublicKey(
					gqGroupGenerator.genRandomGqElementVector(PHI));
			final ExponentiationProof exponentiationProof = new ExponentiationProof(zqGroupGenerator.genRandomZqElementMember(),
					zqGroupGenerator.genRandomZqElementMember());
			final PlaintextEqualityProof plaintextEqualityProof = new PlaintextEqualityProof(zqGroupGenerator.genRandomZqElementMember(),
					zqGroupGenerator.genRandomZqElementVector(2));

			input = builder.setVerificationCardId(verificationCardId)
					.setEncryptedVote(encryptedVote)
					.setExponentiatedEncryptedVote(exponentiatedEncryptedVote)
					.setEncryptedPartialChoiceReturnCodes(encryptedPartialChoiceReturnCodes)
					.setVerificationCardPublicKey(verificationCardPublicKey)
					.setElectionPublicKey(electionPublicKey)
					.setChoiceReturnCodesEncryptionPublicKey(choiceReturnCodesEncryptionPublicKey)
					.setExponentiationProof(exponentiationProof)
					.setPlaintextEqualityProof(plaintextEqualityProof)
					.build();

			reset(verificationCardSetServiceMock);
		}

		@Test
		@DisplayName("valid parameters does not throw")
		void validParams() {
			final CombinedCorrectnessInformation combinedCorrectnessInformation = new CombinedCorrectnessInformation(
					Collections.singletonList(new CorrectnessInformation(CORRECTNESS_ID, PSI, PSI)));
			final VerificationCardSetEntity verificationCardSetEntity = new VerificationCardSetEntity();
			verificationCardSetEntity.setCombinedCorrectnessInformation(combinedCorrectnessInformation);
			when(verificationCardSetServiceMock.getVerificationCardSet(verificationCardSetId)).thenReturn(verificationCardSetEntity);

			when(zeroKnowledgeProofService.verifyExponentiation(any(), any(), any(), any())).thenReturn(true);
			when(zeroKnowledgeProofService.verifyPlaintextEquality(any(), any(), any(), any(), any(), any())).thenReturn(true);

			assertTrue(verifyBallotCCRService.verifyBallotCCR(context, input));
		}

		@Test
		@DisplayName("invalid proofs return false")
		void invalidExponentiationProof() {
			final CombinedCorrectnessInformation combinedCorrectnessInformation = new CombinedCorrectnessInformation(
					Collections.singletonList(new CorrectnessInformation(CORRECTNESS_ID, PSI, PSI)));
			final VerificationCardSetEntity verificationCardSetEntity = new VerificationCardSetEntity();
			verificationCardSetEntity.setCombinedCorrectnessInformation(combinedCorrectnessInformation);
			when(verificationCardSetServiceMock.getVerificationCardSet(verificationCardSetId)).thenReturn(
					verificationCardSetEntity);

			when(zeroKnowledgeProofService.verifyExponentiation(any(), any(), any(), any())).thenReturn(false);
			when(zeroKnowledgeProofService.verifyPlaintextEquality(any(), any(), any(), any(), any(), any())).thenReturn(true);
			assertFalse(verifyBallotCCRService.verifyBallotCCR(context, input));

			when(zeroKnowledgeProofService.verifyExponentiation(any(), any(), any(), any())).thenReturn(true);
			when(zeroKnowledgeProofService.verifyPlaintextEquality(any(), any(), any(), any(), any(), any())).thenReturn(false);
			assertFalse(verifyBallotCCRService.verifyBallotCCR(context, input));

			when(zeroKnowledgeProofService.verifyExponentiation(any(), any(), any(), any())).thenReturn(false);
			when(zeroKnowledgeProofService.verifyPlaintextEquality(any(), any(), any(), any(), any(), any())).thenReturn(false);
			assertFalse(verifyBallotCCRService.verifyBallotCCR(context, input));
		}

		@Test
		@DisplayName("any null parameter throws NullPointerException")
		void nullParams() {
			assertThrows(NullPointerException.class, () -> verifyBallotCCRService.verifyBallotCCR(context, null));
			assertThrows(NullPointerException.class, () -> verifyBallotCCRService.verifyBallotCCR(null, input));
		}

		@Test
		@DisplayName("context and input with different groups throws IllegalArgumentException")
		void differentGroupContextInput() {
			final ReturnCodesNodeContext otherContext = new ReturnCodesNodeContext(NODE_ID, electionEventId, verificationCardSetId, otherGqGroup);

			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> verifyBallotCCRService.verifyBallotCCR(otherContext, input));
			assertEquals("The context and input must have the same group.", Throwables.getRootCause(exception).getMessage());
		}

		@Test
		@DisplayName("wrong size encrypted partial Choice Return Codes")
		void wrongSizeEncryptedPartialChoiceReturnCodes() {
			final CombinedCorrectnessInformation combinedCorrectnessInformation = new CombinedCorrectnessInformation(
					Collections.singletonList(new CorrectnessInformation(CORRECTNESS_ID, 4, PSI)));
			final VerificationCardSetEntity verificationCardSetEntity = new VerificationCardSetEntity();
			verificationCardSetEntity.setCombinedCorrectnessInformation(combinedCorrectnessInformation);
			when(verificationCardSetServiceMock.getVerificationCardSet(verificationCardSetId)).thenReturn(
					verificationCardSetEntity);

			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> verifyBallotCCRService.verifyBallotCCR(context, input));
			assertEquals(String.format("There must be psi encrypted partial Choice Return Codes. [psi: %s]", 4),
					Throwables.getRootCause(exception).getMessage());
		}

		@Test
		@DisplayName("psi bigger than phi throws IllegalArgumentException")
		void wrongSizePsi() {
			final CombinedCorrectnessInformation combinedCorrectnessInformation = new CombinedCorrectnessInformation(
					Collections.singletonList(new CorrectnessInformation(CORRECTNESS_ID, PHI + 1, PHI + 1)));
			final VerificationCardSetEntity verificationCardSetEntity = new VerificationCardSetEntity();
			verificationCardSetEntity.setCombinedCorrectnessInformation(combinedCorrectnessInformation);
			when(verificationCardSetServiceMock.getVerificationCardSet(verificationCardSetId)).thenReturn(verificationCardSetEntity);

			final ElGamalMultiRecipientCiphertext diff = elGamalGenerator.genRandomCiphertext(PHI + 1);
			final VerifyBallotCCRInput otherInput = builder.setEncryptedPartialChoiceReturnCodes(diff)
					.build();

			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> verifyBallotCCRService.verifyBallotCCR(context, otherInput));
			assertEquals(String.format("psi must be smaller or equal to phi. [psi: %s, phi: %s]", PHI + 1, PHI),
					Throwables.getRootCause(exception).getMessage());
		}

	}

	@Nested
	@DisplayName("VerifyBallotCCRInputBuilder with")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class VerifyBallotCCRInputBuilderTest {

		private final RandomService randomService = new RandomService();
		private final ElGamalGenerator elGamalGenerator = new ElGamalGenerator(gqGroup);
		private final ElGamalGenerator otherElGamalGenerator = new ElGamalGenerator(otherGqGroup);

		private String verificationCardId;
		private ElGamalMultiRecipientCiphertext encryptedVote;
		private ElGamalMultiRecipientCiphertext exponentiatedEncryptedVote;
		private ElGamalMultiRecipientCiphertext encryptedPartialChoiceReturnCodes;
		private GqElement verificationCardPublicKey;
		private ElGamalMultiRecipientPublicKey electionPublicKey;
		private ElGamalMultiRecipientPublicKey choiceReturnCodesEncryptionPublicKey;
		private ExponentiationProof exponentiationProof;
		private PlaintextEqualityProof plaintextEqualityProof;

		@BeforeEach
		void setUp() {
			verificationCardId = randomService.genRandomBase16String(l_ID).toLowerCase();
			encryptedVote = elGamalGenerator.genRandomCiphertext(1);
			final ZqElement k_id = zqGroupGenerator.genRandomZqElementMember();
			exponentiatedEncryptedVote = encryptedVote.exponentiate(k_id);
			encryptedPartialChoiceReturnCodes = elGamalGenerator.genRandomCiphertext(PSI);
			verificationCardPublicKey = gqGroupGenerator.genMember();
			electionPublicKey = new ElGamalMultiRecipientPublicKey(gqGroupGenerator.genRandomGqElementVector(DELTA));
			choiceReturnCodesEncryptionPublicKey = new ElGamalMultiRecipientPublicKey(gqGroupGenerator.genRandomGqElementVector(PHI));
			exponentiationProof = new ExponentiationProof(zqGroupGenerator.genRandomZqElementMember(), zqGroupGenerator.genRandomZqElementMember());
			plaintextEqualityProof = new PlaintextEqualityProof(zqGroupGenerator.genRandomZqElementMember(),
					zqGroupGenerator.genRandomZqElementVector(2));
		}

		@Test
		@DisplayName("valid parameters does not throw")
		void validParams() {
			final VerifyBallotCCRInputBuilder builder = new VerifyBallotCCRInputBuilder()
					.setVerificationCardId(verificationCardId)
					.setEncryptedVote(encryptedVote)
					.setExponentiatedEncryptedVote(exponentiatedEncryptedVote)
					.setEncryptedPartialChoiceReturnCodes(encryptedPartialChoiceReturnCodes)
					.setVerificationCardPublicKey(verificationCardPublicKey)
					.setElectionPublicKey(electionPublicKey)
					.setChoiceReturnCodesEncryptionPublicKey(choiceReturnCodesEncryptionPublicKey)
					.setExponentiationProof(exponentiationProof)
					.setPlaintextEqualityProof(plaintextEqualityProof);

			assertDoesNotThrow(builder::build);
		}

		private Stream<Arguments> nullArgumentProvider() {
			return Stream.of(
					Arguments.of(null, encryptedVote, exponentiatedEncryptedVote, encryptedPartialChoiceReturnCodes,
							verificationCardPublicKey, electionPublicKey, choiceReturnCodesEncryptionPublicKey, exponentiationProof,
							plaintextEqualityProof),
					Arguments.of(verificationCardId, null, exponentiatedEncryptedVote, encryptedPartialChoiceReturnCodes,
							verificationCardPublicKey, electionPublicKey, choiceReturnCodesEncryptionPublicKey, exponentiationProof,
							plaintextEqualityProof),
					Arguments.of(verificationCardId, encryptedVote, null, encryptedPartialChoiceReturnCodes,
							verificationCardPublicKey, electionPublicKey, choiceReturnCodesEncryptionPublicKey, exponentiationProof,
							plaintextEqualityProof),
					Arguments.of(verificationCardId, encryptedVote, exponentiatedEncryptedVote, null,
							verificationCardPublicKey, electionPublicKey, choiceReturnCodesEncryptionPublicKey, exponentiationProof,
							plaintextEqualityProof),
					Arguments.of(verificationCardId, encryptedVote, exponentiatedEncryptedVote, encryptedPartialChoiceReturnCodes,
							null, electionPublicKey, choiceReturnCodesEncryptionPublicKey, exponentiationProof,
							plaintextEqualityProof),
					Arguments.of(verificationCardId, encryptedVote, exponentiatedEncryptedVote, encryptedPartialChoiceReturnCodes,
							verificationCardPublicKey, null, choiceReturnCodesEncryptionPublicKey, exponentiationProof,
							plaintextEqualityProof),
					Arguments.of(verificationCardId, encryptedVote, exponentiatedEncryptedVote, encryptedPartialChoiceReturnCodes,
							verificationCardPublicKey, electionPublicKey, null, exponentiationProof,
							plaintextEqualityProof),
					Arguments.of(verificationCardId, encryptedVote, exponentiatedEncryptedVote, encryptedPartialChoiceReturnCodes,
							verificationCardPublicKey, electionPublicKey, choiceReturnCodesEncryptionPublicKey, null,
							plaintextEqualityProof),
					Arguments.of(verificationCardId, encryptedVote, exponentiatedEncryptedVote, encryptedPartialChoiceReturnCodes,
							verificationCardPublicKey, electionPublicKey, choiceReturnCodesEncryptionPublicKey, exponentiationProof,
							null)
			);
		}

		@ParameterizedTest
		@MethodSource("nullArgumentProvider")
		@DisplayName("any null parameter throws NullPointerException")
		void nullParams(
				final String verificationCardId,
				final ElGamalMultiRecipientCiphertext encryptedVote,
				final ElGamalMultiRecipientCiphertext exponentiatedEncryptedVote,
				final ElGamalMultiRecipientCiphertext encryptedPartialChoiceReturnCodes,
				final GqElement verificationCardPublicKey,
				final ElGamalMultiRecipientPublicKey electionPublicKey,
				final ElGamalMultiRecipientPublicKey choiceReturnCodesEncryptionPublicKey,
				final ExponentiationProof exponentiationProof,
				final PlaintextEqualityProof plaintextEqualityProof) {

			final VerifyBallotCCRInputBuilder builder = new VerifyBallotCCRInputBuilder()
					.setVerificationCardId(verificationCardId)
					.setEncryptedVote(encryptedVote)
					.setExponentiatedEncryptedVote(exponentiatedEncryptedVote)
					.setEncryptedPartialChoiceReturnCodes(encryptedPartialChoiceReturnCodes)
					.setVerificationCardPublicKey(verificationCardPublicKey)
					.setElectionPublicKey(electionPublicKey)
					.setChoiceReturnCodesEncryptionPublicKey(choiceReturnCodesEncryptionPublicKey)
					.setExponentiationProof(exponentiationProof)
					.setPlaintextEqualityProof(plaintextEqualityProof);

			assertThrows(NullPointerException.class, builder::build);
		}

		@Test
		@DisplayName("wrong size encrypted vote throws IllegalArgumentException")
		void wrongSizeEncryptedVote() {
			final ElGamalMultiRecipientCiphertext wrongEncryptedVote = elGamalGenerator.genRandomCiphertext(2);
			final VerifyBallotCCRInputBuilder builder = new VerifyBallotCCRInputBuilder()
					.setVerificationCardId(verificationCardId)
					.setEncryptedVote(wrongEncryptedVote)
					.setExponentiatedEncryptedVote(exponentiatedEncryptedVote)
					.setEncryptedPartialChoiceReturnCodes(encryptedPartialChoiceReturnCodes)
					.setVerificationCardPublicKey(verificationCardPublicKey)
					.setElectionPublicKey(electionPublicKey)
					.setChoiceReturnCodesEncryptionPublicKey(choiceReturnCodesEncryptionPublicKey)
					.setExponentiationProof(exponentiationProof)
					.setPlaintextEqualityProof(plaintextEqualityProof);

			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, builder::build);
			assertEquals("The encrypted vote must have exactly 1 phi.", Throwables.getRootCause(exception).getMessage());
		}

		@Test
		@DisplayName("wrong size exponentiated encrypted vote throws IllegalArgumentException")
		void wrongSizeExponentiatedEncryptedVote() {
			final ElGamalMultiRecipientCiphertext wrongExponentiatedEncryptedVote = elGamalGenerator.genRandomCiphertext(2);
			final VerifyBallotCCRInputBuilder builder = new VerifyBallotCCRInputBuilder()
					.setVerificationCardId(verificationCardId)
					.setEncryptedVote(encryptedVote)
					.setExponentiatedEncryptedVote(wrongExponentiatedEncryptedVote)
					.setEncryptedPartialChoiceReturnCodes(encryptedPartialChoiceReturnCodes)
					.setVerificationCardPublicKey(verificationCardPublicKey)
					.setElectionPublicKey(electionPublicKey)
					.setChoiceReturnCodesEncryptionPublicKey(choiceReturnCodesEncryptionPublicKey)
					.setExponentiationProof(exponentiationProof)
					.setPlaintextEqualityProof(plaintextEqualityProof);

			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, builder::build);
			assertEquals("The exponentiated encrypted vote must have exactly 1 phi.", Throwables.getRootCause(exception).getMessage());
		}

		@Test
		@DisplayName("wrong size election public key throws IllegalArgumentException")
		void wrongSizeElectionPublicKey() {
			final ElGamalMultiRecipientPublicKey wrongElectionPublicKey = new ElGamalMultiRecipientPublicKey(
					gqGroupGenerator.genRandomGqElementVector(DELTA + 1));
			final VerifyBallotCCRInputBuilder builder = new VerifyBallotCCRInputBuilder()
					.setVerificationCardId(verificationCardId)
					.setEncryptedVote(encryptedVote)
					.setExponentiatedEncryptedVote(exponentiatedEncryptedVote)
					.setEncryptedPartialChoiceReturnCodes(encryptedPartialChoiceReturnCodes)
					.setVerificationCardPublicKey(verificationCardPublicKey)
					.setElectionPublicKey(wrongElectionPublicKey)
					.setChoiceReturnCodesEncryptionPublicKey(choiceReturnCodesEncryptionPublicKey)
					.setExponentiationProof(exponentiationProof)
					.setPlaintextEqualityProof(plaintextEqualityProof);

			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, builder::build);
			assertEquals(String.format("The election public key must be of size delta. [delta: %s]", DELTA),
					Throwables.getRootCause(exception).getMessage());
		}

		@Test
		@DisplayName("wrong size public key throws IllegalArgumentException")
		void wrongSizePublicKey() {
			final ElGamalMultiRecipientPublicKey longEncryptionPublicKey = new ElGamalMultiRecipientPublicKey(
					gqGroupGenerator.genRandomGqElementVector(PHI + 1));
			final VerifyBallotCCRInputBuilder longBuilder = new VerifyBallotCCRInputBuilder()
					.setVerificationCardId(verificationCardId)
					.setEncryptedVote(encryptedVote)
					.setExponentiatedEncryptedVote(exponentiatedEncryptedVote)
					.setEncryptedPartialChoiceReturnCodes(encryptedPartialChoiceReturnCodes)
					.setVerificationCardPublicKey(verificationCardPublicKey)
					.setElectionPublicKey(electionPublicKey)
					.setChoiceReturnCodesEncryptionPublicKey(longEncryptionPublicKey)
					.setExponentiationProof(exponentiationProof)
					.setPlaintextEqualityProof(plaintextEqualityProof);

			final IllegalArgumentException longException = assertThrows(IllegalArgumentException.class, longBuilder::build);
			assertEquals(String.format("The choice return codes encryption public key must be of size phi. [phi: %s]", PHI),
					Throwables.getRootCause(longException).getMessage());

			final ElGamalMultiRecipientPublicKey shortEncryptionPublicKey = new ElGamalMultiRecipientPublicKey(
					gqGroupGenerator.genRandomGqElementVector(PHI + 1));
			final VerifyBallotCCRInputBuilder shortBuilder = new VerifyBallotCCRInputBuilder()
					.setVerificationCardId(verificationCardId)
					.setEncryptedVote(encryptedVote)
					.setExponentiatedEncryptedVote(exponentiatedEncryptedVote)
					.setEncryptedPartialChoiceReturnCodes(encryptedPartialChoiceReturnCodes)
					.setVerificationCardPublicKey(verificationCardPublicKey)
					.setElectionPublicKey(electionPublicKey)
					.setChoiceReturnCodesEncryptionPublicKey(shortEncryptionPublicKey)
					.setExponentiationProof(exponentiationProof)
					.setPlaintextEqualityProof(plaintextEqualityProof);

			final IllegalArgumentException shortException = assertThrows(IllegalArgumentException.class, shortBuilder::build);
			assertEquals(String.format("The choice return codes encryption public key must be of size phi. [phi: %s]", PHI),
					Throwables.getRootCause(shortException).getMessage());
		}

		private Stream<Arguments> differentGroupArgumentProvider() {
			final ElGamalMultiRecipientCiphertext otherEncryptedVote = otherElGamalGenerator.genRandomCiphertext(1);
			final ZqElement k_id = otherZqGroupGenerator.genRandomZqElementMember();
			final ElGamalMultiRecipientCiphertext otherExponentiatedEncryptedVote = otherEncryptedVote.exponentiate(k_id);
			final ElGamalMultiRecipientCiphertext otherEncryptedPartialChoiceReturnCodes = otherElGamalGenerator.genRandomCiphertext(PSI);
			final GqElement otherVerificationCardPublicKey = otherGqGroupGenerator.genMember();
			final ElGamalMultiRecipientPublicKey otherElectionPublicKey = new ElGamalMultiRecipientPublicKey(
					otherGqGroupGenerator.genRandomGqElementVector(DELTA));
			final ElGamalMultiRecipientPublicKey otherChoiceReturnCodesEncryptionPublicKey = new ElGamalMultiRecipientPublicKey(
					otherGqGroupGenerator.genRandomGqElementVector(PHI));

			return Stream.of(
					Arguments.of(otherEncryptedVote, exponentiatedEncryptedVote, encryptedPartialChoiceReturnCodes, verificationCardPublicKey,
							electionPublicKey, choiceReturnCodesEncryptionPublicKey),
					Arguments.of(encryptedVote, otherExponentiatedEncryptedVote, encryptedPartialChoiceReturnCodes, verificationCardPublicKey,
							electionPublicKey, choiceReturnCodesEncryptionPublicKey),
					Arguments.of(encryptedVote, exponentiatedEncryptedVote, otherEncryptedPartialChoiceReturnCodes, verificationCardPublicKey,
							electionPublicKey, choiceReturnCodesEncryptionPublicKey),
					Arguments.of(encryptedVote, exponentiatedEncryptedVote, encryptedPartialChoiceReturnCodes, otherVerificationCardPublicKey,
							electionPublicKey, choiceReturnCodesEncryptionPublicKey),
					Arguments.of(encryptedVote, exponentiatedEncryptedVote, encryptedPartialChoiceReturnCodes, verificationCardPublicKey,
							otherElectionPublicKey, choiceReturnCodesEncryptionPublicKey),
					Arguments.of(encryptedVote, exponentiatedEncryptedVote, encryptedPartialChoiceReturnCodes, verificationCardPublicKey,
							electionPublicKey, otherChoiceReturnCodesEncryptionPublicKey)
			);
		}

		@ParameterizedTest
		@MethodSource("differentGroupArgumentProvider")
		@DisplayName("different group parameters throws IllegalArgumentException")
		void differentGroup(
				final ElGamalMultiRecipientCiphertext encryptedVote,
				final ElGamalMultiRecipientCiphertext exponentiatedEncryptedVote,
				final ElGamalMultiRecipientCiphertext encryptedPartialChoiceReturnCodes,
				final GqElement verificationCardPublicKey,
				final ElGamalMultiRecipientPublicKey electionPublicKey,
				final ElGamalMultiRecipientPublicKey choiceReturnCodesEncryptionPublicKey) {

			final VerifyBallotCCRInputBuilder shortBuilder = new VerifyBallotCCRInputBuilder()
					.setVerificationCardId(verificationCardId)
					.setEncryptedVote(encryptedVote)
					.setExponentiatedEncryptedVote(exponentiatedEncryptedVote)
					.setEncryptedPartialChoiceReturnCodes(encryptedPartialChoiceReturnCodes)
					.setVerificationCardPublicKey(verificationCardPublicKey)
					.setElectionPublicKey(electionPublicKey)
					.setChoiceReturnCodesEncryptionPublicKey(choiceReturnCodesEncryptionPublicKey)
					.setExponentiationProof(exponentiationProof)
					.setPlaintextEqualityProof(plaintextEqualityProof);

			final IllegalArgumentException shortException = assertThrows(IllegalArgumentException.class, shortBuilder::build);
			assertEquals("All input Gq groups must be the same.", Throwables.getRootCause(shortException).getMessage());
		}

		@Test
		@DisplayName("different group order exponentiation proof throws IllegalArgumentException")
		void differentOrderExponentiationProof() {
			final ExponentiationProof otherExponentiationProof = new ExponentiationProof(otherZqGroupGenerator.genRandomZqElementMember(),
					otherZqGroupGenerator.genRandomZqElementMember());
			final VerifyBallotCCRInputBuilder shortBuilder = new VerifyBallotCCRInputBuilder()
					.setVerificationCardId(verificationCardId)
					.setEncryptedVote(encryptedVote)
					.setExponentiatedEncryptedVote(exponentiatedEncryptedVote)
					.setEncryptedPartialChoiceReturnCodes(encryptedPartialChoiceReturnCodes)
					.setVerificationCardPublicKey(verificationCardPublicKey)
					.setElectionPublicKey(electionPublicKey)
					.setChoiceReturnCodesEncryptionPublicKey(choiceReturnCodesEncryptionPublicKey)
					.setExponentiationProof(otherExponentiationProof)
					.setPlaintextEqualityProof(plaintextEqualityProof);

			final IllegalArgumentException shortException = assertThrows(IllegalArgumentException.class, shortBuilder::build);
			assertEquals("The exponentiation proof must have the same group order than the other inputs.",
					Throwables.getRootCause(shortException).getMessage());
		}

		@Test
		@DisplayName("different group order plaintext equality proof throws IllegalArgumentException")
		void differentOrderPlaintextEqualityProof() {
			final PlaintextEqualityProof otherPlaintextEqualityProof = new PlaintextEqualityProof(otherZqGroupGenerator.genRandomZqElementMember(),
					otherZqGroupGenerator.genRandomZqElementVector(2));
			final VerifyBallotCCRInputBuilder shortBuilder = new VerifyBallotCCRInputBuilder()
					.setVerificationCardId(verificationCardId)
					.setEncryptedVote(encryptedVote)
					.setExponentiatedEncryptedVote(exponentiatedEncryptedVote)
					.setEncryptedPartialChoiceReturnCodes(encryptedPartialChoiceReturnCodes)
					.setVerificationCardPublicKey(verificationCardPublicKey)
					.setElectionPublicKey(electionPublicKey)
					.setChoiceReturnCodesEncryptionPublicKey(choiceReturnCodesEncryptionPublicKey)
					.setExponentiationProof(exponentiationProof)
					.setPlaintextEqualityProof(otherPlaintextEqualityProof);

			final IllegalArgumentException shortException = assertThrows(IllegalArgumentException.class, shortBuilder::build);
			assertEquals("The plaintext equality proof must have the same group order than the other inputs.",
					Throwables.getRootCause(shortException).getMessage());
		}

	}

}
