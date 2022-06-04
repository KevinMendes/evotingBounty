/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.util;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Test;

public class ZipUtilsTest {

	@Test
	public void whenZipAndUnzipShortStringThenOk() throws IOException {

		String testString = "This is a short string";

		byte[] zippedBytes = ZipUtils.zipText(testString);

		String unzipper = ZipUtils.unzip(zippedBytes);

		assertEquals(testString, unzipper);
	}

	@Test
	public void whenZipAndUnzipLongStringThenOk() throws IOException {

		String testString = generateString(1000);

		byte[] zippedBytes = ZipUtils.zipText(testString);

		String unzipper = ZipUtils.unzip(zippedBytes);

		assertEquals(testString, unzipper);
	}

	private String generateString(int numRequiredCharacters) {

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < numRequiredCharacters; i++) {
			sb.append("a");
		}
		return sb.toString();
	}
}
