/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.signature;

import java.security.Signature;

/**
 * Factory of {@link Signature}.
 * <p>
 * This factory is introduced to make it possible working with {@link Signature} API and to control the used cryptography algorithms applying the
 * current {@code cryptolibPolicy.propeties}.
 * <p>
 * Implementation must be thread-safe.
 */
public interface SignatureFactory {
	/**
	 * Creates a new {@link Signature} instance
	 *
	 * @return the new instance.
	 */
	Signature newSignature();
}
