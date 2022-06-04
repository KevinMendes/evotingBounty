/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.commons.validations;

import static java.util.Arrays.copyOf;
import static java.util.Arrays.fill;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.test.tools.utils.CommonTestDataGenerator;

class ValidateTest {

	private static final int MAX_INT_VALUE = 100;
	private static final BigInteger MAX_BIG_INTEGER_VALUE = BigInteger.valueOf(MAX_INT_VALUE);
	private static final int STRING_LENGTH = 20;
	private static final int NUM_BYTE_ELEMENTS = 100;
	private static final int NUM_INTEGER_ELEMENTS = 10;
	private static final int NUM_STRING_ELEMENTS = 10;
	private static final String TEST_INT_LABEL = "test int";
	private static final String TEST_BIG_INTEGER_LABEL = "test big integer";
	private static final String TEST_DOUBLE_LABEL = "test double";
	private static final String TEST_STRING_LABEL = "test string";
	private static final String TEST_CHAR_ARRAY_LABEL = "test character array";
	private static final String TEST_BYTE_ARRAY_LABEL = "test byte array";
	private static final String TEST_OBJECT_ARRAY_LABEL = "test object array";
	private static final String TEST_COLLECTION_LABEL = "test collection";
	private static final String TEST_MAP_LABEL = "test map";
	private static final String TEST_TIME_LABEL = "test time";
	private static final String ALLOWED_CHARACTER_SET = "abcdefghijklmnopqrstuvwxyz0ABCDEFGHIJKLMNOPQRSTUVWXYZ123456789_-";
	private static final String ALLOWED_CHARACTER_SET_AS_REGEX = "[a-zA-Z0-9_-]+";
	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	private static int testInt;
	private static BigInteger testBigInteger;
	private static double testDouble;
	private static String testString;
	private static char[] testCharArray;
	private static byte[] testByteArray;
	private static Object[] testObjectArray;
	private static Collection<?> testCollection;
	private static Map<?, ?> testMap;
	private static Pattern allowedCharsPattern;
	private static Date earlierTime;
	private static Date laterTime;
	private static char[] whiteSpaceChars;

