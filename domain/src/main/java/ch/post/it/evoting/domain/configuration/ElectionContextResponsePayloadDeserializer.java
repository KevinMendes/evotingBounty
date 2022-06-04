/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.configuration;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesPayloadSignature;

public class ElectionContextResponsePayloadDeserializer extends JsonDeserializer<ElectionContextResponsePayload> {
	@Override
	public ElectionContextResponsePayload deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
		final ObjectMapper mapper = DomainObjectMapper.getNewInstance();

		final JsonNode node = mapper.readTree(jsonParser);
		final int nodeId = node.get("nodeId").asInt();
		final String electionEventId = mapper.readValue(node.get("electionEventId").toString(), String.class);
		final CryptoPrimitivesPayloadSignature signature = mapper.readValue(node.get("signature").toString(), CryptoPrimitivesPayloadSignature.class);

		return new ElectionContextResponsePayload(nodeId, electionEventId, signature);
	}
}