/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.commons.validations;

import static java.util.Arrays.binarySearch;
import static java.util.Arrays.sort;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;

/**
 * Utility class to validate arguments.
 */
public final class Validate {

	public static final String ONE_OR_MORE_NULL_ELEMENTS = " contains one or more null elements.";
	private static final Pattern UUID_PATTERN = Pattern.compile("^[0-9a-f]{32}$");
	private static final String IS_BLANK = " is blank.";
	private static final String IS_EMPTY = " is empty.";
	private static final String FOUND = "; Found ";
	private static final String MUST_BE_GREATER_THAN_OR_EQUAL_TO = " must be greater than or equal to ";
	private static final String MUST_BE_LESS_THAN_OR_EQUAL_TO = " must be less than or equal to ";
	private static final String CONTAINS_CHARACTERS_OUTSIDE_OF_ALLOWED_SET = " contains characters outside of allowed set ";
	private static final int ASCII_LOWER_BOUND = 32;
	private static final int ASCII_UPPER_BOUND = 127;

	private Validate() {
	}

	/**
	 * Validates that an object argument is not null.
	 *
	 * @param objectArg the object argument to validate.
	 * @param label     the label of the object argument, used for error messages.
	 * @throws GeneralCryptoLibException if the check fails.
	 */
	public static void notNull(final Object objectArg, final String label) throws GeneralCryptoLibException {

		if (objectArg == null) {
			throw new GeneralCryptoLibException(label + " is null.");
		}
	}

	/**
	 * Validates that a string argument is not null or empty and does not consist only of white spaces.
	 *
	 * @param stringArg the string argument to validate.
	 * @param label     the label of the string argument, used for error messages.
	 * @throws GeneralCryptoLibException if the check fails.
	 */
	public static void notNullOrBlank(final String stringArg, final String label) throws GeneralCryptoLibException {

		notNull(stringArg, label);

		for (int i = 0; i < stringArg.length(); i++) {
			if (!Character.isWhitespace(stringArg.charAt(i))) {
				return;
			}
		}

		throw new GeneralCryptoLibException(label + IS_BLANK);
	}

	/**
	 * Validates that a char array argument is not null or empty and does not consist only of white space characters.
	 *
	 * @param charArrayArg the char array argument to validate.
	 * @param label        the label of the char array argument, used for error messages.
	 * @throws GeneralCryptoLibException if the check fails.
	 */
	public static void notNullOrBlank(final char[] charArrayArg, final String label) throws GeneralCryptoLibException {

		notNull(charArrayArg, label);

		if (charArrayArg.length == 0) {
			throw new GeneralCryptoLibException(label + IS_BLANK);
		}

		for (final char element : charArrayArg) {
			if (!Character.isWhitespace(element)) {
				return;
			}
		}

		throw new GeneralCryptoLibException(label + IS_BLANK);
	}

	/**
	 * Validates that a byte array argument is not null nor empty.
	 *
	 * @param byteArrayArg the byte array argument to validate.
	 * @param label        the label of the byte array argument, used for error messages.
	 * @throws GeneralCryptoLibException if the check fails.
	 */
	public static void notNullOrEmpty(final byte[] byteArrayArg, final String label) throws GeneralCryptoLibException {

		notNull(byteArrayArg, label);

		if (byteArrayArg.length == 0) {
			throw new GeneralCryptoLibException(label + IS_EMPTY);
		}
	}

	/**
	 * Validates that an object array argument is not null or empty.
	 *
	 * @param objectArrayArg the object array argument to validate.
	 * @param label          the label of the object array argument, used for error messages.
	 * @throws GeneralCryptoLibException if the check fails.
	 */
	public static void notNullOrEmpty(final Object[] objectArrayArg, final String label) throws GeneralCryptoLibException {

		notNull(objectArrayArg, label);

		if (objectArrayArg.length == 0) {
			throw new GeneralCryptoLibException(label + IS_EMPTY);
		}
	}

	/**
	 * Validates that a collection argument is not null or empty.
	 *
	 * @param collectionArg the collection argument to validate.
	 * @param label         the label of the collection argument, used for error messages.
	 * @throws GeneralCryptoLibException if the check fails.
	 */
	public static void notNullOrEmpty(final Collection<?> collectionArg, final String label) throws GeneralCryptoLibException {

		notNull(collectionArg, label);

		if (collectionArg.isEmpty()) {
			throw new GeneralCryptoLibException(label + IS_EMPTY);
		}
	}

