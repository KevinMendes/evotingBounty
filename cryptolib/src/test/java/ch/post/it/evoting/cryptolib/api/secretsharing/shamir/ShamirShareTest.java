/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.api.secretsharing.shamir;

import java.math.BigInteger;
import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.mathematical.polynomials.Point;
import ch.post.it.evoting.cryptolib.secretsharing.shamir.ShamirShare;

class ShamirShareTest {
	private static final byte[] zero = new byte[] { 0 };
	private static final byte[] one = new byte[] { 1 };

	private static Point p0;
	private static Point p1;
	private static ShamirShare s0;
	private static ShamirShare s1;

	@BeforeEach
	public void setUp() {
		p0 = new Point(new BigInteger(zero), new BigInteger(zero));
		p1 = new Point(new BigInteger(one), new BigInteger(one));
		s0 = new ShamirShare(0, 0, new BigInteger(zero), 0, Collections.singletonList(p0));
		s1 = new ShamirShare(1, 1, new BigInteger(one), 0, Collections.singletonList(p1));
	}

	@Test
	void testEquals() {
		final ShamirShare s0b = new ShamirShare(0, 0, new BigInteger(0, zero), 0, Collections.singletonList(p0));

		Assertions.assertNotEquals(s0, s1);
		Assertions.assertNotEquals(s1, s0);
		Assertions.assertEquals(s0, s0b);
	}

	@Test
	void equalPoints() {
		final Point p1b = new Point(new BigInteger(one), new BigInteger(one));

		Assertions.assertEquals(p1, p1b);
		Assertions.assertNotEquals(p1, p0);
		Assertions.assertNotEquals(p0, p1);
	}
}
