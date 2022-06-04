/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.shares.shares.domain;

import java.security.PublicKey;

/**
 * Defines the context in which a read operation is to be performed.
 */
public final class ReadSharesOperationContext {

	private final PublicKey authoritiesPublicKey;

	private final PublicKey boardPublicKey;

	/**
	 * @param authoritiesPublicKey the public key corresponding to the private key with which the shares are signed.
	 * @param boardPublicKey       the board's public key, that will be used to extract parameters that will help the reconstruction of the private
	 *                             key.
	 */
	public ReadSharesOperationContext(final PublicKey authoritiesPublicKey, final PublicKey boardPublicKey) {
		this.authoritiesPublicKey = authoritiesPublicKey;
		this.boardPublicKey = boardPublicKey;
	}

	/**
	 * @return the authoritiesPublicKey.
	 */
	public PublicKey getAuthoritiesPublicKey() {
		return authoritiesPublicKey;
	}

	/**
	 * @return the boardPublicKey.
	 */
	public PublicKey getBoardPublicKey() {
		return boardPublicKey;
	}
}
