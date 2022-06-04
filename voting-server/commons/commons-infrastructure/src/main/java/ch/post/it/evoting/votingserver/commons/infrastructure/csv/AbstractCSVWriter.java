/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.infrastructure.csv;

import java.io.Closeable;
import java.io.Flushable;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;

import com.opencsv.CSVWriter;
import com.opencsv.ICSVWriter;

/**
 * Abstract CSV writer for a generic Class
 *
 * @param <T>
 */
public abstract class AbstractCSVWriter<T> implements Closeable, Flushable {

	protected CSVWriter csvWriter;

	/**
	 * Creates a CSVWriter for a given outputStream
	 *
	 * @param outputStream
	 */
	protected AbstractCSVWriter(final OutputStream outputStream) {
		this(outputStream, CSVConstants.DEFAULT_CHARSET, CSVConstants.DEFAULT_SEPARATOR, CSVConstants.NO_QUOTE_CHARACTER,
				CSVConstants.NO_ESCAPE_CHARACTER);
	}

	/**
	 * Creates a CSVWriter for the given parameters
	 *
	 * @param outputStream
	 * @param charset      - accepted charset
	 * @param separator    - separator that splits into columns
	 * @param quote
	 */
	protected AbstractCSVWriter(final OutputStream outputStream, final Charset charset, final char separator, final char quote, final char escape) {
		createCSVWriter(outputStream, charset, separator, quote, escape);
	}

	/**
	 * @param outputStream
	 * @param charset      - accepted charset
	 * @param separator    - separator that splits into columns
	 * @param quote
	 */
	protected void createCSVWriter(final OutputStream outputStream, final Charset charset, final char separator, final char quote,
			final char escape) {
		csvWriter = new CSVWriter(new OutputStreamWriter(outputStream, charset), separator, quote, escape, ICSVWriter.DEFAULT_LINE_END);
	}

	/**
	 * Write an object in the csv
	 *
	 * @param object
	 */
	public void write(final T object) {
		final String[] line = extractValues(object);
		csvWriter.writeNext(line, false);
	}

	/**
	 * Extracts the values of an object into an array
	 *
	 * @param object
	 * @return
	 */
	protected abstract String[] extractValues(T object);

	/**
	 * Closes the writer
	 *
	 * @throws IOException
	 */
	@Override
	public void close() throws IOException {
		// close already flushes
		csvWriter.close();
	}

	/**
	 * Flush the content in the writer
	 *
	 * @throws IOException
	 */
	@Override
	public void flush() throws IOException {
		csvWriter.flush();
	}

}
