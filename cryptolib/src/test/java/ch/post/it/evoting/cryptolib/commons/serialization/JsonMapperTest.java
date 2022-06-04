/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.commons.serialization;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.test.tools.utils.CommonTestDataGenerator;

class JsonMapperTest {

	private static final int MAX_STRING_LENGTH = 20;

	private static final int MAX_INTEGER = 100000;

	private static final int LIST_SIZE = 10;

	private static String testString;

	private static Integer testInteger;

	private static BigInteger testBigInteger;

	private static List<BigInteger> testBigIntegerList;

	private static String testBigIntegerAsString;

	private static String anotherTestString;

	private static Integer anotherTestInteger;

	private static StringBuilder listOfBigIntegersAsStrings;

	@BeforeAll
	public static void setUp() {

		testString = CommonTestDataGenerator.getAlphanumeric(MAX_STRING_LENGTH);
		testInteger = generateRandomInteger();
		testBigInteger = new BigInteger(generateRandomInteger().toString());
		testBigIntegerAsString = toEncodedString(testBigInteger);
		testBigIntegerList = new ArrayList<>();
		for (int i = 1; i <= LIST_SIZE; i++) {
			testBigIntegerList.add(testBigInteger.add(BigInteger.valueOf(i)));
		}
		anotherTestString = CommonTestDataGenerator.getAlphanumeric(MAX_STRING_LENGTH);
		anotherTestInteger = generateRandomInteger();

		listOfBigIntegersAsStrings = new StringBuilder("[");
		for (int i = 0; i < (LIST_SIZE - 1); i++) {
			listOfBigIntegersAsStrings.append(toEncodedString(testBigIntegerList.get(i))).append(",");
		}
		listOfBigIntegersAsStrings.append(toEncodedString(testBigIntegerList.get(LIST_SIZE - 1))).append("]");
	}

	private static String toEncodedString(final BigInteger value) {
		return "\"" + Base64.getEncoder().encodeToString(value.toByteArray()) + "\"";
	}

	private static Integer generateRandomInteger() {
		return new SecureRandom().nextInt(MAX_INTEGER);
	}

	@Test
	void testJsonSerializationToString() throws Exception {

		final SerializableTestClass testClassOut = new SerializableTestClass(testString, testInteger, testBigInteger, testBigIntegerList);
		testClassOut.setAnotherTestString(anotherTestString);
		testClassOut.setAnotherTestInteger(anotherTestInteger);

		final String jsonString = testClassOut.toJson();

		Assertions.assertTrue(jsonString.contains(SerializableTestClass.class.getSimpleName()));
		Assertions.assertTrue(jsonString.contains(testString));
		Assertions.assertTrue(jsonString.contains(testInteger.toString()));
		Assertions.assertTrue(jsonString.contains(testBigIntegerAsString));
		Assertions.assertTrue(jsonString.contains(listOfBigIntegersAsStrings.toString()));
		Assertions.assertFalse(jsonString.contains(anotherTestString));
		Assertions.assertFalse(jsonString.contains(anotherTestInteger.toString()));
	}

	@Test
	void testJsonDeserializationFromString() throws Exception {

		final SerializableTestClass testClassOut = new SerializableTestClass(testString, testInteger, testBigInteger, testBigIntegerList);
		testClassOut.setAnotherTestString(anotherTestString);
		testClassOut.setAnotherTestInteger(anotherTestInteger);

		final String jsonStr = testClassOut.toJson();

		final SerializableTestClass testClassIn = new SerializableTestClass(jsonStr);

		Assertions.assertEquals(testClassIn, testClassOut);
	}
}
