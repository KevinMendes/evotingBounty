/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.cache;

import org.apache.jcs.JCS;
import org.apache.jcs.access.behavior.ICacheAccess;
import org.apache.jcs.access.exception.CacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link Cache} to be used by other classes as a super class or as a delegate.
 */
public class CacheSupport<K, V> implements Cache<K, V> {

	private static final Logger LOGGER = LoggerFactory.getLogger(CacheSupport.class);

	private final ICacheAccess cache;

	public CacheSupport(String cacheName) {
		try {
			cache = JCS.getInstance(cacheName);
		} catch (CacheException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public final boolean containsKey(final K key) {
		return cache.get(key) != null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public final V get(final K key) {
		Object object = cache.get(key);
		if (object != null) {
			return (V) object;
		}
		return null;
	}

	@Override
	public final void put(final K key, final V value) {
		try {
			cache.put(key, value);
		} catch (CacheException cE) {
			LOGGER.info("Error trying to put key/value {}/{}.", key, value, cE);
			return;
		}
	}

	@Override
	public final void remove(final K key) {
		try {
			cache.remove(key);
		} catch (CacheException cE) {
			LOGGER.error("Error trying to remove key {}.", key, cE);
			return;
		}
	}
}
