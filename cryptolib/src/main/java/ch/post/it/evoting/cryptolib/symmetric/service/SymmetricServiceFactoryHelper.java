/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.symmetric.service;

import ch.post.it.evoting.cryptolib.api.services.ServiceFactory;
import ch.post.it.evoting.cryptolib.api.symmetric.SymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.commons.concurrent.ServiceFactoryHelper;

/**
 * This class sets up default factories of {@link SymmetricServiceAPI} objects.
 */
public final class SymmetricServiceFactoryHelper {

	/**
	 * This is a helper class. It cannot be instantiated.
	 */
	private SymmetricServiceFactoryHelper() {
	}

	/**
	 * Retrieves a new factory of non thread-safe services.
	 *
	 * <p>Default factory is {@link BasicSymmetricServiceFactory}.
	 *
	 * @param params list of parameters used in the creation of the default factory.
	 * @return the new factory.
	 */
	@SuppressWarnings("unchecked")
	public static ServiceFactory<SymmetricServiceAPI> getInstance(final Object... params) {
		return ServiceFactoryHelper.get(BasicSymmetricServiceFactory.class, params);
	}

	/**
	 * Retrieves a new factory of thread-safe services.
	 *
	 * <p>Default factory is {link {@link PollingSymmetricServiceFactory}.
	 *
	 * @param params list of parameters used in the creation of the default factory.
	 * @return the new factory of thread-safe services.
	 */
	@SuppressWarnings("unchecked")
	public static ServiceFactory<SymmetricServiceAPI> getFactoryOfThreadSafeServices(final Object... params) {
		return ServiceFactoryHelper.get(PollingSymmetricServiceFactory.class, params);
	}
}
