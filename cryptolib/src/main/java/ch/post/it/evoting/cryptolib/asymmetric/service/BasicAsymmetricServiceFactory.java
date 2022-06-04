/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.asymmetric.service;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.commons.concurrent.BaseFactory;

/**
 * Factory of {@link AsymmetricService} objects which are non thread-safe.
 */
public class BasicAsymmetricServiceFactory extends BaseFactory<AsymmetricServiceAPI> {

	/**
	 * This factory will create services configured with default values.
	 */
	public BasicAsymmetricServiceFactory() {
		super(AsymmetricServiceAPI.class);
	}

	@Override
	public AsymmetricServiceAPI create() {
		return new AsymmetricService();
	}
}
