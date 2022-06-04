/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain;

import static ch.post.it.evoting.domain.SharedQueue.CREATE_LCC_SHARE_RESPONSE_PATTERN;
import static ch.post.it.evoting.domain.SharedQueue.CREATE_LVCC_SHARE_RESPONSE_PATTERN;
import static ch.post.it.evoting.domain.SharedQueue.ELECTION_CONTEXT_RESPONSE_PATTERN;
import static ch.post.it.evoting.domain.SharedQueue.PARTIAL_DECRYPT_PCC_RESPONSE_PATTERN;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class SharedQueueTest {

	@Test
	void fromNameExceptionTest() {
		final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> SharedQueue.fromName("unknownQueue"));
		assertEquals("Unknown shared queue name provided. [provided name: unknownQueue]", exception.getMessage());
	}

	@Test
	void fromNameSuccessfulTest() {
		assertAll(
				() -> assertEquals(CREATE_LVCC_SHARE_RESPONSE_PATTERN, SharedQueue.fromName("CREATE_LVCC_SHARE_RESPONSE_PATTERN")),
				() -> assertEquals(CREATE_LCC_SHARE_RESPONSE_PATTERN, SharedQueue.fromName("CREATE_LCC_SHARE_RESPONSE_PATTERN")),
				() -> assertEquals(PARTIAL_DECRYPT_PCC_RESPONSE_PATTERN, SharedQueue.fromName("PARTIAL_DECRYPT_PCC_RESPONSE_PATTERN")),
				() -> assertEquals(ELECTION_CONTEXT_RESPONSE_PATTERN, SharedQueue.fromName("ELECTION_CONTEXT_RESPONSE_PATTERN"))
		);
	}
}
