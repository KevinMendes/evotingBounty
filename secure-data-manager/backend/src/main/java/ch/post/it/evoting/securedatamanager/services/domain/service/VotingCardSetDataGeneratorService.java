/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.domain.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.annotation.PostConstruct;
import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ch.post.it.evoting.cryptolib.certificates.utils.PemUtils;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.commons.PathResolver;
import ch.post.it.evoting.securedatamanager.commons.domain.CreateVotingCardSetCertificatePropertiesContainer;
import ch.post.it.evoting.securedatamanager.commons.domain.CreateVotingCardSetInput;
import ch.post.it.evoting.securedatamanager.commons.domain.StartVotingCardGenerationJobResponse;
import ch.post.it.evoting.securedatamanager.config.commons.utils.JsonUtils;
import ch.post.it.evoting.securedatamanager.config.engine.commands.api.ConfigurationService;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.VotersParametersHolder;
import ch.post.it.evoting.securedatamanager.services.application.exception.DatabaseException;
import ch.post.it.evoting.securedatamanager.services.application.service.PlatformRootCAService;
import ch.post.it.evoting.securedatamanager.services.domain.model.config.VotingCardGenerationJobStatus;
import ch.post.it.evoting.securedatamanager.services.domain.model.generator.DataGeneratorResponse;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.Status;
import ch.post.it.evoting.securedatamanager.services.domain.service.impl.VotersParametersHolderAdapter;
import ch.post.it.evoting.securedatamanager.services.domain.service.impl.progress.VotingCardSetProgressManagerService;
import ch.post.it.evoting.securedatamanager.services.infrastructure.JsonConstants;
import ch.post.it.evoting.securedatamanager.services.infrastructure.ballotbox.BallotBoxRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.electionevent.ElectionEventRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.service.ConfigurationEntityStatusService;
import ch.post.it.evoting.securedatamanager.services.infrastructure.votingcardset.VotingCardSetRepository;

/**
 * This implementation generates the voting card set data.
 */
@Service
public class VotingCardSetDataGeneratorService {

	private static final Logger LOGGER = LoggerFactory.getLogger(VotingCardSetDataGeneratorService.class);

	// The name of the json parameter number of voting card to generate.
	private static final String JSON_PARAM_NAME_NR_OF_VC_TO_GENERATE = "numberOfVotingCardsToGenerate";

	private final VotingCardSetRepository votingCardSetRepository;
	private final BallotBoxRepository ballotBoxRepository;
	private final ElectionEventRepository electionEventRepository;
	private final PathResolver pathResolver;
	private final ConfigurationEntityStatusService configurationEntityStatusService;
	private final VotingCardSetProgressManagerService votingCardSetProgressManagerService;
	private final PlatformRootCAService platformRootCAService;
	private final ConfigurationService configurationService;
	private final VotersParametersHolderAdapter votersParametersHolderAdapter;

	@Value("${tenantID}")
	private String tenantId;

	@Value("${credential.auth.certificate.properties}")
	private String credentialAuthCertificateProperties;

	@Value("${verification.card.set.certificate.properties}")
	private String verificationCardSetCertificateProperties;

	private ExecutorService jobCompletionExecutor;

	public VotingCardSetDataGeneratorService(final VotingCardSetRepository votingCardSetRepository, final BallotBoxRepository ballotBoxRepository,
			final ElectionEventRepository electionEventRepository, final PathResolver pathResolver,
			final ConfigurationEntityStatusService configurationEntityStatusService,
			final VotingCardSetProgressManagerService votingCardSetProgressManagerService, final PlatformRootCAService platformRootCAService,
			final ConfigurationService configurationService, final VotersParametersHolderAdapter votersParametersHolderAdapter) {
		this.votingCardSetRepository = votingCardSetRepository;
		this.ballotBoxRepository = ballotBoxRepository;
		this.electionEventRepository = electionEventRepository;
		this.pathResolver = pathResolver;
		this.configurationEntityStatusService = configurationEntityStatusService;
		this.votingCardSetProgressManagerService = votingCardSetProgressManagerService;
		this.platformRootCAService = platformRootCAService;
		this.configurationService = configurationService;
		this.votersParametersHolderAdapter = votersParametersHolderAdapter;
	}

