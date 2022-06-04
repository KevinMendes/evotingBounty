/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.commons.concurrent;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import ch.post.it.evoting.cryptolib.api.services.ServiceFactory;

/**
 * This class creates a pool object factory from a service factory in order to fulfill the pool.
 *
 * @param <T> The type of objects created by this factory.
 */
class BasePooledServiceFactory<T> extends BasePooledObjectFactory<T> {
	private final ServiceFactory<T> factory;

	/**
	 * Constructor that stores the given factory in order to create objects pool ready.
	 *
	 * @param factory Factory for creating an objects pool.
	 */
	public BasePooledServiceFactory(final ServiceFactory<T> factory) {
		this.factory = factory;
	}

	@Override
	public T create() throws Exception {
		return factory.create();
	}

	@Override
	public PooledObject<T> wrap(final T obj) {
		return new DefaultPooledObject<>(obj);
	}
}
