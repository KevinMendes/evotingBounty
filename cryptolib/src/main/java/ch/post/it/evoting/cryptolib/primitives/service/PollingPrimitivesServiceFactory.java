/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.primitives.service;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import ch.post.it.evoting.cryptolib.api.primitives.PrimitivesServiceAPI;
import ch.post.it.evoting.cryptolib.api.services.ServiceFactory;
import ch.post.it.evoting.cryptolib.commons.concurrent.PooledProxiedServiceFactory;

/**
 * This Service factory creates thread-safe services. Thread-safe services proxy all requests to a pool of non thread-safe service instances.
 */
public class PollingPrimitivesServiceFactory extends PooledProxiedServiceFactory<PrimitivesServiceAPI>
		implements ServiceFactory<PrimitivesServiceAPI> {
	/**
	 * Constructor that uses default values.
	 */
	public PollingPrimitivesServiceFactory() {
		super(new BasicPrimitivesServiceFactory(), new GenericObjectPoolConfig());
	}

	/**
	 * Constructor that uses the given pool config to setup the pool of services.
	 *
	 * @param poolConfig the configuration of the pool.
	 */
	public PollingPrimitivesServiceFactory(final GenericObjectPoolConfig poolConfig) {
		super(new BasicPrimitivesServiceFactory(), poolConfig);
	}
}
