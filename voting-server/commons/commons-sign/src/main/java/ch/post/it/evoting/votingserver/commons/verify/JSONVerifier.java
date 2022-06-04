/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.verify;

import java.security.PublicKey;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Jwts;

public class JSONVerifier {

	public <T> T verifyFromMap(PublicKey publicKey, String signedJSON, Class<T> clazz) {
		@SuppressWarnings("unchecked")
		Map<String, Object> claimMapRecovered = Jwts.parser().setSigningKey(publicKey).parseClaimsJws(signedJSON).getBody();

		final ObjectMapper mapper = new ObjectMapper();
		return mapper.convertValue(claimMapRecovered, clazz);
	}
}
