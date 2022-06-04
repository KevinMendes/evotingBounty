/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.mathematical.groups.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.math.BigInteger;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.test.tools.configuration.GroupLoader;

class ExponentValidationTest {

	static Stream<Arguments> createExponent() {

		final BigInteger q = new GroupLoader().getQ();
		final BigInteger exponentValue = BigInteger.TEN;

		return Stream.of(arguments(null, exponentValue, "Zp subgroup q parameter is null."), arguments(q, null, "Exponent value is null."),
				arguments(BigInteger.ZERO, exponentValue, "Zp subgroup q parameter must be greater than or equal to : 1; Found 0"));
	}

	@ParameterizedTest
	@MethodSource("createExponent")
	void testExponentCreationValidation(final BigInteger q, final BigInteger value, final String errorMsg) {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class, () -> new Exponent(q, value));
		assertEquals(errorMsg, exception.getMessage());
	}
}
