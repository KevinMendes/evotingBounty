/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.voting.sendvote;

import static ch.post.it.evoting.controlcomponents.voting.sendvote.DecryptPCCService.DecryptPPCInput;
import static ch.post.it.evoting.cryptoprimitives.domain.VotingOptionsConstants.MAXIMUM_NUMBER_OF_SELECTABLE_VOTING_OPTIONS;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
import ch.post.it.evoting.cryptoprimitives.domain.election.CombinedCorrectnessInformation;
import ch.post.it.evoting.cryptoprimitives.domain.election.CorrectnessInformation;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.RandomService;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;
import ch.post.it.evoting.cryptoprimitives.test.tools.TestGroupSetup;
import ch.post.it.evoting.cryptoprimitives.test.tools.generator.ElGamalGenerator;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ExponentiationProof;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ZeroKnowledgeProof;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ZeroKnowledgeProofService;

@DisplayName("DecryptPCCService")
class DecryptPCCServiceTest extends TestGroupSetup {

	private static final int PHI = MAXIMUM_NUMBER_OF_SELECTABLE_VOTING_OPTIONS;
	private static final int PSI = 5;
	private static final int NODE_ID = 1;
	private static final String CORRECTNESS_ID = "1";
	private static final int ID_STRING_LENGTH = 32;
	private static final RandomService randomService = new RandomService();
	private static final ZeroKnowledgeProof zeroKnowledgeProofMock = spy(ZeroKnowledgeProofService.class);
	private static final VerificationCardSetService verificationCardSetServiceMock = mock(VerificationCardSetService.class);

	private static DecryptPCCService decryptPCCService;
	private static ElGamalGenerator elGamalGenerator;

	private ReturnCodesNodeContext returnCodesNodeContext;
	private DecryptPPCInput decryptPCCInput;

	@BeforeAll
	static void init() {
		decryptPCCService = new DecryptPCCService(NODE_ID, zeroKnowledgeProofMock, verificationCardSetServiceMock);
		elGamalGenerator = new ElGamalGenerator(gqGroup);
	}

	private GroupVector<ExponentiationProof, ZqGroup> genRandomExponentiationProofVector(final int size) {
		return Stream.generate(
						() -> new ExponentiationProof(zqGroupGenerator.genRandomZqElementMember(), zqGroupGenerator.genRandomZqElementMember()))
				.limit(size)
				.collect(GroupVector.toGroupVector());
	}

