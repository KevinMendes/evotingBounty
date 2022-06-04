/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.commons.concurrent;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import ch.post.it.evoting.cryptolib.api.exceptions.CryptoLibException;
import ch.post.it.evoting.cryptolib.api.services.ServiceFactory;

/**
 * This class helps to set up default factories of the desired objects.
 */
public final class ServiceFactoryHelper {

	/**
	 * This is a helper class. It cannot be instantiated.
	 */
	private ServiceFactoryHelper() {
	}

	/**
	 * Creates an instance of the given Class by invoking the constructor with the given parameters.
	 *
	 * @param clazz  Class type.
	 * @param params Needed parameters.
	 * @return The created factory.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static ServiceFactory get(final Class clazz, final Object... params) {

		final List<Class> list = new ArrayList<>();

		for (final Object object : params) {
			list.add(object.getClass());
		}
		final Class[] paramTypes = list.toArray(new Class[params.length]);
		final Object instance;

		try {
			if (params.length == 0) {
				instance = clazz.newInstance();
			} else {
				instance = clazz.getConstructor(paramTypes).newInstance(params);
			}
		} catch (final InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			throw new CryptoLibException(e);
		}
		return (ServiceFactory) instance;
	}
}