	/**
	 * Validates that a map argument is not null or empty.
	 *
	 * @param mapArg the map argument to validate.
	 * @param label  the label of the map argument, used for error messages.
	 * @throws GeneralCryptoLibException if the check fails.
	 */
	public static void notNullOrEmpty(final Map<?, ?> mapArg, final String label) throws GeneralCryptoLibException {

		notNull(mapArg, label);

		if (mapArg.isEmpty()) {
			throw new GeneralCryptoLibException(label + IS_EMPTY);
		}
	}

	/**
	 * Validates that an object array argument is not null  and that it contains no null elements.
	 *
	 * @param objectArrayArg the object array argument to validate.
	 * @param label          the label of the object array argument, used for error messages.
	 * @throws GeneralCryptoLibException if the check fails.
	 */
	public static void notNullAndNoNulls(final Object[] objectArrayArg, final String label) throws GeneralCryptoLibException {

		notNull(objectArrayArg, label);

		for (final Object element : objectArrayArg) {
			if (element == null) {
				throw new GeneralCryptoLibException(label + ONE_OR_MORE_NULL_ELEMENTS);
			}
		}
	}

	/**
	 * Validates that an object array argument is not null or empty and that it contains no null elements.
	 *
	 * @param objectArrayArg the object array argument to validate.
	 * @param label          the label of the object array argument, used for error messages.
	 * @throws GeneralCryptoLibException if the check fails.
	 */
	public static void notNullOrEmptyAndNoNulls(final Object[] objectArrayArg, final String label) throws GeneralCryptoLibException {

		notNull(objectArrayArg, label);

		if (objectArrayArg.length == 0) {
			throw new GeneralCryptoLibException(label + IS_EMPTY);
		}

		for (final Object element : objectArrayArg) {
			if (element == null) {
				throw new GeneralCryptoLibException(label + " contains one or more null elements.");
			}
		}
	}

	/**
	 * Validates that a collection argument is not null or empty and that it contains no null elements.
	 *
	 * @param collectionArg the collection argument to validate.
	 * @param label         the label of the collection argument, used for error messages.
	 * @throws GeneralCryptoLibException if the check fails.
	 */
	public static void notNullOrEmptyAndNoNulls(final Collection<?> collectionArg, final String label) throws GeneralCryptoLibException {

		notNull(collectionArg, label);

		if (collectionArg.isEmpty()) {
			throw new GeneralCryptoLibException(label + IS_EMPTY);
		}

		for (final Object element : collectionArg) {
			if (element == null) {
				throw new GeneralCryptoLibException(label + " contains one or more null elements.");
			}
		}
	}

	/**
	 * Validates that a map argument is not null or empty and that it contains no null values.
	 *
	 * @param mapArg the map argument to validate.
	 * @param label  the label of the map argument, used for error messages.
	 * @throws GeneralCryptoLibException if the check fails.
	 */
	public static void notNullOrEmptyAndNoNulls(final Map<?, ?> mapArg, final String label) throws GeneralCryptoLibException {

		notNull(mapArg, label);

		if (mapArg.isEmpty()) {
			throw new GeneralCryptoLibException(label + IS_EMPTY);
		}

		if (Collections.frequency(mapArg.values(), null) > 0) {
			throw new GeneralCryptoLibException(label + " contains one or more null values.");
		}
	}

	/**
	 * Validates that an int argument is greater than or equal to one.
	 *
	 * @param arg   the argument to validate.
	 * @param label the label of the int argument, used for error messages.
	 * @throws GeneralCryptoLibException if the check fails.
	 */
	public static void isPositive(final int arg, final String label) throws GeneralCryptoLibException {

		if (arg <= 0) {
			throw new GeneralCryptoLibException(label + " must be a positive integer; Found " + arg);
		}
	}

	/**
	 * Validates that an int argument is equal to a specified value.
	 *
	 * @param arg        the argument to validate.
	 * @param value      the value to compare to.
	 * @param argLabel   the label of the argument to validate, used for error messages.
	 * @param valueLabel the label of the value to compare to, used for error messages (can be an empty string).
	 * @throws GeneralCryptoLibException if the check fails.
	 */
	public static void isEqual(final int arg, final int value, final String argLabel, final String valueLabel) throws GeneralCryptoLibException {

		if (arg != value) {
			throw new GeneralCryptoLibException(argLabel + " must be equal to " + valueLabel + ": " + value + FOUND + arg);
		}
	}