	@Nested
	@DisplayName("DecryptPPCBuilder with")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class DecryptPCCInputBuilderTest {

		private final int NUM_OTHER_NODES = 3;

		private String verificationCardId;
		private GroupVector<GqElement, GqGroup> exponentiatedGammaElements;
		private GroupVector<GroupVector<GqElement, GqGroup>, GqGroup> otherCcrExponentiatedGammaElements;
		private GroupVector<GroupVector<ExponentiationProof, ZqGroup>, ZqGroup> otherCcrExponentiationProofs;
		private GroupVector<ElGamalMultiRecipientPublicKey, GqGroup> otherCcrChoiceReturnCodesEncryptionKeys;
		private ElGamalMultiRecipientCiphertext encryptedVote;
		private ElGamalMultiRecipientCiphertext exponentiatedEncryptedVote;
		private ElGamalMultiRecipientCiphertext encryptedPartialChoiceReturnCodes;

		@BeforeEach
		void setup() {
			verificationCardId = randomService.genRandomBase64String(ID_STRING_LENGTH);
			exponentiatedGammaElements = gqGroupGenerator.genRandomGqElementVector(PHI);
			otherCcrExponentiatedGammaElements = gqGroupGenerator.genRandomGqElementMatrix(PHI, NUM_OTHER_NODES).columnStream()
					.collect(GroupVector.toGroupVector());
			otherCcrExponentiationProofs = GroupVector.of(
					genRandomExponentiationProofVector(PHI),
					genRandomExponentiationProofVector(PHI),
					genRandomExponentiationProofVector(PHI)
			);
			otherCcrChoiceReturnCodesEncryptionKeys = GroupVector.of(
					elGamalGenerator.genRandomPublicKey(PHI),
					elGamalGenerator.genRandomPublicKey(PHI),
					elGamalGenerator.genRandomPublicKey(PHI)
			);
			encryptedVote = elGamalGenerator.genRandomCiphertext(1);
			exponentiatedEncryptedVote = elGamalGenerator.genRandomCiphertext(1);
			encryptedPartialChoiceReturnCodes = elGamalGenerator.genRandomCiphertext(PSI);
		}

		private Stream<Arguments> nullArgumentsProvider() {
			return Stream.of(
					Arguments.of(null, exponentiatedGammaElements, otherCcrExponentiatedGammaElements, otherCcrExponentiationProofs,
							otherCcrChoiceReturnCodesEncryptionKeys, encryptedVote, exponentiatedEncryptedVote, encryptedPartialChoiceReturnCodes),
					Arguments.of(verificationCardId, null, otherCcrExponentiatedGammaElements, otherCcrExponentiationProofs,
							otherCcrChoiceReturnCodesEncryptionKeys, encryptedVote, exponentiatedEncryptedVote, encryptedPartialChoiceReturnCodes),
					Arguments.of(verificationCardId, exponentiatedGammaElements, null, otherCcrExponentiationProofs,
							otherCcrChoiceReturnCodesEncryptionKeys, encryptedVote, exponentiatedEncryptedVote, encryptedPartialChoiceReturnCodes),
					Arguments.of(verificationCardId, exponentiatedGammaElements, otherCcrExponentiatedGammaElements, null,
							otherCcrChoiceReturnCodesEncryptionKeys, encryptedVote, exponentiatedEncryptedVote, encryptedPartialChoiceReturnCodes),
					Arguments.of(verificationCardId, exponentiatedGammaElements, otherCcrExponentiatedGammaElements, otherCcrExponentiationProofs,
							null, encryptedVote, exponentiatedEncryptedVote, encryptedPartialChoiceReturnCodes),
					Arguments.of(verificationCardId, exponentiatedGammaElements, otherCcrExponentiatedGammaElements, otherCcrExponentiationProofs,
							otherCcrChoiceReturnCodesEncryptionKeys, null, exponentiatedEncryptedVote, encryptedPartialChoiceReturnCodes),
					Arguments.of(verificationCardId, exponentiatedGammaElements, otherCcrExponentiatedGammaElements, otherCcrExponentiationProofs,
							otherCcrChoiceReturnCodesEncryptionKeys, encryptedVote, null, encryptedPartialChoiceReturnCodes),
					Arguments.of(verificationCardId, exponentiatedGammaElements, otherCcrExponentiatedGammaElements, otherCcrExponentiationProofs,
							otherCcrChoiceReturnCodesEncryptionKeys, encryptedVote, exponentiatedEncryptedVote, null)
			);
		}

		@ParameterizedTest
		@MethodSource("nullArgumentsProvider")
		@DisplayName("null arguments throws a NullPointerException")
		void buildWithNullObjectsThrows(final String verificationCardId,
				final GroupVector<GqElement, GqGroup> exponentiatedGammaElements,
				final GroupVector<GroupVector<GqElement, GqGroup>, GqGroup> otherExponentiatedGammaElements,
				final GroupVector<GroupVector<ExponentiationProof, ZqGroup>, ZqGroup> otherExponentiationProofs,
				final GroupVector<ElGamalMultiRecipientPublicKey, GqGroup> otherChoiceReturnCodesEncryptionKeys,
				final ElGamalMultiRecipientCiphertext encryptedVote, final ElGamalMultiRecipientCiphertext exponentiatedEncryptedVote,
				final ElGamalMultiRecipientCiphertext encryptedPartialChoiceReturnCodes) {

			final DecryptPPCInput.Builder decryptPCCInputBuilder = new DecryptPPCInput.Builder()
					.addVerificationCardId(verificationCardId)
					.addExponentiatedGammaElements(exponentiatedGammaElements)
					.addOtherCcrExponentiatedGammaElements(otherExponentiatedGammaElements)
					.addOtherCcrExponentiationProofs(otherExponentiationProofs)
					.addOtherCcrChoiceReturnCodesEncryptionKeys(otherChoiceReturnCodesEncryptionKeys)
					.addEncryptedVote(encryptedVote)
					.addExponentiatedEncryptedVote(exponentiatedEncryptedVote)
					.addEncryptedPartialChoiceReturnCodes(encryptedPartialChoiceReturnCodes);

			assertThrows(NullPointerException.class, decryptPCCInputBuilder::build);
		}

		@Test
		@DisplayName("valid arguments does not throw")
		void buildWithValidArgumentsDoesNotThrow() {
			final DecryptPPCInput.Builder decryptPCCInputBuilder = new DecryptPPCInput.Builder()
					.addVerificationCardId(verificationCardId)
					.addExponentiatedGammaElements(exponentiatedGammaElements)
					.addOtherCcrExponentiatedGammaElements(otherCcrExponentiatedGammaElements)
					.addOtherCcrExponentiationProofs(otherCcrExponentiationProofs)
					.addOtherCcrChoiceReturnCodesEncryptionKeys(otherCcrChoiceReturnCodesEncryptionKeys)
					.addEncryptedVote(encryptedVote)
					.addExponentiatedEncryptedVote(exponentiatedEncryptedVote)
					.addEncryptedPartialChoiceReturnCodes(encryptedPartialChoiceReturnCodes);

			assertDoesNotThrow(decryptPCCInputBuilder::build);
		}

		@Test
		@DisplayName("exponentiated gama elements of size different than size of other Ccr exponentiated gamma elements IllegalArgumentException")
		void buildWithExponentiatedGammaElementsSizeNotPhiThrows() {
			final GroupVector<GqElement, GqGroup> tooShortExponentiatedGammaElements = gqGroupGenerator.genRandomGqElementVector(PHI - 1);

			DecryptPPCInput.Builder decryptPCCInputBuilder = new DecryptPPCInput.Builder()
					.addVerificationCardId(verificationCardId)
					.addExponentiatedGammaElements(tooShortExponentiatedGammaElements)
					.addOtherCcrExponentiatedGammaElements(otherCcrExponentiatedGammaElements)
					.addOtherCcrExponentiationProofs(otherCcrExponentiationProofs)
					.addOtherCcrChoiceReturnCodesEncryptionKeys(otherCcrChoiceReturnCodesEncryptionKeys)
					.addEncryptedVote(encryptedVote)
					.addExponentiatedEncryptedVote(exponentiatedEncryptedVote)
					.addEncryptedPartialChoiceReturnCodes(encryptedPartialChoiceReturnCodes);

			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, decryptPCCInputBuilder::build);
			assertEquals(
					"The exponentiated gamma elements, the other CCR's exponentiated gamma elements and the other CCR's exponentiation proofs must have the same size",
					Throwables.getRootCause(exception).getMessage());

			final GroupVector<GqElement, GqGroup> tooLongExponentiatedGammaElements = gqGroupGenerator.genRandomGqElementVector(PHI + 1);

			decryptPCCInputBuilder = new DecryptPPCInput.Builder()
					.addVerificationCardId(verificationCardId)
					.addExponentiatedGammaElements(tooLongExponentiatedGammaElements)
					.addOtherCcrExponentiatedGammaElements(otherCcrExponentiatedGammaElements)
					.addOtherCcrExponentiationProofs(otherCcrExponentiationProofs)
					.addOtherCcrChoiceReturnCodesEncryptionKeys(otherCcrChoiceReturnCodesEncryptionKeys)
					.addEncryptedVote(encryptedVote)
					.addExponentiatedEncryptedVote(exponentiatedEncryptedVote)
					.addEncryptedPartialChoiceReturnCodes(encryptedPartialChoiceReturnCodes);

			exception = assertThrows(IllegalArgumentException.class, decryptPCCInputBuilder::build);
			assertEquals(
					"The exponentiated gamma elements, the other CCR's exponentiated gamma elements and the other CCR's exponentiation proofs must have the same size",
					Throwables.getRootCause(exception).getMessage());
		}

