/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

package ch.post.it.evoting.securedatamanager.config.shares.shares.it;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Set;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.api.secretsharing.Share;
import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricService;
import ch.post.it.evoting.cryptolib.secretsharing.service.ThresholdSecretSharingService;
import ch.post.it.evoting.securedatamanager.config.shares.shares.keys.rsa.RSAKeyPairGenerator;
import ch.post.it.evoting.securedatamanager.config.shares.shares.keys.rsa.RSAPrivateKeySerializer;

class SharesSplitRecoverITest {

	private static RSAKeyPairGenerator rsaKeyPairGenerator;
	private static AsymmetricServiceAPI asymmetricService;
	private static ThresholdSecretSharingService secretSharingService;

	@BeforeAll
	public static void beforeClass() {
		secretSharingService = new ThresholdSecretSharingService();
		asymmetricService = new AsymmetricService();
		rsaKeyPairGenerator = new RSAKeyPairGenerator(asymmetricService);
	}

	@RepeatedTest(10)
	void testWrapAndRecoverRSAKey() throws Exception {
		final KeyPair keyPair = rsaKeyPairGenerator.generate();
		final RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
		final RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

		// Serialize key that is then split.
		final RSAPrivateKeySerializer serializer = new RSAPrivateKeySerializer();
		final byte[] serializedKey = serializer.serialize(privateKey);
		final byte[] clonedSerializedKey = serializedKey.clone(); //Secrets get destroyed by the split method

		// Split and recoveredKey (not interested in writing to cards here).
		final Set<Share> shares = secretSharingService.split(clonedSerializedKey, 2, 1, privateKey.getModulus().nextProbablePrime());
		final byte[] recoveredKey = secretSharingService.recover(shares);

		// Ensure that the key before and after split/recover are equal.
		assertArrayEquals(serializedKey, recoveredKey);

		// Reconstruct from recovered key.
		final PrivateKey reconstructedKey = serializer.reconstruct(recoveredKey, publicKey);

		// The final reconstructed key (checking private exponent only here) has to be equal to the initially generated one.
		assertEquals(privateKey.getPrivateExponent(), ((RSAPrivateKey) reconstructedKey).getPrivateExponent());

		// Quick check that the key is still working.
		final String testString = "a random-looking test string...";
		final byte[] encrypted = asymmetricService.encrypt(publicKey, testString.getBytes(StandardCharsets.UTF_8));
		final byte[] decrypted = asymmetricService.decrypt(reconstructedKey, encrypted);
		final String decryptedString = new String(decrypted, StandardCharsets.UTF_8);

		assertEquals(testString, decryptedString);
	}
}
