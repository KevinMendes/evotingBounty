/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.shares.multishare;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.security.KeyPair;
import java.security.PublicKey;
import java.security.SecureRandom;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricService;
import ch.post.it.evoting.securedatamanager.config.shares.shares.exception.SharesException;
import ch.post.it.evoting.securedatamanager.config.shares.shares.keys.rsa.RSAKeyPairGenerator;

class EncryptedMultipleSharesContainerTest {

	private static final AsymmetricService asymmetricService = new AsymmetricService();

	private byte[] encryptedShares;
	private byte[] encryptedSharesSignature;
	private PublicKey publicKey;

	@BeforeEach
	void setup() throws GeneralCryptoLibException {
		encryptedShares = new byte[10];
		new SecureRandom().nextBytes(encryptedShares);
		final RSAKeyPairGenerator rsaKeyPairGenerator = new RSAKeyPairGenerator(asymmetricService);
		final KeyPair keyPair = rsaKeyPairGenerator.generate();
		encryptedSharesSignature = asymmetricService.sign(keyPair.getPrivate(), encryptedShares);
		publicKey = keyPair.getPublic();
	}

	@Test
	void constructWithValidValuesDoesNotThrow() {
		assertDoesNotThrow(() -> new EncryptedMultipleSharesContainer(encryptedShares, encryptedSharesSignature, publicKey));
	}

	@Test
	void constructWithInvalidValuesThrows() {
		assertThrows(SharesException.class,
				() -> new EncryptedMultipleSharesContainer(new byte[] { 0b0000001 }, encryptedSharesSignature, publicKey));
	}

	@Test
	void constructWithNullValuesThrows() {
		assertThrows(SharesException.class, () -> new EncryptedMultipleSharesContainer(null, encryptedSharesSignature, publicKey));
		assertThrows(SharesException.class, () -> new EncryptedMultipleSharesContainer(encryptedShares, null, publicKey));
	}

	@Test
	void whenDestroyThenEncryptedSharesIsZero() throws SharesException {
		final EncryptedMultipleSharesContainer encryptedMultipleSharesContainer = new EncryptedMultipleSharesContainer(encryptedShares,
				encryptedSharesSignature, publicKey);
		encryptedMultipleSharesContainer.destroy();
		final byte[] expectedEncryptedShares = new byte[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		assertArrayEquals(expectedEncryptedShares, encryptedMultipleSharesContainer.getEncryptedShare());
	}
}