	/**
	 * Validates that an Object argument is equal to a specified value.
	 *
	 * @param arg        the argument to validate.
	 * @param value      the value to compare to.
	 * @param argLabel   the label of the argument to validate, used for error messages.
	 * @param valueLabel the label of the value to compare to, used for error messages (can be an empty string).
	 * @throws GeneralCryptoLibException if the check fails.
	 */
	public static void isEqual(final Object arg, final Object value, final String argLabel, final String valueLabel)
			throws GeneralCryptoLibException {

		if (!arg.equals(value)) {
			throw new GeneralCryptoLibException(argLabel + " must be equal to " + valueLabel + ": " + value + FOUND + arg);
		}
	}

	/**
	 * Validates that an int argument is not less than a specified minimum value.
	 *
	 * @param arg           the argument to validate.
	 * @param minValue      the minimum allowed value, inclusive.
	 * @param argLabel      the label of the argument, used for error messages.
	 * @param minValueLabel the label of the minimum allowed value, used for error messages (can be an empty string).
	 * @throws GeneralCryptoLibException if the check fails.
	 */
	public static void notLessThan(final int arg, final int minValue, final String argLabel, final String minValueLabel)
			throws GeneralCryptoLibException {

		if (arg < minValue) {
			throw new GeneralCryptoLibException(argLabel + MUST_BE_GREATER_THAN_OR_EQUAL_TO + minValueLabel + ": " + minValue + FOUND + arg);
		}
	}

	/**
	 * Validates that a BigInteger argument is not less than a specified minimum value.
	 *
	 * @param arg           the argument to validate.
	 * @param minValue      the minimum allowed value, inclusive.
	 * @param argLabel      the label of the argument, used for error messages.
	 * @param minValueLabel the label of the minimum allowed value, used for error messages (can be an empty string).
	 * @throws GeneralCryptoLibException if the check fails.
	 */
	public static void notLessThan(final BigInteger arg, final BigInteger minValue, final String argLabel, final String minValueLabel)
			throws GeneralCryptoLibException {

		if (arg.compareTo(minValue) < 0) {
			throw new GeneralCryptoLibException(argLabel + MUST_BE_GREATER_THAN_OR_EQUAL_TO + minValueLabel + ": " + minValue + FOUND + arg);
		}
	}

	/**
	 * Validates that an int argument is not greater than a specified maximum value.
	 *
	 * @param arg           the argument to validate.
	 * @param maxValue      the maximum allowed value, inclusive.
	 * @param argLabel      the label of the argument, used for error messages.
	 * @param maxValueLabel the label of the maximum allowed value, used for error messages (can be an empty string).
	 * @throws GeneralCryptoLibException if the check fails.
	 */
	public static void notGreaterThan(final int arg, final int maxValue, final String argLabel, final String maxValueLabel)
			throws GeneralCryptoLibException {

		if (arg > maxValue) {
			throw new GeneralCryptoLibException(argLabel + MUST_BE_LESS_THAN_OR_EQUAL_TO + maxValueLabel + ": " + maxValue + FOUND + arg);
		}
	}

	/**
	 * Validates that a BigInteger argument is not greater than a specified maximum value.
	 *
	 * @param arg           the argument to validate.
	 * @param maxValue      the maximum allowed value, inclusive.
	 * @param argLabel      the label of the argument, used for error messages.
	 * @param maxValueLabel the label of the maximum allowed value, used for error messages (can be an empty string).
	 * @throws GeneralCryptoLibException if the check fails.
	 */
	public static void notGreaterThan(final BigInteger arg, final BigInteger maxValue, final String argLabel, final String maxValueLabel)
			throws GeneralCryptoLibException {

		if (arg.compareTo(maxValue) > 0) {
			throw new GeneralCryptoLibException(argLabel + MUST_BE_LESS_THAN_OR_EQUAL_TO + maxValueLabel + ": " + maxValue + FOUND + arg);
		}
	}

