/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.mathematical.groups.impl;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigInteger;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.commons.serialization.BigIntegerDeserializer;
import ch.post.it.evoting.cryptolib.commons.serialization.BigIntegerSerializer;

class ZpGroupElementTest {

	private static BigInteger q;
	private static BigInteger p;
	private static BigInteger g;
	private static ZpSubgroup groupG2Q11;

	@BeforeAll
	static void setUp() throws GeneralCryptoLibException {

		q = new BigInteger("11");
		p = new BigInteger("23");
		g = new BigInteger("2");

		groupG2Q11 = new ZpSubgroup(g, p, q);
	}

	@Test
	void givenAValueWhenAGroupElementIsCreatedWithThatValueThenHasThatValue() throws GeneralCryptoLibException {
		final BigInteger value = new BigInteger("2");

		final ZpGroupElement element = new ZpGroupElement(value, groupG2Q11);

		assertEquals(value, element.getValue(), "The returned element value is not the expected one");
	}

	@Test
	void whenCreateAnElementWithValueZeroThenError() {
		final BigInteger value = BigInteger.ZERO;

		assertThrows(GeneralCryptoLibException.class, () -> new ZpGroupElement(value, groupG2Q11));
	}

	@Test
	void whenCreateAnElementWithNegativeValueThenError() {
		final BigInteger value = new BigInteger("-1");

		assertThrows(GeneralCryptoLibException.class, () -> new ZpGroupElement(value, groupG2Q11));
	}

	@Test
	void whenCreateAnElementWithValueGreaterThanPThenError() {
		final BigInteger value = new BigInteger("24");

		assertThrows(GeneralCryptoLibException.class, () -> new ZpGroupElement(value, groupG2Q11));
	}

	@Test
	void whenCreateAnElementWithNullValueThenError() {
		assertThrows(GeneralCryptoLibException.class, () -> new ZpGroupElement(null, groupG2Q11));
	}

	@Test
	void whenCreateAnElementWithNullGroupThenError() {
		final BigInteger value = BigInteger.ONE;

		assertThrows(GeneralCryptoLibException.class, () -> new ZpGroupElement(value, null));
	}

	@Test
	void whenCreateAnElement() {
		final BigInteger value = new BigInteger("17");

		assertDoesNotThrow(() -> new ZpGroupElement(value, groupG2Q11));
	}

	@Test
	void whenCreateAnElementWithValueOneThenResultHasValueOne() throws GeneralCryptoLibException {
		final BigInteger value = BigInteger.ONE;

		final ZpGroupElement element = new ZpGroupElement(value, groupG2Q11);

		assertEquals(value, element.getValue(), "The result has a wrong value");
	}

	@Test
	void givenNullElementWhenMultiplyThenException() throws GeneralCryptoLibException {
		final BigInteger value1 = new BigInteger("3");
		final ZpGroupElement element1 = new ZpGroupElement(value1, groupG2Q11);

		assertThrows(GeneralCryptoLibException.class, () -> element1.multiply(null));
	}

	@Test
	void givenTwoElementsFromDifferentGroupsWhenMultiplyThenException() throws GeneralCryptoLibException {
		final BigInteger value1 = new BigInteger("3");
		final BigInteger value2 = new BigInteger("3");

		final ZpGroupElement element1 = new ZpGroupElement(value1, groupG2Q11);
		final ZpGroupElement element2 = new ZpGroupElement(value2, new ZpSubgroup(g, new BigInteger("7"), new BigInteger("3")));

		assertThrows(GeneralCryptoLibException.class, () -> element1.multiply(element2));
	}

	@Test
	void givenTwoElementsWhenMultipliedThenSucceeds() throws GeneralCryptoLibException {
		final BigInteger value1 = new BigInteger("3");
		final BigInteger value2 = new BigInteger("4");
		final BigInteger expectedResult = new BigInteger("12");

		multiplyAndAssert(value1, value2, expectedResult);
	}

	@Test
	void givenAnElementWithValueOneWhenMultipliedWithASecondElementThenTheResultIsSecondElement() throws GeneralCryptoLibException {
		final BigInteger value1 = new BigInteger("2");
		final BigInteger value2 = BigInteger.ONE;
		final BigInteger expectedResult = new BigInteger("2");

		multiplyAndAssert(value1, value2, expectedResult);
	}

	@Test
	void givenTwoElementWhenMultipliedThenTheResultIsGreaterThanP() throws GeneralCryptoLibException {
		final BigInteger value1 = new BigInteger("12");
		final BigInteger value2 = new BigInteger("13");
		final BigInteger expectedResult = new BigInteger("18");

		multiplyAndAssert(value1, value2, expectedResult);
	}

	@Test
	void givenElementAndNullExponentWhenExponentiateThenException() throws GeneralCryptoLibException {
		final BigInteger value1 = new BigInteger("3");
		final ZpGroupElement element = new ZpGroupElement(value1, groupG2Q11);

		assertThrows(GeneralCryptoLibException.class, () -> element.exponentiate(null));
	}

	@Test
	void givenElementAndExponentWithNullValueWhenExponentiateThenException() throws GeneralCryptoLibException {
		final BigInteger value1 = new BigInteger("3");
		final ZpGroupElement element = new ZpGroupElement(value1, groupG2Q11);

		final Exponent mockExponent = mock(Exponent.class);
		when(mockExponent.getValue()).thenReturn(null);

		assertThrows(GeneralCryptoLibException.class, () -> element.exponentiate(mockExponent));
	}

