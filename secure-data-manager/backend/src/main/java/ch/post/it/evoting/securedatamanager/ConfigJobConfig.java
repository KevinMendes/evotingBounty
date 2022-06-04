/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager;

import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.api.primitives.PrimitivesServiceAPI;
import ch.post.it.evoting.cryptolib.api.securerandom.CryptoAPIRandomString;
import ch.post.it.evoting.cryptolib.certificates.factory.X509CertificateGenerator;
import ch.post.it.evoting.cryptolib.certificates.utils.CryptolibPayloadSigningCertificateValidator;
import ch.post.it.evoting.cryptolib.certificates.utils.PayloadSigningCertificateValidator;
import ch.post.it.evoting.cryptolib.extendedkeystore.service.ExtendedKeyStoreService;
import ch.post.it.evoting.domain.election.payload.verify.CryptolibPayloadVerifier;
import ch.post.it.evoting.domain.election.payload.verify.PayloadVerifier;
import ch.post.it.evoting.securedatamanager.batch.batch.EncryptedLongReturnCodesCombiner;
import ch.post.it.evoting.securedatamanager.batch.batch.GeneratedVotingCardOutput;
import ch.post.it.evoting.securedatamanager.batch.batch.NodeContributions;
import ch.post.it.evoting.securedatamanager.batch.batch.VotingCardGenerationJobExecutionContext;
import ch.post.it.evoting.securedatamanager.batch.batch.VotingCardGenerator;
import ch.post.it.evoting.securedatamanager.batch.batch.WriteVerificationDataTasklet;
import ch.post.it.evoting.securedatamanager.batch.batch.listeners.VotingCardGeneratedOutputWriterListener;
import ch.post.it.evoting.securedatamanager.batch.batch.readers.ComputedValuesReader;
import ch.post.it.evoting.securedatamanager.batch.batch.readers.NodeContributionsReader;
import ch.post.it.evoting.securedatamanager.batch.batch.readers.OutputQueueReader;
import ch.post.it.evoting.securedatamanager.batch.batch.writers.CodesMappingTableWriter;
import ch.post.it.evoting.securedatamanager.batch.batch.writers.CompositeOutputWriter;
import ch.post.it.evoting.securedatamanager.batch.batch.writers.CredentialDataWriter;
import ch.post.it.evoting.securedatamanager.batch.batch.writers.ExtendedAuthenticationWriter;
import ch.post.it.evoting.securedatamanager.batch.batch.writers.GeneratedVotingCardOutputWriter;
import ch.post.it.evoting.securedatamanager.batch.batch.writers.VerificationCardDataWriter;
import ch.post.it.evoting.securedatamanager.batch.batch.writers.VoterInformationWriter;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.commons.domain.VcIdCombinedReturnCodesGenerationValues;
import ch.post.it.evoting.securedatamanager.config.commons.config.model.authentication.AuthenticationKeyGenerator;
import ch.post.it.evoting.securedatamanager.config.commons.config.model.authentication.AuthenticationKeyGeneratorStrategyType;
import ch.post.it.evoting.securedatamanager.config.commons.config.model.authentication.ChallengeGenerator;
import ch.post.it.evoting.securedatamanager.config.engine.commands.progress.ProgressManager;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.JobExecutionObjectContext;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.NodeContributionsPath;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.VotersSerializationDestProvider;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.datapacks.generators.VerificationCardCredentialDataPackGenerator;
import ch.post.it.evoting.securedatamanager.config.engine.model.authentication.ChallengeGeneratorStrategyType;
import ch.post.it.evoting.securedatamanager.config.engine.model.authentication.service.AuthenticationGeneratorFactory;
import ch.post.it.evoting.securedatamanager.config.engine.model.authentication.service.ChallengeGeneratorFactory;
import ch.post.it.evoting.securedatamanager.config.engine.model.authentication.service.SequentialProvidedChallengeSource;

/**
 * Configuration class for voting card generation jobs
 */
public class ConfigJobConfig {

	@Bean("verificationCardCredentialDataPackGeneratorWithJobScope")
	@JobScope
	public VerificationCardCredentialDataPackGenerator verificationCardCredentialDataPackGeneratorWithJobScope(
			final PrimitivesServiceAPI primitivesService, final AsymmetricServiceAPI asymmetricService,
			final X509CertificateGenerator certificatesGenerator) {

		final ExtendedKeyStoreService keyStoreService = new ExtendedKeyStoreService();
		final CryptoAPIRandomString cryptoRandomString = primitivesService.get32CharAlphabetCryptoRandomString();

		return new VerificationCardCredentialDataPackGenerator(asymmetricService, cryptoRandomString, certificatesGenerator, keyStoreService
		);

	}

