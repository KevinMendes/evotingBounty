/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.voting.confirmvote;

import static ch.post.it.evoting.controlcomponents.voting.confirmvote.CreateLVCCShareService.CreateLVCCShareOutput;
import static ch.post.it.evoting.controlcomponents.voting.confirmvote.CreateLVCCShareService.MAX_CONFIRMATION_ATTEMPTS;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.controlcomponents.voting.ReturnCodesNodeContext;
import ch.post.it.evoting.controlcomponents.voting.VerificationCardStateService;
import ch.post.it.evoting.cryptoprimitives.CryptoPrimitivesService;
import ch.post.it.evoting.cryptoprimitives.hashing.HashService;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;
import ch.post.it.evoting.cryptoprimitives.test.tools.TestGroupSetup;
import ch.post.it.evoting.cryptoprimitives.test.tools.data.GroupTestData;
import ch.post.it.evoting.cryptoprimitives.test.tools.generator.GqGroupGenerator;
import ch.post.it.evoting.cryptoprimitives.test.tools.generator.ZqGroupGenerator;
import ch.post.it.evoting.cryptoprimitives.utils.KDFService;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ExponentiationProof;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ZeroKnowledgeProof;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ZeroKnowledgeProofService;

@DisplayName("CreateLVCCShareService")
class CreateLVCCShareServiceTest extends TestGroupSetup {

	private static final int NODE_ID = 1;
	private static final int l_ID = 32;
	private static final HashService hashService = spy(HashService.class);
	private static final ZeroKnowledgeProof zeroKnowledgeProofService = spy(ZeroKnowledgeProofService.class);
	private static final KDFService kdfService = spy(KDFService.getInstance());
	private static final VerificationCardStateService verificationCardStateService = mock(VerificationCardStateService.class);

	private static CreateLVCCShareService createLVCCShareService;

	@BeforeAll
	static void setUpAll() {
		createLVCCShareService = new CreateLVCCShareService(NODE_ID, kdfService, hashService, zeroKnowledgeProofService,
				verificationCardStateService);
	}

	@Nested
	@DisplayName("calling createLVCCShare with")
	class CreateLVCCTest {

		private GqElement confirmationKey;
		private ZqElement ccrjReturnCodesGenerationSecretKey;
		private String verificationCardId;
		private ReturnCodesNodeContext returnCodesNodeContext;

		@BeforeEach
		void setUp() {
			confirmationKey = gqGroupGenerator.genMember();
			ccrjReturnCodesGenerationSecretKey = zqGroupGenerator.genRandomZqElementMember();
			final String electionEventId = CryptoPrimitivesService.get().genRandomBase16String(l_ID).toLowerCase();
			final String verificationCardSetId = CryptoPrimitivesService.get().genRandomBase16String(l_ID).toLowerCase();
			verificationCardId = CryptoPrimitivesService.get().genRandomBase16String(l_ID).toLowerCase();

			returnCodesNodeContext = new ReturnCodesNodeContext(NODE_ID, electionEventId, verificationCardSetId, gqGroup);
		}

