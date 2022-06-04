/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */

package ch.post.it.evoting.securedatamanager.config.shares.shares;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.secretsharing.Share;
import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricService;
import ch.post.it.evoting.cryptolib.secretsharing.service.ThresholdSecretSharingService;
import ch.post.it.evoting.securedatamanager.config.shares.shares.exception.SharesException;

class EncryptedShareTest {

	protected static KeyPair keyPair;
	protected static Set<Share> shares;
	protected static byte[] originalSecretBytes;
	protected static byte[] clonedSecretBytes;
	protected static ThresholdSecretSharingService thresholdSecretSharingService;
	protected static AsymmetricServiceAPI asymmetricService;
	private EncryptedShare encryptedShare;

	@BeforeEach
	void init() {
		thresholdSecretSharingService = new ThresholdSecretSharingService();
		asymmetricService = new AsymmetricService();
		keyPair = asymmetricService.getKeyPairForEncryption();

		final BigInteger privateExponent = ((RSAPrivateKey) keyPair.getPrivate()).getPrivateExponent();
		originalSecretBytes = privateExponent.toByteArray();

		// The split method clears the original secret bytes.
		clonedSecretBytes = originalSecretBytes.clone();

		shares = thresholdSecretSharingService.split(clonedSecretBytes, 5, 3, privateExponent.nextProbablePrime());
		encryptedShare = new EncryptedShare(shares.iterator().next(), keyPair.getPrivate());
	}

	@Test
	void testEncryptDecrypt() throws SharesException {
		final Set<WrittenShare> writtenShares = new HashSet<>();

		for (final Share share : shares) {
			final EncryptedShare encrypting = new EncryptedShare(share, keyPair.getPrivate());
			writtenShares
					.add(new WrittenShare(encrypting.getSecretKeyBytes(), encrypting.getEncryptedShareBytes(), encrypting.getEncryptedShareSignature()));

			encrypting.destroy();
			assertEmpty(encrypting.getSecretKeyBytes());
			assertEmpty(encrypting.getEncryptedShareBytes());
			assertEmpty(encrypting.getEncryptedShareSignature());
		}

		for (final WrittenShare ws : writtenShares) {
			final EncryptedShare decrypting = new EncryptedShare(ws.getEncryptedShare(), ws.getEncryptedShareSignature(), keyPair.getPublic());
			final Share decrypted = decrypting.decrypt(ws.getSecretKeyBytes());

			assertTrue(shares.contains(decrypted));
		}
	}

	@Test
	void testShareOfDifferentBoard() {

		final SharesException sharesException = assertThrows(SharesException.class,
				() -> new EncryptedShare(encryptedShare.getEncryptedShareBytes(), encryptedShare.getEncryptedShareSignature(),
						asymmetricService.getKeyPairForEncryption().getPublic()));

		assertEquals("This share does not belong to this board", sharesException.getMessage());
	}

	@Test
	void testShareWithIllegalKey() throws NoSuchAlgorithmException {
		// Generate another keypair with different key size. 3072 as defined in cryptolibPolicy.properties in this resources.
		final KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
		keyPairGenerator.initialize(3072);

		final SharesException sharesException = assertThrows(SharesException.class,
				() -> new EncryptedShare(encryptedShare.getEncryptedShareBytes(), encryptedShare.getEncryptedShareSignature(),
						keyPairGenerator.generateKeyPair().getPublic()));

		assertAll(() -> assertTrue(sharesException.getMessage().contains(
						"Byte length of signature verification public key must be equal to byte length of corresponding key in cryptographic policy for asymmetric service")),
				() -> assertTrue(sharesException.getCause() instanceof GeneralCryptoLibException));
	}

	@Test
	void testDecryptWithNullKey() throws SharesException {
		assertNull(encryptedShare.decrypt(null));
	}

	private void assertEmpty(final byte[] bytes) {
		for (byte b : bytes) {
			assertEquals(0x00, b);
		}
	}

}
