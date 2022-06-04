/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

package ch.post.it.evoting.securedatamanager.services.application.service;

import static org.mockito.Mockito.mock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.securedatamanager.commons.PathResolver;
import ch.post.it.evoting.securedatamanager.config.engine.commands.api.ConfigurationService;
import ch.post.it.evoting.securedatamanager.configuration.setupvoting.EncryptedNodeLongReturnCodeSharesService;
import ch.post.it.evoting.securedatamanager.configuration.setupvoting.ReturnCodesMappingTableFileCreationService;
import ch.post.it.evoting.securedatamanager.configuration.setupvoting.ReturnCodesMappingTablePayloadService;
import ch.post.it.evoting.securedatamanager.services.domain.service.BallotBoxDataGeneratorService;
import ch.post.it.evoting.securedatamanager.services.domain.service.BallotDataGeneratorService;
import ch.post.it.evoting.securedatamanager.services.domain.service.VotingCardSetDataGeneratorService;
import ch.post.it.evoting.securedatamanager.services.domain.service.impl.VotersParametersHolderAdapter;
import ch.post.it.evoting.securedatamanager.services.domain.service.impl.progress.VotingCardSetProgressManagerService;
import ch.post.it.evoting.securedatamanager.services.domain.service.utils.PublicKeyLoader;
import ch.post.it.evoting.securedatamanager.services.domain.service.utils.SystemTenantPublicKeyLoader;
import ch.post.it.evoting.securedatamanager.services.infrastructure.ballot.BallotRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.ballotbox.BallotBoxRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.cc.ReturnCodeGenerationRequestPayloadFileSystemRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.electionevent.ElectionEventRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.electoralauthority.ElectoralAuthorityRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.service.ConfigurationEntityStatusService;
import ch.post.it.evoting.securedatamanager.services.infrastructure.votingcardset.VotingCardSetRepository;

@Configuration
public class VotingCardSetServiceTestSpringConfig {

	@Bean
	public IdleStatusService idleStatusService() {
		return mock(IdleStatusService.class);
	}

	@Bean
	public SignatureService signatureService() {
		return new SignatureService();
	}

	@Bean
	public ExtendedAuthenticationService extendedAuthenticationService() {
		return new ExtendedAuthenticationService();
	}

	@Bean
	public ConfigurationEntityStatusService configurationEntityStatusService() {
		return mock(ConfigurationEntityStatusService.class);
	}

	@Bean
	public PathResolver pathResolver() {
		return mock(PathResolver.class);
	}

	@Bean
	public VotingCardSetRepository votingCardSetRepository() {
		return mock(VotingCardSetRepository.class);
	}

	@Bean
	public ElectionEventRepository electionEventRepository() {
		return mock(ElectionEventRepository.class);
	}

	@Bean
	public ObjectMapper objectMapper() {
		return mock(ObjectMapper.class);
	}

	@Bean
	public BallotRepository ballotRepository() {
		return mock(BallotRepository.class);
	}

	@Bean
	public BallotBoxRepository ballotBoxRepository() {
		return mock(BallotBoxRepository.class);
	}

	@Bean
	public BallotBoxDataGeneratorService ballotBoxDataGeneratorService() {
		return mock(BallotBoxDataGeneratorService.class);
	}

	@Bean
	public BallotDataGeneratorService ballotDataGeneratorService() {
		return mock(BallotDataGeneratorService.class);
	}

	@Bean
	public VotingCardSetDataGeneratorService votingCardSetDataGeneratorService() {
		return mock(VotingCardSetDataGeneratorService.class);
	}

	@Bean
	public VotingCardSetChoiceCodesService votingCardSetChoiceCodesService() {
		return mock(VotingCardSetChoiceCodesService.class);
	}

	@Bean
	public ReturnCodeGenerationRequestPayloadFileSystemRepository returnCodeGenerationRequestPayloadRepository() {
		return mock(ReturnCodeGenerationRequestPayloadFileSystemRepository.class);
	}

	@Bean
	public SystemTenantPublicKeyLoader systemTenantPublicKeyLoader() {
		return mock(SystemTenantPublicKeyLoader.class);
	}

	@Bean
	public ControlComponentKeysAccessorService controlComponentKeysAccessorService() {
		return mock(ControlComponentKeysAccessorService.class);
	}

	@Bean
	public PublicKeyLoader publicKeyLoader() {
		return mock(PublicKeyLoader.class);
	}

	@Bean
	public KeyStoreService keyStoreService() {
		return new KeyStoreServiceForTesting();
	}

	@Bean
	public ElectoralAuthorityRepository electoralAuthorityRepository() {
		return mock(ElectoralAuthorityRepository.class);
	}

	@Bean
	public VotingCardSetProgressManagerService progressManagerService() {
		return mock(VotingCardSetProgressManagerService.class);
	}

	@Bean
	public PlatformRootCAService platformRootCAService() {
		return mock(PlatformRootCAService.class);
	}

	@Bean
	public ConfigurationService configurationService() {
		return mock(ConfigurationService.class);
	}

	@Bean
	public VotersParametersHolderAdapter votersParametersHolderAdapter() {
		return mock(VotersParametersHolderAdapter.class);
	}

	@Bean
	public EncryptedNodeLongReturnCodeSharesService encryptedNodeLongCodeSharesService() {
		return mock(EncryptedNodeLongReturnCodeSharesService.class);
	}

	//	@Bean
	//	public CombineEncLongCodeSharesService combineEncLongCodeSharesService() {
	//		return new CombineEncLongCodeSharesService();
	//	}

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
	public ReturnCodesMappingTablePayloadService returnCodesMappingTablePayloadService() {
		return mock(ReturnCodesMappingTablePayloadService.class);
	}

	@Bean
	public ReturnCodesMappingTableFileCreationService returnCodesMappingTableFileCreationService() {
		return mock(ReturnCodesMappingTableFileCreationService.class);
	}

}
