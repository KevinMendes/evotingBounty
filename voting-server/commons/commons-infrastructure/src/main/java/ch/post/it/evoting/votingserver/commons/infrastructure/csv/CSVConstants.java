/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.infrastructure.csv;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Constants to be used into Csv readers and writers
 */
public final class CSVConstants {

	public static final char SEMICOLON_SEPARATOR = ';';

	public static final char COMMA_SEPARATOR = ',';

	public static final char PIPE_SEPARATOR = '|';

	public static final char DEFAULT_SEPARATOR = PIPE_SEPARATOR;

	public static final char DEFAULT_QUOTE = '"';

	public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

	public static final char NO_QUOTE_CHARACTER = '\u0000';

	public static final char NO_ESCAPE_CHARACTER = '\u0000';

	private CSVConstants() {
	}
}