	@BeforeAll
	static void setUp() {
		testInt = SECURE_RANDOM.nextInt(MAX_INT_VALUE);

		testDouble = SECURE_RANDOM.nextInt(MAX_INT_VALUE);

		testBigInteger = new BigInteger(testInt, new SecureRandom());

		testString = CommonTestDataGenerator.getAlphanumeric(STRING_LENGTH);

		testCharArray = testString.toCharArray();

		testByteArray = new byte[NUM_BYTE_ELEMENTS];
		SECURE_RANDOM.nextBytes(testByteArray);

		testObjectArray = generateRandomArray();

		testCollection = generateRandomCollection();

		testMap = generateRandomMap();

		whiteSpaceChars = new char[STRING_LENGTH];
		fill(whiteSpaceChars, ' ');

		allowedCharsPattern = Pattern.compile(ALLOWED_CHARACTER_SET_AS_REGEX);

		earlierTime = new Date(System.currentTimeMillis());

		final Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.YEAR, 1);
		laterTime = calendar.getTime();
	}

	private static Collection<?> generateRandomCollection() {

		final Object[] objArray = generateRandomArray();

		return Arrays.asList(objArray);
	}

	private static Map<?, ?> generateRandomMap() {

		final Map<Object, Object> objMap = new HashMap<>();
		for (int i = 0; i < ValidateTest.NUM_INTEGER_ELEMENTS; i++) {
			final String key = CommonTestDataGenerator.getAlphanumeric(ValidateTest.STRING_LENGTH);
			final Object value = SECURE_RANDOM.nextInt(ValidateTest.MAX_INT_VALUE);
			objMap.put(key, value);
		}
		for (int i = 0; i < ValidateTest.NUM_STRING_ELEMENTS; i++) {
			final String key = CommonTestDataGenerator.getAlphanumeric(ValidateTest.STRING_LENGTH);
			final Object value = SECURE_RANDOM.nextInt(ValidateTest.MAX_INT_VALUE);
			objMap.put(key, value);
		}

		return objMap;
	}

	private static Object[] generateRandomArray() {

		final Object[] objArray = new Object[ValidateTest.NUM_INTEGER_ELEMENTS + ValidateTest.NUM_STRING_ELEMENTS];

		for (int i = 0; i < ValidateTest.NUM_INTEGER_ELEMENTS; i++) {
			objArray[i] = SECURE_RANDOM.nextInt(ValidateTest.MAX_INT_VALUE);
		}
		for (int i = 0; i < ValidateTest.NUM_STRING_ELEMENTS; i++) {
			objArray[ValidateTest.NUM_INTEGER_ELEMENTS + i] = CommonTestDataGenerator.getAlphanumeric(ValidateTest.STRING_LENGTH);
		}

		return objArray;
	}

	@Test
	void whenDataIsValidThenNoExceptionsThrown() {

		assertAll(() -> assertDoesNotThrow(() -> Validate.notNull(testInt, TEST_INT_LABEL)),
				() -> assertDoesNotThrow(() -> Validate.notNull(testDouble, TEST_DOUBLE_LABEL)),
				() -> assertDoesNotThrow(() -> Validate.notNull(testString, TEST_STRING_LABEL)),
				() -> assertDoesNotThrow(() -> Validate.notNull(testByteArray, TEST_BYTE_ARRAY_LABEL)),
				() -> assertDoesNotThrow(() -> Validate.notNull(testObjectArray, TEST_OBJECT_ARRAY_LABEL)),
				() -> assertDoesNotThrow(() -> Validate.notNull(testCollection, TEST_COLLECTION_LABEL)),
				() -> assertDoesNotThrow(() -> Validate.notNull(testMap, TEST_MAP_LABEL)),
				() -> assertDoesNotThrow(() -> Validate.notNullOrBlank(testString, TEST_STRING_LABEL)),
				() -> assertDoesNotThrow(() -> Validate.notNullOrBlank(testCharArray, TEST_CHAR_ARRAY_LABEL)),
				() -> assertDoesNotThrow(() -> Validate.notNullOrEmpty(testByteArray, TEST_BYTE_ARRAY_LABEL)),
				() -> assertDoesNotThrow(() -> Validate.notNullOrEmpty(testObjectArray, TEST_OBJECT_ARRAY_LABEL)),
				() -> assertDoesNotThrow(() -> Validate.notNullOrEmpty(testCollection, TEST_COLLECTION_LABEL)),
				() -> assertDoesNotThrow(() -> Validate.notNullOrEmpty(testMap, TEST_MAP_LABEL)),
				() -> assertDoesNotThrow(() -> Validate.notNullOrEmptyAndNoNulls(testObjectArray, TEST_OBJECT_ARRAY_LABEL)),
				() -> assertDoesNotThrow(() -> Validate.notNullOrEmptyAndNoNulls(testCollection, TEST_COLLECTION_LABEL)),
				() -> assertDoesNotThrow(() -> Validate.notNullOrEmptyAndNoNulls(testMap, TEST_MAP_LABEL)),
				() -> assertDoesNotThrow(() -> Validate.isPositive(1, TEST_INT_LABEL)),
				() -> assertDoesNotThrow(() -> Validate.isEqual(1, 1, TEST_INT_LABEL, TEST_INT_LABEL)),
				() -> assertDoesNotThrow(() -> Validate.isEqual(BigInteger.ONE, BigInteger.ONE, TEST_BIG_INTEGER_LABEL, TEST_BIG_INTEGER_LABEL)),
				() -> assertDoesNotThrow(() -> Validate.notLessThan(0, 0, TEST_INT_LABEL, TEST_INT_LABEL)), () -> assertDoesNotThrow(
						() -> Validate.notLessThan(BigInteger.ZERO, BigInteger.ZERO, TEST_BIG_INTEGER_LABEL, TEST_BIG_INTEGER_LABEL)),
				() -> assertDoesNotThrow(() -> Validate.notGreaterThan(MAX_INT_VALUE, MAX_INT_VALUE, TEST_INT_LABEL, TEST_INT_LABEL)),
				() -> assertDoesNotThrow(
						() -> Validate.notGreaterThan(MAX_BIG_INTEGER_VALUE, MAX_BIG_INTEGER_VALUE, TEST_BIG_INTEGER_LABEL, TEST_BIG_INTEGER_LABEL)),
				() -> assertDoesNotThrow(
						() -> Validate.inRange((MAX_INT_VALUE - 1), 0, MAX_INT_VALUE, TEST_INT_LABEL, TEST_INT_LABEL, TEST_INT_LABEL)),
				() -> assertDoesNotThrow(() -> Validate
						.inRange((MAX_BIG_INTEGER_VALUE.subtract(BigInteger.ONE)), BigInteger.ZERO, MAX_BIG_INTEGER_VALUE, TEST_BIG_INTEGER_LABEL,
								TEST_BIG_INTEGER_LABEL, TEST_BIG_INTEGER_LABEL)),
				() -> assertDoesNotThrow(() -> Validate.onlyContains(testString, ALLOWED_CHARACTER_SET, TEST_STRING_LABEL)),
				() -> assertDoesNotThrow(() -> Validate.onlyContains(testString, allowedCharsPattern, TEST_STRING_LABEL)),
				() -> assertDoesNotThrow(() -> Validate.isAsciiPrintable(testString, TEST_STRING_LABEL)),
				() -> assertDoesNotThrow(() -> Validate.isBefore(earlierTime, laterTime, TEST_TIME_LABEL, TEST_TIME_LABEL)),
				() -> assertDoesNotThrow(() -> Validate.isAfter(laterTime, earlierTime, TEST_TIME_LABEL, TEST_TIME_LABEL)),
				() -> assertDoesNotThrow(() -> Validate.notBefore(laterTime, earlierTime, TEST_TIME_LABEL, TEST_TIME_LABEL)),
				() -> assertDoesNotThrow(() -> Validate.notAfter(earlierTime, laterTime, TEST_TIME_LABEL, TEST_TIME_LABEL)),
				() -> assertDoesNotThrow(() -> Validate.notBefore(earlierTime, earlierTime, TEST_TIME_LABEL, TEST_TIME_LABEL)),
				() -> assertDoesNotThrow(() -> Validate.notAfter(earlierTime, earlierTime, TEST_TIME_LABEL, TEST_TIME_LABEL)));
	}

	@Test
	void whenNullPrimitiveCheckedForNullThenExpectedExceptionThrown() {

		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class, () -> Validate.notNull(null, TEST_INT_LABEL));
		assertEquals(TEST_INT_LABEL + " is null.", exception.getMessage());
	}

	@Test
	void whenNullStringCheckedForNullThenExpectedExceptionThrown() {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class, () -> Validate.notNull(null, TEST_STRING_LABEL));
		assertEquals(TEST_STRING_LABEL + " is null.", exception.getMessage());
	}

	@Test
	void whenNullStringCheckedForNullOrEmptyThenExpectedExceptionThrown() {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> Validate.notNullOrBlank((String) null, TEST_STRING_LABEL));
		assertEquals(TEST_STRING_LABEL + " is null.", exception.getMessage());
	}

	@Test
	void whenEmptyStringCheckedForNullOrEmptyThenExpectedExceptionThrown() {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> Validate.notNullOrBlank("", TEST_STRING_LABEL));
		assertEquals(TEST_STRING_LABEL + " is blank.", exception.getMessage());
	}

	@Test
	void whenWhiteSpaceStringCheckedForNullOrEmptyThenExpectedExceptionThrown() {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> Validate.notNullOrBlank(new String(whiteSpaceChars), TEST_STRING_LABEL));
		assertEquals(TEST_STRING_LABEL + " is blank.", exception.getMessage());
	}

	@Test
	void whenNullCharArrayCheckedForNullThenExpectedExceptionThrown() {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> Validate.notNull(null, TEST_CHAR_ARRAY_LABEL));
		assertEquals(TEST_CHAR_ARRAY_LABEL + " is null.", exception.getMessage());
	}

	@Test
	void whenEmptyCharArrayCheckedForNullOrEmptyThenExpectedExceptionThrown() {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> Validate.notNullOrBlank("", TEST_CHAR_ARRAY_LABEL));
		assertEquals(TEST_CHAR_ARRAY_LABEL + " is blank.", exception.getMessage());
	}

	@Test
	void whenWhiteCharArrayCheckedForNullOrEmptyThenExpectedExceptionThrown() {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> Validate.notNullOrBlank(whiteSpaceChars, TEST_CHAR_ARRAY_LABEL));
		assertEquals(TEST_CHAR_ARRAY_LABEL + " is blank.", exception.getMessage());
	}

	@Test
	void whenNullByteArrayCheckedForNullThenExpectedExceptionThrown() {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> Validate.notNull(null, TEST_BYTE_ARRAY_LABEL));
		assertEquals(TEST_BYTE_ARRAY_LABEL + " is null.", exception.getMessage());
	}

	@Test
	void whenNullByteArrayCheckedForNullOrEmptyThenExpectedExceptionThrown() {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> Validate.notNullOrEmpty((byte[]) null, TEST_BYTE_ARRAY_LABEL));
		assertEquals(TEST_BYTE_ARRAY_LABEL + " is null.", exception.getMessage());
	}

	@Test
	void whenEmptyByteArrayCheckedForNullOrEmptyThenExpectedExceptionThrown() {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> Validate.notNullOrEmpty(new byte[0], TEST_BYTE_ARRAY_LABEL));
		assertEquals(TEST_BYTE_ARRAY_LABEL + " is empty.", exception.getMessage());
	}

	@Test
	void whenNullObjectArrayCheckedForNullThenExpectedExceptionThrown() {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> Validate.notNull(null, TEST_OBJECT_ARRAY_LABEL));
		assertEquals(TEST_OBJECT_ARRAY_LABEL + " is null.", exception.getMessage());
	}

	@Test
	void whenNullObjectArrayCheckedForNullOrEmptyThenExpectedExceptionThrown() {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> Validate.notNullOrEmpty((Object[]) null, TEST_OBJECT_ARRAY_LABEL));
		assertEquals(TEST_OBJECT_ARRAY_LABEL + " is null.", exception.getMessage());
	}

	@Test
	void whenEmptyObjectArrayCheckedForNullOrEmptyThenExpectedExceptionThrown() {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> Validate.notNullOrEmpty(new Object[0], TEST_OBJECT_ARRAY_LABEL));
		assertEquals(TEST_OBJECT_ARRAY_LABEL + " is empty.", exception.getMessage());
	}

	@Test
	void whenObjectArrayWithNullElementCheckedForNullsThenExpectedExceptionThrown() {
		final Object[] testObjectArrayWithNullElement = copyOf(testObjectArray, testObjectArray.length + 1);

		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> Validate.notNullOrEmptyAndNoNulls(testObjectArrayWithNullElement, TEST_OBJECT_ARRAY_LABEL));
		assertEquals(TEST_OBJECT_ARRAY_LABEL + " contains one or more null elements.", exception.getMessage());
	}

	@Test
	void whenNullCollectionCheckedForNullThenExpectedExceptionThrown() {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> Validate.notNull(null, TEST_COLLECTION_LABEL));
		assertEquals(TEST_COLLECTION_LABEL + " is null.", exception.getMessage());
	}

	@Test
	void whenNullCollectionCheckedForNullOrEmptyThenExpectedExceptionThrown() {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> Validate.notNullOrEmpty((Collection<?>) null, TEST_COLLECTION_LABEL));
		assertEquals(TEST_COLLECTION_LABEL + " is null.", exception.getMessage());
	}

	@Test
	void whenEmptyCollectionCheckedForNullOrEmptyThenExpectedExceptionThrown() {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> Validate.notNullOrEmpty(new ArrayList<>(), TEST_COLLECTION_LABEL));
		assertEquals(TEST_COLLECTION_LABEL + " is empty.", exception.getMessage());
	}

	@Test
	void whenCollectionWithNullElementCheckedForNullsThenExpectedExceptionThrown() {
		final Collection<?> testCollectionWithNullElement = new ArrayList<Object>(testCollection);
		testCollectionWithNullElement.add(null);

		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> Validate.notNullOrEmptyAndNoNulls(testCollectionWithNullElement, TEST_COLLECTION_LABEL));
		assertEquals(TEST_COLLECTION_LABEL + " contains one or more null elements.", exception.getMessage());
	}

	@Test
	void whenNullMapCheckedForNullThenExpectedExceptionThrown() {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class, () -> Validate.notNull(null, TEST_MAP_LABEL));
		assertEquals(TEST_MAP_LABEL + " is null.", exception.getMessage());
	}

	@Test
	void whenNullMapCheckedForNullOrEmptyThenExpectedExceptionThrown() {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> Validate.notNullOrEmpty((Map<?, ?>) null, TEST_MAP_LABEL));
		assertEquals(TEST_MAP_LABEL + " is null.", exception.getMessage());
	}

	@Test
	void whenEmptyMapCheckedForNullOrEmptyThenExpectedExceptionThrown() {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> Validate.notNullOrEmpty(new HashMap<>(), TEST_MAP_LABEL));
		assertEquals(TEST_MAP_LABEL + " is empty.", exception.getMessage());
	}

	@Test
	void whenMapWithNullValueCheckedForNullsThenExpectedExceptionThrown() {
		final Map<Object, Object> testMapWithNullValue = new HashMap<>(testMap);
		testMapWithNullValue.put("key for null value", null);

		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> Validate.notNullOrEmptyAndNoNulls(testMapWithNullValue, TEST_MAP_LABEL));
		assertEquals(TEST_MAP_LABEL + " contains one or more null values.", exception.getMessage());
	}

	@Test
	void whenIntNotPositiveThenExpectedExceptionThrown() {
		final int notPositiveInt = 0;

		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> Validate.isPositive(notPositiveInt, TEST_INT_LABEL));
		assertEquals(TEST_INT_LABEL + " must be a positive integer; Found " + notPositiveInt, exception.getMessage());
	}

	@Test
	void whenBigIntegerNotEqualToRequiredValueThenExpectedExceptionThrown() {
		final BigInteger arg = testBigInteger;
		final BigInteger value = testBigInteger.add(BigInteger.ONE);
		final String valueLabel = "int value label";

		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> Validate.isEqual(arg, value, TEST_BIG_INTEGER_LABEL, valueLabel));
		assertEquals(TEST_BIG_INTEGER_LABEL + " must be equal to " + valueLabel + ": " + value + "; Found " + arg, exception.getMessage());
	}

	@Test
	void whenIntNotEqualToRequiredValueThenExpectedExceptionThrown() {
		final int arg = testInt;
		final int value = testInt + 1;
		final String valueLabel = "int value label";

		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> Validate.isEqual(arg, value, TEST_INT_LABEL, valueLabel));
		assertEquals(TEST_INT_LABEL + " must be equal to " + valueLabel + ": " + value + "; Found " + arg, exception.getMessage());
	}

	@Test
	void whenIntLessThanMinimumThenExpectedExceptionThrown() {
		final int belowMinInt = -1;
		final String minValueLabel = "min int value label";

		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> Validate.notLessThan(belowMinInt, 0, TEST_INT_LABEL, minValueLabel));
		assertEquals(TEST_INT_LABEL + " must be greater than or equal to " + minValueLabel + ": " + 0 + "; Found " + belowMinInt,
				exception.getMessage());
	}

	@Test
	void whenBigIntegerLessThanMinimumThenExpectedExceptionThrown() {
		final BigInteger belowMinInt = BigInteger.valueOf(-1);
		final String minValueLabel = "min int value label";

		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> Validate.notLessThan(belowMinInt, BigInteger.ZERO, TEST_BIG_INTEGER_LABEL, minValueLabel));
		assertEquals(TEST_BIG_INTEGER_LABEL + " must be greater than or equal to " + minValueLabel + ": " + 0 + "; Found " + belowMinInt,
				exception.getMessage());
	}

	@Test
	void whenIntGreaterThanMaximumThenExpectedExceptionThrown() {
		final int aboveMaxInt = MAX_INT_VALUE + 1;
		final String maxValueLabel = "max int value label";

		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> Validate.notGreaterThan(aboveMaxInt, MAX_INT_VALUE, TEST_INT_LABEL, maxValueLabel));
		assertEquals(TEST_INT_LABEL + " must be less than or equal to " + maxValueLabel + ": " + MAX_INT_VALUE + "; Found " + aboveMaxInt,
				exception.getMessage());
	}

	@Test
	void whenBigIntegerGreaterThanMaximumThenExpectedExceptionThrown() {
		final BigInteger aboveMaxInt = MAX_BIG_INTEGER_VALUE.add(BigInteger.ONE);
		final String maxValueLabel = "max int value label";

		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> Validate.notGreaterThan(aboveMaxInt, MAX_BIG_INTEGER_VALUE, TEST_BIG_INTEGER_LABEL, maxValueLabel));
		assertEquals(
				TEST_BIG_INTEGER_LABEL + " must be less than or equal to " + maxValueLabel + ": " + MAX_BIG_INTEGER_VALUE + "; Found " + aboveMaxInt,
				exception.getMessage());
	}

	@Test
	void whenIntLessThanMinimumInRangeThenExpectedExceptionThrown() {
		final int belowMinInt = -1;
		final String minValueLabel = "min int value label";
		final String maxValueLabel = "max int value label";

		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> Validate.inRange(belowMinInt, 0, MAX_INT_VALUE, TEST_INT_LABEL, minValueLabel, maxValueLabel));
		assertEquals(TEST_INT_LABEL + " must be greater than or equal to " + minValueLabel + ": " + 0 + "; Found " + belowMinInt,
				exception.getMessage());
	}

	@Test
	void whenIntGreaterThanMaximumInRangeThenExpectedExceptionThrown() {
		final int aboveMaxInt = MAX_INT_VALUE + 1;
		final String minValueLabel = "min int value label";
		final String maxValueLabel = "max int value label";

		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> Validate.inRange(aboveMaxInt, 0, MAX_INT_VALUE, TEST_INT_LABEL, minValueLabel, maxValueLabel));
		assertEquals(TEST_INT_LABEL + " must be less than or equal to " + maxValueLabel + ": " + MAX_INT_VALUE + "; Found " + aboveMaxInt,
				exception.getMessage());
	}

	@Test
	void whenBigIntegerLessThanMinimumInRangeThenExpectedExceptionThrown() {
		final BigInteger belowMinInt = BigInteger.valueOf(-1);
		final String minValueLabel = "min big integer value label";
		final String maxValueLabel = "max big integer value label";

		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> Validate.inRange(belowMinInt, BigInteger.ZERO, MAX_BIG_INTEGER_VALUE, TEST_BIG_INTEGER_LABEL, minValueLabel, maxValueLabel));
		assertEquals(
				TEST_BIG_INTEGER_LABEL + " must be greater than or equal to " + minValueLabel + ": " + BigInteger.ZERO + "; Found " + belowMinInt,
				exception.getMessage());
	}

	@Test
	void whenBigIntegerGreaterThanMaximumInRangeThenExpectedExceptionThrown() {
		final BigInteger aboveMaxInt = MAX_BIG_INTEGER_VALUE.add(BigInteger.ONE);
		final String minValueLabel = "min int value label";
		final String maxValueLabel = "max int value label";

		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> Validate.inRange(aboveMaxInt, BigInteger.ZERO, MAX_BIG_INTEGER_VALUE, TEST_BIG_INTEGER_LABEL, minValueLabel, maxValueLabel));
		assertEquals(
				TEST_BIG_INTEGER_LABEL + " must be less than or equal to " + maxValueLabel + ": " + MAX_BIG_INTEGER_VALUE + "; Found " + aboveMaxInt,
				exception.getMessage());
	}

	@Test
	void whenStringContainsDisallowedCharactersThenExpectedExceptionThrown() {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> Validate.onlyContains(testString + "@", ALLOWED_CHARACTER_SET, TEST_STRING_LABEL));
		assertEquals(TEST_STRING_LABEL + " contains characters outside of allowed set " + ALLOWED_CHARACTER_SET, exception.getMessage());
	}

	@Test
	void whenStringContainsNonAsciiPrintableCharactersThenExpectedExceptionThrown() {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> Validate.isAsciiPrintable(testString + "ä", TEST_STRING_LABEL));
		assertEquals(TEST_STRING_LABEL + " contains characters that are not ASCII printable.", exception.getMessage());
	}

	@Test
	void whenStringContainsDisallowedCharactersInRegexThenExpectedExceptionThrown() {
		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> Validate.onlyContains(testString + "@", allowedCharsPattern, TEST_STRING_LABEL));
		assertEquals(TEST_STRING_LABEL + " contains characters outside of allowed set " + ALLOWED_CHARACTER_SET_AS_REGEX, exception.getMessage());
	}

	@Test
	void whenLaterTimeValidatedAsBeforeEarlierTimeThenExpectedExceptionThrown() {
		final String earlierTimeLabel = "earlier value label";

		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> Validate.isBefore(laterTime, earlierTime, TEST_TIME_LABEL, earlierTimeLabel));
		assertEquals(TEST_TIME_LABEL + " " + laterTime.toString() + " is not before " + earlierTimeLabel + " " + earlierTime.toString(),
				exception.getMessage());
	}

	@Test
	void whenEarlierTimeValidatedAsAfterLaterTimeThenExpectedExceptionThrown() {
		final String laterTimeLabel = "later value label";

		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> Validate.isAfter(earlierTime, laterTime, TEST_TIME_LABEL, laterTimeLabel));
		assertEquals(TEST_TIME_LABEL + " " + earlierTime.toString() + " is not after " + laterTimeLabel + " " + laterTime.toString(),
				exception.getMessage());
	}

	@Test
	void whenEarlerTimeValidatedAsNotBeforeLaterTimeThenExpectedExceptionThrown() {
		final String laterTimeLabel = "later value label";

		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> Validate.notBefore(earlierTime, laterTime, TEST_TIME_LABEL, laterTimeLabel));
		assertEquals(TEST_TIME_LABEL + " " + earlierTime.toString() + " is before " + laterTimeLabel + " " + laterTime.toString(),
				exception.getMessage());
	}

	@Test
	void whenLaterTimeValidatedAsNotAfterEarlierTimeThenExpectedExceptionThrown() {
		final String earlierTimeLabel = "early value label";

		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> Validate.notAfter(laterTime, earlierTime, TEST_TIME_LABEL, earlierTimeLabel));
		assertEquals(TEST_TIME_LABEL + " " + laterTime.toString() + " is after " + earlierTimeLabel + " " + earlierTime.toString(),
				exception.getMessage());
	}

	@Test
	void whenTimeValidatedAsBeforeSameTimeThenExpectedExceptionThrown() {
		final String laterTimeLabel = "later value label";

		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> Validate.isBefore(earlierTime, earlierTime, TEST_TIME_LABEL, laterTimeLabel));
		assertEquals(TEST_TIME_LABEL + " " + earlierTime.toString() + " is not before " + laterTimeLabel + " " + earlierTime.toString(),
				exception.getMessage());
	}

	@Test
	void whenTimeValidatedAsAfterSameTimeThenExpectedExceptionThrown() {
		final String earlierTimeLabel = "early value label";

		final GeneralCryptoLibException exception = assertThrows(GeneralCryptoLibException.class,
				() -> Validate.isAfter(earlierTime, earlierTime, TEST_TIME_LABEL, earlierTimeLabel));
		assertEquals(TEST_TIME_LABEL + " " + earlierTime.toString() + " is not after " + earlierTimeLabel + " " + earlierTime.toString(),
				exception.getMessage());
	}

	@Test
	void whenCompliantUUIDThenValidateSuccessful() {
		assertDoesNotThrow(() -> Validate.validateUUID("17ccbe962cf341bc93208c26e911090c"));
	}

	@Test
	void whenCompliantUUIDThenReturnsGivenInput() {
		final String uuid = "17ccbe962cf341bc93208c26e911090c";
		assertEquals(uuid, Validate.validateUUID(uuid));
	}

	@Test
	void whenUUIDLessThan32ThenExpectedExceptionThrown() {
		assertThrows(IllegalArgumentException.class, () -> Validate.validateUUID("17ccbe962cf341bc93208c26e911090"));
	}

	@Test
	void whenUUIDMoreThan32ThenExpectedExceptionThrown() {
		assertThrows(IllegalArgumentException.class, () -> Validate.validateUUID("17ccbe962cf341bc93208c26e911090cd"));
	}

	@Test
	void whenNullUUIDThenExpectedExceptionThrown() {
		assertThrows(IllegalArgumentException.class, () -> Validate.validateUUID(null));
	}

}
