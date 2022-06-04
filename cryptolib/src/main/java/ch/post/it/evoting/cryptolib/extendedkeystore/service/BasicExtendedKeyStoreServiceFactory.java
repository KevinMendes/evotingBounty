/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.extendedkeystore.service;

import ch.post.it.evoting.cryptolib.api.extendedkeystore.KeyStoreService;
import ch.post.it.evoting.cryptolib.commons.concurrent.BaseFactory;

/**
 * Factory of {@link ExtendedKeyStoreService} objects which are non thread-safe.
 */
public class BasicExtendedKeyStoreServiceFactory extends BaseFactory<KeyStoreService> {
	/**
	 * Default constructor. This factory will create services configured with default values.
	 */
	public BasicExtendedKeyStoreServiceFactory() {
		super(KeyStoreService.class);
	}

	@Override
	public KeyStoreService create() {
		return new ExtendedKeyStoreService();
	}
}
