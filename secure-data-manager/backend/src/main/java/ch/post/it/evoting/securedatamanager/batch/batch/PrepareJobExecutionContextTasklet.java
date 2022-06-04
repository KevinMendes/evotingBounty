/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.batch.batch;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import ch.post.it.evoting.securedatamanager.commons.Constants;

@Component
public class PrepareJobExecutionContextTasklet implements Tasklet {

	private static final Logger LOGGER = LoggerFactory.getLogger(PrepareJobExecutionContextTasklet.class);

	@Override
	public RepeatStatus execute(final StepContribution contribution, final ChunkContext chunkContext) {

		try {
			final Map<String, Object> jobParameters = chunkContext.getStepContext().getJobParameters();
			final VotingCardGenerationJobExecutionContext jobExecutionContext = new VotingCardGenerationJobExecutionContext(
					chunkContext.getStepContext().getStepExecution().getJobExecution().getExecutionContext());

			final String jobInstanceId = (String) jobParameters.get(Constants.JOB_INSTANCE_ID);
			jobExecutionContext.setJobInstanceId(jobInstanceId);
			final String tenantId = (String) jobParameters.get(Constants.TENANT_ID);
			jobExecutionContext.setTenantId(tenantId);
			final String electionEventId = (String) jobParameters.get(Constants.ELECTION_EVENT_ID);
			jobExecutionContext.setElectionEventId(electionEventId);
			final String ballotBoxId = (String) jobParameters.get(Constants.BALLOT_BOX_ID);
			jobExecutionContext.setBallotBoxId(ballotBoxId);
			final String ballotId = (String) jobParameters.get(Constants.BALLOT_ID);
			jobExecutionContext.setBallotId(ballotId);
			final String basePath = (String) jobParameters.get(Constants.BASE_PATH);
			jobExecutionContext.setBasePath(basePath);
			final String votingCardSetId = (String) jobParameters.get(Constants.VOTING_CARD_SET_ID);
			jobExecutionContext.setVotingCardSetId(votingCardSetId);
			final String electoralAuthorityId = (String) jobParameters.get(Constants.ELECTORAL_AUTHORITY_ID);
			jobExecutionContext.setElectoralAuthorityId(electoralAuthorityId);

			final int numberOfVotingCards = Integer.parseInt((String) jobParameters.get(Constants.NUMBER_VOTING_CARDS));
			jobExecutionContext.setNumberOfVotingCards(numberOfVotingCards);

			final String votingCardSetName = (String) jobParameters.get(Constants.VOTING_CARD_SET_NAME);
			jobExecutionContext.setVotingCardSetName(votingCardSetName);

			final String startValidityPeriod = (String) jobParameters.get(Constants.VALIDITY_PERIOD_START);
			final String endValidityPeriod = (String) jobParameters.get(Constants.VALIDITY_PERIOD_START);

			jobExecutionContext.setValidityPeriodStart(startValidityPeriod);
			jobExecutionContext.setValidityPeriodEnd(endValidityPeriod);

			final String verificationCardSetId = (String) jobParameters.get(Constants.VERIFICATION_CARD_SET_ID);
			jobExecutionContext.setVerificationCardSetId(verificationCardSetId);

			final String platformRootCACertificate = (String) jobParameters.get(Constants.PLATFORM_ROOT_CA_CERTIFICATE);
			jobExecutionContext.setPlatformRootCACertificate(platformRootCACertificate);

		} catch (final Exception e) {
			LOGGER.error("Prepare job execution task failed.", e);
			throw e;
		}

		return RepeatStatus.FINISHED;
	}
}