	@PostConstruct
	void setup() {
		jobCompletionExecutor = Executors.newCachedThreadPool();
	}

	public DataGeneratorResponse generate(final String id, final String electionEventId) {
		final DataGeneratorResponse result = new DataGeneratorResponse();

		try {

			// basic validation of the input
			if (id == null || id.isEmpty()) {
				result.setSuccessful(false);
				return result;
			}

			final String votingCardSetAsJson = votingCardSetRepository.find(id);
			// simple check if there is a voting card set data returned
			if (JsonConstants.EMPTY_OBJECT.equals(votingCardSetAsJson)) {
				result.setSuccessful(false);
				return result;
			}

			// create the list of parameters to call the configuration json
			final JsonObject votingCardSet = JsonUtils.getJsonObject(votingCardSetAsJson);
			final String verificationCardSetId = votingCardSet.getString(JsonConstants.VERIFICATION_CARD_SET_ID);
			final Path configPath = pathResolver.resolve(Constants.CONFIG_FILES_BASE_DIR);
			final Path configElectionEventPath = configPath.resolve(electionEventId);
			final String ballotBoxId = votingCardSet.getJsonObject(JsonConstants.BALLOT_BOX).getString(JsonConstants.ID);
			final String ballotBoxAsJson = ballotBoxRepository.find(ballotBoxId);
			final JsonObject ballotBox = JsonUtils.getJsonObject(ballotBoxAsJson);
			final String ballotId = ballotBox.getJsonObject(JsonConstants.BALLOT).getString(JsonConstants.ID);
			final String electoralAuthorityId = ballotBox.getJsonObject(JsonConstants.ELECTORAL_AUTHORITY).getString(JsonConstants.ID);
			final JsonObject electionEvent = JsonUtils.getJsonObject(electionEventRepository.find(electionEventId));
			final Path destinationBallotFilePath = pathResolver.resolve(Constants.CONFIG_FILES_BASE_DIR).resolve(electionEventId)
					.resolve(Constants.CONFIG_DIR_NAME_ONLINE).resolve(Constants.CONFIG_DIR_NAME_ELECTIONINFORMATION)
					.resolve(Constants.CONFIG_DIR_NAME_BALLOTS).resolve(ballotId).resolve(Constants.CONFIG_FILE_NAME_BALLOT_JSON);

			final CreateVotingCardSetInput createVotingCardSetInput = new CreateVotingCardSetInput();
			createVotingCardSetInput.setStart(ballotBox.getString(JsonConstants.DATE_FROM));
			createVotingCardSetInput.setElectoralAuthorityID(electoralAuthorityId);
			createVotingCardSetInput.setEnd(ballotBox.getString(JsonConstants.DATE_TO));
			createVotingCardSetInput
					.setValidityPeriod(electionEvent.getJsonObject(JsonConstants.SETTINGS).getInt(JsonConstants.CERTIFICATES_VALIDITY_PERIOD));
			createVotingCardSetInput.setBasePath(configElectionEventPath.toString());
			createVotingCardSetInput.setBallotBoxID(ballotBoxId);
			createVotingCardSetInput.setBallotID(ballotId);
			createVotingCardSetInput.setBallotPath(destinationBallotFilePath.toString());
			createVotingCardSetInput.setEeID(electionEventId);
			createVotingCardSetInput.setNumberVotingCards(votingCardSet.getInt(JSON_PARAM_NAME_NR_OF_VC_TO_GENERATE));
			createVotingCardSetInput.setVerificationCardSetID(verificationCardSetId);
			createVotingCardSetInput.setVotingCardSetID(id);
			createVotingCardSetInput.setVotingCardSetAlias(votingCardSet.getString(JsonConstants.ALIAS, ""));
			createVotingCardSetInput.setPlatformRootCACertificate(PemUtils.certificateToPem(platformRootCAService.load()));
			createVotingCardSetInput.setCreateVotingCardSetCertificateProperties(getCertificateProperties());

			final VotersParametersHolder holder = votersParametersHolderAdapter.adapt(createVotingCardSetInput);
			final StartVotingCardGenerationJobResponse startJobResponse;
			try {
				startJobResponse = configurationService.startVotingCardGenerationJob(tenantId, electionEventId, holder);
			} catch (final Exception e) {
				LOGGER.error(String.format("Failed to start voting card generation job. [electionEventId=%s, ballotId=%s, ballotBoxId=%s]",
						electionEventId, ballotId, ballotBoxId), e);
				result.setSuccessful(false);
				return result;
			}

			final String jobId = startJobResponse.getJobId();
			final Future<VotingCardGenerationJobStatus> future = votingCardSetProgressManagerService.registerJob(jobId);

			// wait for job to complete to update the verification card set id and make it possible to sign the voting card set.
			waitForJobCompletion(id, jobId, future);
			result.setResult(jobId);
			result.setSuccessful(true);

		} catch (final DatabaseException e) {
			LOGGER.error("Error storing in database", e);
			result.setSuccessful(false);
		} catch (final Exception e) {
			LOGGER.error("Error processing request", e);
			result.setSuccessful(false);
		}
		return result;
	}

