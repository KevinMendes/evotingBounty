/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.infrastructure;

import java.io.IOException;
import java.io.InputStream;

/**
 * Implementation of {@link InputStream} which combines a given sequence of {@link InputStream} to a
 * single on.
 * <p>
 * Implementation has the following features:
 * <ul>
 * <li>Method {@link #close()} closes all combined streams.</li>
 * <li>Method {@link #available()} always returns {@code 0}.</li>
 * <li>Marks are not supported.</li>
 * </ul>
 */
public final class CompositeInputStream extends InputStream {
	private final InputStream[] parts;

	private int index;

	public CompositeInputStream(final InputStream... parts) {
		this.parts = parts;
	}

	@Override
	public int read() throws IOException {
		int b = -1;
		while (b == -1 && index < parts.length) {
			b = parts[index].read();
			if (b == -1) {
				index++;
			}
		}
		return b;
	}

	@Override
	public void close() throws IOException {
		IOException exception = null;
		for (InputStream part : parts) {
			try {
				part.close();
			} catch (IOException e) {
				if (exception == null) {
					exception = e;
				} else {
					exception.addSuppressed(e);
				}
			}
		}
		if (exception != null) {
			throw exception;
		}
	}
}
