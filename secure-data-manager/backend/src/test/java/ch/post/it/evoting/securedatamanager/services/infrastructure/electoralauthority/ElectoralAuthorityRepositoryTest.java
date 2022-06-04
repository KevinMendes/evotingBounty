/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.infrastructure.electoralauthority;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.orientechnologies.common.exception.OException;
import com.orientechnologies.orient.core.db.document.ODatabaseDocument;
import com.orientechnologies.orient.core.iterator.ORecordIteratorClass;
import com.orientechnologies.orient.core.record.impl.ODocument;

import ch.post.it.evoting.securedatamanager.config.commons.utils.JsonUtils;
import ch.post.it.evoting.securedatamanager.services.application.exception.DatabaseException;
import ch.post.it.evoting.securedatamanager.services.infrastructure.DatabaseFixture;
import ch.post.it.evoting.securedatamanager.services.infrastructure.JsonConstants;
import ch.post.it.evoting.securedatamanager.services.infrastructure.ballotbox.BallotBoxRepository;

/**
 * Tests of {@link ElectoralAuthorityRepository}.
 */
class ElectoralAuthorityRepositoryTest {

	private DatabaseFixture fixture;
	private BallotBoxRepository ballotBoxRepository;
	private ElectoralAuthorityRepository repository;

	@BeforeEach
	void setUp() throws OException, IOException {
		fixture = new DatabaseFixture(getClass());
		fixture.setUp();
		ballotBoxRepository = mock(BallotBoxRepository.class);
		repository = new ElectoralAuthorityRepository(fixture.databaseManager());
		repository.ballotBoxRepository = ballotBoxRepository;
		repository.initialize();
		final URL resource = getClass().getResource(getClass().getSimpleName() + ".json");
		fixture.createDocuments(repository.entityName(), resource);
	}

	@AfterEach
	void tearDown() {
		fixture.tearDown();
	}

	@Test
	void testUpdateRelatedBallotBox() {
		JsonObject ballotBoxes = Json.createObjectBuilder().add(JsonConstants.RESULT,
				Json.createArrayBuilder().add(Json.createObjectBuilder().add(JsonConstants.ALIAS, "4"))
						.add(Json.createObjectBuilder().add(JsonConstants.ALIAS, "5"))).build();
		when(ballotBoxRepository.findByElectoralAuthority("331279febbb0423298d44ee58702d581")).thenReturn(ballotBoxes.toString());
		ballotBoxes = Json.createObjectBuilder().add(JsonConstants.RESULT,
				Json.createArrayBuilder().add(Json.createObjectBuilder().add(JsonConstants.ALIAS, "6"))
						.add(Json.createObjectBuilder().add(JsonConstants.ALIAS, "7"))).build();
		when(ballotBoxRepository.findByElectoralAuthority("331279febbb0423298d44ee58702d582")).thenReturn(ballotBoxes.toString());
		repository.updateRelatedBallotBox(asList("331279febbb0423298d44ee58702d581", "331279febbb0423298d44ee58702d582"));
		try (final ODatabaseDocument database = fixture.databaseManager().openDatabase()) {
			final ORecordIteratorClass<ODocument> iterator = database.browseClass(repository.entityName());
			while (iterator.hasNext()) {
				final ODocument document = iterator.next();
				final String id = document.field(JsonConstants.ID, String.class);
				final List<String> aliases = document.field(JsonConstants.BALLOT_BOX_ALIAS, List.class);
				switch (id) {
				case "331279febbb0423298d44ee58702d581":
					assertEquals(2, aliases.size());
					assertEquals("4", aliases.get(0));
					assertEquals("5", aliases.get(1));
					break;
				case "331279febbb0423298d44ee58702d582":
					assertEquals(2, aliases.size());
					assertEquals("6", aliases.get(0));
					assertEquals("7", aliases.get(1));
					break;
				default:
					assertEquals(1, aliases.size());
					assertEquals("3", aliases.get(0));
					break;
				}
			}
		}
	}

	@Test
	void testUpdateRelatedBallotBoxNotFound() {
		final List<String> unknownElectoralAuthority = singletonList("unknownElectoralAuthority");
		assertThrows(DatabaseException.class, () -> repository.updateRelatedBallotBox(unknownElectoralAuthority));
	}

	@Test
	void testListByElectionEvent() {
		final String json = repository.listByElectionEvent("101549c5a4a04c7b88a0cb9be8ab3df6");
		final JsonArray array = JsonUtils.getJsonObject(json).getJsonArray(JsonConstants.RESULT);
		assertEquals(2, array.size());
		final Set<String> ids = new HashSet<>();
		for (final JsonValue value : array) {
			ids.add(((JsonObject) value).getString(JsonConstants.ID));
		}
		assertTrue(ids.contains("331279febbb0423298d44ee58702d581"));
		assertTrue(ids.contains("331279febbb0423298d44ee58702d582"));
	}

	@Test
	void testListByElectionEventNotFound() {
		final String json = repository.listByElectionEvent("unknownElectionEvent");
		final JsonArray array = JsonUtils.getJsonObject(json).getJsonArray(JsonConstants.RESULT);
		assertTrue(array.isEmpty());
	}
}
