/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.stores.service;

import ch.post.it.evoting.cryptolib.api.services.ServiceFactory;
import ch.post.it.evoting.cryptolib.api.stores.StoresServiceAPI;
import ch.post.it.evoting.cryptolib.commons.concurrent.ServiceFactoryHelper;

/**
 * This class sets up default factories of {@link StoresServiceAPI} objects.
 */
public final class StoresServiceFactoryHelper {

	/**
	 * This is a helper class. It cannot be instantiated.
	 */
	private StoresServiceFactoryHelper() {
		super();
	}

	/**
	 * Retrieves a new factory of non thread-safe services.
	 *
	 * <p>Default factory is {@link BasicStoresServiceFactory}.
	 *
	 * @param params a list of parameters used in the creation of the default factory.
	 * @return the new factory.
	 */
	@SuppressWarnings("unchecked")
	public static ServiceFactory<StoresServiceAPI> getInstance(final Object... params) {
		return ServiceFactoryHelper.get(BasicStoresServiceFactory.class, params);
	}

	/**
	 * Retrieves a new factory of thread-safe services.
	 *
	 * <p>Default factory is {link {@link PollingStoresServiceFactory}.
	 *
	 * @param params a list of parameters used in the creation of the default factory.
	 * @return the new factory of thread-safe services.
	 */
	@SuppressWarnings("unchecked")
	public static ServiceFactory<StoresServiceAPI> getFactoryOfThreadSafeServices(final Object... params) {
		return ServiceFactoryHelper.get(PollingStoresServiceFactory.class, params);
	}
}
