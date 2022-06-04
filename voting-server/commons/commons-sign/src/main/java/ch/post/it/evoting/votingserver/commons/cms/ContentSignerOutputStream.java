/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.cms;

import java.io.IOException;
import java.io.OutputStream;
import java.security.Signature;
import java.security.SignatureException;

/**
 * Implementation of {@link OutputStream} stream used by {@link ContentSignerImpl}.
 */
class ContentSignerOutputStream extends OutputStream {
	private final Signature signature;

	/**
	 * Constructor.
	 *
	 * @param signature
	 */
	public ContentSignerOutputStream(final Signature signature) {
		this.signature = signature;
	}

	@Override
	public void write(final int b) throws IOException {
		try {
			signature.update((byte) b);
		} catch (SignatureException e) {
			throw new IOException("Failed to update signature.", e);
		}
	}
}
