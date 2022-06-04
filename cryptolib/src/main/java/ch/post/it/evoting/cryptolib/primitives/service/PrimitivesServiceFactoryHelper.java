/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.primitives.service;

import ch.post.it.evoting.cryptolib.api.primitives.PrimitivesServiceAPI;
import ch.post.it.evoting.cryptolib.api.services.ServiceFactory;
import ch.post.it.evoting.cryptolib.commons.concurrent.ServiceFactoryHelper;

/**
 * This class sets up default factories of {@link PrimitivesServiceAPI} objects.
 */
public final class PrimitivesServiceFactoryHelper {

	/**
	 * This is a helper class. It cannot be instantiated.
	 */
	private PrimitivesServiceFactoryHelper() {
	}

	/**
	 * Retrieves a new factory of non thread-safe services.
	 *
	 * <p>Default factory is {@link BasicPrimitivesServiceFactory}.
	 *
	 * @param params List of parameters used in the creation of the default factory.
	 * @return The new factory.
	 */
	@SuppressWarnings("unchecked")
	public static ServiceFactory<PrimitivesServiceAPI> getInstance(final Object... params) {
		return ServiceFactoryHelper.get(BasicPrimitivesServiceFactory.class, params);
	}

	/**
	 * Retrieves a new factory of thread-safe services.
	 *
	 * <p>Default factory is {link {@link PollingPrimitivesServiceFactory}
	 *
	 * @param params List of parameters used in the creation of the default factory.
	 * @return The new factory of thread-safe services.
	 */
	@SuppressWarnings("unchecked")
	public static ServiceFactory<PrimitivesServiceAPI> getFactoryOfThreadSafeServices(final Object... params) {
		return ServiceFactoryHelper.get(PollingPrimitivesServiceFactory.class, params);
	}
}
