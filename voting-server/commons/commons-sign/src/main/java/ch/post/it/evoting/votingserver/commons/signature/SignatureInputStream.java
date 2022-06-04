/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.signature;

import static java.util.Objects.requireNonNull;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Signature;
import java.security.SignatureException;

/**
 * Implementation of {@link InputStream} as a decorator which updates a given {@link Signature} while reading data from the underlying {@link
 * InputStream}.
 * <p>
 * A typical usage of the class looks like the following:
 *
 * <pre>
 * <code>
 * SignatureFactory factory = ...
 * Signature signature = factory.newSignature();
 * signature.initVerify(privateKey);
 * try(InputStream stream = new SignatureInputStream(..., signature)) {
 *     // read the stream
 *     ...
 * }
 * signature.verify(..);
 * </code>
 * </pre>
 * <p>
 * Buffering is not used so only the bytes read by {@code read} methods are read from the underlying stream and are used to update the supplied {@link
 * Signature}.
 */
public final class SignatureInputStream extends FilterInputStream {
	private final Signature signature;

	/**
	 * Constructor.
	 *
	 * @param in        the underlying input stream
	 * @param signature the signature
	 */
	public SignatureInputStream(final InputStream in, final Signature signature) {
		super(in);
		this.signature = requireNonNull(signature, "Signature is null.");
	}

	@Override
	public int read() throws IOException {
		int b = super.read();
		if (b != -1) {
			try {
				signature.update((byte) b);
			} catch (SignatureException e) {
				throw new IOException("Failed to update signature.", e);
			}
		}
		return b;
	}

	@Override
	public int read(final byte[] b, final int off, final int len) throws IOException {
		int bytesRead = super.read(b, off, len);
		if (bytesRead > 0) {
			try {
				signature.update(b, off, bytesRead);
			} catch (SignatureException e) {
				throw new IOException("Failed to update signature.", e);
			}
		}
		return bytesRead;
	}
}
