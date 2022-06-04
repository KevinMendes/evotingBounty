/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.commons.concurrent;

import ch.post.it.evoting.cryptolib.CryptolibFactory;
import ch.post.it.evoting.cryptolib.api.services.ServiceFactory;

/**
 * Abstract class for factories.
 *
 * @param <T> This parameter sets the class of objects that this factory is going to create.
 */
public abstract class BaseFactory<T> extends CryptolibFactory implements ServiceFactory<T> {

	private final Class<T> objectType;

	/**
	 * Creates an instance of factory what will create objects of specified {@code objectType}. This factory will create services configured with
	 * default values.
	 *
	 * @param objectType Interface of the objects created by this factory.
	 */
	protected BaseFactory(final Class<T> objectType) {
		this.objectType = objectType;
	}

	@Override
	public final Class<T> getCreatedObjectType() {
		return objectType;
	}
}
