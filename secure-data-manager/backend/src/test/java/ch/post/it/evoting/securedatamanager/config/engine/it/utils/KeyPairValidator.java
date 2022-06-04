/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.it.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricService;

public class KeyPairValidator {

	public void validateKeyPair(final PublicKey publicKey, final PrivateKey privateKey) throws GeneralCryptoLibException {

		final AsymmetricServiceAPI asymmetricService = new AsymmetricService();

		final String testString = "Lorem ipsum dolor sit amet, consectetur adipiscing elit";
		final byte[] encryptedTestString = asymmetricService.encrypt(publicKey, testString.getBytes(StandardCharsets.UTF_8));
		final byte[] decryptedTestString = asymmetricService.decrypt(privateKey, encryptedTestString);
		final String decrypted = new String(decryptedTestString, StandardCharsets.UTF_8);

		assertEquals(testString, decrypted);
	}
}
