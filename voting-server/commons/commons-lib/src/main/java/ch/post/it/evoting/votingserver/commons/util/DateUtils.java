/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.util;

import org.apache.commons.lang3.StringUtils;

/**
 * The util class to handle dates.
 */
public final class DateUtils {

	private static final int TIMESTAMP_LENGTH = 13;

	private static final String PAD_CHARACTER = "0";

	/**
	 * Non-public constructor
	 */
	private DateUtils() {

	}

	/**
	 * Returns the timestamp in miliseconds.
	 *
	 * @return timestamp.
	 */
	public static String getTimestamp() {
		Long miliseconds = System.currentTimeMillis();
		return StringUtils.leftPad(String.valueOf(miliseconds), TIMESTAMP_LENGTH, PAD_CHARACTER);
	}
}
