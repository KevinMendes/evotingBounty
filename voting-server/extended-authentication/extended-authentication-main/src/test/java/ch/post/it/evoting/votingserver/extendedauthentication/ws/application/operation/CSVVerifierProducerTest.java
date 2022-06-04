/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.extendedauthentication.ws.application.operation;

import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.Test;

import ch.post.it.evoting.votingserver.commons.verify.CSVVerifier;
import ch.post.it.evoting.votingserver.extendedauthentication.domain.model.extendedauthentication.CSVVerifierProducer;

public class CSVVerifierProducerTest {

	@Test
	public void testCreation() {

		CSVVerifierProducer verifierProducer = new CSVVerifierProducer();
		assertThat(verifierProducer.getCSVVerifier(), isA(CSVVerifier.class));
	}
}
