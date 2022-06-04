/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.commons.binary;

import static java.lang.System.arraycopy;
import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder to compose byte arrays from a sequence of byte arrays.
 */
public final class ByteArrayBuilder {
	private final List<Range> ranges = new ArrayList<>();

	private int length;

	/**
	 * Appends a given byte.
	 *
	 * @param b the byte
	 * @return this instance.
	 */
	public ByteArrayBuilder append(final byte b) {
		return append(new byte[] { b });
	}

	/**
	 * Appends given bytes. Client must not modify the content of the specified array.
	 *
	 * @param bytes the bytes
	 * @return this instance.
	 */
	public ByteArrayBuilder append(final byte[] bytes) {
		return append(bytes, 0, bytes.length);
	}

	/**
	 * /** Appends a range of given bytes. Client must not modify the content of the specified range.
	 *
	 * @param bytes  the bytes
	 * @param offset the offset, must be nonnegative
	 * @param length the length, must be nonnegative and must satisfy {@code offset + length <= bytes.length}
	 * @return this instance.
	 */
	public ByteArrayBuilder append(final byte[] bytes, final int offset, final int length) {
		requireNonNull(bytes, "Bytes are null.");
		if (offset < 0 || offset > bytes.length) {
			throw new IllegalArgumentException("Invalid offset.");
		}
		if (length < 0 || bytes.length - offset < length) {
			throw new IllegalArgumentException("Invalid length.");
		}
		ranges.add(new Range(bytes, offset, length));
		this.length += length;
		return this;
	}

	/**
	 * Build the byte array.
	 *
	 * @return the byte array.
	 */
	public byte[] build() {
		final byte[] bytes = new byte[length];
		int position = 0;
		for (final Range range : ranges) {
			arraycopy(range.bytes, range.offset, bytes, position, range.length);
			position += range.length;
		}
		return bytes;
	}

	private static class Range {
		private final byte[] bytes;

		private final int offset;

		private final int length;

		private Range(final byte[] bytes, final int offset, final int length) {
			this.bytes = bytes;
			this.offset = offset;
			this.length = length;
		}
	}
}
