/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.extendedkeystore.service;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import ch.post.it.evoting.cryptolib.api.extendedkeystore.KeyStoreService;
import ch.post.it.evoting.cryptolib.commons.concurrent.PooledProxiedServiceFactory;

/**
 * This Service factory creates thread-safe services. Thread-safe services proxy all requests to a pool of non thread-safe service instances.
 */
public class PollingExtendedKeyStoreServiceFactory extends PooledProxiedServiceFactory<KeyStoreService> {

	/**
	 * Constructor that uses default values.
	 */
	public PollingExtendedKeyStoreServiceFactory() {
		super(new BasicExtendedKeyStoreServiceFactory(), new GenericObjectPoolConfig());
	}

	/**
	 * Constructor that uses the default cryptographic values and the pool config to setup the pool of services.
	 *
	 * @param poolConfig the configuration of the pool.
	 */
	public PollingExtendedKeyStoreServiceFactory(final GenericObjectPoolConfig poolConfig) {
		super(new BasicExtendedKeyStoreServiceFactory(), poolConfig);
	}
}
