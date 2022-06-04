/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.voting.sendvote;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.cryptoprimitives.domain.signature.CryptoPrimitivesPayloadSignature;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;

public class PartiallyDecryptedEncryptedPCCPayloadDeserializer extends JsonDeserializer<PartiallyDecryptedEncryptedPCCPayload> {

	@Override
	public PartiallyDecryptedEncryptedPCCPayload deserialize(final JsonParser parser, final DeserializationContext deserializationContext)
			throws IOException {

		final ObjectMapper mapper = DomainObjectMapper.getNewInstance();

		final JsonNode node = mapper.readTree(parser);
		final JsonNode encryptionGroupNode = node.get("encryptionGroup");
		final GqGroup gqGroup = mapper.readValue(encryptionGroupNode.toString(), GqGroup.class);

		final PartiallyDecryptedEncryptedPCC partiallyDecryptedEncryptedPCC = mapper.reader().withAttribute("group", gqGroup)
				.readValue(node.get("partiallyDecryptedEncryptedPCC"), PartiallyDecryptedEncryptedPCC.class);

		final CryptoPrimitivesPayloadSignature signature = mapper.readValue(node.get("signature").toString(), CryptoPrimitivesPayloadSignature.class);

		return new PartiallyDecryptedEncryptedPCCPayload(gqGroup, partiallyDecryptedEncryptedPCC, node.get("requestId").asText(), signature);
	}

}
