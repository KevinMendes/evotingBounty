/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.elgamal.bean;

import static java.util.Arrays.asList;

import java.math.BigInteger;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.mathematical.groups.impl.Exponent;
import ch.post.it.evoting.cryptolib.mathematical.groups.impl.ZpGroupElement;
import ch.post.it.evoting.cryptolib.mathematical.groups.impl.ZpSubgroup;

/**
 * Tests of {@link Codec}.
 */
class CodecTest {
	private ZpSubgroup group;

	@BeforeEach
	public void setUp() throws GeneralCryptoLibException {
		group = new ZpSubgroup(BigInteger.valueOf(2), BigInteger.valueOf(23), BigInteger.valueOf(11));
	}

	@Test
	void testDecodePrivateKey() throws GeneralCryptoLibException {
		final List<Exponent> exponents = asList(new Exponent(group.getQ(), BigInteger.valueOf(4)), new Exponent(group.getQ(), BigInteger.valueOf(5)));
		final ElGamalPrivateKey expected = new ElGamalPrivateKey(exponents, group);
		final byte[] bytes = Codec.encode(expected);
		final ElGamalPrivateKey actual = Codec.decodePrivateKey(bytes);
		Assertions.assertEquals(expected, actual);
	}

	@Test
	void testDecodePublicKey() throws GeneralCryptoLibException {
		final List<ZpGroupElement> elements = asList(new ZpGroupElement(BigInteger.valueOf(4), group), new ZpGroupElement(BigInteger.valueOf(8), group));
		final ElGamalPublicKey expected = new ElGamalPublicKey(elements, group);
		final byte[] bytes = Codec.encode(expected);
		final ElGamalPublicKey actual = Codec.decodePublicKey(bytes);
		Assertions.assertEquals(expected, actual);
	}
}
