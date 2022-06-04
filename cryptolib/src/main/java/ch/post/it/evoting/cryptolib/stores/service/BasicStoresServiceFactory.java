/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.stores.service;

import ch.post.it.evoting.cryptolib.api.stores.StoresServiceAPI;
import ch.post.it.evoting.cryptolib.commons.concurrent.BaseFactory;

/**
 * Factory of {@link StoresService} objects which are non thread-safe.
 */
public class BasicStoresServiceFactory extends BaseFactory<StoresServiceAPI> {
	/**
	 * Default constructor. This factory will create services configured with default values.
	 */
	public BasicStoresServiceFactory() {
		super(StoresServiceAPI.class);
	}

	@Override
	public StoresServiceAPI create() {
		return new StoresService();
	}
}