	@Bean("certificateValidatorWithJobScope")
	@JobScope
	public PayloadSigningCertificateValidator certificateValidatorWithJobScope() {
		return new CryptolibPayloadSigningCertificateValidator();
	}

	@Bean("payloadVerifierWithJobScope")
	@JobScope
	public PayloadVerifier payloadVerifierWithJobScope(
			final AsymmetricServiceAPI asymmetricServiceAPI,
			@Qualifier("certificateValidatorWithJobScope")
			final PayloadSigningCertificateValidator certificateValidator) {
		return new CryptolibPayloadVerifier(asymmetricServiceAPI, certificateValidator);
	}

	@Bean
	@JobScope
	NodeContributionsReader nodeContributionsReader(final CommonBatchInfrastructure commonBatchInfrastructure,
			@Value("#{jobExecutionContext['" + Constants.BASE_PATH + "']}")
			final String basePath,
			@Value("#{jobExecution}")
			final JobExecution jobExecution) {

		final VotingCardGenerationJobExecutionContext jobExecutionContext = new VotingCardGenerationJobExecutionContext(
				jobExecution.getExecutionContext());

		final List<NodeContributionsPath> allComputeInputAndOutputFiles = commonBatchInfrastructure
				.getDataSerializationProvider(basePath, jobExecutionContext.getVotingCardSetId(), jobExecutionContext.getVerificationCardSetId())
				.getNodeContributions();

		return new NodeContributionsReader(allComputeInputAndOutputFiles);
	}

	/**
	 * This step loads all input data received from the request, necessary for voting card generation, in to the spring batch job execution context
	 *
	 * @return the step
	 */
	@Bean
	public Step prepareJobExecutionContextStep(final CommonBatchInfrastructure commonBatchInfrastructure,
			final Tasklet prepareJobExecutionContextTasklet) {
		return commonBatchInfrastructure.getStepBuilder("prepareExecutionContext").tasklet(prepareJobExecutionContextTasklet).build();
	}

	/**
	 * This step creates and stores in a 'cache' bean some extra complex classes needed for the voting card generation that are not possible (or easy)
	 * to store in the standard spring batch job execution context.
	 *
	 * @return the step
	 */
	@Bean
	public Step prepareVotingCardGenerationDataStep(final CommonBatchInfrastructure commonBatchInfrastructure,
			final Tasklet prepareVotingCardGenerationDataTasklet) {
		return commonBatchInfrastructure.getStepBuilder("prepareVotingCardGenerationDataStep").tasklet(prepareVotingCardGenerationDataTasklet)
				.build();
	}

	@Bean
	public Step verificationAndCombinationStep(final ItemWriter<List<VcIdCombinedReturnCodesGenerationValues>> computedValuesWriter,
			final CommonBatchInfrastructure commonBatchInfrastructure, final NodeContributionsReader nodeContributionsReader,
			final EncryptedLongReturnCodesCombiner encryptedLongReturnCodesCombiner, final StepExecutionListener nodeContributionsStepListener) {
		final CompositeItemWriter<List<VcIdCombinedReturnCodesGenerationValues>> compositeComptuedValuesWriter = new CompositeItemWriter<>();
		compositeComptuedValuesWriter.setDelegates(Collections.singletonList(computedValuesWriter));

		return commonBatchInfrastructure.getStepBuilder("verificationAndCombinationStep")
				.<NodeContributions, List<VcIdCombinedReturnCodesGenerationValues>>chunk(1)
				.reader(nodeContributionsReader).processor(encryptedLongReturnCodesCombiner)
				.writer(compositeComptuedValuesWriter).listener(nodeContributionsStepListener).build();
	}

	/**
	 * This step generates the voting cards and other associated data one by one and puts them in a queue used as a buffer for writing to the various
	 * output files.
	 *
	 * @return the step
	 */
	@Bean
	public Step generateVotingCardStep(final CommonBatchInfrastructure commonBatchInfrastructure, final ComputedValuesReader computedValuesReader,
			final VotingCardGenerator votingCardGenerator, final GeneratedVotingCardOutputWriter generatedVotingCardOutputWriter,
			final TaskExecutor stepExecutor, final StepExecutionListener votingCardGenerationStepListener,
			@Value("${spring.batch.steps.concurrency:4}")
			final String stepConcurrency) {
		return commonBatchInfrastructure.getStepBuilder("generateVotingCardStep")
				.<VcIdCombinedReturnCodesGenerationValues, GeneratedVotingCardOutput>chunk(1).reader(computedValuesReader)
				.processor(votingCardGenerator).writer(generatedVotingCardOutputWriter)
				.taskExecutor(stepExecutor)
				.throttleLimit(Integer.parseInt(stepConcurrency)).listener(votingCardGenerationStepListener).build();
	}

