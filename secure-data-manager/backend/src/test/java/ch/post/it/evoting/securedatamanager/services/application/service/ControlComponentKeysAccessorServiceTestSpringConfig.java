/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

package ch.post.it.evoting.securedatamanager.services.application.service;

import static org.mockito.Mockito.mock;

import java.nio.file.Paths;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ch.post.it.evoting.securedatamanager.commons.PathResolver;
import ch.post.it.evoting.securedatamanager.commons.PrefixPathResolver;
import ch.post.it.evoting.securedatamanager.services.infrastructure.ballot.BallotRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.ballotbox.BallotBoxRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.electoralauthority.ElectoralAuthorityRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.votingcardset.VotingCardSetRepository;

@Configuration
public class ControlComponentKeysAccessorServiceTestSpringConfig {

	private static final String TARGET_DIR = "target/ccKeysAccessorServiceTest/";

	@Bean
	public ControlComponentKeysAccessorService controlComponentKeysAccessorService(final ElectoralAuthorityRepository electoralAuthorityRepository) {
		return new ControlComponentKeysAccessorService(electoralAuthorityRepository);
	}

	@Bean
	public PathResolver pathResolver() {
		return new PrefixPathResolver(Paths.get(TARGET_DIR).toAbsolutePath().toString());
	}

	@Bean
	public ElectoralAuthorityRepository electoralAuthorityRepository() {
		return mock(ElectoralAuthorityRepository.class);
	}

	@Bean
	public VotingCardSetRepository votingCardSetRepository() {
		return mock(VotingCardSetRepository.class);
	}

	@Bean
	public BallotRepository ballotRepository() {
		return mock(BallotRepository.class);
	}

	@Bean
	public BallotBoxRepository ballotBoxRepository() {
		return mock(BallotBoxRepository.class);
	}
}
