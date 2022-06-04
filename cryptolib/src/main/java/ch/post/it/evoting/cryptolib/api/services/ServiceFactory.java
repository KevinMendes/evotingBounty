/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.api.services;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;

/**
 * Interface that any factory of services should implement. All the objects created by it should be equivalent.
 *
 * @param <T> The type of service to be created.
 */
public interface ServiceFactory<T> {

	/**
	 * Creates a new instance of the service.
	 *
	 * @return A new instance of the service.
	 * @throws GeneralCryptoLibException if the service creation fails.
	 */
	T create() throws GeneralCryptoLibException;

	/**
	 * Returns the type of service which this factory creates.
	 *
	 * @return The type of service which this factory creates.
	 */
	Class<T> getCreatedObjectType();
}
