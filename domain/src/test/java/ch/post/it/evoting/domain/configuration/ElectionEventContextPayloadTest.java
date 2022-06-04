/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Streams;

import ch.post.it.evoting.cryptoprimitives.CryptoPrimitives;
import ch.post.it.evoting.cryptoprimitives.CryptoPrimitivesService;
import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesPayloadSignature;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalMultiRecipientPublicKey;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalService;
import ch.post.it.evoting.cryptoprimitives.hashing.HashService;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.domain.SerializationUtils;

class ElectionEventContextPayloadTest {

	private static final ObjectMapper mapper = DomainObjectMapper.getNewInstance();
	private static final HashService hashService = HashService.getInstance();
	private final static GqGroup encryptionGroup = SerializationUtils.getGqGroup();
	private static final CryptoPrimitives cryptoPrimitives = CryptoPrimitivesService.get();
	private static ElectionEventContextPayload electionEventContextPayload;
	private static ObjectNode rootNode;

	@BeforeAll
	static void setupAll() {

		// Create payload.
		final String electionEventId = cryptoPrimitives.genRandomBase16String(32).toLowerCase();
		final List<VerificationCardSetContext> verificationCardSetContexts = new ArrayList<>();

		final List<ControlComponentPublicKeys> combinedControlComponentPublicKeys = new ArrayList<>();

		IntStream.rangeClosed(1, 2).forEach(i -> verificationCardSetContexts.add(generatedVerificationCardSetContext()));

		IntStream.rangeClosed(1, 4).forEach(nodeId -> combinedControlComponentPublicKeys.add(generateCombinedControlComponentPublicKeys(nodeId)));

		final ElGamalMultiRecipientPublicKey electoralBoardPublicKey = SerializationUtils.getPublicKey();

		final GroupVector<ElGamalMultiRecipientPublicKey, GqGroup> ccrChoiceReturnCodePublicKeys = combinedControlComponentPublicKeys.stream()
				.map(ControlComponentPublicKeys::getCcrChoiceReturnCodesEncryptionPublicKey).collect(GroupVector.toGroupVector());

		final ElGamalService elGamalService = new ElGamalService();
		final ElGamalMultiRecipientPublicKey choiceReturnCodesPublicKey = elGamalService.combinePublicKeys(ccrChoiceReturnCodePublicKeys);

		final GroupVector<ElGamalMultiRecipientPublicKey, GqGroup> ccmElectionPublicKeys = Streams.concat(
				combinedControlComponentPublicKeys.stream()
						.map(ControlComponentPublicKeys::getCcmElectionPublicKey),
				Stream.of(electoralBoardPublicKey)).collect(GroupVector.toGroupVector());

		final ElGamalMultiRecipientPublicKey electionPublicKey = elGamalService.combinePublicKeys(ccmElectionPublicKeys);

		final LocalDateTime startTime = LocalDateTime.now();
		final LocalDateTime finishTime = startTime.plusWeeks(1);

		final ElectionEventContext electionEventContext = new ElectionEventContext(electionEventId, verificationCardSetContexts,
				combinedControlComponentPublicKeys, electoralBoardPublicKey, electionPublicKey, choiceReturnCodesPublicKey, startTime, finishTime);

		electionEventContextPayload = new ElectionEventContextPayload(encryptionGroup, electionEventContext);

		final byte[] payloadHash = hashService.recursiveHash(electionEventContextPayload);

		final X509Certificate certificate = SerializationUtils.generateTestCertificate();
		final CryptoPrimitivesPayloadSignature signature = new CryptoPrimitivesPayloadSignature(payloadHash, new X509Certificate[] { certificate });
		electionEventContextPayload.setSignature(signature);

		// Create expected Json.
		rootNode = mapper.createObjectNode();
		final JsonNode encryptionGroupNode = SerializationUtils.createEncryptionGroupNode(encryptionGroup);
		rootNode.set("encryptionGroup", encryptionGroupNode);

		final ObjectNode electionEventContextNode = mapper.createObjectNode();
		electionEventContextNode.put("electionEventId", electionEventId);

		final ArrayNode verificationCardSetContextsNodes = mapper.createArrayNode();
		for (VerificationCardSetContext verificationCardSetContext : verificationCardSetContexts) {
			ObjectNode verificationCardSetContextNode = mapper.createObjectNode();
			verificationCardSetContextNode.put("verificationCardSetId", verificationCardSetContext.getVerificationCardSetId());
			verificationCardSetContextNode.put("ballotBoxId", verificationCardSetContext.getBallotBoxId());
			verificationCardSetContextNode.put("testBallotBox", verificationCardSetContext.getTestBallotBox());
			verificationCardSetContextNode.put("numberOfWriteInFields", verificationCardSetContext.getNumberOfWriteInFields());
			verificationCardSetContextsNodes.add(verificationCardSetContextNode);
		}
		electionEventContextNode.set("verificationCardSetContexts", verificationCardSetContextsNodes);

		final ArrayNode combinedControlComponentPublicKeysNodes = mapper.createArrayNode();

		for (final ControlComponentPublicKeys combinedControlComponentPublicKey : combinedControlComponentPublicKeys) {
			final ObjectNode combinedControlComponentPublicKeyNode = mapper.createObjectNode();
			combinedControlComponentPublicKeyNode.put("nodeId", combinedControlComponentPublicKey.getNodeId());

			final ElGamalMultiRecipientPublicKey ccrChoiceReturnCodesEncryptionPublicKey = combinedControlComponentPublicKey.getCcrChoiceReturnCodesEncryptionPublicKey();
			final ArrayNode ccrChoiceReturnCodesEncryptionPublicKeyElements = mapper.createArrayNode();
			for (final GqElement element : ccrChoiceReturnCodesEncryptionPublicKey.getKeyElements()) {
				ccrChoiceReturnCodesEncryptionPublicKeyElements.add("0x" + element.toHashableForm());
			}

			final ElGamalMultiRecipientPublicKey ccmElectionPublicKey = combinedControlComponentPublicKey.getCcmElectionPublicKey();
			final ArrayNode ccmElectionPublicKeyElements = mapper.createArrayNode();
			for (final GqElement element : ccmElectionPublicKey.getKeyElements()) {
				ccmElectionPublicKeyElements.add("0x" + element.toHashableForm());
			}

			combinedControlComponentPublicKeyNode.set("ccrChoiceReturnCodesEncryptionPublicKey", ccrChoiceReturnCodesEncryptionPublicKeyElements);
			combinedControlComponentPublicKeyNode.set("ccmElectionPublicKey", ccmElectionPublicKeyElements);
			combinedControlComponentPublicKeysNodes.add(combinedControlComponentPublicKeyNode);
		}

		electionEventContextNode.set("combinedControlComponentPublicKeys", combinedControlComponentPublicKeysNodes);

		final ArrayNode electoralBoardPublicKeyNodeElements = mapper.createArrayNode();
		for (final GqElement element : electoralBoardPublicKey.getKeyElements()) {
			electoralBoardPublicKeyNodeElements.add("0x" + element.toHashableForm());
		}
		electionEventContextNode.set("electoralBoardPublicKey", electoralBoardPublicKeyNodeElements);

		final ArrayNode electionPublicKeyNodeElements = mapper.createArrayNode();
		for (final GqElement element : electionPublicKey.getKeyElements()) {
			electionPublicKeyNodeElements.add("0x" + element.toHashableForm());
		}
		electionEventContextNode.set("electionPublicKey", electionPublicKeyNodeElements);

		final ArrayNode choiceReturnCodesPublicKeyNodeElements = mapper.createArrayNode();
		for (final GqElement element : choiceReturnCodesPublicKey.getKeyElements()) {
			choiceReturnCodesPublicKeyNodeElements.add("0x" + element.toHashableForm());
		}
		electionEventContextNode.set("choiceReturnCodesEncryptionPublicKey", choiceReturnCodesPublicKeyNodeElements);

		final ArrayNode startTimeNodeElements = mapper.createArrayNode();
		startTimeNodeElements.add(startTime.getYear());
		startTimeNodeElements.add(startTime.getMonthValue());
		startTimeNodeElements.add(startTime.getDayOfMonth());
		startTimeNodeElements.add(startTime.getHour());
		startTimeNodeElements.add(startTime.getMinute());
		startTimeNodeElements.add(startTime.getSecond());
		startTimeNodeElements.add(startTime.getNano());
		electionEventContextNode.set("startTime", startTimeNodeElements);

		final ArrayNode finishTimeNodeElements = mapper.createArrayNode();
		finishTimeNodeElements.add(finishTime.getYear());
		finishTimeNodeElements.add(finishTime.getMonthValue());
		finishTimeNodeElements.add(finishTime.getDayOfMonth());
		finishTimeNodeElements.add(finishTime.getHour());
		finishTimeNodeElements.add(finishTime.getMinute());
		finishTimeNodeElements.add(finishTime.getSecond());
		finishTimeNodeElements.add(finishTime.getNano());
		electionEventContextNode.set("finishTime", finishTimeNodeElements);

		rootNode.set("electionEventContext", electionEventContextNode);

		final JsonNode signatureNode = SerializationUtils.createSignatureNode(signature);
		rootNode.set("signature", signatureNode);
	}

