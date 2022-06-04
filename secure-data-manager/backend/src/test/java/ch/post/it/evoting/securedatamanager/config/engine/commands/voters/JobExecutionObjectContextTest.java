/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.commands.voters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class JobExecutionObjectContextTest {

	JobExecutionObjectContext sut = new JobExecutionObjectContext();

	@Test
	void returnSameObjectAsStored() {
		final String id = UUID.randomUUID().toString();
		final String expectedValue = "someValue";

		sut.put(id, expectedValue, String.class);

		final String storedValue = sut.get(id, String.class);

		assertEquals(expectedValue.getClass(), storedValue.getClass());
		assertEquals(expectedValue, storedValue);
	}

	@Test
	void returnNullRemoveAllOfId() {
		final String id = UUID.randomUUID().toString();
		final String expectedValue = "someValue";
		sut.put(id, expectedValue, String.class);

		sut.removeAll(id);

		final String shouldBeNull = sut.get(id, String.class);
		assertNull(shouldBeNull);
	}

	@Test
	void keepValueForIdWhenRemoveAllOfOtherId() {
		final String id = UUID.randomUUID().toString();
		final String expectedValue = "someValue";
		sut.put(id, expectedValue, String.class);

		final String id2 = UUID.randomUUID().toString();
		final String expectedValue2 = "someValue2";
		sut.put(id2, expectedValue2, String.class);

		sut.removeAll(id);

		final String shouldBeNull = sut.get(id, String.class);
		final String shouldNotBeNull = sut.get(id2, String.class);
		assertNull(shouldBeNull);
		assertNotNull(shouldNotBeNull);
	}
}
