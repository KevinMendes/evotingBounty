/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.voting.confirmvote;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.google.common.collect.ImmutableList;

import ch.post.it.evoting.controlcomponents.VerificationCardSetEntity;
import ch.post.it.evoting.controlcomponents.VerificationCardSetService;
import ch.post.it.evoting.controlcomponents.voting.ReturnCodesNodeContext;
import ch.post.it.evoting.controlcomponents.voting.VerificationCardStateService;
import ch.post.it.evoting.cryptoprimitives.hashing.HashService;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableList;
import ch.post.it.evoting.cryptoprimitives.hashing.HashableString;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.RandomService;
import ch.post.it.evoting.cryptoprimitives.test.tools.data.GroupTestData;

@DisplayName("VerifyLVCCHashServiceTest calling")
class VerifyLVCCHashServiceTest {

	private static final RandomService randomService = new RandomService();
	private static final int HLVCC_LENGTH = 10;
	private static final int ID_SIZE = 32;

	private static final int NODE_ID = 1;
	private static final HashService hashService = spy(HashService.class);
	private static final VerificationCardSetService verificationCardSetServiceMock = mock(VerificationCardSetService.class);
	private static final VerificationCardStateService verificationCardStateServiceMock = mock(VerificationCardStateService.class);

	private static VerifyLVCCHashService verifyLVCCHashService;

	private String hlVCC1;
	private List<String> otherCCRhlVCC;
	private String verificationCardId;
	private ReturnCodesNodeContext context;

	@BeforeAll
	static void setupAll() {
		verifyLVCCHashService = new VerifyLVCCHashService(NODE_ID, hashService, verificationCardSetServiceMock, verificationCardStateServiceMock);
	}

	@BeforeEach
	void setup() {
		hlVCC1 = randomService.genRandomBase64String(HLVCC_LENGTH);
		otherCCRhlVCC = Stream.generate(() -> randomService.genRandomBase64String(HLVCC_LENGTH)).limit(3).collect(Collectors.toList());
		verificationCardId = randomService.genRandomBase64String(ID_SIZE);
		final String electionEventId = randomService.genRandomBase64String(ID_SIZE);
		final String verificationCardSetId = randomService.genRandomBase64String(ID_SIZE);
		final GqGroup encryptionGroup = GroupTestData.getGqGroup();
		context = new ReturnCodesNodeContext(NODE_ID, electionEventId, verificationCardSetId, encryptionGroup);
	}

	@Test
	@DisplayName("verifyLVCCHash with null parameters throws a NullPointerException")
	void verifyLVCCHashWithNullParametersThrows() {
		assertThrows(NullPointerException.class, () -> verifyLVCCHashService.verifyLVCCHash(null, otherCCRhlVCC, verificationCardId, context));
		assertThrows(NullPointerException.class, () -> verifyLVCCHashService.verifyLVCCHash(hlVCC1, null, verificationCardId, context));
		assertThrows(NullPointerException.class, () -> verifyLVCCHashService.verifyLVCCHash(hlVCC1, otherCCRhlVCC, null, context));
		assertThrows(NullPointerException.class, () -> verifyLVCCHashService.verifyLVCCHash(hlVCC1, otherCCRhlVCC, verificationCardId, null));
	}

