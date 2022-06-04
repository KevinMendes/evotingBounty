/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.asymmetric.service;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.api.services.ServiceFactory;
import ch.post.it.evoting.cryptolib.commons.concurrent.PooledProxiedServiceFactory;

/**
 * This Service factory creates thread-safe services. Thread-safe services proxy all requests to a pool of non thread-safe service instances.
 */
public class PollingAsymmetricServiceFactory extends PooledProxiedServiceFactory<AsymmetricServiceAPI>
		implements ServiceFactory<AsymmetricServiceAPI> {
	/**
	 * Default constructor. This factory will create services configured with default values and a default pool.
	 */
	public PollingAsymmetricServiceFactory() {
		super(new BasicAsymmetricServiceFactory(), new GenericObjectPoolConfig());
	}

	/**
	 * Creates an instance of service configured with default values and the given pool configuration.
	 *
	 * @param poolConfig the configuration of the pool.
	 */
	public PollingAsymmetricServiceFactory(final GenericObjectPoolConfig poolConfig) {
		super(new BasicAsymmetricServiceFactory(), poolConfig);
	}
}
