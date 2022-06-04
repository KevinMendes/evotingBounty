/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.commons.concurrent;

import java.lang.reflect.Proxy;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

import ch.post.it.evoting.cryptolib.api.services.ServiceFactory;

/**
 * Base class for pooled and proxied service factories. This class handles pool and proxy creation.
 *
 * @param <T> Service type.
 */
public abstract class PooledProxiedServiceFactory<T> extends BaseFactory<T> implements ServiceFactory<T> {

	private final GenericObjectPoolConfig poolConfig;

	private final BasePooledObjectFactory<T> factory;

	/**
	 * Constructor that configures the factory with the given factory of services and the given pool configuration.
	 *
	 * @param factory    the factory is any factory that creates service instances (specially non thread-safe).
	 * @param poolConfig The configuration of the pool.
	 */
	public PooledProxiedServiceFactory(final ServiceFactory<T> factory, final GenericObjectPoolConfig poolConfig) {
		super(factory.getCreatedObjectType());
		this.poolConfig = poolConfig.clone();
		this.factory = new BasePooledServiceFactory<>(factory);
	}

	/**
	 * Creates a Thread-safe service instance.
	 */
	@Override
	public T create() {
		final Class<T> type = getCreatedObjectType();
		final Object proxy = Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type },
				new PooledProxiedServiceAPIInvocationHandler<>(factory, poolConfig));
		return type.cast(proxy);
	}
}
