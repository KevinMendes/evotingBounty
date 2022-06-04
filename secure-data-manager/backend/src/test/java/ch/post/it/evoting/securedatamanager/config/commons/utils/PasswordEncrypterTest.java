/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.commons.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricService;

class PasswordEncrypterTest {

	private static final char[] PLAINTEXT_PASSWORD = "Q5H4B5WUIQWCR6J3UQS2KEVVZU".toCharArray();
	private static AsymmetricService asymmetricService;
	private static final PasswordEncrypter TARGET = new PasswordEncrypter(asymmetricService);

	@BeforeAll
	static void init() {
		asymmetricService = new AsymmetricService();
	}

	@Test
	void givenEmptyPrivateKeyWhenEncryptThenReturnOriginalPassword() throws GeneralCryptoLibException {

		final String NO_KEY = "";

		final String encryptedPassword = TARGET.encryptPasswordIfEncryptionKeyAvailable(PLAINTEXT_PASSWORD, NO_KEY);

		final String errorMsg = "Encrypted password did not match original password";
		assertEquals(new String(PLAINTEXT_PASSWORD), encryptedPassword, errorMsg);
	}

}
