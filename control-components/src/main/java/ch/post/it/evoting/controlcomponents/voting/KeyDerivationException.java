/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.voting;

public class KeyDerivationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public KeyDerivationException(final String verificationCardId) {
		super(String.format("The derivation of the private key has failed for verificationCardId %s", verificationCardId));
	}

}
