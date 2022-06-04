/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.mathematical.groups.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.math.BigInteger;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.test.tools.configuration.GroupLoader;

/**
 * Tests of the Zp group element input validation.
 */

class ZpGroupElementValidationTest {

	private static ZpSubgroup zpSubgroup;
	private static BigInteger p;
	private static BigInteger q;
	private static BigInteger g;
	private static BigInteger pMinusOne;
	private static BigInteger elementValue;

	@BeforeAll
	public static void setUp() throws GeneralCryptoLibException {

		final GroupLoader zpGroupLoader = new GroupLoader();
		zpSubgroup = new ZpSubgroup(zpGroupLoader.getG(), zpGroupLoader.getP(), zpGroupLoader.getQ());

		p = zpSubgroup.getP();
		q = zpSubgroup.getQ();
		g = zpSubgroup.getG();

		pMinusOne = p.subtract(BigInteger.ONE);

		elementValue = BigInteger.ONE;
	}

	static Stream<Arguments> createZpGroupElementFromGroup() {

		return Stream.of(arguments(null, zpSubgroup, "Zp group element value is null."), arguments(elementValue, null, "Zp subgroup is null."),
				arguments(BigInteger.ZERO, zpSubgroup, "Zp group element value must be greater than or equal to : 1; Found 0"),
				arguments(p, zpSubgroup,
						"Zp group element value must be less than or equal to Zp subgroup p parameter minus 1: " + pMinusOne + "; Found " + p));
	}

	static Stream<Arguments> createZpGroupElementFromParams() {

		return Stream
				.of(arguments(null, p, q, "Zp group element value is null."), arguments(elementValue, null, q, "Zp subgroup p parameter is null."),
						arguments(elementValue, p, null, "Zp subgroup q parameter is null."),
						arguments(g, p, BigInteger.ZERO, "Zp subgroup q parameter must be greater than or equal to : 1; Found 0"),
						arguments(elementValue, p, p,
								"Zp subgroup q parameter must be less than or equal to Zp subgroup p parameter minus 1: " + pMinusOne + "; Found "
										+ p),
						arguments(BigInteger.ZERO, p, q, "Zp group element value must be greater than or equal to : 1; Found 0"), arguments(p, p, q,
								"Zp group element value must be less than or equal to Zp subgroup p parameter minus 1: " + pMinusOne + "; Found "
										+ p));
	}

	@ParameterizedTest
	@MethodSource("createZpGroupElementFromGroup")
	void testZpGroupElementCreationFromGroupValidation(final BigInteger value, final ZpSubgroup group, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class, () -> new ZpGroupElement(value, group));
		assertEquals(errorMsg, exception.getMessage());
	}

	@ParameterizedTest
	@MethodSource("createZpGroupElementFromParams")
	void testZpGroupElementCreationFromParamsValidation(final BigInteger value, final BigInteger p, final BigInteger q, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class, () -> new ZpGroupElement(value, p, q));
		assertEquals(errorMsg, exception.getMessage());
	}
}
