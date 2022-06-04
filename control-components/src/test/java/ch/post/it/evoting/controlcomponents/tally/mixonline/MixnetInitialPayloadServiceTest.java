/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.tally.mixonline;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.controlcomponents.ElectionEventEntity;
import ch.post.it.evoting.controlcomponents.ElectionEventService;
import ch.post.it.evoting.controlcomponents.VerificationCardEntity;
import ch.post.it.evoting.controlcomponents.VerificationCardService;
import ch.post.it.evoting.controlcomponents.VerificationCardSetEntity;
import ch.post.it.evoting.controlcomponents.voting.EncryptedVerifiableVoteEntity;
import ch.post.it.evoting.controlcomponents.voting.EncryptedVerifiableVoteRepository;
import ch.post.it.evoting.controlcomponents.voting.EncryptedVerifiableVoteService;
import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.MixnetInitialPayload;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientCiphertext;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientMessage;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.RandomService;
import ch.post.it.evoting.cryptoprimitives.math.ZqElement;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;
import ch.post.it.evoting.cryptoprimitives.test.tools.data.GroupTestData;
import ch.post.it.evoting.cryptoprimitives.test.tools.generator.ElGamalGenerator;
import ch.post.it.evoting.cryptoprimitives.test.tools.generator.ZqGroupGenerator;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ExponentiationProof;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.PlaintextEqualityProof;
import ch.post.it.evoting.domain.ContextIds;
import ch.post.it.evoting.domain.voting.sendvote.EncryptedVerifiableVote;

@DisplayName("A MixnetInitialPayloadService calling")
class MixnetInitialPayloadServiceTest {

	private static final RandomService RANDOM_SERVICE = new RandomService();
	private static final SecureRandom RANDOM = new SecureRandom();

	private static ObjectMapper objectMapper;
	private static EncryptedVerifiableVoteRepository encryptedVerifiableVoteRepository;
	private static MixnetInitialPayloadService mixnetInitialPayloadService;
	private static ElectionEventService electionEventService;

	private ElGamalGenerator elGamalGenerator;
	private String electionEventId;
	private String ballotBoxId;
	private int numberOfWriteInsPlusOne;
	private List<EncryptedVerifiableVoteEntity> listOfConfirmedVotes;
	private ElGamalMultiRecipientPublicKey electionPublicKey;

	@BeforeAll
	static void setupAll() {
		encryptedVerifiableVoteRepository = mock(EncryptedVerifiableVoteRepository.class);
		objectMapper = DomainObjectMapper.getNewInstance();
		electionEventService = mock(ElectionEventService.class);
		final VerificationCardService verificationCardService = mock(VerificationCardService.class);
		final EncryptedVerifiableVoteService encryptedVerifiableVoteService = new EncryptedVerifiableVoteService(objectMapper, electionEventService,
				verificationCardService, encryptedVerifiableVoteRepository);
		mixnetInitialPayloadService = new MixnetInitialPayloadService(encryptedVerifiableVoteService);
	}

	@BeforeEach
	void setup() throws JsonProcessingException {
		electionEventId = RANDOM_SERVICE.genRandomBase16String(32).toLowerCase();
		ballotBoxId = RANDOM_SERVICE.genRandomBase16String(32).toLowerCase();
		numberOfWriteInsPlusOne = RANDOM.nextInt(10) + 1;
		final String verificationCardSetId = RANDOM_SERVICE.genRandomBase16String(32).toLowerCase();
		final String verificationCardId = RANDOM_SERVICE.genRandomBase16String(32).toLowerCase();
		final ContextIds contextIds = new ContextIds(electionEventId, verificationCardSetId, verificationCardId);
		final GqGroup gqGroup = GroupTestData.getGqGroup();
		elGamalGenerator = new ElGamalGenerator(gqGroup);
		final EncryptedVerifiableVote encryptedVerifiableVote = genEncryptedVerifiableVote(elGamalGenerator, contextIds);

		final ElectionEventEntity electionEventEntity = new ElectionEventEntity(electionEventId, gqGroup);
		final VerificationCardSetEntity verificationCardSetEntity = new VerificationCardSetEntity(verificationCardSetId, electionEventEntity);
		final VerificationCardEntity verificationCardEntity = new VerificationCardEntity(verificationCardId, verificationCardSetEntity,
				new byte[] {});

		listOfConfirmedVotes = Collections.singletonList(new EncryptedVerifiableVoteEntity.Builder()
				.setContextIds(objectMapper.writeValueAsBytes(encryptedVerifiableVote.getContextIds()))
				.setEncryptedVote(objectMapper.writeValueAsBytes(encryptedVerifiableVote.getEncryptedVote()))
				.setExponentiatedEncryptedVote(objectMapper.writeValueAsBytes(encryptedVerifiableVote.getExponentiatedEncryptedVote()))
				.setEncryptedPartialChoiceReturnCodes(objectMapper.writeValueAsBytes(encryptedVerifiableVote.getEncryptedPartialChoiceReturnCodes()))
				.setExponentiationProof(objectMapper.writeValueAsBytes(encryptedVerifiableVote.getExponentiationProof()))
				.setPlaintextEqualityProof(objectMapper.writeValueAsBytes(encryptedVerifiableVote.getPlaintextEqualityProof()))
				.setVerificationCardEntity(verificationCardEntity)
				.build());
		electionPublicKey = elGamalGenerator.genRandomPublicKey(numberOfWriteInsPlusOne);
		when(electionEventService.getEncryptionGroup(any())).thenReturn(gqGroup);
	}

