/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.model.authentication;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;

import ch.post.it.evoting.securedatamanager.config.commons.config.model.authentication.ChallengeGenerator;
import ch.post.it.evoting.securedatamanager.config.commons.config.model.authentication.ExtraParams;
import ch.post.it.evoting.securedatamanager.config.engine.model.authentication.service.ProvidedChallengeGenerator;
import ch.post.it.evoting.securedatamanager.config.engine.model.authentication.service.SequentialProvidedChallengeSource;

class ProvidedChallengeGeneratorTest {

	final URL url = this.getClass().getResource("/aliasDataSample.csv");
	Path aliasesPath = new File(url.toURI()).toPath();

	private final ChallengeGenerator challengeGenerator = new ProvidedChallengeGenerator(
			new SequentialProvidedChallengeSource(aliasesPath));
	//() -> new ProvidedChallenges(RandomStringUtils.random(10), Collections.emptyList()));

	ProvidedChallengeGeneratorTest() throws URISyntaxException {
	}

	@Test
	void generateNonNullParams() {
		final ExtraParams extraParams = challengeGenerator.generateExtraParams();
		assertNotNull(extraParams);
	}

	@Test
	void generatesNonNullValue() {
		final ExtraParams extraParams = challengeGenerator.generateExtraParams();
		assertTrue(extraParams.getValue().isPresent());
	}

	@Test
	void generatesNonNullAlias() {
		final ExtraParams extraParams = challengeGenerator.generateExtraParams();
		assertTrue(extraParams.getAlias().isPresent());
	}

}
