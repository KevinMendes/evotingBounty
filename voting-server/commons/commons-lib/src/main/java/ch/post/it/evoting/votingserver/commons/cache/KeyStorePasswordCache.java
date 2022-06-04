/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.cache;

/**
 * Implementation of {@link Cache} which stores the key store passwords.
 */
public class KeyStorePasswordCache extends CacheSupport<String, String> {

	public KeyStorePasswordCache(String cacheName) {
		super(cacheName);
	}
}