	@Bean
	public Step writeOutputStep(final CommonBatchInfrastructure commonBatchInfrastructure, final OutputQueueReader generationOutputQueueReader,
			final CompositeOutputWriter compositeOutputWriter,
			final VotingCardGeneratedOutputWriterListener votingCardGeneratedOutputWriterListener) {
		return commonBatchInfrastructure.getStepBuilder("writeOutputStep").<GeneratedVotingCardOutput, GeneratedVotingCardOutput>chunk(1)
				.reader(generationOutputQueueReader)
				.writer(compositeOutputWriter)
				.listener(votingCardGeneratedOutputWriterListener).build();
	}

	@Bean
	public Step writeVerificationDataStep(final CommonBatchInfrastructure commonBatchInfrastructure, final Tasklet writeVerificationDataTasklet) {
		return commonBatchInfrastructure.getStepBuilder("writeVerificationData")
				.tasklet(writeVerificationDataTasklet).build();
	}

	@Bean
	@JobScope
	Tasklet writeVerificationDataTasklet(final CommonBatchInfrastructure commonBatchInfrastructure, final JobExecutionObjectContext objectContext,
			@Value("#{jobExecutionContext['" + Constants.BASE_PATH + "']}")
			final String outputPath,
			@Value("#{jobExecutionContext['" + Constants.VOTING_CARD_SET_ID + "']}")
			final String votingCardSetId,
			@Value("#{jobExecutionContext['" + Constants.VERIFICATION_CARD_SET_ID + "']}")
			final String verificationCardSetId) {

		final VotersSerializationDestProvider destProvider = commonBatchInfrastructure
				.getDataSerializationProvider(outputPath, votingCardSetId, verificationCardSetId);
		return new WriteVerificationDataTasklet(destProvider, objectContext);
	}

	@Bean
	@JobScope
	GeneratedVotingCardOutputWriter generatedVotingCardOutputWriter(final BlockingQueue<GeneratedVotingCardOutput> generationOutputQueue) {
		return new GeneratedVotingCardOutputWriter(generationOutputQueue);
	}

	@Bean
	@JobScope
	VotingCardGeneratedOutputWriterListener votingCardGeneratedOutputWriterListener(final ProgressManager progressManager,
			@Value("#{jobExecutionContext['" + Constants.JOB_INSTANCE_ID + "']}")
			final String jobInstanceId,
			@Value("#{jobExecution}")
			final JobExecution jobExecution) {

		return new VotingCardGeneratedOutputWriterListener(UUID.fromString(jobInstanceId), jobExecution.getExecutionContext(), progressManager);
	}

	@Bean
	@JobScope
	BlockingQueue<GeneratedVotingCardOutput> generationOutputQueue() {
		return new LinkedBlockingQueue<>();
	}

	@Bean
	@JobScope
	OutputQueueReader generationOutputQueueReader(final BlockingQueue<GeneratedVotingCardOutput> generationOutputQueue) {
		return new OutputQueueReader(generationOutputQueue);
	}

	@Bean
	@JobScope
	BlockingQueue<VcIdCombinedReturnCodesGenerationValues> computedValuesQueue(
			@Value("${spring.batch.steps.queue.capacity:1000}")
			final int queueCapacity) {
		return new LinkedBlockingQueue<>(queueCapacity);
	}

	@Bean
	@JobScope
	public SequentialProvidedChallengeSource providedChallengeSource(final CommonBatchInfrastructure commonBatchInfrastructure,
			@Value("#{jobExecutionContext['" + Constants.BASE_PATH + "']}")
			final String outputPath,
			@Value("#{jobExecutionContext['" + Constants.VOTING_CARD_SET_ID + "']}")
			final String votingCardSetId,
			@Value("#{jobExecutionContext['" + Constants.VERIFICATION_CARD_SET_ID + "']}")
			final String verificationCardSetId) {

		final Path providedChallengePath = commonBatchInfrastructure.getDataSerializationProvider(outputPath, votingCardSetId, verificationCardSetId)
				.getProvidedChallenge();
		return new SequentialProvidedChallengeSource(providedChallengePath);
	}

