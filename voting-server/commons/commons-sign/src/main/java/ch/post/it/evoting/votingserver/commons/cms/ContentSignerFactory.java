/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.cms;

import java.security.InvalidKeyException;
import java.security.PrivateKey;

import org.bouncycastle.operator.ContentSigner;

/**
 * Factory of {@link ContentSigner}.
 * <p>
 * Implementation must be thread-safe.
 */
interface ContentSignerFactory {
	/**
	 * Creates a new {@link ContentSigner} instance for a given private key.
	 *
	 * @param key the key
	 * @return the instance.
	 * @throws InvalidKeyException the key is invalid.
	 */
	ContentSigner newContentSigner(PrivateKey key) throws InvalidKeyException;
}
