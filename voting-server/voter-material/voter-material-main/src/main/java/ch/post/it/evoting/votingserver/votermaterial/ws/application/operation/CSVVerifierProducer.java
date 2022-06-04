/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votermaterial.ws.application.operation;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import ch.post.it.evoting.votingserver.commons.verify.CSVVerifier;

/**
 * Produces a {@link CSVVerifier} instance to be published in the CDI context.
 */
public class CSVVerifierProducer {
	/**
	 * Returns a {@link CSVVerifier} instance.
	 *
	 * @return the instance.
	 */
	@Produces
	@ApplicationScoped
	public CSVVerifier getCSVVerifier() {
		return new CSVVerifier();
	}
}
