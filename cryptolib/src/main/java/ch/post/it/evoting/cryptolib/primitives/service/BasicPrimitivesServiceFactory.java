/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.primitives.service;

import ch.post.it.evoting.cryptolib.api.primitives.PrimitivesServiceAPI;
import ch.post.it.evoting.cryptolib.commons.concurrent.BaseFactory;

/**
 * Factory of {@link PrimitivesService} objects which are non thread-safe.
 */
class BasicPrimitivesServiceFactory extends BaseFactory<PrimitivesServiceAPI> {
	/**
	 * This factory will create services configured with default values.
	 */
	public BasicPrimitivesServiceFactory() {
		super(PrimitivesServiceAPI.class);
	}

	@Override
	public PrimitivesServiceAPI create() {
		return new PrimitivesService();
	}
}
