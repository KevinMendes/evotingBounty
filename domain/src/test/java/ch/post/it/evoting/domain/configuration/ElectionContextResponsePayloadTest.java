/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.security.cert.X509Certificate;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ch.post.it.evoting.cryptoprimitives.CryptoPrimitives;
import ch.post.it.evoting.cryptoprimitives.CryptoPrimitivesService;
import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesPayloadSignature;
import ch.post.it.evoting.cryptoprimitives.hashing.HashService;
import ch.post.it.evoting.domain.SerializationUtils;

class ElectionContextResponsePayloadTest {

	private static final ObjectMapper mapper = DomainObjectMapper.getNewInstance();
	private static final HashService hashService = HashService.getInstance();
	private static final CryptoPrimitives cryptoPrimitives = CryptoPrimitivesService.get();
	private static ElectionContextResponsePayload electionContextResponsePayload;
	private static ObjectNode rootNode;

	@BeforeAll
	static void setupAll() {

		// Create payload.
		final String electionEventId = cryptoPrimitives.genRandomBase16String(32).toLowerCase();
		final int nodeId = 1;
		electionContextResponsePayload = new ElectionContextResponsePayload(nodeId, electionEventId);

		byte[] payloadHash = hashService.recursiveHash(electionContextResponsePayload);
		final X509Certificate certificate = SerializationUtils.generateTestCertificate();
		final CryptoPrimitivesPayloadSignature signature = new CryptoPrimitivesPayloadSignature(payloadHash, new X509Certificate[] { certificate });
		electionContextResponsePayload.setSignature(signature);

		// Create expected Json.
		rootNode = mapper.createObjectNode();
		rootNode.put("nodeId", nodeId);
		rootNode.put("electionEventId", electionEventId);

		final JsonNode signatureNode = SerializationUtils.createSignatureNode(signature);
		rootNode.set("signature", signatureNode);
	}

	@Test
	@DisplayName("serialized gives expected json")
	void serializePayload() throws JsonProcessingException {
		final String serializedPayload = mapper.writeValueAsString(electionContextResponsePayload);
		assertEquals(rootNode.toString(), serializedPayload);
	}

	@Test
	@DisplayName("deserialized gives expected payload")
	void deserializePayload() throws IOException {
		final ElectionContextResponsePayload deserializedPayload = mapper.readValue(rootNode.toString(), ElectionContextResponsePayload.class);
		assertEquals(electionContextResponsePayload, deserializedPayload);
	}

	@Test
	@DisplayName("serialized then deserialized gives original payload")
	void cycle() throws IOException {
		final ElectionContextResponsePayload deserializedPayload = mapper
				.readValue(mapper.writeValueAsString(electionContextResponsePayload), ElectionContextResponsePayload.class);

		assertEquals(electionContextResponsePayload, deserializedPayload);
	}
}