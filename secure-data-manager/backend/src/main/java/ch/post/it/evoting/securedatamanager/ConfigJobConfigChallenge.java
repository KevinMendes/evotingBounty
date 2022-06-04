/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager;

import java.nio.file.Path;
import java.util.Arrays;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.task.TaskExecutor;

import ch.post.it.evoting.securedatamanager.batch.batch.writers.ChallengePrintingDataWriter;
import ch.post.it.evoting.securedatamanager.batch.batch.writers.CompositeOutputWriter;
import ch.post.it.evoting.securedatamanager.commons.Constants;

@Configuration("configuration-job-config-challenge")
@Profile("challenge")
public class ConfigJobConfigChallenge extends ConfigJobConfig {

	@Bean
	Job job(final CommonBatchInfrastructure commonBatchInfrastructure, final Step verificationAndCombinationStep, final Step generateVotingCardStep,
			final Step writeOutputStep, final TaskExecutor stepExecutor, final JobExecutionListener jobExecutionListener,
			final Step prepareJobExecutionContextStep, final Step prepareVotingCardGenerationDataStep, final Step writeVerificationDataStep) {

		return commonBatchInfrastructure
				.getJobBuilder(Constants.VOTING_CARD_SET_GENERATION + "-challenge", new RunIdIncrementer(), jobExecutionListener)
				.start(preProcessingFlow(prepareJobExecutionContextStep, prepareVotingCardGenerationDataStep).build())
				.next(verificationCombinationAndGenerationFlow(verificationAndCombinationStep, generateVotingCardStep, writeOutputStep,
						stepExecutor).build())
				.next(postProcessingFlow(writeVerificationDataStep).build())
				.end().build();
	}

	@Bean
	@JobScope
	CompositeOutputWriter compositeOutputWriter(final CommonBatchInfrastructure commonBatchInfrastructure,
			@Value("#{jobExecutionContext['" + Constants.BASE_PATH + "']}")
			final String outputPath,
			@Value("#{jobExecutionContext['" + Constants.VOTING_CARD_SET_ID + "']}")
			final String votingCardSetId,
			@Value("#{jobExecutionContext['" + Constants.VERIFICATION_CARD_SET_ID + "']}")
			final String verificationCardSetId,
			@Value("${maximum.number.credentials.per.file:1000}")
			final int maxNumCredentialsPerFile) {

		final CompositeOutputWriter writer = new CompositeOutputWriter();
		writer.setDelegates(Arrays.asList(
				voterInformationWriter(commonBatchInfrastructure, outputPath, votingCardSetId, verificationCardSetId, maxNumCredentialsPerFile),
				credentialDataWriter(commonBatchInfrastructure, outputPath, votingCardSetId, verificationCardSetId, maxNumCredentialsPerFile),
				codesMappingTableWriter(commonBatchInfrastructure, outputPath, votingCardSetId, verificationCardSetId, maxNumCredentialsPerFile),
				challengePrintingDataWriter(commonBatchInfrastructure, outputPath, votingCardSetId, verificationCardSetId),
				verificationCardDataWriter(commonBatchInfrastructure, outputPath, votingCardSetId, verificationCardSetId, maxNumCredentialsPerFile),
				extendedAuthenticationWriter(commonBatchInfrastructure, outputPath, votingCardSetId, verificationCardSetId,
						maxNumCredentialsPerFile)));
		return writer;
	}

	private ChallengePrintingDataWriter challengePrintingDataWriter(final CommonBatchInfrastructure commonBatchInfrastructure,
			final String baseOutputPath, final String votingCardSetId, final String verificationCardSetId) {

		final Path path = commonBatchInfrastructure.getDataSerializationProvider(baseOutputPath, votingCardSetId, verificationCardSetId)
				.getTempPrintingData("");

		return new ChallengePrintingDataWriter(path);
	}
}
