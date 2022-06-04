/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.cache;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.jcs.access.exception.CacheException;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests of {@link CacheSupport}.
 */
public class CacheSupportTest {
	private CacheSupport<String, Boolean> cache;

	@Before
	public void setUp() throws CacheException {
		cache = new CacheSupport<>("test");
	}

	@Test
	public void testContainsKey() {
		cache.put("true", Boolean.TRUE);
		assertTrue(cache.containsKey("true"));
		assertFalse(cache.containsKey("false"));
	}

	@Test
	public void testGet() {
		cache.put("true", Boolean.TRUE);
		assertEquals(Boolean.TRUE, cache.get("true"));
		assertNull(cache.get("false"));
	}

	@Test
	public void testPut() {
		cache.put("true", Boolean.TRUE);
		assertEquals(Boolean.TRUE, cache.get("true"));
	}

	@Test
	public void testRemove() {
		cache.put("true", Boolean.TRUE);
		cache.remove("true");
		assertFalse(cache.containsKey("true"));
	}
}
