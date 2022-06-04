/*
 * (c) Original Developers indicated in attribution.txt, 2022. All Rights Reserved.
 */
package ch.post.it.evoting.cryptolib.commons.serialization;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Base64;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Class is used by {@link ObjectMapper} (and other chained {@link JsonDeserializer}s too) to deserialize Objects by {@link BigInteger} type from
 * JSON, using provided {@link JsonParser}.
 */
public class BigIntegerDeserializer extends JsonDeserializer<BigInteger> {

	@Override
	public BigInteger deserialize(final JsonParser jsonParser, final DeserializationContext context) throws IOException {

		final String text = jsonParser.getText().trim();
		if (text.length() == 0) {
			return null;
		} else {
			return new BigInteger(Base64.getDecoder().decode(text));
		}
	}
}
