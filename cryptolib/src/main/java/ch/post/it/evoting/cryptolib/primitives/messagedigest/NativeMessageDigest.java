/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.primitives.messagedigest;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;

import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.commons.configuration.Provider;
import ch.post.it.evoting.cryptolib.primitives.messagedigest.configuration.MessageDigestPolicy;
import ch.post.it.evoting.cryptolib.primitives.messagedigest.factory.CryptoMessageDigest;

/**
 * A generator of message digests based on Java's built-in one.
 *
 * <p>Instances of this class are immutable.
 */
public class NativeMessageDigest implements CryptoMessageDigest {

	private final MessageDigest messageDigestGenerator;

	/**
	 * Creates an instance of message digest generator with the provided policy.
	 *
	 * @param messageDigestPolicy Policy for generating message digests.
	 */
	public NativeMessageDigest(final MessageDigestPolicy messageDigestPolicy) {

		try {
			if (Provider.DEFAULT == messageDigestPolicy.getMessageDigestAlgorithmAndProvider().getProvider()) {
				messageDigestGenerator = MessageDigest.getInstance(messageDigestPolicy.getMessageDigestAlgorithmAndProvider().getAlgorithm());
			} else {
				messageDigestGenerator = MessageDigest.getInstance(messageDigestPolicy.getMessageDigestAlgorithmAndProvider().getAlgorithm(),
						messageDigestPolicy.getMessageDigestAlgorithmAndProvider().getProvider().getProviderName());
			}
		} catch (final GeneralSecurityException e) {
			throw new CryptoLibException(
					"Failed to create message digest generator in this environment. Attempted to use the provider: " + messageDigestPolicy
							.getMessageDigestAlgorithmAndProvider().getProvider().getProviderName() + ", and the algorithm: " + messageDigestPolicy
							.getMessageDigestAlgorithmAndProvider().getAlgorithm() + ". Error message was " + e.getMessage(), e);
		}
	}

	/**
	 * Generate a message digest for the given data.
	 *
	 * @param data the input data for the message digest.
	 * @return The byte[] representing the generated message digest.
	 */
	@Override
	public byte[] generate(final byte[] data) {
		return messageDigestGenerator.digest(data);
	}

	/**
	 * Generate a message digest for the data readable from the received input stream.
	 *
	 * @param in the {@link InputStream} from which to read data.
	 * @return the byte[] representing the generated message digest.
	 * @throws GeneralCryptoLibException if there are any problems reading data from {@code in}.
	 */
	@Override
	public byte[] generate(final InputStream in) throws GeneralCryptoLibException {

		final byte[] buf = new byte[4096];
		int len;
		try {
			while ((len = in.read(buf)) >= 0) {
				messageDigestGenerator.update(buf, 0, len);
			}
		} catch (final IOException e) {
			throw new CryptoLibException("Exception while generating message digest", e);
		}

		return messageDigestGenerator.digest();
	}

	/**
	 * @return the length of the digest in bytes.
	 */
	@Override
	public int getDigestLength() {
		return messageDigestGenerator.getDigestLength();
	}

	/**
	 * Method for returning a raw {@link MessageDigest} with the appropriate policies set for working on more low-level functionalities.
	 *
	 * @return The raw {@link MessageDigest}.
	 */
	@Override
	public MessageDigest getRawMessageDigest() {
		return messageDigestGenerator;
	}
}
