/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.cryptolib.mathematical.groups.impl;

import java.math.BigInteger;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.test.tools.configuration.GroupLoader;

class GqGroupValidationTest {

	private static BigInteger p;

	private static BigInteger q;

	private static BigInteger g;

	@BeforeAll
	public static void setUp() {
		GroupLoader gqGroupLoader = new GroupLoader();
		p = gqGroupLoader.getP();
		q = gqGroupLoader.getQ();
		g = gqGroupLoader.getG();
	}

	public static Stream<Arguments> createZpSubgroup() {
		return Stream.of(Arguments.of(null, q, g), Arguments.of(p, null, g), Arguments.of(p, q, null), Arguments.of(p, BigInteger.ZERO, g),
				//Check range q
				Arguments.of(p, p, g),                    //Check range q
				Arguments.of(p, q, BigInteger.ONE),    //Check range g
				Arguments.of(p, q, p)                    //Check range g
		);
	}

	@ParameterizedTest
	@MethodSource("createZpSubgroup")
	void testZpSubgroupCreationValidation(BigInteger p, BigInteger q, BigInteger g) throws GeneralCryptoLibException {
		Assertions.assertThrows(GeneralCryptoLibException.class, () -> new ZpSubgroup(g, p, q));
	}
}