	private CreateVotingCardSetCertificatePropertiesContainer getCertificateProperties() throws IOException {

		LOGGER.info("Obtaining certificate properties from the following paths:");
		LOGGER.info(" {}", credentialAuthCertificateProperties);
		LOGGER.info(" {}", verificationCardSetCertificateProperties);

		final CreateVotingCardSetCertificatePropertiesContainer createVotingCardSetCertificateProperties = new CreateVotingCardSetCertificatePropertiesContainer();

		final Properties loadedCredentialAuthCertificateProperties = getCertificateParameters(credentialAuthCertificateProperties);
		final Properties loadedVerificationCardSetCertificateProperties = getCertificateParameters(verificationCardSetCertificateProperties);

		createVotingCardSetCertificateProperties.setCredentialAuthCertificateProperties(loadedCredentialAuthCertificateProperties);
		createVotingCardSetCertificateProperties.setVerificationCardSetCertificateProperties(loadedVerificationCardSetCertificateProperties);

		LOGGER.info("Obtained certificate properties");

		return createVotingCardSetCertificateProperties;
	}

	private Properties getCertificateParameters(final String path) throws IOException {
		final Properties props = new Properties();
		try (final FileInputStream fis = new FileInputStream(path)) {
			props.load(fis);
		}
		return props;
	}

	private void waitForJobCompletion(final String votingCardSetId, final String jobId, final Future<VotingCardGenerationJobStatus> future) {
		jobCompletionExecutor.submit(() -> {
			final VotingCardGenerationJobStatus votingCardGenerationJobStatus;
			try {
				votingCardGenerationJobStatus = future.get();
				if (BatchStatus.COMPLETED.equals(votingCardGenerationJobStatus.getStatus())) {
					// update the status of the voting card set
					configurationEntityStatusService.update(Status.GENERATED.name(), votingCardSetId, votingCardSetRepository);
					LOGGER.info("VotingCardSet generation job '{}' has completed successfully. Final status: {}", jobId,
							votingCardGenerationJobStatus);
				} else {
					LOGGER.error("Voting card generation job failed to complete. Response: {}", votingCardGenerationJobStatus);
				}
			} catch (final InterruptedException e) {
				LOGGER.error("We got interrupted while waiting for job completion.");
				Thread.currentThread().interrupt();
			} catch (final ExecutionException e) {
				LOGGER.error("Unexpected exception while waiting for job completion.", e);
			}
		});
	}
}
