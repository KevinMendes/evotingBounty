/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.commons.serialization;

import static ch.post.it.evoting.cryptolib.commons.validations.Validate.notNullOrBlank;

import java.io.IOException;
import java.math.BigInteger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;

/**
 * Basic abstract implementation of {@link JsonSerializable}.
 */
public class AbstractJsonSerializable implements JsonSerializable {
	private static final ObjectMapper MAPPER;

	static {
		MAPPER = new ObjectMapper();
		MAPPER.configure(SerializationFeature.WRAP_ROOT_VALUE, true);
		MAPPER.configure(DeserializationFeature.UNWRAP_ROOT_VALUE, true);
		final SimpleModule module = new SimpleModule();
		module.addSerializer(new BigIntegerSerializer());
		module.addDeserializer(BigInteger.class, new BigIntegerDeserializer());
		MAPPER.registerModule(module);
	}

	/**
	 * Deserializes the instance from a given string in JSON format.
	 *
	 * @param <T>         the object type
	 * @param json        the JSON
	 * @param objectClass the object class
	 * @return the instance
	 * @throws GeneralCryptoLibException failed to deserialize the instance.
	 */
	protected static <T> T fromJson(final String json, final Class<T> objectClass) throws GeneralCryptoLibException {
		notNullOrBlank(json, objectClass.getSimpleName() + " JSON string");
		try {
			return MAPPER.readValue(json, objectClass);
		} catch (final JsonMappingException e) {
			final Throwable cause = e.getCause();
			if (cause instanceof GeneralCryptoLibException) {
				throw (GeneralCryptoLibException) cause;
			} else {
				throw new GeneralCryptoLibException("Failed to deserialize instance from JSON.", e);
			}
		} catch (final IOException e) {
			throw new GeneralCryptoLibException("Failed to deserialize instance from JSON.", e);
		}
	}

	/**
	 * Serializes the instance to a string in JSON format.
	 *
	 * @return the string in JSON format.
	 * @throws GeneralCryptoLibException if the serialization process fails.
	 */
	@Override
	public final String toJson() throws GeneralCryptoLibException {
		try {
			return MAPPER.writeValueAsString(this);
		} catch (final JsonProcessingException e) {
			throw new GeneralCryptoLibException("Failed to serialize instance to JSON.", e);
		}
	}
}
