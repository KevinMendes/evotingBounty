/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.stores.service;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import ch.post.it.evoting.cryptolib.api.stores.StoresServiceAPI;
import ch.post.it.evoting.cryptolib.commons.concurrent.PooledProxiedServiceFactory;

/**
 * This Service factory creates thread-safe services. Thread-safe services proxy all requests to a pool of non thread-safe service instances.
 */
public class PollingStoresServiceFactory extends PooledProxiedServiceFactory<StoresServiceAPI> {

	/**
	 * Constructor that uses default values.
	 */
	public PollingStoresServiceFactory() {
		super(new BasicStoresServiceFactory(), new GenericObjectPoolConfig());
	}

	/**
	 * Constructor that uses the default cryptographic values and the pool config to setup the pool of services.
	 *
	 * @param poolConfig the configuration of the pool.
	 */
	public PollingStoresServiceFactory(final GenericObjectPoolConfig poolConfig) {
		super(new BasicStoresServiceFactory(), poolConfig);
	}
}
