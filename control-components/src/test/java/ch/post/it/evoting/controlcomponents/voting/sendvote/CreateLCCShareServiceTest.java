/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.voting.sendvote;

import static ch.post.it.evoting.controlcomponents.voting.sendvote.CreateLCCShareService.CreateLCCShareOutput;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.controlcomponents.VerificationCardSetEntity;
import ch.post.it.evoting.controlcomponents.VerificationCardSetService;
import ch.post.it.evoting.controlcomponents.voting.ReturnCodesNodeContext;
import ch.post.it.evoting.controlcomponents.voting.VerificationCardStateService;
import ch.post.it.evoting.cryptoprimitives.CryptoPrimitivesService;
import ch.post.it.evoting.cryptoprimitives.domain.election.CombinedCorrectnessInformation;
import ch.post.it.evoting.cryptoprimitives.domain.election.CorrectnessInformation;
import ch.post.it.evoting.cryptoprimitives.hashing.HashService;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableString;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
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

@DisplayName("CreateLCCShareService")
class CreateLCCShareServiceTest extends TestGroupSetup {

	private static final int NODE_ID = 1;
	private static final int PSI = 5;
	private static final int l_ID = 32;
	private static final String CORRECTNESS_ID = "1";
	private static final HashService hashService = spy(HashService.class);
	private static final ZeroKnowledgeProof zeroKnowledgeProofService = spy(ZeroKnowledgeProofService.class);
	private static final VerificationCardSetService verificationCardSetServiceMock = mock(VerificationCardSetService.class);
	private static final VerificationCardStateService verificationCardStateServiceMock = mock(VerificationCardStateService.class);
	private static final KDFService kdfService = spy(KDFService.getInstance());

	private static CreateLCCShareService createLCCShareService;

	@BeforeAll
	static void setUpAll() {
		createLCCShareService = new CreateLCCShareService(kdfService, hashService, zeroKnowledgeProofService, verificationCardSetServiceMock,
				verificationCardStateServiceMock);
	}

	@Nested
	@DisplayName("calling createLCCShare with")
	class CreateLCCShareTest {

		private GroupVector<GqElement, GqGroup> partialChoiceReturnCodes;
		private ZqElement ccrjReturnCodesGenerationSecretKey;
		private String verificationCardId;
		private ReturnCodesNodeContext context;

		@BeforeEach
		void setUp() {
			boolean allDistinct;
			do {
				partialChoiceReturnCodes = gqGroupGenerator.genRandomGqElementVector(PSI);
				allDistinct = partialChoiceReturnCodes.stream()
						.allMatch(ConcurrentHashMap.newKeySet()::add);
			}
			while (!allDistinct);

			ccrjReturnCodesGenerationSecretKey = zqGroupGenerator.genRandomZqElementMember();
			verificationCardId = CryptoPrimitivesService.get().genRandomBase16String(l_ID).toLowerCase();

			final String electionEventId = CryptoPrimitivesService.get().genRandomBase16String(l_ID).toLowerCase();
			final String verificationCardSetId = CryptoPrimitivesService.get().genRandomBase16String(l_ID).toLowerCase();
			context = new ReturnCodesNodeContext(NODE_ID, electionEventId, verificationCardSetId, gqGroup);

			reset(verificationCardSetServiceMock);
		}

