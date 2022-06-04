/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.commons.serialization;

import java.io.IOException;
import java.math.BigInteger;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;

/**
 * Defines a utility for data serialization and deserialization, using JSON format.
 */
public final class JsonMapper {

	/**
	 * JSON serializes an object of type <code>T</code> and returns the result as a string.
	 *
	 * @param <T> the type parameter of the object to be serialized.
	 * @param t   the type of the object to be serialized.
	 * @return the serialized object of type <code>T</code>, as a string.
	 * @throws GeneralCryptoLibException if the serialization process fails.
	 */
	public <T> String toJson(final T t) throws GeneralCryptoLibException {

		final ObjectMapper mapper = new ObjectMapper();

		mapper.configure(SerializationFeature.WRAP_ROOT_VALUE, true);

		final SimpleModule module = new SimpleModule();
		module.addSerializer(new BigIntegerSerializer());
		mapper.registerModule(module);

		try {
			return mapper.writeValueAsString(t);
		} catch (final IOException e) {
			throw new GeneralCryptoLibException("Could not serialize object of type parameter " + t + " to string.", e);
		}
	}

	/**
	 * Deserializes a JSON serialized object of type <code>T</code>, retrieved as a string.
	 *
	 * @param <T>     the type parameter of the object to be deserialized.
	 * @param type    the class type of the object to be deserialized.
	 * @param jsonStr the serialized object of type <code>T</code>, as a string.
	 * @return the deserialized object of type <code>T</code>.
	 * @throws GeneralCryptoLibException if the deserialization process fails.
	 */
	public <T> T fromJson(final Class<T> type, final String jsonStr) throws GeneralCryptoLibException {

		final ObjectMapper mapper = new ObjectMapper();
		mapper.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);

		final SimpleModule module = new SimpleModule();
		module.addDeserializer(BigInteger.class, new BigIntegerDeserializer());
		mapper.registerModule(module);

		try {
			return mapper.readValue(jsonStr, type);
		} catch (final IOException e) {
			throw new GeneralCryptoLibException("Could not deserialize object of type " + type + " from string " + jsonStr, e);
		}
	}
}
