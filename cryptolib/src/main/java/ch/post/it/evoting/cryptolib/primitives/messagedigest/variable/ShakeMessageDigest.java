/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.primitives.messagedigest.variable;

import static java.text.MessageFormat.format;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.BitSet;
import java.util.Objects;

import org.bouncycastle.crypto.digests.SHAKEDigest;

/**
 * Variable-output-length message digest based on the Bouncy Castle implementation of SHAKE.
 */
abstract class ShakeMessageDigest implements VariableOutputLengthMessageDigest {

	public static final int MINIMUM_MESSAGE_LENGTH = 1;

	private static final int BUFFER_SIZE = 1024;

	/**
	 * Ensures that a bit set does not exceed the requested bit length, by discarding the eventual surplus most-significant bits..
	 *
	 * @param bitSet           the bit set to truncate
	 * @param maximumBitLength the maximum length (in bits) of the bit set
	 */
	private static BitSet truncate(final BitSet bitSet, final int maximumBitLength) {
		// Truncate the output if required.
		if (maximumBitLength < bitSet.length()) {
			bitSet.clear(maximumBitLength, bitSet.length());
		}

		return bitSet;
	}

	/**
	 * @return the security bits of the implementing variant.
	 */
	abstract int getDigestSecurityBits();

	@Override
	public BitSet digest(final byte[] message, final int digestBitLength) {
		if (message.length < MINIMUM_MESSAGE_LENGTH) {
			throw new IllegalArgumentException(format("At least {0} bytes are required for the message", MINIMUM_MESSAGE_LENGTH));
		}
		validateOutputBitLength(digestBitLength);

		final SHAKEDigest digest = new SHAKEDigest(getDigestSecurityBits());
		digest.update(message, 0, message.length);

		// Calculate the amount of bytes required to fit the requested bits.
		final int outputByteLength = (digestBitLength + 7) / 8;
		final byte[] output = new byte[outputByteLength];
		// Close the digest and produce the output.
		digest.doFinal(output, 0, outputByteLength);

		// Return (at most) the requested bits.
		return truncate(BitSet.valueOf(output), digestBitLength);
	}

	@Override
	public BitSet digest(final InputStream message, final int digestBitLength) {
		Objects.requireNonNull(message, "A message input stream is required");

		validateOutputBitLength(digestBitLength);

		// Calculate the amount of bytes required to fit the requested bits.
		final int outputByteLength = (digestBitLength + Byte.SIZE - 1) / Byte.SIZE;

		final byte[] output = new byte[outputByteLength];

		final SHAKEDigest digest = new SHAKEDigest(getDigestSecurityBits());
		try (final BufferedInputStream bis = new BufferedInputStream(message)) {
			final byte[] buffer = new byte[BUFFER_SIZE];
			int bytesRead;
			while ((bytesRead = bis.read(buffer)) > 0) {
				digest.update(buffer, 0, bytesRead);
			}
		} catch (final IOException e) {
			throw new IllegalStateException(e);
		}

		// Close the digest and produce the output.
		digest.doFinal(output, 0, outputByteLength);

		// Return (at most) the requested bits.
		return truncate(BitSet.valueOf(output), digestBitLength);
	}

	/**
	 * Ensures the output's requested length is valid
	 *
	 * @param outputBitLength the requested length of the output
	 */
	private void validateOutputBitLength(final int outputBitLength) {
		final int mininumOutputBitLength = getDigestSecurityBits();
		if (outputBitLength < mininumOutputBitLength) {
			throw new IllegalArgumentException(format("Minimum output length is {0} bits", mininumOutputBitLength));
		}
	}
}
