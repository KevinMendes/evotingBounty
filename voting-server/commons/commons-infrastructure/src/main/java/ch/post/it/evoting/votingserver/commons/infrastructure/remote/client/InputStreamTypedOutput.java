/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.infrastructure.remote.client;

import static java.text.MessageFormat.format;
import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.IOUtils;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import okio.BufferedSource;
import okio.Okio;
import okio.Source;

/**
 * Implementation of {@link TypedInput} which wraps a given input stream with provided mime type and length.
 * <p>
 * Implementation has the following features:
 * <ul>
 * <li>The supplied {@link InputStream} can be read only once, so instances of this class are not
 * reusable.</li>
 * <li>{@link #length()} defines the number of bytes to be copied to the destination
 * {@link OutputStream} in {@link #writeTo(OutputStream)}. If it equals to {@code -1} then all the
 * bytes are copied.</li>
 * <li>{@link #fileName()} is optional and can be any string specified by client.</li>
 * </ul>
 */
public final class InputStreamTypedOutput extends RequestBody {
	private final String mimeType;

	private final InputStream in;

	private final long length;

	private final String fileName;

	/**
	 * Constructor. This is a shortcut for {@code TypedInputStream(mimeType, in, -1)}. Client is responsible for closing the supplied stream.
	 *
	 * @param mimeType the mime type
	 * @param in       the stream
	 */
	public InputStreamTypedOutput(final String mimeType, final InputStream in) {
		this(mimeType, in, -1, null);
	}

	/**
	 * Constructor. This is a shortcut for {@code TypedInputStream(mimeType, in, length, null)}. Client is responsible for closing the supplied
	 * stream.
	 *
	 * @param mimeType the mime type
	 * @param in       the stream
	 * @param length   the length or {@code -1} if unknown, must be great or equal to {@code -1}.
	 */
	public InputStreamTypedOutput(final String mimeType, final InputStream in, final long length) {
		this(mimeType, in, length, null);
	}

	/**
	 * Constructor. Client is responsible for closing the supplied stream.
	 *
	 * @param mimeType the mime type
	 * @param in       the stream
	 * @param length   the length or {@code -1} if unknown, must be great or equal to {@code -1}.
	 * @param fileName the file name or {@code null} is unknown.
	 */
	public InputStreamTypedOutput(final String mimeType, final InputStream in, final long length, final String fileName) {
		requireNonNull(mimeType, "Mime type is null.");
		requireNonNull(in, "Stream is null.");
		if (length < -1) {
			throw new IllegalArgumentException(format("Invalid length ''{0}''", length));
		}
		this.mimeType = mimeType;
		this.in = in;
		this.length = length;
		this.fileName = fileName;
	}

	/**
	 * Constructor. This is a shortcut for {@code TypedInputStream(mimeType, in, -1, fileName)}. Client is responsible for closing the supplied
	 * stream.
	 *
	 * @param mimeType the mime type
	 * @param in       the stream
	 * @param fileName the file name or {@code null} is unknown.
	 */
	public InputStreamTypedOutput(final String mimeType, final InputStream in, final String fileName) {
		this(mimeType, in, -1, fileName);
	}

	public String fileName() {
		return fileName;
	}

	public void writeTo(final OutputStream out) throws IOException {
		if (length == -1) {
			IOUtils.copyLarge(in, out);
		} else {
			IOUtils.copyLarge(in, out, 0, length);
		}
	}

	@Override
	public MediaType contentType() {
		return MediaType.parse(mimeType);
	}

	@Override
	public long contentLength() {
		return length;
	}

	public BufferedSource source() {
		return Okio.buffer(Okio.source(in));
	}

	@Override
	public void writeTo(final BufferedSink sink) throws IOException {
		try (final Source source = Okio.source(in)) {
			sink.writeAll(source);
		}
	}
}
