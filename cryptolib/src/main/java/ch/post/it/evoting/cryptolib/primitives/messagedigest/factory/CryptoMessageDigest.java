/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.primitives.messagedigest.factory;

import java.io.InputStream;
import java.security.MessageDigest;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;

/**
 * A generator of message digests.
 *
 * <p>Instances of this class are immutable.
 */
public interface CryptoMessageDigest {

	/**
	 * Generate a message digest for the given data.
	 *
	 * @param data the input data for the message digest.
	 * @return The byte[] representing the generated message digest.
	 */
	byte[] generate(final byte[] data);

	/**
	 * Generate a message digest for the data readable from the received input stream.
	 *
	 * @param in the {@link InputStream} from which to read data.
	 * @return the byte[] representing the generated message digest.
	 * @throws GeneralCryptoLibException if there are any problems reading data from {@code in}.
	 */
	byte[] generate(final InputStream in) throws GeneralCryptoLibException;

	/**
	 * @return the length of the digest in bytes.
	 */
	int getDigestLength();

	/**
	 * Method for returning a raw {@link MessageDigest} with the appropriate policies set for working on more low-level functionalities.
	 *
	 * @return The raw {@link MessageDigest}.
	 */
	MessageDigest getRawMessageDigest();
}
