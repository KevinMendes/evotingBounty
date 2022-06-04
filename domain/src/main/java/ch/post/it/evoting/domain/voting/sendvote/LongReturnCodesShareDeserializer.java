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
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;

public class LongReturnCodesShareDeserializer extends JsonDeserializer<LongReturnCodesShare> {

	private static final String GROUP_KEY = "group";

	@Override
	public LongReturnCodesShare deserialize(final JsonParser parser, final DeserializationContext context)
			throws IOException {
		final ObjectMapper mapper = DomainObjectMapper.getNewInstance();
		final GqGroup gqGroup = (GqGroup) context.getAttribute(GROUP_KEY);

		final JsonNode node = mapper.readTree(parser);

		final boolean isCastCode = node.get("isCastCode").asBoolean();
		if (isCastCode) {
			return mapper.reader().withAttribute(GROUP_KEY, gqGroup).readValue(node.toString(), LongVoteCastReturnCodesShare.class);
		} else {
			return mapper.reader().withAttribute(GROUP_KEY, gqGroup).readValue(node.toString(), LongChoiceReturnCodesShare.class);
		}
	}
}
