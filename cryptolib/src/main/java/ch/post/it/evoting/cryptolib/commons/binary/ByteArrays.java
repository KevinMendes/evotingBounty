/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.commons.binary;

/**
 * Utility class for working with byte arrays.
 */
public final class ByteArrays {

	private ByteArrays() {
	}

	/**
	 * Concatenates given byte arrays.
	 *
	 * @param array1 the first array
	 * @param array2 the second array
	 * @return the byte array.
	 */
	public static byte[] concatenate(final byte[] array1, final byte[] array2) {
		final ByteArrayBuilder builder = new ByteArrayBuilder();
		return builder.append(array1).append(array2).build();
	}

	/**
	 * Concatenates given byte arrays.
	 *
	 * @param array1 the first array
	 * @param array2 the second array
	 * @param others the other arrays
	 * @return the byte array.
	 */
	public static byte[] concatenate(final byte[] array1, final byte[] array2, final byte[]... others) {
		final ByteArrayBuilder builder = new ByteArrayBuilder();
		builder.append(array1);
		builder.append(array2);
		for (final byte[] other : others) {
			builder.append(other);
		}
		return builder.build();
	}

	/**
	 * Returns whether two byte arrays have the same content. The operation time is {@code O(array.length)} to avoid timing attacks.
	 *
	 * @param array1 the first array
	 * @param array2 the second array
	 * @return the arrays are equal.
	 */
	public static boolean constantTimeEquals(final byte[] array1, final byte[] array2) {
		if (array1 == null || array2 == null || array1.length != array2.length) {
			return false;
		}
		boolean equals = true;
		for (int i = 0; i < array1.length; i++) {
			equals &= array1[i] == array2[i];
		}
		return equals;
	}
}
