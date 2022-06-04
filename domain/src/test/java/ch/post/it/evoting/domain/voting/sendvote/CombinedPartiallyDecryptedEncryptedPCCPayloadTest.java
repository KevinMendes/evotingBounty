/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.voting.sendvote;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Throwables;

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
class CombinedPartiallyDecryptedEncryptedPCCPayloadTest extends MapperSetUp {

	private static final String electionEventId = "1";
	private static final String verificationCardSetId = "2";
	private static final String verificationCardId = "3";
	private static final String requestId = "4";
	private static final HashService hashService = HashService.getInstance();

	private static ObjectNode rootNode;
	private static CombinedPartiallyDecryptedEncryptedPCCPayload combinedPartiallyDecryptedEncryptedPCCPayload;
	private static GqGroup gqGroup;
	private static ContextIds contextIds;
	private static GroupVector<GqElement, GqGroup> exponentiatedGammas;
	private static GroupVector<ExponentiationProof, ZqGroup> exponentiationProofs;
	private static X509Certificate certificate;

	@BeforeAll
	static void setUpAll() {
		gqGroup = GroupTestData.getGqGroup();
		final GqGroupGenerator gqGroupGenerator = new GqGroupGenerator(gqGroup);
		final ZqGroupGenerator zqGroupGenerator = new ZqGroupGenerator(ZqGroup.sameOrderAs(gqGroup));

		// Create payload.
		contextIds = new ContextIds(electionEventId, verificationCardSetId, verificationCardId);
		exponentiatedGammas = gqGroupGenerator.genRandomGqElementVector(2);
		exponentiationProofs = Stream.generate(
						() -> new ExponentiationProof(zqGroupGenerator.genRandomZqElementMember(), zqGroupGenerator.genRandomZqElementMember()))
				.limit(2)
				.collect(GroupVector.toGroupVector());

		certificate = SerializationUtils.generateTestCertificate();
		final List<PartiallyDecryptedEncryptedPCCPayload> payloads = IntStream.range(0, 4)
				.mapToObj(i -> buildPayload(contextIds, i + 1, exponentiatedGammas, exponentiationProofs, gqGroup, certificate))
				.collect(Collectors.toList());

		// Create and sign combined payloads.
		final CombinedPartiallyDecryptedEncryptedPCCPayload combined = new CombinedPartiallyDecryptedEncryptedPCCPayload(payloads);
		final byte[] combinedBytes = hashService.recursiveHash(combined);
		final CryptoPrimitivesPayloadSignature combinedSignature = new CryptoPrimitivesPayloadSignature(combinedBytes,
				new X509Certificate[] { certificate });
		combined.setSignature(combinedSignature);

		combinedPartiallyDecryptedEncryptedPCCPayload = combined;

		// Create expected Json.

		// Combined node.
		rootNode = mapper.createObjectNode();

		// Payload node.
		final ArrayNode payloadsNode = mapper.createArrayNode();
		for (int i = 0; i < 4; i++) {
			final ObjectNode payloadNode = mapper.createObjectNode();
			final JsonNode encryptionGroupNode = SerializationUtils.createEncryptionGroupNode(gqGroup);
			payloadNode.set("encryptionGroup", encryptionGroupNode);

			final ObjectNode contextIdsNode = mapper.createObjectNode();
			contextIdsNode.put("electionEventId", electionEventId);
			contextIdsNode.put("verificationCardSetId", verificationCardSetId);
			contextIdsNode.put("verificationCardId", verificationCardId);

			final ObjectNode partiallyDecryptedEncryptedPCCNode = mapper.createObjectNode();
			final ArrayNode exponentiationProofsNode = SerializationUtils.createExponentiationProofsNode(exponentiationProofs);
			final ArrayNode exponentiatedGammasNode = SerializationUtils.createGqGroupVectorNode(exponentiatedGammas);
			partiallyDecryptedEncryptedPCCNode.set("contextIds", contextIdsNode);
			partiallyDecryptedEncryptedPCCNode.put("nodeId", i + 1);
			partiallyDecryptedEncryptedPCCNode.set("exponentiatedGammas", exponentiatedGammasNode);
			partiallyDecryptedEncryptedPCCNode.set("exponentiationProofs", exponentiationProofsNode);

			payloadNode.set("partiallyDecryptedEncryptedPCC", partiallyDecryptedEncryptedPCCNode);

			payloadNode.put("requestId", requestId);

			final JsonNode payloadSignatureNode = SerializationUtils.createSignatureNode(payloads.get(i).getSignature());
			payloadNode.set("signature", payloadSignatureNode);
			payloadsNode.add(payloadNode);
		}
		rootNode.set("partiallyDecryptedEncryptedPCCPayloads", payloadsNode);

		final JsonNode combinedSignatureNode = SerializationUtils.createSignatureNode(combinedSignature);
		rootNode.set("signature", combinedSignatureNode);
	}

