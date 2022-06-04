/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.symmetric.service;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import ch.post.it.evoting.cryptolib.api.symmetric.SymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.commons.concurrent.PooledProxiedServiceFactory;

/**
 * This factory creates thread-safe services. Thread-safe services proxy all requests to a pool of non thread-safe service instances.
 */
public class PollingSymmetricServiceFactory extends PooledProxiedServiceFactory<SymmetricServiceAPI> {

	/**
	 * Constructor that uses default values.
	 */
	public PollingSymmetricServiceFactory() {
		super(new BasicSymmetricServiceFactory(), new GenericObjectPoolConfig());
	}

	/**
	 * Constructor that uses the given path to read cryptographic properties and the pool config to setup the pool of services.
	 *
	 * @param poolConfig the configuration of the pool.
	 */
	public PollingSymmetricServiceFactory(final GenericObjectPoolConfig poolConfig) {
		super(new BasicSymmetricServiceFactory(), poolConfig);
	}
}
