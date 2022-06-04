/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.voting.sendvote;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.security.cert.X509Certificate;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesPayloadSignature;
import ch.post.it.evoting.cryptoprimitives.hashing.HashService;
import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.GroupVector;
import ch.post.it.evoting.cryptoprimitives.math.ZqGroup;
import ch.post.it.evoting.cryptoprimitives.test.tools.data.GroupTestData;
import ch.post.it.evoting.cryptoprimitives.test.tools.generator.GqGroupGenerator;
import ch.post.it.evoting.cryptoprimitives.test.tools.generator.ZqGroupGenerator;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ExponentiationProof;
import ch.post.it.evoting.domain.ContextIds;
import ch.post.it.evoting.domain.MapperSetUp;
import ch.post.it.evoting.domain.SerializationUtils;

@DisplayName("A PartiallyDecryptedEncryptedPCCPayload")
class PartiallyDecryptedEncryptedPCCPayloadTest extends MapperSetUp {

	private static final String electionEventId = "1";
	private static final String verificationCardSetId = "2";
	private static final String verificationCardId = "3";
	private static final String requestId = "4";
	private static final int nodeId = 1;
	private static final HashService hashService = HashService.getInstance();

	private static ObjectNode rootNode;
	private static PartiallyDecryptedEncryptedPCCPayload partiallyDecryptedEncryptedPCCPayload;

	@BeforeAll
	static void setUpAll() {
		final GqGroup gqGroup = GroupTestData.getGqGroup();
		final GqGroupGenerator gqGroupGenerator = new GqGroupGenerator(gqGroup);
		final ZqGroupGenerator zqGroupGenerator = new ZqGroupGenerator(ZqGroup.sameOrderAs(gqGroup));

		// Create payload.
		final ContextIds contextIds = new ContextIds(electionEventId, verificationCardSetId, verificationCardId);
		final GroupVector<GqElement, GqGroup> exponentiatedGammas = gqGroupGenerator.genRandomGqElementVector(2);
		final GroupVector<ExponentiationProof, ZqGroup> exponentiationProofs = Stream.generate(
						() -> new ExponentiationProof(zqGroupGenerator.genRandomZqElementMember(), zqGroupGenerator.genRandomZqElementMember()))
				.limit(2)
				.collect(GroupVector.toGroupVector());

		final PartiallyDecryptedEncryptedPCC partiallyDecryptedEncryptedPCC = new PartiallyDecryptedEncryptedPCC(contextIds, nodeId,
				exponentiatedGammas, exponentiationProofs);

		final PartiallyDecryptedEncryptedPCCPayload payload = new PartiallyDecryptedEncryptedPCCPayload(gqGroup, partiallyDecryptedEncryptedPCC,
				requestId);
		final byte[] payloadBytes = hashService.recursiveHash(payload);
		final X509Certificate certificate = SerializationUtils.generateTestCertificate();
		final CryptoPrimitivesPayloadSignature signature = new CryptoPrimitivesPayloadSignature(payloadBytes, new X509Certificate[] { certificate });
		payload.setSignature(signature);

		partiallyDecryptedEncryptedPCCPayload = payload;

		// Create expected Json.
		rootNode = mapper.createObjectNode();

		final JsonNode encryptionGroupNode = SerializationUtils.createEncryptionGroupNode(gqGroup);
		rootNode.set("encryptionGroup", encryptionGroupNode);

		final ObjectNode contextIdsNode = mapper.createObjectNode();
		contextIdsNode.put("electionEventId", electionEventId);
		contextIdsNode.put("verificationCardSetId", verificationCardSetId);
		contextIdsNode.put("verificationCardId", verificationCardId);

		final ObjectNode partiallyDecryptedEncryptedPCCNode = mapper.createObjectNode();
		final ArrayNode exponentiationProofsNode = SerializationUtils.createExponentiationProofsNode(exponentiationProofs);
		final ArrayNode exponentiatedGammasNode = SerializationUtils.createGqGroupVectorNode(exponentiatedGammas);
		partiallyDecryptedEncryptedPCCNode.set("contextIds", contextIdsNode);
		partiallyDecryptedEncryptedPCCNode.put("nodeId", nodeId);
		partiallyDecryptedEncryptedPCCNode.set("exponentiatedGammas", exponentiatedGammasNode);
		partiallyDecryptedEncryptedPCCNode.set("exponentiationProofs", exponentiationProofsNode);

		rootNode.set("partiallyDecryptedEncryptedPCC", partiallyDecryptedEncryptedPCCNode);

		rootNode.put("requestId", requestId);

		final JsonNode signatureNode = SerializationUtils.createSignatureNode(signature);
		rootNode.set("signature", signatureNode);
	}

	@Test
	@DisplayName("serialized gives expected json")
	void serializePayload() throws JsonProcessingException {
		final String serializedPayload = mapper.writeValueAsString(partiallyDecryptedEncryptedPCCPayload);

		assertEquals(rootNode.toString(), serializedPayload);
	}

	@Test
	@DisplayName("deserialized gives expected payload")
	void deserializePayload() throws JsonProcessingException {
		final PartiallyDecryptedEncryptedPCCPayload deserializedPayload = mapper.readValue(rootNode.toString(),
				PartiallyDecryptedEncryptedPCCPayload.class);

		assertEquals(partiallyDecryptedEncryptedPCCPayload, deserializedPayload);
	}

	@Test
	@DisplayName("serialized then deserialized gives original payload")
	void cycle() throws JsonProcessingException {
		final PartiallyDecryptedEncryptedPCCPayload deserializedPayload = mapper.readValue(
				mapper.writeValueAsString(partiallyDecryptedEncryptedPCCPayload), PartiallyDecryptedEncryptedPCCPayload.class);

		assertEquals(partiallyDecryptedEncryptedPCCPayload, deserializedPayload);
	}

}
