/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.primitives.messagedigest.factory;

import ch.post.it.evoting.cryptolib.CryptolibFactory;
import ch.post.it.evoting.cryptolib.primitives.messagedigest.NativeMessageDigest;
import ch.post.it.evoting.cryptolib.primitives.messagedigest.configuration.MessageDigestPolicy;

/**
 * A factory class for creating a message digest generator and verifier.
 */
public class MessageDigestFactory extends CryptolibFactory {

	private final MessageDigestPolicy messageDigestPolicy;

	/**
	 * Constructs a MessageDigestFactory using the provided {@link MessageDigestPolicy}.
	 *
	 * @param messageDigestPolicy The MessageDigestPolicy to be used to configure this MessageDigestFactory.
	 *                            <p>NOTE: The received {@link MessageDigestPolicy} should be an immutable object. If this is
	 *                            the case, then the entire class is thread safe.
	 */
	public MessageDigestFactory(final MessageDigestPolicy messageDigestPolicy) {

		this.messageDigestPolicy = messageDigestPolicy;
	}

	/**
	 * Create a {@link CryptoMessageDigest} according to the given policy.
	 *
	 * @return A {@link CryptoMessageDigest} object.
	 */
	public CryptoMessageDigest create() {

		return new NativeMessageDigest(messageDigestPolicy);
	}
}
