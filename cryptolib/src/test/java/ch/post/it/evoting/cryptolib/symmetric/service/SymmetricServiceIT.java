/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.symmetric.service;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.api.derivation.CryptoAPIDerivedKey;
import ch.post.it.evoting.cryptolib.api.derivation.CryptoAPIKDFDeriver;
import ch.post.it.evoting.cryptolib.api.derivation.CryptoAPIPBKDFDeriver;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.primitives.service.PrimitivesService;

class SymmetricServiceIT {

	private static SymmetricService target;
	private static PrimitivesService primitivesService;

	@BeforeAll
	static void setup() {
		primitivesService = new PrimitivesService();
		target = new SymmetricService();
	}

	@Test
	void generateKey() {
		final SecretKey secretKeyForEncryption = target.getSecretKeyForEncryption();

		assertDoesNotThrow(() -> saveSecretKey(secretKeyForEncryption));
	}

	@Test
	void getSecretKeyForEncryptionFromKDFKeyTest() throws GeneralCryptoLibException {
		final CryptoAPIKDFDeriver kdfDeriver = primitivesService.getKDFDeriver();
		final CryptoAPIDerivedKey key = kdfDeriver.deriveKey("mySeed".getBytes(StandardCharsets.UTF_8), 16);

		final SecretKey secretKey = target.getSecretKeyForEncryptionFromDerivedKey(key);

		assertDoesNotThrow(() -> target.encrypt(secretKey, "myData".getBytes(StandardCharsets.UTF_8)));
	}

	@Test
	void getSecretKeyForMacFromKDFKeyTest() throws GeneralCryptoLibException {
		final CryptoAPIKDFDeriver kdfDeriver = primitivesService.getKDFDeriver();
		final CryptoAPIDerivedKey key = kdfDeriver.deriveKey("mySeed".getBytes(StandardCharsets.UTF_8), 32);

		final SecretKey secretKey = target.getSecretKeyForMacFromDerivedKey(key);

		assertDoesNotThrow(() -> target.getMac(secretKey, "myData".getBytes(StandardCharsets.UTF_8)));
	}

	@Test
	void getSecretKeyForEncriptionFromPBKDFKeyTest() throws GeneralCryptoLibException {
		final CryptoAPIPBKDFDeriver pbkdfDeriver = primitivesService.getPBKDFDeriver();
		final CryptoAPIDerivedKey key = pbkdfDeriver
				.deriveKey("password01234567890".toCharArray(), "saltsaltsaltsaltsaltsaltsaltsalt".getBytes(StandardCharsets.UTF_8));

		final SecretKey secretKey = target.getSecretKeyForEncryptionFromDerivedKey(key);

		assertDoesNotThrow(() -> target.encrypt(secretKey, "myData".getBytes(StandardCharsets.UTF_8)));
	}

	@Test
	void getSecretKeyForMacFromPBKDFKeyTest() throws GeneralCryptoLibException {
		final CryptoAPIPBKDFDeriver pbkdfDeriver = primitivesService.getPBKDFDeriver();
		final CryptoAPIDerivedKey key = pbkdfDeriver
				.deriveKey("password01234567890".toCharArray(), "saltsaltsaltsaltsaltsaltsaltsalt".getBytes(StandardCharsets.UTF_8));

		final SecretKey secretKey = target.getSecretKeyForMacFromDerivedKey(key);

		assertDoesNotThrow(() -> target.getMac(secretKey, "myData".getBytes(StandardCharsets.UTF_8)));
	}

	@Test
	void getSecretKeyForEncryptionFromDerivedPasswordTest() throws GeneralCryptoLibException {
		final CryptoAPIPBKDFDeriver deriver = primitivesService.getPBKDFDeriver();
		final CryptoAPIDerivedKey key = deriver
				.deriveKey("password01234567890".toCharArray(), "saltsaltsaltsaltsaltsaltsaltsalt".getBytes(StandardCharsets.UTF_8));

		final SecretKey secretKey = target.getSecretKeyForEncryptionFromDerivedKey(key);

		assertDoesNotThrow(() -> target.encrypt(secretKey, "myData".getBytes(StandardCharsets.UTF_8)));
	}

	@Test
	void getSecretKeyForEncryptionFromPolicy() throws GeneralCryptoLibException {
		final byte[] key = "saltsaltsaltsalt".getBytes(StandardCharsets.UTF_8);
		final SecretKey secretKeyCreated = new SecretKeySpec(key, "AES");

		final byte[] encrypt = target.encrypt(secretKeyCreated, "myData".getBytes(StandardCharsets.UTF_8));

		assertArrayEquals("myData".getBytes(StandardCharsets.UTF_8), target.decrypt(secretKeyCreated, encrypt));
	}

	private void saveSecretKey(final SecretKey secretKeyForEncryption) throws IOException {
		try (final OutputStream out = new ByteArrayOutputStream()) {
			out.write(secretKeyForEncryption.getEncoded());
		}
	}

}
