/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.voting.confirmvote;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Throwables;

import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesPayloadSignature;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.RandomService;
import ch.post.it.evoting.cryptoprimitives.test.tools.data.GroupTestData;
import ch.post.it.evoting.cryptoprimitives.test.tools.generator.GqGroupGenerator;
import ch.post.it.evoting.domain.ContextIds;
import ch.post.it.evoting.domain.MapperSetUp;
import ch.post.it.evoting.domain.SerializationUtils;

@DisplayName("ConfirmationKeyPayload")
class ConfirmationKeyPayloadTest extends MapperSetUp {

	private static final int ID_SIZE = 32;
	private static final RandomService RANDOM_SERVICE = new RandomService();
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();
	private static final byte[] randomBytes = new byte[10];

	private GqGroup encryptionGroup;
	private ConfirmationKey confirmationKey;
	private String requestId;
	private CryptoPrimitivesPayloadSignature signature;
	private ConfirmationKeyPayload confirmationKeyPayload;
	private ObjectNode rootNode;

	@BeforeEach
	void setup() throws JsonProcessingException {
		final String electionEventId = RANDOM_SERVICE.genRandomBase16String(ID_SIZE);
		final String verificationCardSetId = RANDOM_SERVICE.genRandomBase16String(ID_SIZE);
		final String verificationCardId = RANDOM_SERVICE.genRandomBase16String(ID_SIZE);
		final ContextIds contextIds = new ContextIds(electionEventId, verificationCardSetId, verificationCardId);

		encryptionGroup = GroupTestData.getGqGroup();
		final GqElement element = new GqGroupGenerator(encryptionGroup).genMember();

		confirmationKey = new ConfirmationKey(contextIds, element);
		requestId = RANDOM_SERVICE.genRandomBase16String(ID_SIZE);

		// Generate random bytes for signature content and create payload signature.
		SECURE_RANDOM.nextBytes(randomBytes);
		final X509Certificate certificate = SerializationUtils.generateTestCertificate();
		signature = new CryptoPrimitivesPayloadSignature(randomBytes, new X509Certificate[] { certificate });

		confirmationKeyPayload = new ConfirmationKeyPayload(encryptionGroup, confirmationKey, requestId, signature);

		// Create expected json
		rootNode = mapper.createObjectNode();

		final JsonNode encryptionGroupNode = SerializationUtils.createEncryptionGroupNode(encryptionGroup);
		rootNode.set("encryptionGroup", encryptionGroupNode);

		final ObjectNode contextIdsNode = mapper.createObjectNode();
		contextIdsNode.put("electionEventId", electionEventId);
		contextIdsNode.put("verificationCardSetId", verificationCardSetId);
		contextIdsNode.put("verificationCardId", verificationCardId);

		final ObjectNode confirmationKeyNode = mapper.createObjectNode();
		confirmationKeyNode.set("contextIds", contextIdsNode);
		confirmationKeyNode.put("element", "0x" + element.getValue().toString(16).toUpperCase());
		rootNode.set("confirmationKey", confirmationKeyNode);

		rootNode.put("requestId", requestId);

		final JsonNode signatureNode = SerializationUtils.createSignatureNode(signature);
		rootNode.set("signature", signatureNode);
	}

	@Test
	@DisplayName("construction with null parameters throws a NullPointerException")
	void constructWithNullParametersThrows() {
		assertThrows(NullPointerException.class, () -> new ConfirmationKeyPayload(null, confirmationKey, requestId));
		assertThrows(NullPointerException.class, () -> new ConfirmationKeyPayload(encryptionGroup, null, requestId));
		assertThrows(NullPointerException.class, () -> new ConfirmationKeyPayload(encryptionGroup, confirmationKey, null));
	}

	@Test
	@DisplayName("construction with confirmation key element not from encryption group throws IllegalArgumentException")
	void constructWithConfirmationKeyNotInEncryptionGroupThrows() {
		final GqGroup differentEncryptionGroup = GroupTestData.getDifferentGqGroup(encryptionGroup);
		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> new ConfirmationKeyPayload(differentEncryptionGroup, confirmationKey, requestId));
		assertEquals("The confirmation key must be in the encryption group", Throwables.getRootCause(exception).getMessage());
	}

	@Test
	@DisplayName("serializing a ConfirmationKey gives the expected json")
	void serializeConfirmationKey() throws JsonProcessingException {
		final String serializedConfirmationKeyPayload = mapper.writeValueAsString(confirmationKeyPayload);
		assertEquals(rootNode.toString(), serializedConfirmationKeyPayload);
	}

	@Test
	@DisplayName("deserializing a ConfirmationKey gives the expected ConfirmationKey")
	void deserializeConfirmationKey() throws IOException {
		final ConfirmationKeyPayload deserializedConfirmationKeyPayload = mapper.readValue(rootNode.toString(), ConfirmationKeyPayload.class);
		assertEquals(confirmationKeyPayload, deserializedConfirmationKeyPayload);
	}

	@Test
	@DisplayName("serializing then deserializing a ConfirmationKey gives the original ConfirmationKey")
	void cycle() throws IOException {
		final String serializedConfirmationKeyPayload = mapper.writeValueAsString(confirmationKeyPayload);
		final ConfirmationKeyPayload deserializedConfirmationKeyPayload = mapper.readValue(serializedConfirmationKeyPayload, ConfirmationKeyPayload.class);
		assertEquals(confirmationKeyPayload, deserializedConfirmationKeyPayload);
	}

	@Test
	@DisplayName("equals returns expected value")
	void testEquals() {
		final GqGroup encryptionGroup = GroupTestData.getGqGroup();
		final GqGroupGenerator gqGroupGenerator = new GqGroupGenerator(encryptionGroup);
		final GqElement element1 = gqGroupGenerator.genMember();
		final ContextIds contextIds = confirmationKey.getContextIds();
		final ConfirmationKey confirmationKey1 = new ConfirmationKey(contextIds, element1);
		final String requestId1 = "requestId1";

		final ConfirmationKeyPayload payload1 = new ConfirmationKeyPayload(encryptionGroup, confirmationKey1, requestId1);
		final GqElement element2 = gqGroupGenerator.otherElement(element1);
		final ConfirmationKey confirmationKey2 = new ConfirmationKey(contextIds, element2);
		final ConfirmationKeyPayload payload2 = new ConfirmationKeyPayload(encryptionGroup, confirmationKey2, requestId1);

		final ConfirmationKeyPayload payload3 = new ConfirmationKeyPayload(encryptionGroup, confirmationKey1, "requestId2");

		final ConfirmationKeyPayload payload4 = new ConfirmationKeyPayload(encryptionGroup, confirmationKey1, requestId1);

		assertEquals(payload1, payload1);
		assertNotEquals(null, payload1);
		assertNotEquals(payload1, payload2);
		assertNotEquals(payload1, payload3);
		assertEquals(payload1, payload4);
	}

	@Test
	@DisplayName("hashCode of equal ConfirmationKeyPayloads is equal")
	void testHashCode() {
		final ConfirmationKeyPayload payload = new ConfirmationKeyPayload(confirmationKeyPayload.getEncryptionGroup(), confirmationKey, requestId,
				confirmationKeyPayload.getSignature());

		assertEquals(confirmationKeyPayload.hashCode(), payload.hashCode());
	}
}