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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
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
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.support.AnnotationConfigContextLoader;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.domain.election.EncryptionParameters;
import ch.post.it.evoting.domain.election.VoteVerificationContextData;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.JobExecutionObjectContext;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.VotersParametersHolder;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.VotersSerializationDestProvider;

/**
 * This tests a specific batch job step. By default, JobLauncherTestUtils expects to find _only one_ Job bean in the context, that's why i made the
 * batch configuration class an inner class and not a "shared" class for all tests. We could extract the class into a different file but i don't see
 * any advantage
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(loader = AnnotationConfigContextLoader.class)
class WriteVerificationDataTaskletTest {

	private static final String STEP_IN_TEST = "writeVerificationData";
	private final ObjectMapper jsonMapper = new ObjectMapper();
	@TempDir
	Path tempFolder;
	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;

	@Autowired
	private JobExecutionObjectContext stepExecutionObjectContext;

	@Autowired
	private VotersSerializationDestProvider destProviderMock;

	private File voteVerificationContextDataExpectedFile;

	@Test
	void generateVerificationDataFiles() {

		// given
		final JobParameters jobParameters = getJobInputParameters();

		// when
		final JobExecution jobExecution = jobLauncherTestUtils.launchStep(STEP_IN_TEST, jobParameters);

		// then (we want to know that the "new" job parameters are generated and
		// stored in the context)
		assertEquals(BatchStatus.COMPLETED, jobExecution.getStatus());

		assertTrue(voteVerificationContextDataExpectedFile.exists());
		try {
			jsonMapper.readValue(voteVerificationContextDataExpectedFile, VoteVerificationContextData.class);
		} catch (final IOException e) {
			fail("unexpected format for VoteVerificationContextData");
		}
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

	@BeforeEach
	public void setup() throws Exception {

		voteVerificationContextDataExpectedFile = tempFolder.resolve("vvcData").toFile();

		final VotersParametersHolder parametersHolder = mock(VotersParametersHolder.class);
		when(parametersHolder.getEncryptionParameters()).thenReturn(new EncryptionParameters("23", "11", "2"));
		when(stepExecutionObjectContext.get(any(), eq(VotersParametersHolder.class))).thenReturn(parametersHolder);
		when(destProviderMock.getVoteVerificationContextData()).thenReturn(voteVerificationContextDataExpectedFile.toPath());
	}

	@Configuration
	@EnableBatchProcessing
	@Import(TestConfigServices.class)
	static class JobConfiguration {

		@Autowired
		JobBuilderFactory jobBuilder;

		@Autowired
		StepBuilderFactory stepBuilder;

		@Autowired
		VotersSerializationDestProvider destProvider;

		@Autowired
		private JobExecutionObjectContext objectContext;

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
			return new WriteVerificationDataTasklet(destProvider, objectContext);
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
