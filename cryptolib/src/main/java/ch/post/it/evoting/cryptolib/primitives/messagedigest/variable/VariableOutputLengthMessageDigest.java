/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.primitives.messagedigest.variable;

import java.io.InputStream;
import java.util.BitSet;

/**
 * A generator of message digests whose output's length is adjustable.
 *
 * <p>Instances of this class are immutable.
 */
public interface VariableOutputLengthMessageDigest {

	/**
	 * Generates a message digest for the data readable from the received input stream.
	 *
	 * @param message         the {@link InputStream} from which to read data
	 * @param digestBitLength the desired length of the output in bits
	 * @return the message digest
	 */
	BitSet digest(final InputStream message, final int digestBitLength);

	/**
	 * Generates a message digest for the data in the supplied byte array.
	 *
	 * @param message         the byte array with the data to digest
	 * @param digestBitLength the desired length of the output in bits
	 * @return the byte[] representing the generated message digest
	 */
	BitSet digest(final byte[] message, final int digestBitLength);
}
