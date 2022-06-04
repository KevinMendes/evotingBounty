/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.cryptolib.commons.serialization;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

public class JsonSignatureService {
	public static final String SIGNED_OBJECT_FIELD_NAME = "objectToSign";

	private static final SignatureAlgorithm algorithm = SignatureAlgorithm.PS256;

	private JsonSignatureService() {
		// Should not be instantiated
	}

	/**
	 * Signs any object which can be serialized in JSON format.
	 * <p>
	 * Note that it is important that the objects that are to be signed have <em>getter</em> methods.
	 * </p>
	 */
	public static String sign(final PrivateKey privateKey, final Object objectToSign) {

		final Map<String, Object> claimMap = new HashMap<>();
		claimMap.put(SIGNED_OBJECT_FIELD_NAME, objectToSign);

		return Jwts.builder().setClaims(claimMap).signWith(algorithm, privateKey).compact();
	}

	/**
	 * Verifies a JSON Web Signature, and returns the object specified by {@code clazz}.
	 */
	public static <T> T verify(final PublicKey publicKey, final String signedJSON, final Class<T> clazz) {

		@SuppressWarnings("unchecked")
		final Map<String, Object> claimMapRecovered = Jwts.parser().setSigningKey(publicKey).parseClaimsJws(signedJSON).getBody();

		final ObjectMapper mapper = new ObjectMapper();

		final Object recoveredSignedObject = claimMapRecovered.get(SIGNED_OBJECT_FIELD_NAME);
		return mapper.convertValue(recoveredSignedObject, clazz);
	}
}
