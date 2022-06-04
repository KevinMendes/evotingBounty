/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.voting.confirmvote;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesPayloadSignature;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;

public class ConfirmationKeyPayloadDeserializer extends JsonDeserializer<ConfirmationKeyPayload> {
	@Override
	public ConfirmationKeyPayload deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
			throws IOException {
		final ObjectMapper mapper = DomainObjectMapper.getNewInstance();
		final JsonNode node = mapper.readTree(jsonParser);
		final JsonNode encryptionGroupNode = node.get("encryptionGroup");
		final GqGroup gqGroup = mapper.readValue(encryptionGroupNode.toString(), GqGroup.class);

		final ConfirmationKey payload = mapper.reader().withAttribute("group", gqGroup)
				.readValue(node.get("confirmationKey"), ConfirmationKey.class);

		final String requestId = mapper.readValue(node.get("requestId").toString(), String.class);

		final CryptoPrimitivesPayloadSignature signature = mapper.readValue(node.get("signature").toString(), CryptoPrimitivesPayloadSignature.class);

		return new ConfirmationKeyPayload(gqGroup, payload, requestId, signature);
	}
}