	@Test
	@DisplayName("serialized gives expected json")
	void serializePayload() throws JsonProcessingException {
		final String serializedPayload = mapper.writeValueAsString(electionEventContextPayload);
		assertEquals(rootNode.toString(), serializedPayload);
	}

	@Test
	@DisplayName("deserialized gives expected payload")
	void deserializePayload() throws IOException {
		final ElectionEventContextPayload deserializedPayload = mapper.readValue(rootNode.toString(), ElectionEventContextPayload.class);
		assertEquals(electionEventContextPayload, deserializedPayload);
		assertEquals(1, electionEventContextPayload.getElectionEventContext().getMaxNumberOfWriteInFields());
	}

	@Test
	@DisplayName("serialized then deserialized gives original payload")
	void cycle() throws IOException {
		final ElectionEventContextPayload deserializedPayload = mapper
				.readValue(mapper.writeValueAsString(electionEventContextPayload), ElectionEventContextPayload.class);

		assertEquals(electionEventContextPayload, deserializedPayload);
	}

	private static ControlComponentPublicKeys generateCombinedControlComponentPublicKeys(int nodeId) {
		final ElGamalMultiRecipientPublicKey ccrChoiceReturnCodesEncryptionPublicKey = SerializationUtils.getPublicKey();
		final ElGamalMultiRecipientPublicKey ccmElectionPublicKey = SerializationUtils.getPublicKey();
		return new ControlComponentPublicKeys(nodeId, ccrChoiceReturnCodesEncryptionPublicKey, ccmElectionPublicKey);
	}

