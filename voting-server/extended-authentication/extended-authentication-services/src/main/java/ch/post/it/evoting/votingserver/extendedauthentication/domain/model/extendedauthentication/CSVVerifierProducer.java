/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.extendedauthentication.domain.model.extendedauthentication;

import javax.enterprise.inject.Produces;

import ch.post.it.evoting.votingserver.commons.verify.CSVVerifier;

public class CSVVerifierProducer {

	@Produces
	@EaCsvVerifier
	public CSVVerifier getCSVVerifier() {
		return new CSVVerifier();
	}

}
