/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.commands.api;

import static ch.post.it.evoting.securedatamanager.commons.Constants.BALLOT_BOX_ID;
import static ch.post.it.evoting.securedatamanager.commons.Constants.BALLOT_ID;
import static ch.post.it.evoting.securedatamanager.commons.Constants.ELECTION_EVENT_ID;
import static ch.post.it.evoting.securedatamanager.commons.Constants.ELECTORAL_AUTHORITY_ID;
import static ch.post.it.evoting.securedatamanager.commons.Constants.GENERATED_VC_COUNT;
import static ch.post.it.evoting.securedatamanager.commons.Constants.JOB_INSTANCE_ID;
import static ch.post.it.evoting.securedatamanager.commons.Constants.NUMBER_VOTING_CARDS;
import static ch.post.it.evoting.securedatamanager.commons.Constants.TENANT_ID;
import static ch.post.it.evoting.securedatamanager.commons.Constants.VALIDITY_PERIOD_END;
import static ch.post.it.evoting.securedatamanager.commons.Constants.VALIDITY_PERIOD_START;
import static ch.post.it.evoting.securedatamanager.commons.Constants.VERIFICATION_CARD_SET_ID;
import static ch.post.it.evoting.securedatamanager.commons.Constants.VOTING_CARD_SET_ID;
import static ch.post.it.evoting.securedatamanager.commons.Constants.VOTING_CARD_SET_NAME;

import java.time.Instant;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Service;

import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.commons.domain.StartVotingCardGenerationJobResponse;
import ch.post.it.evoting.securedatamanager.commons.domain.spring.batch.SensitiveAwareJobParametersBuilder;
import ch.post.it.evoting.securedatamanager.config.commons.config.commons.progress.JobProgressDetails;
import ch.post.it.evoting.securedatamanager.config.commons.config.exceptions.ConfigurationEngineException;
import ch.post.it.evoting.securedatamanager.config.engine.commands.progress.ProgressManager;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.JobExecutionObjectContext;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.PropertiesBasedJobSelectionStrategy;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.VotersParametersHolder;
import ch.post.it.evoting.securedatamanager.services.domain.model.config.VotingCardGenerationJobStatus;

@Service
public class ConfigurationService {

	private static final String VOTINGCARDSET_GENERATION = "votingcardset-generation-*";

	private final JobExecutionObjectContext jobExecutionObjectContext;
	private final ProgressManager progressManager;
	private final JobLauncher jobLauncher;
	private final JobExplorer jobExplorer;
	private final PropertiesBasedJobSelectionStrategy jobSelectionStrategy;
	private final JobRegistry jobRegistry;

	public ConfigurationService(final JobExecutionObjectContext jobExecutionObjectContext, final ProgressManager progressManager,
			final JobLauncher jobLauncher, final JobExplorer jobExplorer, final PropertiesBasedJobSelectionStrategy jobSelectionStrategy,
			final JobRegistry jobRegistry) {
		this.jobExecutionObjectContext = jobExecutionObjectContext;
		this.progressManager = progressManager;
		this.jobLauncher = jobLauncher;
		this.jobExplorer = jobExplorer;
		this.jobSelectionStrategy = jobSelectionStrategy;
		this.jobRegistry = jobRegistry;
	}

	/**
	 * Get list of all Voting Card Generation jobs for the specified tenant and election event
	 *
	 * @return list of all Voting Card Generation jobs for the specified tenant and election event
	 */
	public List<VotingCardGenerationJobStatus> getJobs() {
		final Function<JobExecution, VotingCardGenerationJobStatus> mapToJobStatus = mapJobExecutionToJobStatus();

		final List<JobInstance> jobInstances = jobExplorer.findJobInstancesByJobName(VOTINGCARDSET_GENERATION, 0, Integer.MAX_VALUE);
		// one way of having a count at the end of the stream

		return jobInstances.stream().map(jobExplorer::getJobExecutions).flatMap(Collection::stream).map(mapToJobStatus).collect(Collectors.toList());
	}

