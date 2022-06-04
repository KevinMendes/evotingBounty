/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.mathematical.groups.impl;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.commons.serialization.BigIntegerDeserializer;
import ch.post.it.evoting.cryptolib.commons.serialization.BigIntegerSerializer;

class ExponentTest extends ExponentTestBase {

	@Test
	void givenNullNullExponentWhenAttemptToCreateExponentThenException() {
		assertThrows(GeneralCryptoLibException.class, () -> new Exponent(_smallQ, null));
	}

	@Test
	void givenNullQAndValidValueWhenAttemptToCreateExponentThenException() {
		assertThrows(GeneralCryptoLibException.class, () -> new Exponent(null, BigInteger.TEN));
	}

	@Test
	void givenANonRandomExponentValueLessThanQGetExponentValue() throws GeneralCryptoLibException {
		final BigInteger exponentValue = new BigInteger("1");

		createExponentAndAssertExponentValue(_smallQ, exponentValue, exponentValue);
	}

	@Test
	void givenANonRandomExponentValueGreaterThanQGetExponentValue() throws GeneralCryptoLibException {
		final BigInteger exponentValue = new BigInteger("113");
		final BigInteger expectedExponentValue = new BigInteger("3");

		createExponentAndAssertExponentValue(_smallQ, exponentValue, expectedExponentValue);
	}

	@Test
	void givenANonRandomExponentValueEqualToQGetExponentValue() throws GeneralCryptoLibException {
		final BigInteger exponentValue = new BigInteger("11");
		final BigInteger expectedExponentValue = BigInteger.ZERO;

		createExponentAndAssertExponentValue(_smallQ, exponentValue, expectedExponentValue);
	}

	@Test
	void givenANonRandomNegativeExponentGetExponentValue() throws GeneralCryptoLibException {
		final BigInteger exponentValue = new BigInteger("-111");
		final BigInteger expectedExponentValue = BigInteger.TEN;

		createExponentAndAssertExponentValue(_smallQ, exponentValue, expectedExponentValue);
	}

	@Test
	void givenAnExponentWhenGetQThenExpectedQReturned() throws GeneralCryptoLibException {
		final BigInteger exponentValue = new BigInteger("2");
		final BigInteger expectedQ = new BigInteger("11");

		createExponentAndAssertQ(_smallQ, _smallP, _smallG, expectedQ, exponentValue);
	}

	/**
	 * Creates an exponent and then calls the toJson() method on that exponent. Asserts that the returned string can be used to reconstruct the
	 * exponent.
	 */
	@Test
	void givenAnExponentWhenToJsonCanRecoverExponent() throws GeneralCryptoLibException, JsonProcessingException {
		final Exponent exponent = new Exponent(_smallQ, BigInteger.TEN);

		final String jsonStr = exponent.toJson();

		ObjectMapper mapper = new ObjectMapper();
		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
		mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
		final SimpleModule module = new SimpleModule();
		module.addSerializer(new BigIntegerSerializer());
		module.addDeserializer(BigInteger.class, new BigIntegerDeserializer());
		mapper.registerModule(module);
		final Exponent reconstructedExponent = mapper.readValue(jsonStr, Exponent.class);

		final String errorMessage = "The Exponent could not be reconstructed from a JSON string.";
		assertEquals(reconstructedExponent, exponent, errorMessage);
	}

	@Test
	void testEquals() throws GeneralCryptoLibException {
		final ZpSubgroup mathematicalGroup_q11 = new ZpSubgroup(new BigInteger("2"), new BigInteger("23"), new BigInteger("11"));
		final ZpSubgroup mathematicalGroup_q3 = new ZpSubgroup(new BigInteger("2"), new BigInteger("23"), new BigInteger("3"));

		final Exponent exponent1_q11_value10 = new Exponent(mathematicalGroup_q11.getQ(), BigInteger.TEN);
		final Exponent exponent2_q11_value10 = new Exponent(mathematicalGroup_q11.getQ(), BigInteger.TEN);

		final Exponent exponent3_q13_value4 = new Exponent(mathematicalGroup_q3.getQ(), new BigInteger("4"));

		final Exponent exponent4_q11_value9 = new Exponent(mathematicalGroup_q11.getQ(), new BigInteger("9"));

		assertAll(() -> assertEquals(exponent1_q11_value10, exponent2_q11_value10),
				() -> assertNotEquals(exponent1_q11_value10, exponent3_q13_value4),
				() -> assertNotEquals(exponent1_q11_value10, exponent4_q11_value9),
				() -> assertNotEquals(exponent3_q13_value4, exponent4_q11_value9));
	}

	@Test
	void testToString() throws GeneralCryptoLibException {
		final Exponent element = new Exponent(BigInteger.TEN, BigInteger.valueOf(2));
		final String toString = element.toString();
		assertTrue(toString.contains("=2"));
		assertTrue(toString.contains("=" + BigInteger.TEN.toString()));
	}

	/**
	 * @param q                     the Zp subgroup q parameter.
	 * @param exponentValue         The desired exponent value.
	 * @param expectedExponentValue The expected exponent value.
	 */
	private void createExponentAndAssertExponentValue(final BigInteger q, final BigInteger exponentValue, final BigInteger expectedExponentValue)
			throws GeneralCryptoLibException {

		final Exponent exponent = new Exponent(q, exponentValue);

		assertEquals(expectedExponentValue, exponent.getValue(), "The exponent value is not the expected one");
	}

	/**
	 * @param q             The q parameter to be used when creating the exponent.
	 * @param expectedQ     The q that is expected to be returned when the getQ() method is called.
	 * @param exponentValue The exponent value to be used when creating the exponent.
	 */
	private void createExponentAndAssertQ(final BigInteger q, final BigInteger p, final BigInteger g, final BigInteger expectedQ,
			final BigInteger exponentValue) throws GeneralCryptoLibException {

		final Exponent exponent = new Exponent(q, exponentValue);

		assertEquals(expectedQ, exponent.getQ(), "The q is not the expected one");
	}

}
