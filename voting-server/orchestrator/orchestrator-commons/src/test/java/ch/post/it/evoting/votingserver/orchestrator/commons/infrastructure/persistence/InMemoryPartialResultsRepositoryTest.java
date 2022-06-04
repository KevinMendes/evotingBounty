/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.orchestrator.commons.infrastructure.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;

/**
 * Tests of {@link InMemoryPartialResultsRepository}.
 */
public class InMemoryPartialResultsRepositoryTest {
	private static final Object RESULT1 = new Object();

	private static final Object RESULT2 = new Object();

	private static final Object RESULT3 = new Object();

	private static final UUID CORRELATION_ID1 = new UUID(0, 0);

	private static final UUID CORRELATION_ID2 = new UUID(0, 1);

	private PartialResultsRepository<Object> repository = new InMemoryPartialResultsRepository<>();

	@Before
	public void setUp() throws Exception {
		repository = new InMemoryPartialResultsRepository<>();
	}

	@Test
	public void testDeleteAll() {
		repository.deleteAll(CORRELATION_ID1);

		repository.save(CORRELATION_ID1, RESULT1);
		repository.save(CORRELATION_ID1, RESULT2);
		repository.save(CORRELATION_ID2, RESULT3);

		repository.deleteAll(CORRELATION_ID1);
		assertFalse(repository.listIfHasAll(CORRELATION_ID1, 1).isPresent());
		assertTrue(repository.listIfHasAll(CORRELATION_ID2, 1).isPresent());
	}

	@Test
	public void testListIfHasAll() {
		assertFalse(repository.listIfHasAll(CORRELATION_ID1, 2).isPresent());

		repository.save(CORRELATION_ID1, RESULT1);
		assertFalse(repository.listIfHasAll(CORRELATION_ID1, 2).isPresent());

		repository.save(CORRELATION_ID1, RESULT2);
		Optional<List<Object>> optional = repository.listIfHasAll(CORRELATION_ID1, 2);
		assertTrue(optional.isPresent());
		List<Object> results = optional.get();
		assertEquals(2, results.size());
		assertEquals(results.get(0), RESULT1);
		assertEquals(results.get(1), RESULT2);
	}

	@Test
	public void testSave() {
		repository.save(CORRELATION_ID1, RESULT1);
		repository.save(CORRELATION_ID1, RESULT2);
		Optional<List<Object>> optionalt = repository.listIfHasAll(CORRELATION_ID1, 2);
		assertTrue(optionalt.isPresent());
		List<Object> results = optionalt.get();
		assertEquals(2, results.size());
		assertEquals(results.get(0), RESULT1);
		assertEquals(results.get(1), RESULT2);
	}

	@Test
	public void testListAll() {
		assertTrue(repository.listAll(CORRELATION_ID1).isEmpty());

		repository.save(CORRELATION_ID1, RESULT1);
		List<Object> results = repository.listAll(CORRELATION_ID1);
		assertEquals(1, results.size());
		assertEquals(results.get(0), RESULT1);

		repository.save(CORRELATION_ID1, RESULT2);
		results = repository.listAll(CORRELATION_ID1);
		assertEquals(2, results.size());
		assertEquals(results.get(0), RESULT1);
		assertEquals(results.get(1), RESULT2);
	}
}