	@Test
	@DisplayName("verifyLVCCHash with too few other CCRs hashed Long Vote Cast Return Codes throws an IllegalArgumentException")
	void verifyLVCCHashWithTooFewOtherCCRsHashedLongVoteCastReturnCodesThrows() {
		otherCCRhlVCC = Stream.generate(() -> randomService.genRandomBase64String(HLVCC_LENGTH)).limit(2).collect(Collectors.toList());
		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> verifyLVCCHashService.verifyLVCCHash(hlVCC1, otherCCRhlVCC, verificationCardId, context));
		assertEquals("There must be exactly 3 other CCRs hashed long vote cast return codes.", exception.getMessage());
	}

	@Test
	@DisplayName("verifyLVCCHash with too many other CCRs hashed Long Vote Cast Return Codes throws an IllegalArgumentException")
	void verifyLVCCHashWithTooManyOtherCCRsHashedLongVoteCastReturnCodesThrows() {
		otherCCRhlVCC = Stream.generate(() -> randomService.genRandomBase64String(HLVCC_LENGTH)).limit(4).collect(Collectors.toList());
		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> verifyLVCCHashService.verifyLVCCHash(hlVCC1, otherCCRhlVCC, verificationCardId, context));
		assertEquals("There must be exactly 3 other CCRs hashed long vote cast return codes.", exception.getMessage());
	}

	@Test
	@DisplayName("verifyLVCCHash with LCC Share not created throws an IllegalArgumentException")
	void verifyLVCCHashWithLCCSharesNotCreated() {
		final String verificationCardSetId = context.getVerificationCardSetId();

		when(verificationCardStateServiceMock.isLCCShareCreated(verificationCardId)).thenReturn(false);
		when(verificationCardStateServiceMock.isNotConfirmed(verificationCardId)).thenReturn(true);

		final List<String> allowList = Collections.singletonList("");
		final VerificationCardSetEntity verificationCardSetEntity = new VerificationCardSetEntity();
		verificationCardSetEntity.setAllowList(allowList);
		when(verificationCardSetServiceMock.getVerificationCardSet(verificationCardSetId)).thenReturn(verificationCardSetEntity);

		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> verifyLVCCHashService.verifyLVCCHash(hlVCC1, otherCCRhlVCC, verificationCardId, context));
		assertEquals(String.format("The CCR_j did not compute the long Choice Return Code shares for verification card %s.", verificationCardId),
				exception.getMessage());
	}

	@Test
	@DisplayName("verifyLVCCHash with vote confirmed throws an IllegalArgumentException")
	void verifyLVCCHashWithVoteConfirmed() {
		final String verificationCardSetId = context.getVerificationCardSetId();

		when(verificationCardStateServiceMock.isLCCShareCreated(verificationCardId)).thenReturn(true);
		when(verificationCardStateServiceMock.isNotConfirmed(verificationCardId)).thenReturn(false);

		final List<String> allowList = Collections.singletonList("");
		final VerificationCardSetEntity verificationCardSetEntity = new VerificationCardSetEntity();
		verificationCardSetEntity.setAllowList(allowList);
		when(verificationCardSetServiceMock.getVerificationCardSet(verificationCardSetId)).thenReturn(verificationCardSetEntity);

		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> verifyLVCCHashService.verifyLVCCHash(hlVCC1, otherCCRhlVCC, verificationCardId, context));
		assertEquals(String.format("The CCR_j did already confirm the long Choice Return Code shares for verification card %s.", verificationCardId),
				exception.getMessage());
	}

	@Test
	@DisplayName("verifyLVCCHash with hash present in list returns true")
	void verifyLVCCHashWithHashsPresentInAllowList() {
		final String electionEventId = context.getElectionEventId();
		final String verificationCardSetId = context.getVerificationCardSetId();

		when(verificationCardStateServiceMock.isLCCShareCreated(verificationCardId)).thenReturn(true);
		when(verificationCardStateServiceMock.isNotConfirmed(verificationCardId)).thenReturn(true);

		final List<HashableString> i_aux_list = Stream.of("VerifyLVCCHash", electionEventId, verificationCardSetId, verificationCardId)
				.map(HashableString::from)
				.collect(ImmutableList.toImmutableList());
		final HashableList i_aux = HashableList.from(i_aux_list);
		final HashableString hlVCC_id_1 = HashableString.from(hlVCC1);
		final HashableString hlVCC_id_2 = HashableString.from(otherCCRhlVCC.get(0));
		final HashableString hlVCC_id_3 = HashableString.from(otherCCRhlVCC.get(1));
		final HashableString hlVCC_id_4 = HashableString.from(otherCCRhlVCC.get(2));
		final String hhlVCC_id = Base64.getEncoder().encodeToString(hashService.recursiveHash(i_aux, hlVCC_id_1, hlVCC_id_2, hlVCC_id_3, hlVCC_id_4));
		final List<String> allowList = Collections.singletonList(hhlVCC_id);
		final VerificationCardSetEntity verificationCardSetEntity = new VerificationCardSetEntity();
		verificationCardSetEntity.setAllowList(allowList);
		when(verificationCardSetServiceMock.getVerificationCardSet(verificationCardSetId)).thenReturn(verificationCardSetEntity);

		assertTrue(verifyLVCCHashService.verifyLVCCHash(hlVCC1, otherCCRhlVCC, verificationCardId, context));
	}

	@Test
	@DisplayName("verifyLVCCHash with hash not present in list returns false")
	void verifyLVCCHashWithHashsNotPresentInAllowList() {
		final String verificationCardSetId = context.getVerificationCardSetId();

		when(verificationCardStateServiceMock.isLCCShareCreated(verificationCardId)).thenReturn(true);
		when(verificationCardStateServiceMock.isNotConfirmed(verificationCardId)).thenReturn(true);

		final List<String> allowList = Collections.singletonList("");
		final VerificationCardSetEntity verificationCardSetEntity = new VerificationCardSetEntity();
		verificationCardSetEntity.setAllowList(allowList);
		when(verificationCardSetServiceMock.getVerificationCardSet(verificationCardSetId)).thenReturn(verificationCardSetEntity);

		assertFalse(verifyLVCCHashService.verifyLVCCHash(hlVCC1, otherCCRhlVCC, verificationCardId, context));
	}
}