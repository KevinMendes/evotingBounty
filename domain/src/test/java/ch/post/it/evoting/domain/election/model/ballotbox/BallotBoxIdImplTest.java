/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.election.model.ballotbox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class BallotBoxIdImplTest {

	@Test
	void considerEqualTwoInstanceWithTheSameValues() {
		BallotBoxId one = new BallotBoxIdImpl("1", "2", "3");
		BallotBoxId another = new BallotBoxIdImpl("1", "2", "3");

		// Ensure the objects are different.
		assertNotSame(one, another);
		// Ensure the objects are considered to hold the same values.
		assertEquals(one.hashCode(), another.hashCode());
		assertEquals(one, another);
	}

	@Test
	void checkForCollisions() {
		BallotBoxId one = new BallotBoxIdImpl("1", "2", "3");
		BallotBoxId another = new BallotBoxIdImpl("2", "1", "3");

		// Ensure the objects are different.
		assertNotSame(one, another);
		// Ensure the objects are considered to hold the same values.
		assertNotEquals(one.hashCode(), another.hashCode());
		assertNotEquals(one, another);
	}

	@Test
	void failOnNullTenantId() {
		assertThrows(NullPointerException.class, () -> new BallotBoxIdImpl(null, "2", "3"));
	}

	@Test
	void failOnNullElectionEventId() {
		assertThrows(NullPointerException.class, () -> new BallotBoxIdImpl("1", null, "3"));
	}

	@Test
	void failOnNullBallotBoxId() {
		assertThrows(NullPointerException.class, () -> new BallotBoxIdImpl("1", "2", null));
	}
}
