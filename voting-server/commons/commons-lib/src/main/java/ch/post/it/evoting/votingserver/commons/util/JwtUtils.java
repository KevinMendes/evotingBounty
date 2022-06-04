/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.util;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import javax.json.JsonObject;

import ch.post.it.evoting.cryptolib.commons.serialization.JsonSignatureService;

public final class JwtUtils {

	private static final int BODY_POS = 1;

	private static final String SEPARATOR = "\\.";

	private static final Base64.Decoder decoder = Base64.getDecoder();

	/**
	 * Non-public constructor
	 */
	private JwtUtils() {

	}

	/**
	 * Convert json string to json object.
	 *
	 * @param jwt - the jwt in string format.
	 * @return the JsonObject corresponding to json string.
	 */
	public static JsonObject getJsonObject(String jwt) {
		String bodyBase64 = jwt.split(SEPARATOR)[BODY_POS];

		String bodyString = new String(decoder.decode(bodyBase64), StandardCharsets.UTF_8);

		return JsonUtils.getJsonObject(bodyString).getJsonObject(JsonSignatureService.SIGNED_OBJECT_FIELD_NAME);
	}

}
