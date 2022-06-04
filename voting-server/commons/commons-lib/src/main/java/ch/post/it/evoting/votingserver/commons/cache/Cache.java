/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.cache;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;

/**
 * Cache stores key-value pairs.
 * <p>
 * This caching abstraction is very naive, for example, it does not support expiration of entries and other concepts.
 * <p>
 * Both key and values must not be {@code null}. The keys should be immutable with correctly implemented methods {@link Object#equals(Object)} and
 * {@link Object#hashCode()}.
 * <p>
 * Implementation must be thread-safe.
 *
 * @param <K> the type of keys
 * @param <V> the type of values
 */
public interface Cache<K, V> {

	/**
	 * Returns whether the cache contains an entry with the specified key.
	 *
	 * @param key the key
	 * @return the cache contains an entry with the specified key
	 * @throws NullPointerException the key is {@code null}.
	 */
	boolean containsKey(K key);

	/**
	 * Returns the value associated with the specified key.
	 *
	 * @param key the key
	 * @return the value or {@code null} if the value does not exist
	 * @throws NullPointerException the key is {@code null}.
	 */
	V get(K key);

	/**
	 * Puts given key and value into the cache.
	 *
	 * @param key   the key
	 * @param value the value
	 * @return the previous value associated with the key or {@code null} if there was no previous value
	 * @throws NullPointerException the key or the value is {@code null}.
	 */
	void put(K key, V value);

	/**
	 * Removes the entry with a given key.
	 *
	 * @param key the key
	 * @throws ResourceNotFoundException the key is {@code null}.
	 */
	void remove(K key);
}