	/**
	 * Validates that an int argument is within a specified range.
	 *
	 * @param arg           the argument to validate.
	 * @param minValue      the minimum allowed value, inclusive.
	 * @param maxValue      the maximum allowed value, inclusive.
	 * @param argLabel      the label of the argument, used for error messages.
	 * @param minValueLabel the label of the minimum allowed value, used for error messages (can be an empty string).
	 * @param maxValueLabel the label of the maximum allowed value, used for error messages (can be an empty string).
	 * @throws GeneralCryptoLibException if the check fails.
	 */
	public static void inRange(final int arg, final int minValue, final int maxValue, final String argLabel, final String minValueLabel,
			final String maxValueLabel) throws GeneralCryptoLibException {

		if (arg < minValue) {
			throw new GeneralCryptoLibException(argLabel + MUST_BE_GREATER_THAN_OR_EQUAL_TO + minValueLabel + ": " + minValue + FOUND + arg);
		}

		if (arg > maxValue) {
			throw new GeneralCryptoLibException(argLabel + MUST_BE_LESS_THAN_OR_EQUAL_TO + maxValueLabel + ": " + maxValue + FOUND + arg);
		}
	}

	/**
	 * Validates that an BigInteger argument is within a specified range.
	 *
	 * @param arg           the argument to validate.
	 * @param minValue      the minimum allowed value, inclusive.
	 * @param maxValue      the maximum allowed value, inclusive.
	 * @param argLabel      the label of the argument, used for error messages.
	 * @param minValueLabel the label of the minimum allowed value, used for error messages (can be an empty string).
	 * @param maxValueLabel the label of the maximum allowed value, used for error messages (can be an empty string).
	 * @throws GeneralCryptoLibException if the check fails.
	 */
	public static void inRange(final BigInteger arg, final BigInteger minValue, final BigInteger maxValue, final String argLabel,
			final String minValueLabel, final String maxValueLabel) throws GeneralCryptoLibException {

		if (arg.compareTo(minValue) < 0) {
			throw new GeneralCryptoLibException(argLabel + MUST_BE_GREATER_THAN_OR_EQUAL_TO + minValueLabel + ": " + minValue + FOUND + arg);
		}

		if (arg.compareTo(maxValue) > 0) {
			throw new GeneralCryptoLibException(argLabel + MUST_BE_LESS_THAN_OR_EQUAL_TO + maxValueLabel + ": " + maxValue + FOUND + arg);
		}
	}

	/**
	 * Validates that a string argument contains only characters in a specified set.
	 *
	 * @param stringArg    the string argument to validate.
	 * @param allowedChars the set of allowed characters.
	 * @param label        the label of the string argument, used for error messages.
	 * @throws GeneralCryptoLibException if the check fails.
	 */
	public static void onlyContains(final String stringArg, final String allowedChars, final String label) throws GeneralCryptoLibException {
		if (stringArg == null || stringArg.isEmpty()) {
			return;
		}
		if (allowedChars == null || allowedChars.isEmpty()) {
			throw new GeneralCryptoLibException(label + CONTAINS_CHARACTERS_OUTSIDE_OF_ALLOWED_SET + allowedChars);
		}
		final char[] allowed = allowedChars.toCharArray();
		sort(allowed);
		for (int i = 0; i < stringArg.length(); i++) {
			if (binarySearch(allowed, stringArg.charAt(i)) < 0) {
				throw new GeneralCryptoLibException(label + CONTAINS_CHARACTERS_OUTSIDE_OF_ALLOWED_SET + allowedChars);
			}
		}
	}

	/**
	 * Validates that a string argument contains only characters in a specified set, represented as a pattern.
	 *
	 * @param stringArg           the string argument to validate.
	 * @param allowedCharsPattern the set of allowed characters, represented as a pattern.
	 * @param label               the label of the string argument, used for error messages.
	 * @throws GeneralCryptoLibException if the check fails.
	 */
	public static void onlyContains(final String stringArg, final Pattern allowedCharsPattern, final String label) throws GeneralCryptoLibException {

		final Matcher matcher = allowedCharsPattern.matcher(stringArg);

		if (!matcher.matches()) {
			throw new GeneralCryptoLibException(label + CONTAINS_CHARACTERS_OUTSIDE_OF_ALLOWED_SET + allowedCharsPattern.pattern());
		}
	}