	/**
	 * Start a new voting card generation Spring Batch job with the specified input parameters.
	 *
	 * @param holder holder class for all the needed job parameters
	 * @return object with the initial status of the job
	 */
	public StartVotingCardGenerationJobResponse startVotingCardGenerationJob(final String tenantId, final String electionEventId,
			final VotersParametersHolder holder) {

		final JobParameters jobParams = prepareJobParameters(tenantId, electionEventId, holder);
		final String jobInstanceId = jobParams.getString(JOB_INSTANCE_ID);
		jobExecutionObjectContext.put(jobInstanceId, holder, VotersParametersHolder.class);

		final UUID jobId = UUID.fromString(jobInstanceId);
		try {
			final String jobQualifier = jobSelectionStrategy.select();
			final Job job = jobRegistry.getJob(jobQualifier);
			final JobExecution jobExecution = jobLauncher.run(job, jobParams);

			final BatchStatus jobStatus = jobExecution.getStatus();
			final Instant created = jobExecution.getCreateTime().toInstant();
			final String createdStr = created.atZone(ZoneId.systemDefault()).toString();

			final long numberOfVotingCards = Long.parseLong(jobParams.getString(NUMBER_VOTING_CARDS));
			progressManager.registerJob(jobId, new JobProgressDetails(jobId, numberOfVotingCards));

			return new StartVotingCardGenerationJobResponse(jobInstanceId, jobStatus, createdStr);
		} catch (final JobExecutionException e) {
			// in case we registered the job, remove it.
			progressManager.unregisterJob(jobId);
			throw new ConfigurationEngineException(e);
		}
	}

	private JobParameters prepareJobParameters(final String tenantId, final String electionEventId, final VotersParametersHolder input) {

		final SensitiveAwareJobParametersBuilder jobParametersBuilder = new SensitiveAwareJobParametersBuilder();
		jobParametersBuilder.addString(JOB_INSTANCE_ID, UUID.randomUUID().toString(), true);
		jobParametersBuilder.addString(TENANT_ID, tenantId, true);
		jobParametersBuilder.addString(ELECTION_EVENT_ID, electionEventId, true);
		jobParametersBuilder.addString(BALLOT_BOX_ID, input.getBallotBoxID(), true);
		jobParametersBuilder.addString(BALLOT_ID, input.getBallotID());
		jobParametersBuilder.addString(ELECTORAL_AUTHORITY_ID, input.getElectoralAuthorityID());
		jobParametersBuilder.addString(VOTING_CARD_SET_ID, input.getVotingCardSetID());
		jobParametersBuilder.addString(VERIFICATION_CARD_SET_ID, input.getVerificationCardSetID());
		jobParametersBuilder.addString(NUMBER_VOTING_CARDS, Integer.toString(input.getNumberVotingCards()));
		jobParametersBuilder.addString(VOTING_CARD_SET_NAME, input.getVotingCardSetAlias());
		jobParametersBuilder.addString(VALIDITY_PERIOD_START, input.getCertificatesStartValidityPeriod().toString());
		jobParametersBuilder.addString(VALIDITY_PERIOD_END, input.getCertificatesEndValidityPeriod().toString());
		jobParametersBuilder.addString(Constants.BASE_PATH, input.getAbsoluteBasePath().toString());

		jobParametersBuilder.addString(Constants.PLATFORM_ROOT_CA_CERTIFICATE, input.getPlatformRootCACertificate());

		return jobParametersBuilder.toJobParameters();
	}

	private Function<JobExecution, VotingCardGenerationJobStatus> mapJobExecutionToJobStatus() {
		return je -> {
			if (BatchStatus.UNKNOWN.equals(je.getStatus())) {
				return VotingCardGenerationJobStatus.UNKNOWN;
			} else {
				final ExecutionContext executionContext = je.getExecutionContext();
				// JobParameters by id because it might get a request before it runs the
				// "job preparation task" where it populates the job execution context.
				// Meanwhile, the execution context is empty and will return null or empty.
				final String id = je.getJobParameters().getString(JOB_INSTANCE_ID);
				final BatchStatus status = je.getStatus();
				final String statusDetails = je.getStatus().isUnsuccessful() ? je.getExitStatus().getExitDescription() : null;
				// start time may be null if the job has not started yet
				final Instant startTime = Optional.ofNullable(je.getStartTime()).orElse(Date.from(Instant.EPOCH)).toInstant();
				final Optional<JobProgressDetails> details = progressManager.getJobProgress(UUID.fromString(id));
				final String verificationCardSetId = executionContext.getString(VERIFICATION_CARD_SET_ID, null);
				final int votingCardCount = executionContext.getInt(GENERATED_VC_COUNT, 0);
				final int errorCount = executionContext.getInt(Constants.ERROR_COUNT, 0);
				return new VotingCardGenerationJobStatus(UUID.fromString(id), status, startTime, statusDetails, details.orElse(null),
						verificationCardSetId, votingCardCount, errorCount);
			}
		};
	}

}
