/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.certificates.service;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import ch.post.it.evoting.cryptolib.api.certificates.CertificatesServiceAPI;
import ch.post.it.evoting.cryptolib.api.services.ServiceFactory;
import ch.post.it.evoting.cryptolib.commons.concurrent.PooledProxiedServiceFactory;

/**
 * This Service factory creates thread-safe services. Thread-safe services proxy all requests to a pool of non thread-safe service instances.
 */
public class PollingCertificatesServiceFactory extends PooledProxiedServiceFactory<CertificatesServiceAPI>
		implements ServiceFactory<CertificatesServiceAPI> {
	/**
	 * Constructor that uses default values.
	 */
	public PollingCertificatesServiceFactory() {
		super(new BasicCertificatesServiceFactory(), new GenericObjectPoolConfig());
	}

	/**
	 * Constructor that uses the given path to read cryptographic properties and the pool config to setup the pool of services.
	 *
	 * @param poolConfig the configuration of the pool.
	 */
	public PollingCertificatesServiceFactory(final GenericObjectPoolConfig poolConfig) {
		super(new BasicCertificatesServiceFactory(), poolConfig);
	}
}