		@Test
		@DisplayName("other exponentiated gamma elements size different 3 throws")
		void buildWithOtherExponentiatedGammaElementsSizeDifferentThreeThrows() {
			final GroupVector<GroupVector<GqElement, GqGroup>, GqGroup> tooSmallOtherExponentiatedGammaElements = otherCcrExponentiatedGammaElements = gqGroupGenerator.genRandomGqElementMatrix(
					PHI, NUM_OTHER_NODES - 1).columnStream().collect(GroupVector.toGroupVector());

			DecryptPPCInput.Builder decryptPCCInputBuilder = new DecryptPPCInput.Builder()
					.addVerificationCardId(verificationCardId)
					.addExponentiatedGammaElements(exponentiatedGammaElements)
					.addOtherCcrExponentiatedGammaElements(tooSmallOtherExponentiatedGammaElements)
					.addOtherCcrExponentiationProofs(otherCcrExponentiationProofs)
					.addOtherCcrChoiceReturnCodesEncryptionKeys(otherCcrChoiceReturnCodesEncryptionKeys)
					.addEncryptedVote(encryptedVote)
					.addExponentiatedEncryptedVote(exponentiatedEncryptedVote)
					.addEncryptedPartialChoiceReturnCodes(encryptedPartialChoiceReturnCodes);

			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, decryptPCCInputBuilder::build);
			assertEquals("There must be exactly 3 vectors of other CCR's exponentiated gamma elements",
					Throwables.getRootCause(exception).getMessage());

