/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.symmetric.service;

import ch.post.it.evoting.cryptolib.api.symmetric.SymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.commons.concurrent.BaseFactory;

/**
 * Factory of {@link SymmetricService} objects which are non thread-safe.
 */
public class BasicSymmetricServiceFactory extends BaseFactory<SymmetricServiceAPI> {
	/**
	 * Creates an instance of factory that will create services configured with default values.
	 */
	public BasicSymmetricServiceFactory() {
		super(SymmetricServiceAPI.class);
	}

	@Override
	public SymmetricServiceAPI create() {
		return new SymmetricService();
	}
}
