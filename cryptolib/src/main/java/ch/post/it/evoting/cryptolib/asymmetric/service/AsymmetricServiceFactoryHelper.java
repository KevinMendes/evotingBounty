/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.asymmetric.service;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.api.services.ServiceFactory;
import ch.post.it.evoting.cryptolib.commons.concurrent.ServiceFactoryHelper;

/**
 * This class sets up default factories of {@link AsymmetricServiceAPI} objects.
 */
public final class AsymmetricServiceFactoryHelper {

	/**
	 * This is a helper class. It cannot be instantiated.
	 */
	private AsymmetricServiceFactoryHelper() {
	}

	/**
	 * Retrieves a new factory of non thread-safe services.
	 *
	 * <p>Default factory is {@link BasicAsymmetricServiceFactory}.
	 *
	 * @param params a list of parameters used in the creation of the default factory.
	 * @return the new factory.
	 */
	@SuppressWarnings("unchecked")
	public static ServiceFactory<AsymmetricServiceAPI> getInstance(final Object... params) {
		return ServiceFactoryHelper.get(BasicAsymmetricServiceFactory.class, params);
	}

	/**
	 * Retrieves a new factory of thread-safe services.
	 *
	 * <p>Default factory is {@link PollingAsymmetricServiceFactory}
	 *
	 * @param params a list of parameters used in the creation of the default factory.
	 * @return the new factory of thread-safe services.
	 */
	@SuppressWarnings("unchecked")
	public static ServiceFactory<AsymmetricServiceAPI> getFactoryOfThreadSafeServices(final Object... params) {
		return ServiceFactoryHelper.get(PollingAsymmetricServiceFactory.class, params);
	}
}