	@Test
	@DisplayName("serialized gives expected json")
	void serializePayload() throws JsonProcessingException {
		final String serializedPayload = mapper.writeValueAsString(combinedPartiallyDecryptedEncryptedPCCPayload);

		assertEquals(rootNode.toString(), serializedPayload);
	}

	@Test
	@DisplayName("deserialized gives expected payload")
	void deserializePayload() throws JsonProcessingException {
		final CombinedPartiallyDecryptedEncryptedPCCPayload deserializedPayload = mapper.readValue(rootNode.toString(),
				CombinedPartiallyDecryptedEncryptedPCCPayload.class);

		assertEquals(combinedPartiallyDecryptedEncryptedPCCPayload, deserializedPayload);
	}

	@Test
	@DisplayName("serialized then deserialized gives original payload")
	void cycle() throws JsonProcessingException {
		final CombinedPartiallyDecryptedEncryptedPCCPayload deserializedPayload = mapper.readValue(
				mapper.writeValueAsString(combinedPartiallyDecryptedEncryptedPCCPayload), CombinedPartiallyDecryptedEncryptedPCCPayload.class);

		assertEquals(combinedPartiallyDecryptedEncryptedPCCPayload, deserializedPayload);
	}

	@Test
	@DisplayName("not enough contributions throws IllegalArgumentException")
	void notEnoughContributions() {
		final List<PartiallyDecryptedEncryptedPCCPayload> emptyList = Collections.emptyList();

		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> new CombinedPartiallyDecryptedEncryptedPCCPayload(emptyList));
		assertEquals("There must be contributions from 4 nodes.", Throwables.getRootCause(exception).getMessage());
	}

	@Test
	@DisplayName("inconsistent groups throws IllegalArgumentException")
	void inconsistentGroups() {
		final List<PartiallyDecryptedEncryptedPCCPayload> payloads = IntStream.range(0, 3)
				.mapToObj(i -> buildPayload(contextIds, i + 1, exponentiatedGammas, exponentiationProofs, gqGroup, certificate))
				.collect(Collectors.toList());
		payloads.add(buildPayload(contextIds, 4, exponentiatedGammas, exponentiationProofs, GroupTestData.getDifferentGqGroup(gqGroup), certificate));

		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> new CombinedPartiallyDecryptedEncryptedPCCPayload(payloads));
		assertEquals("The contribution payloads do not have all the same group.", Throwables.getRootCause(exception).getMessage());
	}

	@Test
	@DisplayName("inconsistent contextIds throws IllegalArgumentException")
	void inconsistentContextIds() {
		final List<PartiallyDecryptedEncryptedPCCPayload> payloads = IntStream.range(0, 3)
				.mapToObj(i -> buildPayload(contextIds, i + 1, exponentiatedGammas, exponentiationProofs, gqGroup, certificate))
				.collect(Collectors.toList());
		final ContextIds otherContextIds = new ContextIds(electionEventId, verificationCardSetId, "23423");
		payloads.add(buildPayload(otherContextIds, 4, exponentiatedGammas, exponentiationProofs, gqGroup, certificate));

		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> new CombinedPartiallyDecryptedEncryptedPCCPayload(payloads));
		assertEquals("The contribution payloads do not have all the same contextIds.", Throwables.getRootCause(exception).getMessage());
	}

	@Test
	@DisplayName("inconsistent nodeIds throws IllegalArgumentException")
	void inconsistentNodeIds() {
		final List<PartiallyDecryptedEncryptedPCCPayload> payloads = IntStream.range(0, 3)
				.mapToObj(i -> buildPayload(contextIds, i + 1, exponentiatedGammas, exponentiationProofs, gqGroup, certificate))
				.collect(Collectors.toList());
		payloads.add(buildPayload(contextIds, 1, exponentiatedGammas, exponentiationProofs, gqGroup, certificate));

		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
				() -> new CombinedPartiallyDecryptedEncryptedPCCPayload(payloads));
		assertEquals("There must be 4 different node ids.", Throwables.getRootCause(exception).getMessage());
	}

	private static PartiallyDecryptedEncryptedPCCPayload buildPayload(final ContextIds contextIds, final int nodeId,
			final GroupVector<GqElement, GqGroup> exponentiatedGammas, final GroupVector<ExponentiationProof, ZqGroup> exponentiationProofs,
			final GqGroup gqGroup, final X509Certificate certificate) {

		final PartiallyDecryptedEncryptedPCC partiallyDecryptedEncryptedPCC = new PartiallyDecryptedEncryptedPCC(contextIds, nodeId,
				exponentiatedGammas, exponentiationProofs);

		// Create and sign payload.
		final PartiallyDecryptedEncryptedPCCPayload payload = new PartiallyDecryptedEncryptedPCCPayload(gqGroup, partiallyDecryptedEncryptedPCC,
				requestId);
		final byte[] payloadBytes = hashService.recursiveHash(payload);
		final CryptoPrimitivesPayloadSignature payloadSignature = new CryptoPrimitivesPayloadSignature(payloadBytes,
				new X509Certificate[] { certificate });
		payload.setSignature(payloadSignature);

		return payload;
	}

}
