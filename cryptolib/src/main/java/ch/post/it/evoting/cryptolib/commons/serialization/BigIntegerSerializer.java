/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.commons.serialization;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Base64;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

/**
 * A JSON serializer that has been customized so that objects of type BigInteger are always serialized into Base64 encoded strings, in order to be
 * deserializable via JavaScript.
 */
public class BigIntegerSerializer extends JsonSerializer<BigInteger> {

	@Override
	public void serialize(final BigInteger value, final JsonGenerator generator, final SerializerProvider serializers) throws IOException {

		final String encodedValue = Base64.getEncoder().encodeToString(value.toByteArray());
		generator.writeString(encodedValue);
	}

	@Override
	public Class<BigInteger> handledType() {

		return BigInteger.class;
	}
}