	@Test
	@DisplayName("getMixnetInitialPayload with null arguments throws a NullPointerException")
	void getMixnetInitialPayloadWithNullArgumentsThrows() {
		assertThrows(NullPointerException.class,
				() -> mixnetInitialPayloadService.getMixnetInitialPayload(null, ballotBoxId, numberOfWriteInsPlusOne, listOfConfirmedVotes,
						electionPublicKey));
		assertThrows(NullPointerException.class,
				() -> mixnetInitialPayloadService.getMixnetInitialPayload(electionEventId, null, numberOfWriteInsPlusOne, listOfConfirmedVotes,
						electionPublicKey));
		assertThrows(NullPointerException.class,
				() -> mixnetInitialPayloadService.getMixnetInitialPayload(electionEventId, ballotBoxId, numberOfWriteInsPlusOne, null,
						electionPublicKey));
		assertThrows(NullPointerException.class,
				() -> mixnetInitialPayloadService.getMixnetInitialPayload(electionEventId, ballotBoxId, numberOfWriteInsPlusOne, listOfConfirmedVotes,
						null));
	}

	@Test
	@DisplayName("getMixnetInitialPayload with number of write-ins plus one too small throws an IllegalArgumentException")
	void getMixnetInitialPayloadWithTooSmallNumberOfWriteInsPlusOneThrows() {
		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> mixnetInitialPayloadService.getMixnetInitialPayload(electionEventId, ballotBoxId, 0, listOfConfirmedVotes, electionPublicKey));
		assertEquals("The number of allowed write-ins + 1 must be at least 1.", exception.getMessage());
	}

	@Test
	@DisplayName("getMixnetInitialPayload with number of write-ins plus one greater than election public key length throws an IllegalArgumentException")
	void getMixnetInitialPayloadWithTooSmallElectionPublicKey() {
		final ElGamalMultiRecipientPublicKey tooShortElectionPublicKey = elGamalGenerator.genRandomPublicKey(numberOfWriteInsPlusOne - 1);
		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> mixnetInitialPayloadService.getMixnetInitialPayload(electionEventId, ballotBoxId, numberOfWriteInsPlusOne, listOfConfirmedVotes,
						tooShortElectionPublicKey));
		assertEquals("The election public key must have at least as many elements as the number of allowed write-ins + 1.", exception.getMessage());
	}

	@Test
	@DisplayName("getMixnetInitialPayload with valid input gets valid MixnetInitialPayload")
	void getMixnetInitialPayloadWithValidInputGeneratesValidOutput() {
		when(encryptedVerifiableVoteRepository.findByVerificationCardId(any())).thenReturn(Optional.of(listOfConfirmedVotes.get(0)));
		final MixnetInitialPayload mixnetInitialPayload = assertDoesNotThrow(
				() -> mixnetInitialPayloadService.getMixnetInitialPayload(electionEventId, ballotBoxId, numberOfWriteInsPlusOne, listOfConfirmedVotes,
						electionPublicKey));
		assertEquals(electionEventId, mixnetInitialPayload.getElectionEventId());
		assertEquals(ballotBoxId, mixnetInitialPayload.getBallotBoxId());
		assertEquals(electionPublicKey, mixnetInitialPayload.getElectionPublicKey());
		assertEquals(electionPublicKey, mixnetInitialPayload.getRemainingElectionPublicKey());
		assertEquals(3, mixnetInitialPayload.getEncryptedVotes().size());
		final GqGroup group = electionPublicKey.getGroup();
		final ElGamalMultiRecipientMessage oneMessage = ElGamalMultiRecipientMessage.ones(group, numberOfWriteInsPlusOne);
		final ZqElement oneExponent = ZqElement.create(1, ZqGroup.sameOrderAs(group));
		final ElGamalMultiRecipientCiphertext E_trivial = ElGamalMultiRecipientCiphertext.getCiphertext(oneMessage, oneExponent, electionPublicKey);
		assertEquals(E_trivial, mixnetInitialPayload.getEncryptedVotes().get(1));
		assertEquals(E_trivial, mixnetInitialPayload.getEncryptedVotes().get(2));
	}

	private EncryptedVerifiableVote genEncryptedVerifiableVote(final ElGamalGenerator elGamalGenerator, final ContextIds contextIds) {
		final ElGamalMultiRecipientCiphertext encryptedVote = elGamalGenerator.genRandomCiphertext(numberOfWriteInsPlusOne);
		final GqGroup encryptionGroup = encryptedVote.getGroup();
		final ZqGroup zqGroup = ZqGroup.sameOrderAs(encryptionGroup);
		final BigInteger exponentValue = new RandomService().genRandomInteger(encryptionGroup.getQ());
		final ZqElement exponent = ZqElement.create(exponentValue, zqGroup);
		final ElGamalMultiRecipientCiphertext exponentiatedEncryptedVote = encryptedVote.exponentiate(exponent);
		final ZqGroupGenerator zqGroupGenerator = new ZqGroupGenerator(zqGroup);
		final ExponentiationProof exponentiationProof = new ExponentiationProof(zqGroupGenerator.genRandomZqElementMember(),
				zqGroupGenerator.genRandomZqElementMember());
		final PlaintextEqualityProof plaintextEqualityProof = new PlaintextEqualityProof(zqGroupGenerator.genRandomZqElementMember(),
				zqGroupGenerator.genRandomZqElementVector(2));
		final ElGamalMultiRecipientCiphertext encryptedPartialChoieReturnCodes = elGamalGenerator.genRandomCiphertext(numberOfWriteInsPlusOne);
		return new EncryptedVerifiableVote(contextIds, encryptedVote, encryptedPartialChoieReturnCodes, exponentiatedEncryptedVote,
				exponentiationProof, plaintextEqualityProof);
	}
}