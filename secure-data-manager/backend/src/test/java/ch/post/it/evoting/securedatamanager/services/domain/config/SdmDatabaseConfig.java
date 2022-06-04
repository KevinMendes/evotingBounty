/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.domain.config;

import static org.mockito.Mockito.mock;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.web.client.RestTemplate;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricService;
import ch.post.it.evoting.securedatamanager.commons.PathResolver;
import ch.post.it.evoting.securedatamanager.commons.PrefixPathResolver;
import ch.post.it.evoting.securedatamanager.configuration.setupvoting.EncryptedNodeLongReturnCodeSharesService;
import ch.post.it.evoting.securedatamanager.configuration.setupvoting.ReturnCodesMappingTableFileCreationService;
import ch.post.it.evoting.securedatamanager.services.application.service.BallotBoxService;
import ch.post.it.evoting.securedatamanager.services.application.service.ExtendedAuthenticationService;
import ch.post.it.evoting.securedatamanager.services.application.service.IdleStatusService;
import ch.post.it.evoting.securedatamanager.services.application.service.SignatureService;
import ch.post.it.evoting.securedatamanager.services.application.service.VotingCardSetChoiceCodesService;
import ch.post.it.evoting.securedatamanager.services.application.service.VotingCardSetDownloadService;
import ch.post.it.evoting.securedatamanager.services.application.service.VotingCardSetGenerateBallotService;
import ch.post.it.evoting.securedatamanager.services.application.service.VotingCardSetGenerateService;
import ch.post.it.evoting.securedatamanager.services.application.service.VotingCardSetSignService;
import ch.post.it.evoting.securedatamanager.services.domain.service.BallotBoxDataGeneratorService;
import ch.post.it.evoting.securedatamanager.services.domain.service.BallotDataGeneratorService;
import ch.post.it.evoting.securedatamanager.services.domain.service.ElectionEventDataGeneratorService;
import ch.post.it.evoting.securedatamanager.services.domain.service.VotingCardSetDataGeneratorService;
import ch.post.it.evoting.securedatamanager.services.domain.service.utils.PublicKeyLoader;
import ch.post.it.evoting.securedatamanager.services.domain.service.utils.SystemTenantPublicKeyLoader;
import ch.post.it.evoting.securedatamanager.services.infrastructure.ballotbox.BallotBoxRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.cc.ReturnCodeGenerationRequestPayloadFileSystemRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.service.ConfigurationEntityStatusService;

/**
 * MVC Configuration
 */
@Configuration
@ComponentScan(basePackages = { "ch.post.it.evoting.securedatamanager.services.infrastructure" })
@PropertySource("classpath:config/application.properties")
@Profile("test")
public class SdmDatabaseConfig {

	@Value("${user.home}")
	private String prefix;

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertiesResolver() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	@Bean
	public PathResolver getPrefixPathResolver() {
		return new PrefixPathResolver(prefix);
	}

	@Bean
	public SystemTenantPublicKeyLoader getSystemTenantPublicKeyLoader() {
		return new SystemTenantPublicKeyLoader();
	}

	@Bean
	public PublicKeyLoader getPublicKeyLoader() {
		return new PublicKeyLoader();
	}

	@Bean
	public BallotBoxService getBallotBoxService() {
		return new BallotBoxService();
	}

	@Bean
	public VotingCardSetDownloadService votingCardSetDownloadService(
			final IdleStatusService idleStatusService,
			final VotingCardSetChoiceCodesService votingCardSetChoiceCodesService,
			final ConfigurationEntityStatusService configurationEntityStatusService,
			final ReturnCodeGenerationRequestPayloadFileSystemRepository returnCodeGenerationRequestPayloadRepository) {
		return new VotingCardSetDownloadService(idleStatusService, votingCardSetChoiceCodesService, configurationEntityStatusService,
				returnCodeGenerationRequestPayloadRepository);
	}

	@Bean
	public VotingCardSetGenerateBallotService votingCardSetGenerateBallotService(
			final BallotBoxRepository ballotBoxRepository,
			final BallotDataGeneratorService ballotDataGeneratorService,
			final BallotBoxDataGeneratorService ballotBoxDataGeneratorService,
			final ConfigurationEntityStatusService configurationEntityStatusService) {
		return new VotingCardSetGenerateBallotService(ballotBoxRepository, ballotDataGeneratorService, ballotBoxDataGeneratorService,
				configurationEntityStatusService);
	}

	@Bean
	public VotingCardSetGenerateService votingCardSetGenerateService(
			final IdleStatusService idleStatusService,
			final ReturnCodesMappingTableFileCreationService returnCodesMappingTableFileCreationService,
			final VotingCardSetDataGeneratorService votingCardSetDataGeneratorService,
			final VotingCardSetGenerateBallotService votingCardSetGenerateBallotService) {
		return new VotingCardSetGenerateService(idleStatusService, votingCardSetDataGeneratorService, votingCardSetGenerateBallotService,
				returnCodesMappingTableFileCreationService);
	}

	@Bean
	public VotingCardSetSignService votingCardSetSignService(
			final SignatureService signatureService,
			final ExtendedAuthenticationService extendedAuthenticationService,
			final ConfigurationEntityStatusService configurationEntityStatusService) {
		return new VotingCardSetSignService(signatureService, extendedAuthenticationService, configurationEntityStatusService);
	}

	@Bean
	public BallotBoxDataGeneratorService getBallotBoxDataGeneratorService() {
		return mock(BallotBoxDataGeneratorService.class);
	}

	@Bean
	public BallotDataGeneratorService getBallotDataGeneratorService() {
		return mock(BallotDataGeneratorService.class);
	}

	@Bean
	public ElectionEventDataGeneratorService getElectionEventDataGeneratorService() {
		return mock(ElectionEventDataGeneratorService.class);
	}

	@Bean
	public VotingCardSetDataGeneratorService getVotingCardSetDataGeneratorService() {
		return mock(VotingCardSetDataGeneratorService.class);
	}

	@Bean
	public RestTemplate getRestTemplate() {
		return mock(RestTemplate.class);
	}

	@Bean
	AsymmetricServiceAPI asymmetricServiceAPI() {
		return new AsymmetricService();
	}

	@Bean
	EncryptedNodeLongReturnCodeSharesService encryptedNodeLongCodeSharesService() {
		return mock(EncryptedNodeLongReturnCodeSharesService.class);
	}
}
