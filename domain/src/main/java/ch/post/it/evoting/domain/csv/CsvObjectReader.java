/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.domain.csv;

import java.io.IOException;
import java.io.Reader;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;

public final class CsvObjectReader {

	private CsvObjectReader() {
	}

	/**
	 * Reads CSV values from a given reader according the specified class and column names. Client is responsible for closing the returned iterator.
	 * Client is responsible for closing the supplied reader. This method is a shortcut for the following code:
	 *
	 * <pre>
	 * <code>
	 * CsvMapper mapper = new CsvMapper();
	 * CsvSchema schema = mapper.schemaFor(valueClass).sortedBy(columnNames);
	 * mapper.reader(valueClass).with(schema).readValues(reader);
	 * </code>
	 * </pre>
	 *
	 * @param reader      the reader
	 * @param valueClass  the value class
	 * @param columnNames the column names
	 * @return the iterator of values
	 * @throws JsonProcessingException processing failure
	 * @throws IOException             I/O error occurred.
	 */
	public static <T> MappingIterator<T> readCsv(final Reader reader, final Class<T> valueClass, final String... columnNames) throws IOException {
		return newCsvObjectReader(valueClass, CsvSchema.DEFAULT_COLUMN_SEPARATOR, columnNames).readValues(reader);
	}

	/**
	 * Reads CSV values from a given reader according the specified class, column separator and column names. Client is responsible for closing the
	 * returned iterator. Client is responsible for closing the supplied reader. This method is a shortcut for the following code:
	 *
	 * <pre>
	 * <code>
	 * CsvMapper mapper = new CsvMapper();
	 * CsvSchema schema = mapper.schemaFor(valueClass).sortedBy(columnNames);
	 * mapper.reader(valueClass).with(schema).readValues(reader);
	 * </code>
	 * </pre>
	 *
	 * @param reader          the reader
	 * @param valueClass      the value class
	 * @param columnSeparator the column separator of the file
	 * @param columnNames     the column names
	 * @return the iterator of values
	 * @throws JsonProcessingException processing failure
	 * @throws IOException             I/O error occurred.
	 */
	public static <T> MappingIterator<T> readCsv(final Reader reader, final Class<T> valueClass, final char columnSeparator,
			final String... columnNames) throws IOException {
		return newCsvObjectReader(valueClass, columnSeparator, columnNames).readValues(reader);
	}

	private static ObjectReader newCsvObjectReader(final Class<?> valueClass, final char columnSeparator, final String... columnNames) {
		final CsvMapper mapper = new CsvMapper();
		CsvSchema schema = mapper.schemaFor(valueClass).withColumnSeparator(columnSeparator);
		if (columnNames.length > 0) {
			schema = schema.sortedBy(columnNames);
		}
		return mapper.readerFor(valueClass).with(schema);
	}

}
