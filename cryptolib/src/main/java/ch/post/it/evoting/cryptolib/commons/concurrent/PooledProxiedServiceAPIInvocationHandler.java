/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.commons.concurrent;

import static java.lang.System.identityHashCode;
import static java.text.MessageFormat.format;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.PooledObjectFactory;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;

/**
 * Handler of the proxy invocation. This class handles any invocation to the proxied object in order to obtain a idle service to which invoke the
 * call. After returning gives the used service instance back to the pool.
 *
 * @param <T> The type of the interface that is being proxied and at the end the type of the object that become apparently as thread-safe.
 */
public final class PooledProxiedServiceAPIInvocationHandler<T> implements InvocationHandler {

	private final ObjectPool<T> pool;

	/**
	 * Constructor that sets up the handler with the given parameters.
	 *
	 * @param factory    The factory that creates real objects of the proxied type. This factory is used in order to fill the pool.
	 * @param poolConfig The configuration of the pool.
	 */
	public PooledProxiedServiceAPIInvocationHandler(final PooledObjectFactory<T> factory, final GenericObjectPoolConfig poolConfig) {
		this(new GenericObjectPool<>(factory, poolConfig));
	}

	PooledProxiedServiceAPIInvocationHandler(final ObjectPool<T> pool) {
		this.pool = pool;
	}

	private static Object invokeObject(final Object proxy, final Method method, final Object[] args) {
		final Object value;
		switch (method.getName()) {
		case "equals":
			value = proxy == args[0];
			break;
		case "hashCode":
			value = identityHashCode(proxy);
			break;
		case "toString":
			value = proxy.getClass().getName() + '@' + identityHashCode(proxy);
			break;
		default:
			throw new UnsupportedOperationException(format("Method ''{0}'' is not supported.", method));
		}
		return value;
	}

	@Override
	public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
		final Object value;
		if (method.getDeclaringClass() == Object.class) {
			value = invokeObject(proxy, method, args);
		} else {
			value = invokeService(proxy, method, args);
		}
		return value;
	}

	private T acquireService() {
		try {
			return pool.borrowObject();
		} catch (final IllegalStateException e) {
			throw e;
		} catch (final Exception e) {
			throw new IllegalStateException("Failed to acquire service.", e);
		}
	}

	private void destroyService(final T service) {
		try {
			pool.invalidateObject(service);
		} catch (final IllegalStateException e) {
			throw e;
		} catch (final Exception e) {
			throw new IllegalStateException("Failed to destroy service", e);
		}
	}

	@SuppressWarnings("squid:S1181")
	private Object invokeService(final Object proxy, final Method method, final Object[] args) throws Throwable {
		final Object value;
		final T service = acquireService();

		try {
			value = method.invoke(service, args);
		} catch (final Throwable e) {
			// Exception thrown by service, programming error, system error or similar
			destroyService(service);
			throw e.getCause();
		}

		releaseService(service);

		return value;
	}

	private void releaseService(final T service) {
		try {
			pool.returnObject(service);
		} catch (final IllegalStateException e) {
			throw e;
		} catch (final Exception e) {
			try {
				pool.invalidateObject(service);
			} catch (final Exception suppressed) {
				e.addSuppressed(suppressed);
			}
			throw new IllegalStateException("Failed to release service", e);
		}
	}
}