	/**
	 * Validates that a string argument contains only ASCII printable characters.
	 *
	 * @param stringArg the string argument to validate.
	 * @param label     the label of the string argument, used for error messages.
	 * @throws GeneralCryptoLibException if the check fails.
	 */
	public static void isAsciiPrintable(final String stringArg, final String label) throws GeneralCryptoLibException {
		if (stringArg == null) {
			throw new GeneralCryptoLibException(label + " is not ASCII printable.");
		}
		for (int i = 0; i < stringArg.length(); i++) {
			final char c = stringArg.charAt(i);
			if (c < ASCII_LOWER_BOUND || c >= ASCII_UPPER_BOUND) {
				throw new GeneralCryptoLibException(label + " contains characters that are not ASCII printable.");
			}
		}
	}

	/**
	 * Validates that a date argument is before a specified later date.
	 *
	 * @param dateArg        the date argument to validate.
	 * @param laterDate      the later date.
	 * @param dateArgLabel   the label of the date argument, used for error messages.
	 * @param laterDateLabel the label of the later date, used for error messages (can be an empty string).
	 * @throws GeneralCryptoLibException if the check fails.
	 */
	public static void isBefore(final Date dateArg, final Date laterDate, final String dateArgLabel, final String laterDateLabel)
			throws GeneralCryptoLibException {

		if (!dateArg.before(laterDate)) {
			throw new GeneralCryptoLibException(dateArgLabel + " " + dateArg + " is not before " + laterDateLabel + " " + laterDate.toString());
		}
	}

	/**
	 * Validates that a date argument is after a specified earlier date.
	 *
	 * @param dateArg          the date argument to validate.
	 * @param earlierDate      the earlier date.
	 * @param dateArgLabel     the label of the date argument, used for error messages.
	 * @param earlierDateLabel the label of the earlier date, used for error messages (can be an empty string).
	 * @throws GeneralCryptoLibException if the check fails.
	 */
	public static void isAfter(final Date dateArg, final Date earlierDate, final String dateArgLabel, final String earlierDateLabel)
			throws GeneralCryptoLibException {

		if (!dateArg.after(earlierDate)) {
			throw new GeneralCryptoLibException(dateArgLabel + " " + dateArg + " is not after " + earlierDateLabel + " " + earlierDate.toString());
		}
	}

	/**
	 * Validates that a date argument is not before a specified earlier date.
	 *
	 * @param dateArg          the date argument to validate.
	 * @param earlierDate      the earlier date.
	 * @param dateArgLabel     the label of the date argument, used for error messages.
	 * @param earlierDateLabel the label of the earlier date, used for error messages (can be an empty string).
	 * @throws GeneralCryptoLibException if the check fails.
	 */
	public static void notBefore(final Date dateArg, final Date earlierDate, final String dateArgLabel, final String earlierDateLabel)
			throws GeneralCryptoLibException {

		if (dateArg.before(earlierDate)) {
			throw new GeneralCryptoLibException(dateArgLabel + " " + dateArg + " is before " + earlierDateLabel + " " + earlierDate.toString());
		}
	}

	/**
	 * Validates that a date argument is not after a specified later date.
	 *
	 * @param dateArg        the date argument to validate.
	 * @param laterDate      the later date.
	 * @param dateArgLabel   the label of the date argument, used for error messages.
	 * @param laterDateLabel the label of the later date, used for error messages (can be an empty string).
	 * @throws GeneralCryptoLibException if the check fails.
	 */
	public static void notAfter(final Date dateArg, final Date laterDate, final String dateArgLabel, final String laterDateLabel)
			throws GeneralCryptoLibException {

		if (dateArg.after(laterDate)) {
			throw new GeneralCryptoLibException(dateArgLabel + " " + dateArg + " is after " + laterDateLabel + " " + laterDate.toString());
		}
	}

	/**
	 * Validate that the given string matches the expected custom UUID format.
	 *
	 * @param uuidString The string id to validate.
	 * @return the <code>uuidString</code> if it has successfully validated. Otherwise an exception is thrown.
	 * @throws IllegalArgumentException if the <code>uuidString</code> does not successfully validated.
	 */
	public static String validateUUID(final String uuidString) {
		if (uuidString == null || !UUID_PATTERN.matcher(uuidString).matches()) {
			throw new IllegalArgumentException(
					String.format("The given UUIDs must comply with the required UUID format (%s) and must not be null.", UUID_PATTERN));
		}
		return uuidString;
	}
}