		@Test
		@DisplayName("valid parameters does not throw")
		void validParameters() {
			final GqGroup gqGroup = GroupTestData.getLargeGqGroup();
			final ZqGroup zqGroup = ZqGroup.sameOrderAs(gqGroup);
			final GqGroupGenerator gqGroupGenerator = new GqGroupGenerator(gqGroup);
			final ZqGroupGenerator zqGroupGenerator = new ZqGroupGenerator(zqGroup);

			final String electionEventId = context.getElectionEventId();
			final String verificationCardSetId = context.getVerificationCardSetId();

			doReturn(new byte[] { 0x4 }).when(hashService).recursiveHash(any());

			final List<String> allowList = partialChoiceReturnCodes.stream()
					.map(pCC_id_i -> hashService.hashAndSquare(pCC_id_i.getValue(), gqGroup))
					.map(hpCC_id_i -> hashService.recursiveHash(hpCC_id_i, HashableString.from(verificationCardId),
							HashableString.from(electionEventId)))
					.map(lpCC_id_id -> Base64.getEncoder().encodeToString(lpCC_id_id))
					.collect(Collectors.toList());
			final CombinedCorrectnessInformation combinedCorrectnessInformation = new CombinedCorrectnessInformation(
					Collections.singletonList(new CorrectnessInformation(CORRECTNESS_ID, PSI, PSI)));
			final VerificationCardSetEntity verificationCardSetEntity = new VerificationCardSetEntity();
			verificationCardSetEntity.setAllowList(allowList);
			verificationCardSetEntity.setCombinedCorrectnessInformation(combinedCorrectnessInformation);
			when(verificationCardSetServiceMock.getVerificationCardSet(verificationCardSetId)).thenReturn(verificationCardSetEntity);

			when(verificationCardStateServiceMock.isLCCShareCreated(verificationCardId)).thenReturn(false);

			ccrjReturnCodesGenerationSecretKey = zqGroupGenerator.genRandomZqElementMember();

			boolean allDistinct;
			do {
				partialChoiceReturnCodes = gqGroupGenerator.genRandomGqElementVector(PSI);
				allDistinct = partialChoiceReturnCodes.stream()
						.allMatch(ConcurrentHashMap.newKeySet()::add);
			}
			while (!allDistinct);

			final ExponentiationProof exponentiationProof = new ExponentiationProof(zqGroupGenerator.genRandomZqElementMember(),
					zqGroupGenerator.genRandomZqElementMember());
			doReturn(exponentiationProof).when(zeroKnowledgeProofService).genExponentiationProof(any(), any(), any(), any());

			final CreateLCCShareOutput output = createLCCShareService
					.createLCCShare(partialChoiceReturnCodes, ccrjReturnCodesGenerationSecretKey, verificationCardId, context);

			assertEquals(PSI, output.getHashedPartialChoiceReturnCodes().size());
			assertEquals(PSI, output.getLongChoiceReturnCodeShare().size());
			assertTrue(gqGroup.hasSameOrderAs(output.getExponentiationProof().getGroup()));
			assertTrue(gqGroup.hasSameOrderAs(output.getVoterChoiceReturnCodeGenerationPublicKey().getGroup()));
		}

		@Test
		@DisplayName("any null parameter throws NullPointerException")
		void nullParameters() {
			assertAll(
					() -> assertThrows(NullPointerException.class, () -> createLCCShareService
							.createLCCShare(null, ccrjReturnCodesGenerationSecretKey, verificationCardId, context)),
					() -> assertThrows(NullPointerException.class,
							() -> createLCCShareService.createLCCShare(partialChoiceReturnCodes, null, verificationCardId, context)),
					() -> assertThrows(NullPointerException.class, () -> createLCCShareService
							.createLCCShare(partialChoiceReturnCodes, ccrjReturnCodesGenerationSecretKey, null, context)),
					() -> assertThrows(NullPointerException.class, () -> createLCCShareService
							.createLCCShare(partialChoiceReturnCodes, ccrjReturnCodesGenerationSecretKey, verificationCardId, null))
			);
		}

		@Test
		@DisplayName("codes and keys having different group order throws IllegalArgumentException")
		void diffGroupCodesKeys() {
			final ZqElement otherGroupSecretKey = otherZqGroupGenerator.genRandomZqElementMember();

			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> createLCCShareService.createLCCShare(partialChoiceReturnCodes, otherGroupSecretKey, verificationCardId, context));
			assertEquals("The partial choice return codes and return codes generation secret key must have the same group order.",
					exception.getMessage());
		}

