/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.mathematical.polynomials;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;

class PointTest {

	@Test
	void itWorks() {
		final BigInteger x = BigInteger.valueOf(10);
		final BigInteger y = BigInteger.valueOf(100);

		final Point p = new Point(x, y);

		assertEquals(p.getX(), x);
		assertEquals(p.getY(), y);
	}

	@Test
	void failsForNullX() {
		assertThrows(IllegalArgumentException.class, () -> new Point(null, BigInteger.ONE));
	}

	@Test
	void failsForNullY() {
		assertThrows(IllegalArgumentException.class, () -> new Point(BigInteger.ZERO, null));
	}
}
