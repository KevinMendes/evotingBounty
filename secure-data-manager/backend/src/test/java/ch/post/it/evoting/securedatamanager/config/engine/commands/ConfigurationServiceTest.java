/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.config.engine.commands;

import static ch.post.it.evoting.securedatamanager.commons.Constants.ELECTION_EVENT_ID;
import static ch.post.it.evoting.securedatamanager.commons.Constants.JOB_INSTANCE_ID;
import static ch.post.it.evoting.securedatamanager.commons.Constants.TENANT_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;

import ch.post.it.evoting.securedatamanager.config.engine.commands.api.ConfigurationService;
import ch.post.it.evoting.securedatamanager.config.engine.commands.progress.ProgressManager;
import ch.post.it.evoting.securedatamanager.services.domain.model.config.VotingCardGenerationJobStatus;

@ExtendWith(MockitoExtension.class)
class ConfigurationServiceTest {

	@Mock
	JobExplorer jobExplorerMock;
	@Mock
	ProgressManager progressManagerMock;
	@InjectMocks
	@Spy
	private ConfigurationService configurationService;

	@Test
	void returnListOfJobStatusAsStartingWhenJobsAreCreated() {

		// given
		final String tenantId = "fake";
		final String electionEventid = "fake";
		final UUID jobId = UUID.randomUUID();

		final List<JobInstance> fakeJobInstances = new ArrayList<>();
		final JobInstance fakeJobInstance = new JobInstance(1L, "fakeJob");
		fakeJobInstances.add(fakeJobInstance);

		final JobParameters fakeJobParameters = getFakeJobParameters(tenantId, electionEventid, jobId.toString());
		final JobExecution fakeJobExecution = new JobExecution(fakeJobInstance, 1L, fakeJobParameters, null);

		final List<JobExecution> fakeJobExecutions = new ArrayList<>();
		fakeJobExecutions.add(fakeJobExecution);

		when(jobExplorerMock.findJobInstancesByJobName(anyString(), anyInt(), anyInt())).thenReturn(fakeJobInstances);
		when(jobExplorerMock.getJobExecutions(any())).thenReturn(fakeJobExecutions);
		when(progressManagerMock.getJobProgress(any())).thenReturn(Optional.empty());

		// when

		final List<VotingCardGenerationJobStatus> jobs = configurationService.getJobs();

		// then
		assertTrue(jobs.size() > 0);
		jobs.forEach(j -> {
			assertEquals(jobId, UUID.fromString(j.getJobId()));
			assertEquals(BatchStatus.STARTING, j.getStatus());
			assertEquals(0, j.getErrorCount());
			assertEquals(0, j.getGeneratedCount());
			assertNull(j.getProgressDetails());
		});

	}

	private JobParameters getFakeJobParameters(final String tenantId, final String electionEventId, final String jobId) {
		final JobParametersBuilder builder = new JobParametersBuilder();
		builder.addString(TENANT_ID, tenantId);
		builder.addString(ELECTION_EVENT_ID, electionEventId);
		builder.addString(JOB_INSTANCE_ID, jobId);
		return builder.toJobParameters();
	}

}
