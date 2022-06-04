/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.voting.confirmvote;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ch.post.it.evoting.cryptoprimitives.math.GqElement;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.cryptoprimitives.math.RandomService;
import ch.post.it.evoting.cryptoprimitives.test.tools.data.GroupTestData;
import ch.post.it.evoting.cryptoprimitives.test.tools.generator.GqGroupGenerator;
import ch.post.it.evoting.domain.ContextIds;
import ch.post.it.evoting.domain.MapperSetUp;

class ConfirmationKeyTest extends MapperSetUp {

	private static final int ID_SIZE = 32;
	private static final RandomService RANDOM_SERVICE = new RandomService();
	private ContextIds contextIds;
	private GqGroup gqGroup;
	private GqElement element;
	private ConfirmationKey confirmationKey;
	private ObjectNode rootNode;

	@BeforeEach
	void setup() throws JsonProcessingException {
		final String electionEventId = RANDOM_SERVICE.genRandomBase16String(ID_SIZE);
		final String verificationCardSetId = RANDOM_SERVICE.genRandomBase16String(ID_SIZE);
		final String verificationCardId = RANDOM_SERVICE.genRandomBase16String(ID_SIZE);
		contextIds = new ContextIds(electionEventId, verificationCardSetId, verificationCardId);

		gqGroup = GroupTestData.getGqGroup();
		element = new GqGroupGenerator(gqGroup).genMember();

		confirmationKey = new ConfirmationKey(contextIds, element);

		rootNode = mapper.createObjectNode();
		final JsonNode contextIdsNode = mapper.readTree(mapper.writeValueAsString(contextIds));
		rootNode.set("contextIds", contextIdsNode);
		final JsonNode elementNode = mapper.readTree(mapper.writeValueAsString(element));
		rootNode.set("element", elementNode);
	}

	@Test
	@DisplayName("constructing a ConfirmationKey with null parameters throws a NullPointerException")
	void constructWithNullParameters() {
		assertThrows(NullPointerException.class, () -> new ConfirmationKey(null, element));
		assertThrows(NullPointerException.class, () -> new ConfirmationKey(contextIds, null));
	}

	@Test
	@DisplayName("serializing a ConfirmationKey gives the expected json")
	void serializeConfirmationKey() throws JsonProcessingException {
		final String serializedConfirmationKey = mapper.writeValueAsString(confirmationKey);
		assertEquals(rootNode.toString(), serializedConfirmationKey);
	}

	@Test
	@DisplayName("deserializing a ConfirmationKey gives the expected ConfirmationKey")
	void deserializeConfirmationKey() throws IOException {
		final ConfirmationKey deserializedConfirmationKey = mapper.reader().withAttribute("group", gqGroup)
				.readValue(rootNode.toString(), ConfirmationKey.class);
		assertEquals(confirmationKey, deserializedConfirmationKey);
	}

	@Test
	@DisplayName("serializing then deserializing a ConfirmationKey gives the original ConfirmationKey")
	void cycle() throws IOException {
		final String serializedConfirmationKey = mapper.writeValueAsString(confirmationKey);
		final ConfirmationKey deserializedConfirmationKey = mapper.reader().withAttribute("group", gqGroup)
				.readValue(serializedConfirmationKey, ConfirmationKey.class);
		assertEquals(confirmationKey, deserializedConfirmationKey);
	}

}