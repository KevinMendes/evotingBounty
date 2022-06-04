/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.certificates.service;

import ch.post.it.evoting.cryptolib.api.certificates.CertificatesServiceAPI;
import ch.post.it.evoting.cryptolib.api.services.ServiceFactory;
import ch.post.it.evoting.cryptolib.commons.concurrent.ServiceFactoryHelper;

/**
 * This class sets up default factories of {@link CertificatesServiceAPI} objects.
 */
public final class CertificatesServiceFactoryHelper {
	/**
	 * This is a helper class. It cannot be instantiated.
	 */
	private CertificatesServiceFactoryHelper() {
	}

	/**
	 * Retrieves a new factory of non thread-safe services.
	 *
	 * <p>Default factory is {@link BasicCertificatesServiceFactory}.
	 *
	 * @param params list of parameters used in the creation of the default factory.
	 * @return the new factory.
	 */
	@SuppressWarnings("unchecked")
	public static ServiceFactory<CertificatesServiceAPI> getInstance(final Object... params) {
		return ServiceFactoryHelper.get(BasicCertificatesServiceFactory.class, params);
	}

	/**
	 * Retrieves a new factory of thread-safe services.
	 *
	 * <p>Default factory is {link {@link PollingCertificatesServiceFactory}
	 *
	 * @param params list of parameters used in the creation of the default factory.
	 * @return the new factory of thread-safe services.
	 */
	@SuppressWarnings("unchecked")
	public static ServiceFactory<CertificatesServiceAPI> getFactoryOfThreadSafeServices(final Object... params) {
		return ServiceFactoryHelper.get(PollingCertificatesServiceFactory.class, params);
	}
}