	@Test
	void givenElementAndExponentFromDifferentGroupsWhenExponentiateThenException() throws GeneralCryptoLibException {
		final BigInteger value1 = new BigInteger("3");
		final ZpGroupElement element = new ZpGroupElement(value1, groupG2Q11);

		final ZpSubgroup exponentGroup = new ZpSubgroup(g, new BigInteger("7"), new BigInteger("3"));
		final BigInteger exponentValue = new BigInteger("3");
		final Exponent exponent = new Exponent(exponentGroup.getQ(), exponentValue);

		assertThrows(GeneralCryptoLibException.class, () -> element.exponentiate(exponent));
	}

	@Test
	void givenAnExponentWithValueZeroWhenExponentiateWithItThenResultIsOne() throws GeneralCryptoLibException {
		final BigInteger value = new BigInteger("16");
		final BigInteger exponentValue = BigInteger.ZERO;
		final BigInteger expectedResult = BigInteger.ONE;

		exponentiateAndAssert(value, exponentValue, expectedResult);
	}

	@Test
	void givenElementAndExponentWhenExponentiateThenSucceeds() throws GeneralCryptoLibException {
		final BigInteger value = new BigInteger("2");
		final BigInteger exponentValue = new BigInteger("4");
		final BigInteger expectedResult = new BigInteger("16");

		exponentiateAndAssert(value, exponentValue, expectedResult);
	}

	@Test
	void givenElementAndExponentWhenExponentiationThenResultGreaterThanQ() throws GeneralCryptoLibException {
		final BigInteger value = new BigInteger("13");
		final BigInteger exponentValue = new BigInteger("5");
		final BigInteger expectedResult = new BigInteger("4");

		exponentiateAndAssert(value, exponentValue, expectedResult);
	}

	@Test
	void testExponentiateWithANullElement() throws Exception {
		final ZpGroupElement element = new ZpGroupElement(BigInteger.TEN, groupG2Q11);

		assertThrows(GeneralCryptoLibException.class, () -> element.exponentiate(null));
	}

	@Test
	void testEquals() throws GeneralCryptoLibException {
		final ZpGroupElement element1_value1_q11 = new ZpGroupElement(BigInteger.ONE, groupG2Q11);
		final ZpGroupElement element2_value1_q11 = new ZpGroupElement(BigInteger.ONE, groupG2Q11);

		final ZpGroupElement element3_value2_q11 = new ZpGroupElement(new BigInteger("2"), groupG2Q11);

		final ZpSubgroup otherGroup_g4_q3 = new ZpSubgroup(new BigInteger("2"), new BigInteger("7"), new BigInteger("3"));
		final ZpGroupElement element4_value1_q13 = new ZpGroupElement(BigInteger.ONE, otherGroup_g4_q3);

		assertAll(() -> assertEquals(element1_value1_q11, element2_value1_q11), () -> assertNotEquals(element1_value1_q11, element3_value2_q11),
				() -> assertNotEquals(element1_value1_q11, element4_value1_q13), () -> assertNotEquals(element3_value2_q11, element4_value1_q13));
	}

	/**
	 * Creates a ZpGroupElement and then calls the toJson() method on that ZpGroupElement. Asserts that the returned string can be used to reconstruct
	 * the ZpGroupElement.
	 */
	@Test
	void testToJson() throws GeneralCryptoLibException, JsonProcessingException {
		final ZpGroupElement element = new ZpGroupElement(new BigInteger("2"), groupG2Q11);

		final String jsonStr = element.toJson();

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
		mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
		final SimpleModule module = new SimpleModule();
		module.addSerializer(new BigIntegerSerializer());
		module.addDeserializer(BigInteger.class, new BigIntegerDeserializer());
		mapper.registerModule(module);
		final ZpGroupElement reconstructedElement = mapper.readValue(jsonStr, ZpGroupElement.class);

		final String errorMessage = "The reconstructed Zp group element does not equal the original element.";
		assertEquals(reconstructedElement, element, errorMessage);
	}

	@Test
	void testToString() throws GeneralCryptoLibException {
		final ZpGroupElement element = new ZpGroupElement(new BigInteger("2"), groupG2Q11);
		final String toString = element.toString();

		assertAll(() -> assertTrue(toString.contains("=2,")), () -> assertTrue(toString.contains("=" + groupG2Q11.getP().toString())),
				() -> assertTrue(toString.contains("=" + groupG2Q11.getQ().toString())));
	}

	/**
	 * Exponentiates an element by an exponent and asserts the expected result.
	 *
	 * @param elementValue   The group element value to set.
	 * @param exponentValue  The exponent value to set.
	 * @param expectedResult The expected result of the exponentiation.
	 */
	private void exponentiateAndAssert(final BigInteger elementValue, final BigInteger exponentValue, final BigInteger expectedResult)
			throws GeneralCryptoLibException {

		final ZpGroupElement element = new ZpGroupElement(elementValue, groupG2Q11);

		final Exponent exponent = new Exponent(groupG2Q11.getQ(), exponentValue);

		final ZpGroupElement result = element.exponentiate(exponent);

		assertEquals(expectedResult, result.getValue(), "The result of the exponentiation is not the expected.");
	}

	/**
	 * Multiply two group elements with the values {@code value1} and {@code value2}. Then asserts that the result has the value {@code
	 * expectedResult}.
	 *
	 * @param value1         First element to multiply.
	 * @param value2         Second element to multiply.
	 * @param expectedResult The expected result of the {@code value1 * value2}.
	 */
	private void multiplyAndAssert(final BigInteger value1, final BigInteger value2, final BigInteger expectedResult)
			throws GeneralCryptoLibException {
		final ZpGroupElement element1 = new ZpGroupElement(value1, groupG2Q11);
		final ZpGroupElement element2 = new ZpGroupElement(value2, groupG2Q11);

		final ZpGroupElement result = element1.multiply(element2);

		assertEquals(expectedResult, result.getValue(), "The multiplication result is not the expected one");
	}

}
