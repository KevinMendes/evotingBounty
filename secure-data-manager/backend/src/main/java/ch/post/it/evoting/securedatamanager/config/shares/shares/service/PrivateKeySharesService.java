/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.shares.shares.service;

import java.security.KeyException;
import java.security.PrivateKey;
import java.security.interfaces.RSAPrivateKey;
import java.util.ArrayList;
import java.util.List;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.secretsharing.Share;
import ch.post.it.evoting.cryptolib.api.secretsharing.ThresholdSecretSharingServiceAPI;
import ch.post.it.evoting.cryptolib.secretsharing.service.ThresholdSecretSharingService;
import ch.post.it.evoting.securedatamanager.config.shares.shares.keys.PrivateKeySerializer;

public final class PrivateKeySharesService {

	private final PrivateKeySerializer privateKeySerializer;

	public PrivateKeySharesService(final PrivateKeySerializer privateKeySerializer) {
		this.privateKeySerializer = privateKeySerializer;
	}

	public List<Share> split(final PrivateKey privateKey, final int n, final int threshold) throws KeyException {

		final RSAPrivateKey rsaPrivateKey = (RSAPrivateKey) privateKey;
		final byte[] secretForShamir = privateKeySerializer.serialize(privateKey);

		try {
			final ThresholdSecretSharingServiceAPI thresholdSecretSharingServiceAPI = new ThresholdSecretSharingService();
			return new ArrayList<>(
					thresholdSecretSharingServiceAPI.split(secretForShamir, n, threshold, rsaPrivateKey.getModulus().nextProbablePrime()));
		} catch (final GeneralCryptoLibException e) {
			throw new KeyException("An error occurred while trying to split the private key", e);
		}
	}
}
