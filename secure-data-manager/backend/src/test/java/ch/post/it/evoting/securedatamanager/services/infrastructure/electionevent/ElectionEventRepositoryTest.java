/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.infrastructure.electionevent;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.orientechnologies.common.exception.OException;

import ch.post.it.evoting.cryptoprimitives.domain.mixnet.exceptions.FailedValidationException;
import ch.post.it.evoting.securedatamanager.services.infrastructure.DatabaseFixture;

/**
 * Tests of {@link ElectionEventRepository}.
 */
class ElectionEventRepositoryTest {

	private static final String ELECTION_EVENT_ID = "101549c5a4a04c7b88a0cb9be8ab3df6";
	private DatabaseFixture fixture;
	private ElectionEventRepository repository;

	@BeforeEach
	void setUp() throws OException, IOException {
		fixture = new DatabaseFixture(getClass());
		fixture.setUp();
		repository = new ElectionEventRepository(fixture.databaseManager());
		repository.initialize();
		final URL resource = getClass().getResource(getClass().getSimpleName() + ".json");
		fixture.createDocuments(repository.entityName(), resource);
	}

	@AfterEach
	void tearDown() {
		fixture.tearDown();
	}

	@Test
	void testGetElectionEventAlias() {
		assertEquals("legislative2017T2", repository.getElectionEventAlias(ELECTION_EVENT_ID));
	}

	@Test
	void testGetElectionEventAliasNotFound() {
		final String electionEventId = "301549c5a4a04c7b88a0cb9be8ab3df6";
		assertTrue(repository.getElectionEventAlias(electionEventId).isEmpty());
	}

	@Test
	void testListIds() {
		final List<String> ids = repository.listIds();
		assertEquals(2, ids.size());
		assertTrue(ids.contains("101549c5a4a04c7b88a0cb9be8ab3df6"));
		assertTrue(ids.contains("101549c5a4a04c7b88a0cb9be8ab3df7"));
	}

	@Test
	void getDateFromTest() {
		assertEquals("2016-10-13T10:00:00Z", repository.getDateFrom(ELECTION_EVENT_ID));
	}

	@Test
	void getDateFromFailedValidation() {
		assertThrows(FailedValidationException.class, () -> repository.getDateFrom("ELECTION_EVENT_ID"));
	}

	@Test
	void getDateFromNoElectionEvent() {
		assertThrows(IllegalStateException.class, () -> repository.getDateFrom("201549c5a4a04c7b88a0cb9be8ab3df7"));
	}

	@Test
	void getDateToTest() {
		assertEquals("2026-11-05T09:00:00Z", repository.getDateTo(ELECTION_EVENT_ID));
	}

	@Test
	void getDateToFailedValidation() {
		assertThrows(FailedValidationException.class, () -> repository.getDateTo("ELECTION_EVENT_ID"));
	}

	@Test
	void getDateToNoElectionEvent() {
		assertThrows(IllegalStateException.class, () -> repository.getDateTo("201549c5a4a04c7b88a0cb9be8ab3df7"));
	}
}
