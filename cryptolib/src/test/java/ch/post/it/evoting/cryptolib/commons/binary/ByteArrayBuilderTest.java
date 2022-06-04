/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.commons.binary;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Tests of {@link ByteArrayBuilder}.
 */
class ByteArrayBuilderTest {
	@Test
	void testBuild() {
		final ByteArrayBuilder builder = new ByteArrayBuilder();
		builder.append((byte) 0);
		builder.append(new byte[] { 1, 2 });
		builder.append(new byte[0]);
		builder.append(new byte[] { 3, 4, 5, 6 }, 1, 2);
		Assertions.assertArrayEquals(new byte[] { 0, 1, 2, 4, 5 }, builder.build());
	}

	@Test
	void testBuildEmpty() {
		Assertions.assertArrayEquals(new byte[0], new ByteArrayBuilder().build());
	}
}
