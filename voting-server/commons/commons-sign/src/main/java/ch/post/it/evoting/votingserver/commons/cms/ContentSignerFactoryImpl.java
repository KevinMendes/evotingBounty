/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.cms;

import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;

import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.operator.ContentSigner;

import ch.post.it.evoting.votingserver.commons.signature.SignatureFactory;

/**
 * Implementation of {@link ContentSignerFactory}.
 */
class ContentSignerFactoryImpl implements ContentSignerFactory {
	private final SignatureFactory factory;

	private final AlgorithmIdentifier identifier;

	private final SecureRandom prng;

	/**
	 * Constructor.
	 *
	 * @param factory
	 * @param identifier
	 */
	public ContentSignerFactoryImpl(final SignatureFactory factory, final AlgorithmIdentifier identifier, SecureRandom prng) {
		this.factory = factory;
		this.identifier = identifier;
		this.prng = prng;
	}

	@Override
	public ContentSigner newContentSigner(final PrivateKey key) throws InvalidKeyException {
		Signature signature = factory.newSignature();
		signature.initSign(key, prng);
		return new ContentSignerImpl(signature, identifier);
	}
}