		@Test
		@DisplayName("partial codes not all distinct throws IllegalArgumentException")
		void notDistinctCodes() {
			final String verificationCardSetId = context.getVerificationCardSetId();

			final CombinedCorrectnessInformation combinedCorrectnessInformation = new CombinedCorrectnessInformation(
					Collections.singletonList(new CorrectnessInformation(CORRECTNESS_ID, PSI, PSI)));
			final VerificationCardSetEntity verificationCardSetEntity = new VerificationCardSetEntity();
			verificationCardSetEntity.setCombinedCorrectnessInformation(combinedCorrectnessInformation);
			when(verificationCardSetServiceMock.getVerificationCardSet(verificationCardSetId)).thenReturn(verificationCardSetEntity);

			final GroupVector<GqElement, GqGroup> notDistinctCodes = partialChoiceReturnCodes.append(partialChoiceReturnCodes.get(0));

			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> createLCCShareService
					.createLCCShare(notDistinctCodes, ccrjReturnCodesGenerationSecretKey, verificationCardId, context));
			assertEquals(String.format("The number of partial choice return codes (%s) must be equal to psi (%s).", PSI + 1, PSI),
					exception.getMessage());
		}

		@Test
		@DisplayName("long choice return codes share already generated throws IllegalArgumentException")
		void alreadyGeneratedLongChoiceReturnCodesShare() {
			final String verificationCardSetId = context.getVerificationCardSetId();

			final CombinedCorrectnessInformation combinedCorrectnessInformation = new CombinedCorrectnessInformation(
					Collections.singletonList(new CorrectnessInformation("1", PSI, PSI)));
			final VerificationCardSetEntity verificationCardSetEntity = new VerificationCardSetEntity();
			verificationCardSetEntity.setCombinedCorrectnessInformation(combinedCorrectnessInformation);
			when(verificationCardSetServiceMock.getVerificationCardSet(verificationCardSetId)).thenReturn(verificationCardSetEntity);

			when(verificationCardStateServiceMock.isLCCShareCreated(verificationCardId)).thenReturn(true);

			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> createLCCShareService
					.createLCCShare(partialChoiceReturnCodes, ccrjReturnCodesGenerationSecretKey, verificationCardId, context));
			assertEquals(
					String.format("The CCR_j already generated the long Choice Return Code share in a previous attempt for verification card %s.",
							verificationCardId), exception.getMessage());
		}

		@Test
		@DisplayName("partial choice return code not in allow list throws IllegalArgumentException")
		void pccNotInAllowList() {
			final GqGroup gqGroup = GroupTestData.getLargeGqGroup();
			final ZqGroup zqGroup = ZqGroup.sameOrderAs(gqGroup);
			final GqGroupGenerator gqGroupGenerator = new GqGroupGenerator(gqGroup);
			final ZqGroupGenerator zqGroupGenerator = new ZqGroupGenerator(zqGroup);

			final String verificationCardSetId = context.getVerificationCardSetId();

			final List<String> allowList = Collections.singletonList("asdasd");
			final CombinedCorrectnessInformation combinedCorrectnessInformation = new CombinedCorrectnessInformation(
					Collections.singletonList(new CorrectnessInformation(CORRECTNESS_ID, PSI, PSI)));
			final VerificationCardSetEntity verificationCardSetEntity = new VerificationCardSetEntity();
			verificationCardSetEntity.setAllowList(allowList);
			verificationCardSetEntity.setCombinedCorrectnessInformation(combinedCorrectnessInformation);
			when(verificationCardSetServiceMock.getVerificationCardSet(verificationCardSetId)).thenReturn(verificationCardSetEntity);

			when(verificationCardStateServiceMock.isLCCShareCreated(verificationCardId)).thenReturn(false);

			ccrjReturnCodesGenerationSecretKey = zqGroupGenerator.genRandomZqElementMember();

			boolean allDistinct;
			do {
				partialChoiceReturnCodes = gqGroupGenerator.genRandomGqElementVector(PSI);
				allDistinct = partialChoiceReturnCodes.stream()
						.allMatch(ConcurrentHashMap.newKeySet()::add);
			}
			while (!allDistinct);

			final ExponentiationProof exponentiationProof = new ExponentiationProof(zqGroupGenerator.genRandomZqElementMember(),
					zqGroupGenerator.genRandomZqElementMember());
			doReturn(exponentiationProof).when(zeroKnowledgeProofService).genExponentiationProof(any(), any(), any(), any());

			final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> createLCCShareService
					.createLCCShare(partialChoiceReturnCodes, ccrjReturnCodesGenerationSecretKey, verificationCardId, context));
			assertEquals("The partial Choice Return Codes allow list does not contain the partial Choice Return Code.", exception.getMessage());
		}

		@Test
		@DisplayName("allow list not found throws IllegalArgumentException")
		void allowListNotFound() {
			final String verificationCardSetId = context.getVerificationCardSetId();

			final CombinedCorrectnessInformation combinedCorrectnessInformation = new CombinedCorrectnessInformation(
					Collections.singletonList(new CorrectnessInformation(CORRECTNESS_ID, PSI, PSI)));
			final VerificationCardSetEntity verificationCardSetEntity = new VerificationCardSetEntity();
			verificationCardSetEntity.setAllowList(Collections.emptyList());
			verificationCardSetEntity.setCombinedCorrectnessInformation(combinedCorrectnessInformation);
			when(verificationCardSetServiceMock.getVerificationCardSet(verificationCardSetId)).thenReturn(verificationCardSetEntity);

			when(verificationCardStateServiceMock.isLCCShareCreated(verificationCardId)).thenReturn(false);

			doReturn(0).when(hashService).getHashLength();
			doReturn(new byte[] { 0x4 }).when(hashService).recursiveHash(any());

			final String errorMessage = String.format(
					"The partial Choice Return Codes allow list must exist for verification card set. [verificationCardSetId: %s]",
					context.getVerificationCardSetId());
			final IllegalStateException exception = assertThrows(IllegalStateException.class, () -> createLCCShareService
					.createLCCShare(partialChoiceReturnCodes, ccrjReturnCodesGenerationSecretKey, verificationCardId, context));
			assertEquals(errorMessage, exception.getMessage());
		}

	}

}