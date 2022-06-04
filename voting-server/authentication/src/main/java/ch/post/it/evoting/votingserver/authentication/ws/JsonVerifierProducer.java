/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.ws;

import javax.enterprise.inject.Produces;

import ch.post.it.evoting.votingserver.commons.verify.JSONVerifier;

public class JsonVerifierProducer {

	@Produces
	public JSONVerifier getJsonVerifier() {
		return new JSONVerifier();
	}
}
