/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.cms;

import java.io.OutputStream;
import java.security.Signature;
import java.security.SignatureException;

import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.operator.ContentSigner;

/**
 * Implementation of {@link ContentSigner}.
 */
class ContentSignerImpl implements ContentSigner {
	private final Signature signature;

	private final AlgorithmIdentifier identifier;

	/**
	 * Constructor. The supplied signature must be initialized for signing.
	 *
	 * @param signature  the signature
	 * @param identifier the algorithm identifier.
	 */
	public ContentSignerImpl(final Signature signature, final AlgorithmIdentifier identifier) {
		this.signature = signature;
		this.identifier = identifier;
	}

	@Override
	public AlgorithmIdentifier getAlgorithmIdentifier() {
		return identifier;
	}

	@Override
	public OutputStream getOutputStream() {
		return new ContentSignerOutputStream(signature);
	}

	@Override
	public byte[] getSignature() {
		byte[] bytes;
		try {
			bytes = signature.sign();
		} catch (SignatureException e) {
			throw new IllegalStateException("Failed to get signature.", e);
		}
		return bytes;
	}
}
