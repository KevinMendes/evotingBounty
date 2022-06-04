/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.symmetric.mac.factory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.symmetric.key.configuration.SymmetricKeyPolicyFromProperties;
import ch.post.it.evoting.cryptolib.symmetric.mac.configuration.MacPolicyFromProperties;
import ch.post.it.evoting.cryptolib.symmetric.service.SymmetricService;

class MacVerificationTest {

	private static final String TEST_DATA_1 = "test data 1";
	private static final String TEST_DATA_2 = "test data 2";
	private static final String DATA_JS = "Ox2fUJq1gAbX";
	private static final String SECRET_KEY_BASE64_JS = "lFoxPuafE3Kk4jcqqHAatdwfsRS8ZcaF9N4gg+85KoA=";
	private static final String MAC_BASE64_JS = "6qiFgZH6VWXti+ltAqmBO9CryybDJC6oHDJsD4bxVlI=";

	private static SymmetricKeyPolicyFromProperties symmetricKeyPolicyFromProperties;
	private static CryptoMac cryptoMac;
	private static SecretKey key;
	private static byte[] mac1;
	private static byte[] mac2;

	@BeforeAll
	static void setUp() {
		symmetricKeyPolicyFromProperties = new SymmetricKeyPolicyFromProperties();

		final SymmetricService symmetricServiceFromDefaultConstructor = new SymmetricService();

		cryptoMac = new MacFactory(new MacPolicyFromProperties()).create();

		key = symmetricServiceFromDefaultConstructor.getSecretKeyForHmac();

		mac1 = cryptoMac.generate(key, TEST_DATA_1.getBytes(StandardCharsets.UTF_8));
		mac2 = cryptoMac.generate(key, TEST_DATA_2.getBytes(StandardCharsets.UTF_8));
	}

	@Test
	void testMacsForSameDataAreSame() {
		final boolean mac1Verified = cryptoMac.verify(key, mac1, TEST_DATA_1.getBytes(StandardCharsets.UTF_8));
		final boolean mac2Verified = cryptoMac.verify(key, mac2, TEST_DATA_2.getBytes(StandardCharsets.UTF_8));

		assertTrue(mac1Verified && mac2Verified);
	}

	@Test
	void testMacsForDifferentDataAreDifferent() {
		final boolean mac1Verified = cryptoMac.verify(key, mac1, TEST_DATA_2.getBytes(StandardCharsets.UTF_8));
		final boolean mac2Verified = cryptoMac.verify(key, mac2, TEST_DATA_1.getBytes(StandardCharsets.UTF_8));

		assertTrue(!mac1Verified && !mac2Verified);
	}

	@Test
	void testMacsAreSameForJavaAndJavaScript() {
		final byte[] keyBytes = Base64.getDecoder().decode(SECRET_KEY_BASE64_JS);
		final SecretKeySpec key = new SecretKeySpec(keyBytes, symmetricKeyPolicyFromProperties.getHmacSecretKeyAlgorithmAndSpec().getAlgorithm());
		final int keyBitLength = key.getEncoded().length * Byte.SIZE;

		final byte[] mac = cryptoMac.generate(key, DATA_JS.getBytes(StandardCharsets.UTF_8));

		final boolean macVerified = cryptoMac.verify(key, mac, DATA_JS.getBytes(StandardCharsets.UTF_8));

		final String macBase64 = Base64.getEncoder().encodeToString(mac);

		assertEquals(keyBitLength, symmetricKeyPolicyFromProperties.getHmacSecretKeyAlgorithmAndSpec().getKeyLengthInBits());
		assertTrue(macVerified);
		assertEquals(MAC_BASE64_JS, macBase64);
	}
}