		@Test
		@DisplayName("valid parameters does not throw")
		void validParameters() {
			final GqGroup gqGroup = GroupTestData.getLargeGqGroup();
			final ZqGroup zqGroup = ZqGroup.sameOrderAs(gqGroup);
			final GqGroupGenerator gqGroupGenerator = new GqGroupGenerator(gqGroup);
			final ZqGroupGenerator zqGroupGenerator = new ZqGroupGenerator(zqGroup);

			confirmationKey = gqGroupGenerator.genMember();
			ccrjReturnCodesGenerationSecretKey = zqGroupGenerator.genRandomZqElementMember();

			when(verificationCardStateService.isLCCShareCreated(verificationCardId)).thenReturn(true);
			when(verificationCardStateService.isNotConfirmed(verificationCardId)).thenReturn(true);

			doReturn(new byte[] { 0x4 }).when(hashService).recursiveHash(any());

			final ExponentiationProof exponentiationProof = new ExponentiationProof(zqGroupGenerator.genRandomZqElementMember(),
					zqGroupGenerator.genRandomZqElementMember());
			doReturn(exponentiationProof).when(zeroKnowledgeProofService).genExponentiationProof(any(), any(), any(), any());

			final CreateLVCCShareOutput output = createLVCCShareService.createLVCCShare(confirmationKey, ccrjReturnCodesGenerationSecretKey,
					verificationCardId, returnCodesNodeContext);

			assertEquals(0, output.getConfirmationAttempts());
			assertEquals(gqGroup, output.getHashedSquaredConfirmationKey().getGroup());
			assertEquals(gqGroup, output.getLongVoteCastReturnCodeShare().getGroup());
			assertEquals(gqGroup, output.getVoterVoteCastReturnCodeGenerationPublicKey().getGroup());
			assertTrue(gqGroup.hasSameOrderAs(output.getExponentiationProof().getGroup()));
		}

		@Test
		@DisplayName("any null parameter throws NullPointerException")
		void nullParameters() {
			assertAll(
					() -> assertThrows(NullPointerException.class,
							() -> createLVCCShareService.createLVCCShare(null, ccrjReturnCodesGenerationSecretKey, verificationCardId,
									returnCodesNodeContext)),
					() -> assertThrows(NullPointerException.class,
							() -> createLVCCShareService.createLVCCShare(confirmationKey, null, verificationCardId, returnCodesNodeContext)),
					() -> assertThrows(NullPointerException.class,
							() -> createLVCCShareService.createLVCCShare(confirmationKey, ccrjReturnCodesGenerationSecretKey, null,
									returnCodesNodeContext)),
					() -> assertThrows(NullPointerException.class,
							() -> createLVCCShareService.createLVCCShare(confirmationKey, ccrjReturnCodesGenerationSecretKey, verificationCardId,
									null))
			);
		}

		@Test
		@DisplayName("confirmation key and secret key having different group order throws IllegalArgumentException")
		void diffGroupKeys() {
			final GqElement otherGroupConfirmationKey = otherGqGroupGenerator.genMember();

			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> createLVCCShareService.createLVCCShare(otherGroupConfirmationKey, ccrjReturnCodesGenerationSecretKey, verificationCardId,
							returnCodesNodeContext));
			assertEquals("Confirmation key and CCR_j Return Codes Generation secret key must have the same group order.", exception.getMessage());
		}

		@Test
		@DisplayName("long choice return codes not computed throw IllegalArgumentException")
		void notPreviouslyComputedLCC() {
			when(verificationCardStateService.isLCCShareCreated(verificationCardId)).thenReturn(false);

			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> createLVCCShareService.createLVCCShare(confirmationKey, ccrjReturnCodesGenerationSecretKey, verificationCardId,
							returnCodesNodeContext));
			final String message = String.format("The CCR_j did not compute the long Choice Return Code shares for verification card %s.",
					verificationCardId);
			assertEquals(message, exception.getMessage());
		}

		@Test
		@DisplayName("max confirmation attempts exceeded throws IllegalArgumentException")
		void exceededAttempts() {
			when(verificationCardStateService.isLCCShareCreated(verificationCardId)).thenReturn(true);
			when(verificationCardStateService.isNotConfirmed(verificationCardId)).thenReturn(true);
			when(verificationCardStateService.getConfirmationAttempts(verificationCardId)).thenReturn(MAX_CONFIRMATION_ATTEMPTS);

			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> createLVCCShareService.createLVCCShare(confirmationKey, ccrjReturnCodesGenerationSecretKey, verificationCardId,
							returnCodesNodeContext));
			assertEquals(String.format("Max confirmation attempts of %s exceeded.", MAX_CONFIRMATION_ATTEMPTS), exception.getMessage());
		}

	}

}