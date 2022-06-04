/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.configuration;

import io.jsonwebtoken.SignatureAlgorithm;

/**
 * Wraps the different {@link SignatureAlgorithm} that are supported.
 */
public enum JSONSignatureConfiguration {

	RSA_PSS_SHA256(SignatureAlgorithm.PS256);

	private final SignatureAlgorithm algorithm;

	JSONSignatureConfiguration(SignatureAlgorithm algorithm) {

		this.algorithm = algorithm;
	}

	public SignatureAlgorithm getAlgorithm() {
		return algorithm;
	}
}
