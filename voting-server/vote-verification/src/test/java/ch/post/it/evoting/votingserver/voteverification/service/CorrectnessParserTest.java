/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.voteverification.service;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Test;

public class CorrectnessParserTest {

	private final CorrectnessParser target = new CorrectnessParser();

	@Test
	public void handleEmptyArray() {

		String correctness = "[]";

		List<List<String>> result = target.parse(correctness);
		assertEquals(0, result.size());
	}

	@Test
	public void handleArrayWithEmptyArrayInside() {

		String correctness = "[[]]";

		List<List<String>> result = target.parse(correctness);
		assertEquals(1, result.size());
		assertEquals("", String.join("", result.get(0)));
	}

	@Test
	public void parseCorrectlyTwoEmptyArrays() {

		String correctness = "[[],[]]";

		List<List<String>> result = target.parse(correctness);
		assertEquals(2, result.size());
		assertEquals("", String.join("", result.get(0)));
		assertEquals("", String.join("", result.get(1)));
	}

	@Test
	public void parseCorrectlyArrayWithOneValue() {

		String correctness = "[[\"7113d24170be46dd89193242c5481890\"]]";

		List<List<String>> result = target.parse(correctness);
		assertEquals(1, result.size());
		assertEquals("7113d24170be46dd89193242c5481890", String.join("", result.get(0)));
	}

	@Test
	public void parseCorrectlyArrayMultipleValues() {

		String correctness = "[[\"7113d24170be46dd89193242c5481890\",\"96d56bca2c814a3db28fadcec602636d\",\"b917f19979454bad93899aac1428967d\"]]";

		List<List<String>> result = target.parse(correctness);
		assertEquals(1, result.size());
		assertEquals("7113d24170be46dd89193242c548189096d56bca2c814a3db28fadcec602636db917f19979454bad93899aac1428967d",
				String.join("", result.get(0)));
	}

	@Test
	public void parseCorrectlyArrayMultipleValuesWithSpaces() {

		String correctness = "[[\"7113d24170be46dd89193242c5481890\", \"96d56bca2c814a3db28fadcec602636d\", \"b917f19979454bad93899aac1428967d\"]]";

		List<List<String>> result = target.parse(correctness);
		assertEquals(1, result.size());
		assertEquals("7113d24170be46dd89193242c548189096d56bca2c814a3db28fadcec602636db917f19979454bad93899aac1428967d",
				String.join("", result.get(0)));
	}

	@Test
	public void parseCorrectlyArrayOfArrays() {

		String correctness = "[[\"6cdc8d05ebd54d4491f1413833949fbe\"],[],[\"7113d24170be46dd89193242c5481890\",\"96d56bca2c814a3db28fadcec602636d\","
				+ "\"b917f19979454bad93899aac1428967d\"],[\"7113d24170be46dd89193242c5481890\",\"96d56bca2c814a3db28fadcec602636d\","
				+ "\"b917f19979454bad93899aac1428967d\"],[\"7113d24170be46dd89193242c5481890\",\"96d56bca2c814a3db28fadcec602636d\","
				+ "\"b917f19979454bad93899aac1428967d\"],[],[\"f78608e9f7a44fd688ee4469197572ab\",\"7760d60ad66244b79129d960774ce2f4\","
				+ "\"1cdddc29af114058a60182f29937ed2e\"]]";

		List<List<String>> result = target.parse(correctness);
		assertEquals(7, result.size());
		assertEquals("6cdc8d05ebd54d4491f1413833949fbe", String.join("", result.get(0)));
		assertEquals("", String.join("", result.get(1)));
		assertEquals("7113d24170be46dd89193242c548189096d56bca2c814a3db28fadcec602636db917f19979454bad93899aac1428967d",
				String.join("", result.get(2)));
		assertEquals("7113d24170be46dd89193242c548189096d56bca2c814a3db28fadcec602636db917f19979454bad93899aac1428967d",
				String.join("", result.get(3)));
		assertEquals("7113d24170be46dd89193242c548189096d56bca2c814a3db28fadcec602636db917f19979454bad93899aac1428967d",
				String.join("", result.get(4)));
		assertEquals("", String.join("", result.get(5)));
		assertEquals("f78608e9f7a44fd688ee4469197572ab7760d60ad66244b79129d960774ce2f41cdddc29af114058a60182f29937ed2e",
				String.join("", result.get(6)));
	}

	@Test
	public void parseCorrectlyArrayOfArraysConsecutativeEmptyArrays() {

		String correctness =
				"[[],[],[\"7113d24170be46dd89193242c5481890\",\"96d56bca2c814a3db28fadcec602636d\"," + "\"b917f19979454bad93899aac1428967d\"],[]]";

		List<List<String>> result = target.parse(correctness);
		assertEquals(4, result.size());
		assertEquals("", String.join("", result.get(0)));
		assertEquals("", String.join("", result.get(1)));
		assertEquals("7113d24170be46dd89193242c548189096d56bca2c814a3db28fadcec602636db917f19979454bad93899aac1428967d",
				String.join("", result.get(2)));
		assertEquals("", String.join("", result.get(3)));
	}
}
