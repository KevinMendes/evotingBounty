/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.infrastructure.ballotbox;

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
import ch.post.it.evoting.securedatamanager.services.infrastructure.DatabaseManager;
import ch.post.it.evoting.securedatamanager.services.infrastructure.JsonConstants;
import ch.post.it.evoting.securedatamanager.services.infrastructure.ballot.BallotRepository;

/**
 * Tests of {@link BallotBoxRepository}.
 */
class BallotBoxRepositoryTest {

	private DatabaseFixture fixture;
	private DatabaseManager manager;
	private BallotRepository ballotRepository;
	private BallotBoxRepository repository;

	@BeforeEach
	void setUp() throws OException, IOException {
		fixture = new DatabaseFixture(getClass());
		fixture.setUp();
		manager = fixture.databaseManager();
		ballotRepository = mock(BallotRepository.class);
		repository = new BallotBoxRepository(manager);
		repository.ballotRepository = ballotRepository;
		repository.initialize();
		URL resource = getClass().getResource(getClass().getSimpleName() + ".json");
		fixture.createDocuments(repository.entityName(), resource);
	}

	@AfterEach
	void tearDown() {
		fixture.tearDown();
	}

	@Test
	void testFindByElectoralAuthority() {
		String json = repository.findByElectoralAuthority("331279febbb0423298d44ee58702d581");
		JsonArray array = JsonUtils.getJsonObject(json).getJsonArray(JsonConstants.RESULT);
		assertEquals(2, array.size());
		Set<String> ids = new HashSet<>();
		for (JsonValue value : array) {
			ids.add(((JsonObject) value).getString(JsonConstants.ID));
		}
		assertTrue(ids.containsAll(asList("03bbc588d0b04d9bbfe60df81b94943e", "eb6ed778b44c463f8b54c95e846dfe49")));
	}

	@Test
	void testFindByElectoralAuthorityNotFound() {
		String json = repository.findByElectoralAuthority("unknownElectoralAuthority");
		JsonArray array = JsonUtils.getJsonObject(json).getJsonArray(JsonConstants.RESULT);
		assertTrue(array.isEmpty());
	}

	@Test
	void testGetBallotId() {
		String ballotId = repository.getBallotId("03bbc588d0b04d9bbfe60df81b94943e");
		assertEquals("6db73a32b0ec4f72b259ed5b70945267", ballotId);
	}

	@Test
	void testGetBallotIdNotFound() {
		assertTrue(repository.getBallotId("unknownBallotBox").isEmpty());
	}

	@Test
	void testListAliases() {
		List<String> aliases = repository.listAliases("bf411ab6bc76483dbed6dd9684a50592");
		assertEquals(2, aliases.size());
		assertTrue(aliases.containsAll(asList("2", "3")));
	}

	@Test
	void testListAliasesNotFound() {
		assertTrue(repository.listAliases("unknownBallot").isEmpty());
	}

	@Test
	void testListByElectionEvent() {
		String json = repository.listByElectionEvent("101549c5a4a04c7b88a0cb9be8ab3df6");
		JsonArray array = JsonUtils.getJsonObject(json).getJsonArray(JsonConstants.RESULT);
		assertEquals(2, array.size());
		Set<String> ids = new HashSet<>();
		for (JsonValue value : array) {
			ids.add(((JsonObject) value).getString(JsonConstants.ID));
		}
		assertTrue(ids.containsAll(asList("03bbc588d0b04d9bbfe60df81b94943e", "eb6ed778b44c463f8b54c95e846dfe49")));
	}

	@Test
	void testListByElectionEventNotFound() {
		String json = repository.listByElectionEvent("unknownElectionEvent");
		JsonArray array = JsonUtils.getJsonObject(json).getJsonArray(JsonConstants.RESULT);
		assertTrue(array.isEmpty());
	}

	@Test
	void testUpdateRelatedBallotAlias() {
		when(ballotRepository.listAliases("6db73a32b0ec4f72b259ed5b70945267")).thenReturn(asList("alias1", "alias2"));
		when(ballotRepository.listAliases("bf411ab6bc76483dbed6dd9684a50592")).thenReturn(asList("alias3", "alias4"));
		repository.updateRelatedBallotAlias(asList("03bbc588d0b04d9bbfe60df81b94943e", "eb6ed778b44c463f8b54c95e846dfe49"));
		try (ODatabaseDocument database = manager.openDatabase()) {
			ORecordIteratorClass<ODocument> iterator = database.browseClass(repository.entityName());
			while (iterator.hasNext()) {
				ODocument document = iterator.next();
				String id = document.field(JsonConstants.ID, String.class);
				String alias = document.field("ballotAlias", String.class);
				switch (id) {
				case "03bbc588d0b04d9bbfe60df81b94943e":
					assertEquals("alias1,alias2", alias);
					break;
				case "eb6ed778b44c463f8b54c95e846dfe49":
					assertEquals("alias3,alias4", alias);
					break;
				default:
					assertEquals("352", alias);
				}
			}
		}
	}

	@Test
	void testUpdateRelatedBallotAliasNotFound() {
		final List<String> unknownBallotBox = singletonList("unknownBallotBox");
		assertThrows(DatabaseException.class, () -> repository.updateRelatedBallotAlias(unknownBallotBox));
	}

	@Test
	void testIsTestBallotBox() {
		assertTrue(repository.isTestBallotBox("03bbc588d0b04d9bbfe60df81b94943e"));
	}
}
