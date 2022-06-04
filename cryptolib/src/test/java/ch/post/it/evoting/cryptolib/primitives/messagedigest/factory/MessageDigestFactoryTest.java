/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.primitives.messagedigest.factory;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.primitives.messagedigest.configuration.ConfigMessageDigestAlgorithmAndProvider;
import ch.post.it.evoting.cryptolib.primitives.messagedigest.configuration.MessageDigestPolicy;

class MessageDigestFactoryTest {

	@Test
	void whenCreateMessageDigestGeneratorUsingPolicySha256AndSun() {
		final MessageDigestFactory messageDigestFactory = new MessageDigestFactory(getMessageDigestPolicySha256AndSun());
		final CryptoMessageDigest cryptoMessageDigest = messageDigestFactory.create();

		assertNotNull(cryptoMessageDigest);
	}

	@Test
	void whenCreateMessageDigestGeneratorUsingPolicySha256AndBc() {
		final MessageDigestFactory messageDigestFactory = new MessageDigestFactory(getMessageDigestPolicySha256AndBc());
		final CryptoMessageDigest cryptoMessageDigest = messageDigestFactory.create();

		assertNotNull(cryptoMessageDigest);
	}

	private MessageDigestPolicy getMessageDigestPolicySha256AndSun() {
		return () -> ConfigMessageDigestAlgorithmAndProvider.SHA256_SUN;
	}

	private MessageDigestPolicy getMessageDigestPolicySha256AndBc() {
		return () -> ConfigMessageDigestAlgorithmAndProvider.SHA256_BC;
	}
}