	@Bean
	@JobScope
	ChallengeGenerator challengeGenerator(final ChallengeGeneratorFactory challengeGeneratorFactory,
			@Value("${challenge.generator.type}")
			final String challengeGeneratorType) {
		final ChallengeGeneratorStrategyType challengeGeneratorStrategyType = ChallengeGeneratorStrategyType.valueOf(challengeGeneratorType);

		return challengeGeneratorFactory.createStrategy(challengeGeneratorStrategyType);
	}

	@Bean
	@JobScope
	AuthenticationKeyGenerator authenticationKeyGenerator(final AuthenticationGeneratorFactory authenticationGeneratorFactory,
			@Value("${auth.generator.type}")
			final String authenticationGeneratorType) {
		final AuthenticationKeyGeneratorStrategyType authenticationKeyGeneratorStrategyType = AuthenticationKeyGeneratorStrategyType.valueOf(
				authenticationGeneratorType);

		return authenticationGeneratorFactory.createStrategy(authenticationKeyGeneratorStrategyType);
	}

	protected FlowBuilder<Flow> verificationCombinationAndGenerationFlow(final Step verificationAndCombinationStep, final Step generateVotingCardStep,
			final Step writeOutputStep, final TaskExecutor stepExecutor) {
		final Flow verificationAndCombinationFlow = new FlowBuilder<Flow>("verificationAndCombinationFlow").start(verificationAndCombinationStep)
				.end();
		final Flow generationFlow = new FlowBuilder<Flow>("generationFlow").start(generateVotingCardStep).end();
		final Flow writingFlow = new FlowBuilder<Flow>("writingFlow").start(writeOutputStep).end();

		return new FlowBuilder<Flow>("processingFlow").split(stepExecutor).add(verificationAndCombinationFlow, generationFlow, writingFlow);
	}

	protected FlowBuilder<Flow> postProcessingFlow(final Step writeVerificationDataStep) {
		return new FlowBuilder<Flow>("endFlow").start(writeVerificationDataStep);
	}

	protected FlowBuilder<Flow> preProcessingFlow(final Step prepareJobExecutionContextStep, final Step prepareVotingCardGenerationDataStep) {
		return new FlowBuilder<Flow>("preparation").from(prepareJobExecutionContextStep).next(prepareVotingCardGenerationDataStep);
	}

	protected VerificationCardDataWriter verificationCardDataWriter(final CommonBatchInfrastructure commonBatchInfrastructure, final String basePath,
			final String votingCardSetId, final String verificationCardSetId, final int maxNumCredentialsPerFile) {
		final Path path = commonBatchInfrastructure.getDataSerializationProvider(basePath, votingCardSetId, verificationCardSetId)
				.getVerificationCardData();
		return new VerificationCardDataWriter(path, maxNumCredentialsPerFile);
	}

	protected ExtendedAuthenticationWriter extendedAuthenticationWriter(final CommonBatchInfrastructure commonBatchInfrastructure,
			final String basePath, final String votingCardSetId, final String verificationCardSetId, final int maxNumCredentialsPerFile) {
		final Path path = commonBatchInfrastructure.getDataSerializationProvider(basePath, votingCardSetId, verificationCardSetId)
				.getTempExtendedAuth("");
		return new ExtendedAuthenticationWriter(path, maxNumCredentialsPerFile);
	}

	protected CodesMappingTableWriter codesMappingTableWriter(final CommonBatchInfrastructure commonBatchInfrastructure, final String basePath,
			final String votingCardSetId, final String verificationCardSetId, final int maxNumCredentialsPerFile) {
		final Path path = commonBatchInfrastructure.getDataSerializationProvider(basePath, votingCardSetId, verificationCardSetId)
				.getCodesMappingTablesContextData();
		return new CodesMappingTableWriter(path, maxNumCredentialsPerFile);
	}

	protected CredentialDataWriter credentialDataWriter(final CommonBatchInfrastructure commonBatchInfrastructure, final String basePath,
			final String votingCardSetId, final String verificationCardSetId, final int maxNumCredentialsPerFile) {
		final Path path = commonBatchInfrastructure.getDataSerializationProvider(basePath, votingCardSetId, verificationCardSetId)
				.getCredentialsData();
		return new CredentialDataWriter(path, maxNumCredentialsPerFile);
	}

	protected VoterInformationWriter voterInformationWriter(final CommonBatchInfrastructure commonBatchInfrastructure, final String basePath,
			final String votingCardSetId, final String verificationCardSetId, final int maxNumCredentialsPerFile) {
		final Path path = commonBatchInfrastructure.getDataSerializationProvider(basePath, votingCardSetId, verificationCardSetId)
				.getVoterInformation();
		return new VoterInformationWriter(path, maxNumCredentialsPerFile);
	}
}
