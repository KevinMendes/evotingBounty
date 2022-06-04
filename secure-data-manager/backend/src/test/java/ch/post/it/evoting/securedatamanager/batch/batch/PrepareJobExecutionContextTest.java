/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.batch.batch;

import static ch.post.it.evoting.securedatamanager.commons.Constants.BALLOT_BOX_ID;
import static ch.post.it.evoting.securedatamanager.commons.Constants.BALLOT_ID;
import static ch.post.it.evoting.securedatamanager.commons.Constants.BASE_PATH;
import static ch.post.it.evoting.securedatamanager.commons.Constants.ELECTION_EVENT_ID;
import static ch.post.it.evoting.securedatamanager.commons.Constants.ELECTORAL_AUTHORITY_ID;
import static ch.post.it.evoting.securedatamanager.commons.Constants.JOB_INSTANCE_ID;
import static ch.post.it.evoting.securedatamanager.commons.Constants.NUMBER_VOTING_CARDS;
import static ch.post.it.evoting.securedatamanager.commons.Constants.TENANT_ID;
import static ch.post.it.evoting.securedatamanager.commons.Constants.VALIDITY_PERIOD_END;
import static ch.post.it.evoting.securedatamanager.commons.Constants.VALIDITY_PERIOD_START;
import static ch.post.it.evoting.securedatamanager.commons.Constants.VERIFICATION_CARD_SET_ID;
import static ch.post.it.evoting.securedatamanager.commons.Constants.VOTING_CARD_SET_ID;
import static ch.post.it.evoting.securedatamanager.commons.Constants.VOTING_CARD_SET_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.test.ExecutionContextTestUtils;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.VotersParametersHolder;

/**
 * This tests a specific batch job step. By default, JobLauncherTestUtils expects to find _only one_ Job bean in the context, that's why i made the
 * batch configuration class an inner class and not a "shared" class for all tests. We could extract the class into a different file but i don't see
 * any advantage
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
class PrepareJobExecutionContextTest {

	private static final String STEP_IN_TEST = "prepareJobExecutionContextStep";

	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;

	@Test
	void addInputParametersToJobExecutionContext() {

		// given
		final JobParameters jobParameters = getJobInputParameters();

		// when
		final JobExecution jobExecution = jobLauncherTestUtils.launchStep(STEP_IN_TEST, jobParameters);

		// then
		assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());
		assertNotNull(ExecutionContextTestUtils.getValueFromJob(jobExecution, JOB_INSTANCE_ID));
		assertNotNull(ExecutionContextTestUtils.getValueFromJob(jobExecution, TENANT_ID));
		assertNotNull(ExecutionContextTestUtils.getValueFromJob(jobExecution, ELECTION_EVENT_ID));
		assertNotNull(ExecutionContextTestUtils.getValueFromJob(jobExecution, BALLOT_BOX_ID));
		assertNotNull(ExecutionContextTestUtils.getValueFromJob(jobExecution, BALLOT_ID));
		assertNotNull(ExecutionContextTestUtils.getValueFromJob(jobExecution, ELECTORAL_AUTHORITY_ID));
		assertNotNull(ExecutionContextTestUtils.getValueFromJob(jobExecution, VOTING_CARD_SET_ID));
		assertNotNull(ExecutionContextTestUtils.getValueFromJob(jobExecution, NUMBER_VOTING_CARDS));
		assertNotNull(ExecutionContextTestUtils.getValueFromJob(jobExecution, VOTING_CARD_SET_NAME));
		assertNotNull(ExecutionContextTestUtils.getValueFromJob(jobExecution, VALIDITY_PERIOD_START));
		assertNotNull(ExecutionContextTestUtils.getValueFromJob(jobExecution, VALIDITY_PERIOD_END));
		assertNotNull(ExecutionContextTestUtils.getValueFromJob(jobExecution, BASE_PATH));
		assertNotNull(ExecutionContextTestUtils.getValueFromJob(jobExecution, VERIFICATION_CARD_SET_ID));

	}

	private JobParameters getJobInputParameters() {
		final JobParametersBuilder jobParametersBuilder = new JobParametersBuilder();
		jobParametersBuilder.addString(JOB_INSTANCE_ID, UUID.randomUUID().toString(), true);
		jobParametersBuilder.addString(TENANT_ID, "tenantId", true);
		jobParametersBuilder.addString(ELECTION_EVENT_ID, "electionEventId", true);
		jobParametersBuilder.addString(BALLOT_BOX_ID, "ballotBoxId", true);
		jobParametersBuilder.addString(BALLOT_ID, "ballotId");
		jobParametersBuilder.addString(ELECTORAL_AUTHORITY_ID, "electoralAuthorityId");
		jobParametersBuilder.addString(VOTING_CARD_SET_ID, "votingCardSetId");
		jobParametersBuilder.addString(NUMBER_VOTING_CARDS, "10");
		jobParametersBuilder.addString(VOTING_CARD_SET_NAME, "votingCardSetAlias");
		jobParametersBuilder.addString(VALIDITY_PERIOD_START, "2017-02-15");
		jobParametersBuilder.addString(VALIDITY_PERIOD_END, "2018-02-15");
		jobParametersBuilder.addString(BASE_PATH, "absoluteOutputPath");
		jobParametersBuilder.addString(VERIFICATION_CARD_SET_ID, "verificationCardSetId");
		return jobParametersBuilder.toJobParameters();
	}

	@Configuration
	@EnableBatchProcessing
	@Import(TestConfigServices.class)
	static class JobConfiguration {

		@Autowired
		JobBuilderFactory jobBuilder;

		@Autowired
		StepBuilderFactory stepBuilder;

		@Bean
		JobLauncherTestUtils testUtils() {
			return new JobLauncherTestUtils();
		}

		@Bean
		public Step step(final Tasklet tasklet) {
			return stepBuilder.get(STEP_IN_TEST).tasklet(tasklet).build();
		}

		@Bean
		public Tasklet tasklet() {
			return new PrepareJobExecutionContextTasklet();
		}

		@Bean
		Job job(final Step step) {
			return jobBuilder.get("job").start(step).build();
		}

		@Bean
		VotersParametersHolder holder() {
			return mock(VotersParametersHolder.class);
		}
	}
}
