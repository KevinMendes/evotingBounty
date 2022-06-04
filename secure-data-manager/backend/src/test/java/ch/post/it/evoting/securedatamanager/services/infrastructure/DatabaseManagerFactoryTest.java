/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

package ch.post.it.evoting.securedatamanager.services.infrastructure;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DatabaseManagerFactoryTest {

	private static final String noUpperCase = "aaaaaaaaaaa1";
	private static final String noLowerCase = "AAAAAAAAAAA1";
	private static final String noDigit = "aaaaaaAAAAAA";
	private static final String tooShort = "aaaaaaA1";
	private static final String valid = "aaaaaaAAAAA1";

	@Test
	@DisplayName("a password without an upper case letter returns false")
	void noUpperCase() {
		assertFalse(DatabaseManagerFactory.isPasswordValid(noUpperCase));
	}

	@Test
	@DisplayName("a password without a lower case letter returns false")
	void noLowerCase() {
		assertFalse(DatabaseManagerFactory.isPasswordValid(noLowerCase));
	}

	@Test
	@DisplayName("a password without a digit returns false")
	void noDigit() {
		assertFalse(DatabaseManagerFactory.isPasswordValid(noDigit));
	}

	@Test
	@DisplayName("a too short password returns false")
	void tooShort() {
		assertFalse(DatabaseManagerFactory.isPasswordValid(tooShort));
	}

	@Test
	@DisplayName("a valid password returns true")
	void valid() {
		assertTrue(DatabaseManagerFactory.isPasswordValid(valid));
	}

}
