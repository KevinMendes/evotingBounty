/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.voting.sendvote;

import static ch.post.it.evoting.cryptoprimitives.domain.mixnet.ConversionUtils.bigIntegerToHex;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesPayloadSignature;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ExponentiationProof;
import ch.post.it.evoting.domain.MapperSetUp;
import ch.post.it.evoting.domain.SerializationUtils;

@DisplayName("A LongReturnCodesSharePayload")
class LongReturnCodesSharePayloadTest extends MapperSetUp {

	private static final String electionEventId = "1";
	private static final String verificationCardSetId = "2";
	private static final String verificationCardId = "3";
	private static final int NODE_ID = 1;
	private static final SecureRandom secureRandom = new SecureRandom();
	private static final byte[] randomBytes = new byte[10];

	@Nested
	@DisplayName("with a long choice return codes share")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class LCCShare {

		private ObjectNode rootNode;
		private LongReturnCodesSharePayload longReturnCodesSharePayload;

		@BeforeAll
		void setUpAll() {
			final UUID correlationId = UUID.randomUUID();
			final String requestId = "4";
			final GqGroup gqGroup = SerializationUtils.getGqGroup();
			final GroupVector<GqElement, GqGroup> longChoiceCodes = SerializationUtils.getLongChoiceCodes(2);
			final GqElement publicKey = SerializationUtils.getPublicKey().get(0);
			final ExponentiationProof exponentiationProof = SerializationUtils.createExponentiationProof();

			// Generate random bytes for signature content and create payload signature.
			secureRandom.nextBytes(randomBytes);
			final X509Certificate certificate = SerializationUtils.generateTestCertificate();
			final CryptoPrimitivesPayloadSignature signature = new CryptoPrimitivesPayloadSignature(randomBytes,
					new X509Certificate[] { certificate });

			// Create payload.
			final LongChoiceReturnCodesShare payload = new LongChoiceReturnCodesShare(correlationId, electionEventId, verificationCardSetId,
					verificationCardId, requestId, NODE_ID, longChoiceCodes, publicKey, exponentiationProof);

			longReturnCodesSharePayload = new LongReturnCodesSharePayload(gqGroup, payload, signature);

			// Create expected Json.
			rootNode = mapper.createObjectNode();

			final JsonNode encryptionGroupNode = SerializationUtils.createEncryptionGroupNode(gqGroup);
			rootNode.set("encryptionGroup", encryptionGroupNode);

			final ObjectNode payloadNode = mapper.createObjectNode();
			payloadNode.put("correlationId", String.valueOf(correlationId));
			payloadNode.put("electionEventId", electionEventId);
			payloadNode.put("verificationCardSetId", verificationCardSetId);
			payloadNode.put("verificationCardId", verificationCardId);
			payloadNode.put("requestId", requestId);
			payloadNode.put("isCastCode", false);
			payloadNode.put("nodeId", NODE_ID);

			final ArrayNode electionPublicKeyNode = SerializationUtils.createGqGroupVectorNode(longChoiceCodes);
			payloadNode.set("longChoiceReturnCodeShare", electionPublicKeyNode);

			payloadNode.put("voterChoiceReturnCodeGenerationPublicKey", bigIntegerToHex(publicKey.getValue()));

			final JsonNode exponentiationProofNode = SerializationUtils.createExponentiationProofNode(exponentiationProof);
			payloadNode.set("exponentiationProof", exponentiationProofNode);

			rootNode.set("longReturnCodesShare", payloadNode);

			final JsonNode signatureNode = SerializationUtils.createSignatureNode(signature);
			rootNode.set("signature", signatureNode);
		}

		@Test
		@DisplayName("serialized gives expected json")
		void serializePayload() throws JsonProcessingException {
			final String serializedPayload = mapper.writeValueAsString(longReturnCodesSharePayload);

			assertEquals(rootNode.toString(), serializedPayload);
		}

		@Test
		@DisplayName("deserialized gives expected payload")
		void deserializePayload() throws JsonProcessingException {
			final LongReturnCodesSharePayload deserializedPayload = mapper.readValue(rootNode.toString(), LongReturnCodesSharePayload.class);

			assertEquals(longReturnCodesSharePayload, deserializedPayload);
		}

		@Test
		@DisplayName("serialized then deserialized gives original payload")
		void cycle() throws JsonProcessingException {
			final LongReturnCodesSharePayload deserializedPayload = mapper
					.readValue(mapper.writeValueAsString(longReturnCodesSharePayload), LongReturnCodesSharePayload.class);

			assertEquals(longReturnCodesSharePayload, deserializedPayload);
		}
	}

