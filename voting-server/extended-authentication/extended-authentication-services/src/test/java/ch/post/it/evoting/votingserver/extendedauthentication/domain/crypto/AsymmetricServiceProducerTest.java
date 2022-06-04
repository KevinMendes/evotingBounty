/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.extendedauthentication.domain.crypto;

import static org.hamcrest.CoreMatchers.isA;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.rules.TestRule;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;

public class AsymmetricServiceProducerTest {

	public static final String SOME_NUMBER = "50";
	private static final String MAX_ELEMENTS_CRYPTO_POOL = "ASYMMETRIC_MAX_ELEMENTS_CRYPTO_POOL";
	@ClassRule
	public static EnvironmentVariables environmentVariables = new EnvironmentVariables();
	@Rule
	public TestRule restoreSystemProperties = new RestoreSystemProperties();

	@Test
	public void testCreation() {

		environmentVariables.set(MAX_ELEMENTS_CRYPTO_POOL, SOME_NUMBER);
		final AsymmetricServiceProducer producer = new AsymmetricServiceProducer();
		assertThat(producer.getInstance(), isA(AsymmetricServiceAPI.class));

	}
}