	private static VerificationCardSetContext generatedVerificationCardSetContext() {
		final String verificationCardSetId = cryptoPrimitives.genRandomBase16String(32);
		final String ballotBoxId = cryptoPrimitives.genRandomBase16String(32);
		final boolean testBallotBox = Math.random() < 0.5;
		final int numberOfWriteInFields = 1;
		return new VerificationCardSetContext(verificationCardSetId, ballotBoxId, testBallotBox, numberOfWriteInFields);
	}

	@Test
	@DisplayName("test ElectionEventContext constructor validation")
	void testInvalidElectionEventContext() {
		final VerificationCardSetContext verificationCardSetContextOne = new VerificationCardSetContext("verificationCardSetId1", "ballotBoxId1",
				false, 0);
		final VerificationCardSetContext verificationCardSetContextTwo = new VerificationCardSetContext("verificationCardSetId1", "ballotBoxId2",
				true, 0);
		final VerificationCardSetContext verificationCardSetContextThree = new VerificationCardSetContext("verificationCardSetId2", "ballotBoxId1",
				false, 2);
		final VerificationCardSetContext verificationCardSetContextFour = new VerificationCardSetContext("verificationCardSetId2", "ballotBoxId4",
				true, 2);
		final VerificationCardSetContext verificationCardSetContextFive = new VerificationCardSetContext("verificationCardSetId2", "ballotBoxId4",
				true, -2);

		final String electionEventId = cryptoPrimitives.genRandomBase16String(32).toLowerCase();

		final List<VerificationCardSetContext> duplicateVerificationCardSetIds = new ArrayList<>();

		final List<ControlComponentPublicKeys> emptyCombinedControlComponentPublicKeys = new ArrayList<>();
		final ElGamalMultiRecipientPublicKey testElectoralBoardPublicKey = SerializationUtils.getPublicKey();
		final ElGamalMultiRecipientPublicKey testElectionPublicKey = SerializationUtils.getPublicKey();
		final ElGamalMultiRecipientPublicKey testChoiceReturnCodesPublicKey = SerializationUtils.getPublicKey();
		final LocalDateTime start = LocalDateTime.now();
		final LocalDateTime finish = start.plusWeeks(1);
		final IllegalArgumentException emptyIllegalArgumentException = assertThrows(IllegalArgumentException.class,
				() -> new ElectionEventContext(electionEventId, duplicateVerificationCardSetIds, emptyCombinedControlComponentPublicKeys,
						testElectoralBoardPublicKey, testElectionPublicKey, testChoiceReturnCodesPublicKey, start, finish));
		assertEquals("VerificationCardSetContexts cannot be empty.", emptyIllegalArgumentException.getMessage());

		duplicateVerificationCardSetIds.add(verificationCardSetContextOne);
		duplicateVerificationCardSetIds.add(verificationCardSetContextTwo);

		final IllegalArgumentException duplicateVerificationCardSetIdsIllegalArgumentException = assertThrows(IllegalArgumentException.class,
				() -> new ElectionEventContext(electionEventId, duplicateVerificationCardSetIds, emptyCombinedControlComponentPublicKeys,
						testElectoralBoardPublicKey, testElectionPublicKey, testChoiceReturnCodesPublicKey, start, finish));
		assertEquals("VerificationCardSetContexts cannot contain duplicate VerificationCardSetIds.",
				duplicateVerificationCardSetIdsIllegalArgumentException.getMessage());

		final List<VerificationCardSetContext> duplicateBallotBoxIds = new ArrayList<>();
		duplicateBallotBoxIds.add(verificationCardSetContextOne);
		duplicateBallotBoxIds.add(verificationCardSetContextThree);

		final IllegalArgumentException duplicateBallotBoxIdsIllegalArgumentException = assertThrows(IllegalArgumentException.class,
				() -> new ElectionEventContext(electionEventId, duplicateBallotBoxIds, emptyCombinedControlComponentPublicKeys,
						testElectoralBoardPublicKey, testElectionPublicKey, testChoiceReturnCodesPublicKey, start, finish));
		assertEquals("VerificationCardSetContexts cannot contain duplicate BallotBoxIds.",
				duplicateBallotBoxIdsIllegalArgumentException.getMessage());

		final List<VerificationCardSetContext> negativeNumberOfWriteInFields = new ArrayList<>();
		negativeNumberOfWriteInFields.add(verificationCardSetContextOne);
		negativeNumberOfWriteInFields.add(verificationCardSetContextFive);
		final IllegalArgumentException negativeNumberOfWriteInFieldsIllegalArgumentException = assertThrows(IllegalArgumentException.class,
				() -> new ElectionEventContext(electionEventId, negativeNumberOfWriteInFields, emptyCombinedControlComponentPublicKeys,
						testElectoralBoardPublicKey, testElectionPublicKey, testChoiceReturnCodesPublicKey, start, finish));
		assertEquals("VerificationCardSetContexts cannot contain negative numberOfWriteInFields.",
				negativeNumberOfWriteInFieldsIllegalArgumentException.getMessage());

		final List<VerificationCardSetContext> correctVerificationCardSetContexts = new ArrayList<>();
		correctVerificationCardSetContexts.add(verificationCardSetContextOne);
		correctVerificationCardSetContexts.add(verificationCardSetContextFour);

		final List<ControlComponentPublicKeys> controlComponentPublicKeys = new ArrayList<>();
		final IllegalArgumentException emptyCombinedControlComponentPublicKeysIllegalArgumentException = assertThrows(IllegalArgumentException.class,
				() -> new ElectionEventContext(electionEventId, correctVerificationCardSetContexts, controlComponentPublicKeys,
						testElectoralBoardPublicKey, testElectionPublicKey, testChoiceReturnCodesPublicKey, start, finish));
		assertEquals("CombinedControlComponentPublicKeys cannot be empty.",
				emptyCombinedControlComponentPublicKeysIllegalArgumentException.getMessage());

		assertThrows(NullPointerException.class,
				() -> new ElectionEventContext(null, correctVerificationCardSetContexts, emptyCombinedControlComponentPublicKeys,
						testElectoralBoardPublicKey, testElectionPublicKey, testChoiceReturnCodesPublicKey, start, finish));
		assertThrows(NullPointerException.class,
				() -> new ElectionEventContext(electionEventId, null, emptyCombinedControlComponentPublicKeys, testElectoralBoardPublicKey,
						testElectionPublicKey, testChoiceReturnCodesPublicKey, start, finish));
		assertThrows(NullPointerException.class,
				() -> new ElectionEventContext(electionEventId, correctVerificationCardSetContexts, null, testElectoralBoardPublicKey,
						testElectionPublicKey, testChoiceReturnCodesPublicKey, start, finish));
		assertThrows(NullPointerException.class,
				() -> new ElectionEventContext(electionEventId, correctVerificationCardSetContexts, emptyCombinedControlComponentPublicKeys, null,
						testElectionPublicKey, testChoiceReturnCodesPublicKey, start, finish));
		assertThrows(NullPointerException.class,
				() -> new ElectionEventContext(electionEventId, correctVerificationCardSetContexts, emptyCombinedControlComponentPublicKeys,
						testElectoralBoardPublicKey, null, testChoiceReturnCodesPublicKey, start, finish));
		assertThrows(NullPointerException.class,
				() -> new ElectionEventContext(electionEventId, correctVerificationCardSetContexts, emptyCombinedControlComponentPublicKeys,
						testElectoralBoardPublicKey, testElectionPublicKey, null, start, finish));
		assertThrows(NullPointerException.class,
				() -> new ElectionEventContext(electionEventId, correctVerificationCardSetContexts, emptyCombinedControlComponentPublicKeys,
						testElectoralBoardPublicKey, testElectionPublicKey, testChoiceReturnCodesPublicKey, null, finish));
		assertThrows(NullPointerException.class,
				() -> new ElectionEventContext(electionEventId, correctVerificationCardSetContexts, emptyCombinedControlComponentPublicKeys,
						testElectoralBoardPublicKey, testElectionPublicKey, testChoiceReturnCodesPublicKey, start, null));
	}
}