			final GroupVector<GroupVector<GqElement, GqGroup>, GqGroup> tooBigOtherExponentiatedGammaElements = otherCcrExponentiatedGammaElements = gqGroupGenerator.genRandomGqElementMatrix(
					PHI, NUM_OTHER_NODES + 1).columnStream().collect(GroupVector.toGroupVector());

			decryptPCCInputBuilder = new DecryptPPCInput.Builder()
					.addVerificationCardId(verificationCardId)
					.addExponentiatedGammaElements(exponentiatedGammaElements)
					.addOtherCcrExponentiatedGammaElements(tooBigOtherExponentiatedGammaElements)
					.addOtherCcrExponentiationProofs(otherCcrExponentiationProofs)
					.addOtherCcrChoiceReturnCodesEncryptionKeys(otherCcrChoiceReturnCodesEncryptionKeys)
					.addEncryptedVote(encryptedVote)
					.addExponentiatedEncryptedVote(exponentiatedEncryptedVote)
					.addEncryptedPartialChoiceReturnCodes(encryptedPartialChoiceReturnCodes);

			exception = assertThrows(IllegalArgumentException.class, decryptPCCInputBuilder::build);
			assertEquals("There must be exactly 3 vectors of other CCR's exponentiated gamma elements",
					Throwables.getRootCause(exception).getMessage());
		}
	}

	@Nested
	@DisplayName("calling decryptPCC with")
	class DecryptPCCTest {

		@BeforeEach
		void setup() {
			final String electionEventId = randomService.genRandomBase16String(ID_STRING_LENGTH).toLowerCase();
			final String verificationCardId = randomService.genRandomBase16String(ID_STRING_LENGTH).toLowerCase();
			final String verificationCardSetId = randomService.genRandomBase16String(ID_STRING_LENGTH).toLowerCase();
			final GroupVector<GqElement, GqGroup> exponentiatedGammaElements = gqGroupGenerator.genRandomGqElementVector(
					PHI);
			final GroupVector<GroupVector<GqElement, GqGroup>, GqGroup> otherExponentiatedGammaElements = GroupVector.of(
					gqGroupGenerator.genRandomGqElementVector(PHI),
					gqGroupGenerator.genRandomGqElementVector(PHI),
					gqGroupGenerator.genRandomGqElementVector(PHI)
			);
			final GroupVector<GroupVector<ExponentiationProof, ZqGroup>, ZqGroup> otherExponentiationProofs = GroupVector.of(
					genRandomExponentiationProofVector(PHI),
					genRandomExponentiationProofVector(PHI),
					genRandomExponentiationProofVector(PHI)
			);
			final GroupVector<ElGamalMultiRecipientPublicKey, GqGroup> otherChoiceReturnCodesEncryptionKeys = GroupVector.of(
					elGamalGenerator.genRandomPublicKey(PHI),
					elGamalGenerator.genRandomPublicKey(PHI),
					elGamalGenerator.genRandomPublicKey(PHI)
			);
			final ElGamalMultiRecipientCiphertext encryptedVote = elGamalGenerator.genRandomCiphertext(1);
			final ElGamalMultiRecipientCiphertext exponentiatedEncryptedVote = elGamalGenerator.genRandomCiphertext(1);
			final ElGamalMultiRecipientCiphertext encryptedPartialChoiceReturnCodes = elGamalGenerator.genRandomCiphertext(PSI);

			returnCodesNodeContext = new ReturnCodesNodeContext(NODE_ID, electionEventId, verificationCardSetId, gqGroup);

			decryptPCCInput = new DecryptPPCInput.Builder().addVerificationCardId(verificationCardId)
					.addExponentiatedGammaElements(exponentiatedGammaElements)
					.addOtherCcrExponentiatedGammaElements(otherExponentiatedGammaElements)
					.addOtherCcrExponentiationProofs(otherExponentiationProofs)
					.addOtherCcrChoiceReturnCodesEncryptionKeys(otherChoiceReturnCodesEncryptionKeys)
					.addEncryptedVote(encryptedVote)
					.addExponentiatedEncryptedVote(exponentiatedEncryptedVote)
					.addEncryptedPartialChoiceReturnCodes(encryptedPartialChoiceReturnCodes)
					.build();
		}

		@Test
		@DisplayName("any null arguments throws a NullPointerException")
		void decryptPCCWithNullArgumentsThrows() {
			assertThrows(NullPointerException.class, () -> decryptPCCService.decryptPCC(null, decryptPCCInput));
			assertThrows(NullPointerException.class, () -> decryptPCCService.decryptPCC(returnCodesNodeContext, null));
		}

		@Test
		@DisplayName("valid arguments does not throw")
		void decryptPCCWithValidInputDoesNotThrow() {
			final CombinedCorrectnessInformation combinedCorrectnessInformation = new CombinedCorrectnessInformation(
					Collections.singletonList(new CorrectnessInformation(CORRECTNESS_ID, PSI, PSI)));
			final VerificationCardSetEntity verificationCardSetEntity = new VerificationCardSetEntity();
			verificationCardSetEntity.setCombinedCorrectnessInformation(combinedCorrectnessInformation);
			when(verificationCardSetServiceMock.getVerificationCardSet(returnCodesNodeContext.getVerificationCardSetId())).thenReturn(
					verificationCardSetEntity);

			doReturn(true).when(zeroKnowledgeProofMock).verifyExponentiation(any(), any(), any(), anyList());

			final GroupVector<GqElement, GqGroup> gqElements = assertDoesNotThrow(
					() -> decryptPCCService.decryptPCC(returnCodesNodeContext, decryptPCCInput));

			assertEquals(PSI, gqElements.size());
		}

		@Test
		@DisplayName("context and input having different groups throws an IllegalArgumentException")
		void decryptPCCWithContextAndInputFromDifferentGroupsThrows() {
			final ReturnCodesNodeContext returnCodesNodeContextSpy = spy(DecryptPCCServiceTest.this.returnCodesNodeContext);
			doReturn(otherGqGroup).when(returnCodesNodeContextSpy).getEncryptionGroup();
			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> decryptPCCService.decryptPCC(returnCodesNodeContextSpy, decryptPCCInput));
			assertEquals("The context and input must have the same group.", Throwables.getRootCause(exception).getMessage());
		}

		@Test
		@DisplayName("encrypted partial choice return codes with a size different from psi throws an IllegalArgumentException")
		void decryptPCCWithEncryptedPartialChoiceReturnCodesWrongSizeThrows() {
			final CombinedCorrectnessInformation combinedCorrectnessInformation = new CombinedCorrectnessInformation(
					Collections.singletonList(new CorrectnessInformation(CORRECTNESS_ID, PSI, PSI)));
			final VerificationCardSetEntity verificationCardSetEntity = new VerificationCardSetEntity();
			verificationCardSetEntity.setCombinedCorrectnessInformation(combinedCorrectnessInformation);
			when(verificationCardSetServiceMock.getVerificationCardSet(returnCodesNodeContext.getVerificationCardSetId())).thenReturn(
					verificationCardSetEntity);

			// Encrypted partial choice return codes of size psi - 1
			final DecryptPPCInput decryptPPCInputSpy = spy(decryptPCCInput);
			final ElGamalMultiRecipientCiphertext tooShortEncryptedPartialChoiceReturnCodes = elGamalGenerator.genRandomCiphertext(PSI - 1);
			doReturn(tooShortEncryptedPartialChoiceReturnCodes).when(decryptPPCInputSpy).getEncryptedPartialChoiceReturnCodes();
			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> decryptPCCService.decryptPCC(returnCodesNodeContext, decryptPPCInputSpy));
			assertEquals(String.format("There must be psi encrypted partial Choice Return Codes. [psi: %s]", PSI),
					Throwables.getRootCause(exception).getMessage());

			// Encrypted partial choice return codes of size psi + 1
			final ElGamalMultiRecipientCiphertext tooLongEncryptedPartialChoiceReturnCodes = elGamalGenerator.genRandomCiphertext(PSI + 1);
			doReturn(tooLongEncryptedPartialChoiceReturnCodes).when(decryptPPCInputSpy).getEncryptedPartialChoiceReturnCodes();
			exception = assertThrows(IllegalArgumentException.class,
					() -> decryptPCCService.decryptPCC(returnCodesNodeContext, decryptPPCInputSpy));
			assertEquals(String.format("There must be psi encrypted partial Choice Return Codes. [psi: %s]", PSI),
					Throwables.getRootCause(exception).getMessage());
		}

		@Test
		@Disabled("proofs verification are currently disabled until the current control-component has the other ccr encryption keys.")
		@DisplayName("with failing zero knowledge proof validation throws an IllegalStateException")
		void decryptPCCWithFailingZeroKnowledgeProofVerification() {
			final CombinedCorrectnessInformation combinedCorrectnessInformation = new CombinedCorrectnessInformation(
					Collections.singletonList(new CorrectnessInformation(CORRECTNESS_ID, PSI, PSI)));
			final VerificationCardSetEntity verificationCardSetEntity = new VerificationCardSetEntity();
			verificationCardSetEntity.setCombinedCorrectnessInformation(combinedCorrectnessInformation);
			when(verificationCardSetServiceMock.getVerificationCardSet(returnCodesNodeContext.getVerificationCardSetId())).thenReturn(
					verificationCardSetEntity);

			doReturn(false).when(zeroKnowledgeProofMock).verifyExponentiation(any(), any(), any(), anyList());

			final IllegalStateException exception = assertThrows(IllegalStateException.class,
					() -> decryptPCCService.decryptPCC(returnCodesNodeContext, decryptPCCInput));
			assertEquals(String.format("The verification of the other control component's exponentiation proof failed [control component: %d]", 2),
					Throwables.getRootCause(exception).getMessage());
		}

	}
}