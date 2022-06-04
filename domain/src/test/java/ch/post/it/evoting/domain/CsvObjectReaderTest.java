/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain;

import static java.nio.file.Files.newBufferedReader;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.MappingIterator;

import ch.post.it.evoting.domain.csv.CsvObjectReader;

/**
 * Tests of {@link CsvObjectReader}.
 */
class CsvObjectReaderTest {
	private static final String CSV = "A,1";
	private Foo expected;

	@BeforeEach
	void setUp() {
		expected = new Foo();
		expected.setName("name");
		expected.setValue(1);
		expected.setIgnored(true);
	}

	@Test
	void testReadCsvWithSemicolonSeparator() throws IOException {
		final File testCsvFile = new File(
				URLDecoder.decode(this.getClass().getClassLoader().getResource("csvDataTest.csv").getPath(), StandardCharsets.UTF_8.toString()));
		final Path path = Paths.get(testCsvFile.toURI());
		try (final Reader reader = newBufferedReader(path, StandardCharsets.UTF_8);
				final MappingIterator<FooSecond> iterator = CsvObjectReader.readCsv(reader, FooSecond.class, ';', "name", "value")) {
			assertTrue(iterator.hasNext());
			final FooSecond fooSecond = iterator.next();
			assertEquals("21b0ed864457423da108cba4483cc469", fooSecond.getName());
			assertEquals("[\"eyJ6cEdyb3VwRWxl\",\"eyJ6cEdyb3VwRWxlbWV\"]", fooSecond.getValue());
			assertTrue(iterator.hasNext());
		}
	}

	@Test
	void testReadCsvReaderClassOfTStrings() throws IOException {
		try (final Reader stream = new StringReader(CSV); final MappingIterator<Foo> iterator = CsvObjectReader.readCsv(stream, Foo.class, "name", "value")) {
			assertTrue(iterator.hasNext());
			final Foo foo = iterator.next();
			assertEquals("A", foo.getName());
			assertEquals(1, foo.getValue());
			assertFalse(foo.isIgnored());
			assertFalse(iterator.hasNext());
		}
	}

	public static final class Foo {
		private String name;

		private int value;

		private boolean ignored;

		public String getName() {
			return name;
		}

		public void setName(final String name) {
			this.name = name;
		}

		public int getValue() {
			return value;
		}

		public void setValue(final int value) {
			this.value = value;
		}

		@JsonIgnore
		public boolean isIgnored() {
			return ignored;
		}

		public void setIgnored(final boolean ignored) {
			this.ignored = ignored;
		}
	}

	private static final class FooSecond {
		private String name;

		private String value;

		public String getName() {
			return name;
		}

		public void setName(final String name) {
			this.name = name;
		}

		public String getValue() {
			return value;
		}

		public void setValue(final String value) {
			this.value = value;
		}
	}
}