	@Nested
	@DisplayName("with a long vote cast return code share")
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	class LVCCShare {

		private ObjectNode rootNode;
		private LongReturnCodesSharePayload longReturnCodesSharePayload;

		@BeforeAll
		void setUpAll() {
			final UUID correlationId = UUID.randomUUID();
			final String requestId = "5";
			final GqGroup gqGroup = SerializationUtils.getGqGroup();
			final GqElement longVoteCastCode = SerializationUtils.getLongChoiceCodes(1).get(0);
			final GqElement publicKey = SerializationUtils.getPublicKey().get(0);
			final ExponentiationProof exponentiationProof = SerializationUtils.createExponentiationProof();

			// Generate random bytes for signature content and create payload signature.
			secureRandom.nextBytes(randomBytes);
			final X509Certificate certificate = SerializationUtils.generateTestCertificate();
			final CryptoPrimitivesPayloadSignature signature = new CryptoPrimitivesPayloadSignature(randomBytes,
					new X509Certificate[] { certificate });

			// Create payload.
			final LongVoteCastReturnCodesShare payload = new LongVoteCastReturnCodesShare(correlationId, electionEventId, verificationCardSetId,
					verificationCardId, requestId, NODE_ID, longVoteCastCode, publicKey, exponentiationProof);

			longReturnCodesSharePayload = new LongReturnCodesSharePayload(gqGroup, payload, signature);

			// Create expected Json.
			rootNode = mapper.createObjectNode();

			final JsonNode encryptionGroupNode = SerializationUtils.createEncryptionGroupNode(gqGroup);
			rootNode.set("encryptionGroup", encryptionGroupNode);

			final ObjectNode payloadNode = mapper.createObjectNode();
			payloadNode.put("correlationId", String.valueOf(correlationId));
			payloadNode.put("electionEventId", electionEventId);
			payloadNode.put("verificationCardSetId", verificationCardSetId);
			payloadNode.put("verificationCardId", verificationCardId);
			payloadNode.put("requestId", requestId);
			payloadNode.put("isCastCode", true);
			payloadNode.put("nodeId", NODE_ID);

			final JsonNode lvccNode = SerializationUtils.createLVCCNode(longVoteCastCode);
			payloadNode.set("longVoteCastReturnCodeShare", lvccNode);

			payloadNode.put("voterVoteCastReturnCodeGenerationPublicKey", bigIntegerToHex(publicKey.getValue()));

			final JsonNode exponentiationProofNode = SerializationUtils.createExponentiationProofNode(exponentiationProof);
			payloadNode.set("exponentiationProof", exponentiationProofNode);

			rootNode.set("longReturnCodesShare", payloadNode);

			final JsonNode signatureNode = SerializationUtils.createSignatureNode(signature);
			rootNode.set("signature", signatureNode);
		}

		@Test
		@DisplayName("serialized gives expected json")
		void serializePayload() throws JsonProcessingException {
			final String serializedPayload = mapper.writeValueAsString(longReturnCodesSharePayload);

			assertEquals(rootNode.toString(), serializedPayload);
		}

		@Test
		@DisplayName("deserialized gives expected payload")
		void deserializePayload() throws JsonProcessingException {
			final LongReturnCodesSharePayload deserializedPayload = mapper.readValue(rootNode.toString(), LongReturnCodesSharePayload.class);

			assertEquals(longReturnCodesSharePayload, deserializedPayload);
		}

		@Test
		@DisplayName("serialized then deserialized gives original payload")
		void cycle() throws JsonProcessingException {
			final LongReturnCodesSharePayload deserializedPayload = mapper
					.readValue(mapper.writeValueAsString(longReturnCodesSharePayload), LongReturnCodesSharePayload.class);

			assertEquals(longReturnCodesSharePayload, deserializedPayload);
		}
	}

}
