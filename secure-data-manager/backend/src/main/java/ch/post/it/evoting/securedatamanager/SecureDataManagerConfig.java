/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager;

import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.support.ApplicationContextFactory;
import org.springframework.batch.core.configuration.support.GenericApplicationContextFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.Environment;
import org.springframework.integration.util.CallerBlocksPolicy;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.api.certificates.CertificatesServiceAPI;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.api.extendedkeystore.KeyStoreService;
import ch.post.it.evoting.cryptolib.api.primitives.PrimitivesServiceAPI;
import ch.post.it.evoting.cryptolib.api.securerandom.CryptoAPIRandomString;
import ch.post.it.evoting.cryptolib.api.services.ServiceFactory;
import ch.post.it.evoting.cryptolib.api.stores.StoresServiceAPI;
import ch.post.it.evoting.cryptolib.api.symmetric.SymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricServiceFactoryHelper;
import ch.post.it.evoting.cryptolib.certificates.factory.X509CertificateGenerator;
import ch.post.it.evoting.cryptolib.certificates.factory.builders.CertificateDataBuilder;
import ch.post.it.evoting.cryptolib.certificates.service.CertificatesServiceFactoryHelper;
import ch.post.it.evoting.cryptolib.certificates.utils.CryptolibPayloadSigningCertificateValidator;
import ch.post.it.evoting.cryptolib.certificates.utils.PayloadSigningCertificateValidator;
import ch.post.it.evoting.cryptolib.extendedkeystore.service.ExtendedKeyStoreService;
import ch.post.it.evoting.cryptolib.extendedkeystore.service.ExtendedKeyStoreServiceFactoryHelper;
import ch.post.it.evoting.cryptolib.keystore.KeyStoreReader;
import ch.post.it.evoting.cryptolib.primitives.service.PrimitivesServiceFactoryHelper;
import ch.post.it.evoting.cryptolib.returncode.VoterCodesService;
import ch.post.it.evoting.cryptolib.returncode.VoterCodesServiceImpl;
import ch.post.it.evoting.cryptolib.stores.service.PollingStoresServiceFactory;
import ch.post.it.evoting.cryptolib.symmetric.service.SymmetricServiceFactoryHelper;
import ch.post.it.evoting.cryptoprimitives.CryptoPrimitives;
import ch.post.it.evoting.cryptoprimitives.CryptoPrimitivesService;
import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.cryptoprimitives.elgamal.ElGamalService;
import ch.post.it.evoting.cryptoprimitives.hashing.HashService;
import ch.post.it.evoting.cryptoprimitives.math.RandomService;
import ch.post.it.evoting.cryptoprimitives.mixnet.Mixnet;
import ch.post.it.evoting.cryptoprimitives.mixnet.MixnetService;
import ch.post.it.evoting.cryptoprimitives.symmetric.SymmetricService;
import ch.post.it.evoting.cryptoprimitives.utils.KDFService;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ZeroKnowledgeProof;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ZeroKnowledgeProofService;
import ch.post.it.evoting.domain.election.payload.sign.CryptolibPayloadSigner;
import ch.post.it.evoting.domain.election.payload.sign.PayloadSigner;
import ch.post.it.evoting.domain.election.payload.verify.CryptolibPayloadVerifier;
import ch.post.it.evoting.domain.election.payload.verify.PayloadVerifier;
import ch.post.it.evoting.domain.returncodes.safestream.StreamSerializableObjectWriterImpl;
import ch.post.it.evoting.securedatamanager.config.engine.commands.progress.ProgressManager;
import ch.post.it.evoting.securedatamanager.config.engine.commands.voters.datapacks.generators.VerificationCardCredentialDataPackGenerator;
import ch.post.it.evoting.securedatamanager.services.application.config.SmartCardConfig;
import ch.post.it.evoting.securedatamanager.services.infrastructure.DatabaseManager;
import ch.post.it.evoting.securedatamanager.services.infrastructure.DatabaseManagerFactory;

@Configuration
public class SecureDataManagerConfig {
	private static final Logger LOGGER = LoggerFactory.getLogger(SecureDataManagerConfig.class);

	@Value("${spring.profiles.active:}")
	private String activeProfiles;

	@PostConstruct
	private void postConstruct() {
		LOGGER.info("Spring active profiles : {}", activeProfiles);
	}

	@Bean
	@Profile("standard")
	public ApplicationContextFactory configStandardContext() {
		return new GenericApplicationContextFactory(ConfigJobConfigStandard.class);
	}

	@Bean
	@Profile("challenge")
	public ApplicationContextFactory configChallengeContext() {
		return new GenericApplicationContextFactory(ConfigJobConfigChallenge.class);
	}

	@Bean
	public ServiceFactory<AsymmetricServiceAPI> asymmetricServiceAPIServiceFactory(final GenericObjectPoolConfig genericObjectPoolConfig) {
		return AsymmetricServiceFactoryHelper.getFactoryOfThreadSafeServices(genericObjectPoolConfig);
	}

	@Bean
	public AsymmetricServiceAPI asymmetricServiceAPI(final ServiceFactory<AsymmetricServiceAPI> asymmetricServiceAPIServiceFactory)
			throws GeneralCryptoLibException {
		return asymmetricServiceAPIServiceFactory.create();
	}

	@Bean
	public ServiceFactory<KeyStoreService> extendedKeyStoreServiceAPIServiceFactory(final GenericObjectPoolConfig genericObjectPoolConfig) {
		return ExtendedKeyStoreServiceFactoryHelper.getFactoryOfThreadSafeServices(genericObjectPoolConfig);
	}

	@Bean
	public KeyStoreService extendedKeyStoreServiceAPI(final ServiceFactory<KeyStoreService> extendedKeyStoreServiceAPIServiceFactory)
			throws GeneralCryptoLibException {
		return extendedKeyStoreServiceAPIServiceFactory.create();
	}

	@Bean
	public GenericObjectPoolConfig genericObjectPoolConfig(final Environment env) {

		final GenericObjectPoolConfig genericObjectPoolConfig = new GenericObjectPoolConfig();
		genericObjectPoolConfig.setMaxTotal(Integer.parseInt(env.getProperty("services.cryptolib.pool.size")));
		genericObjectPoolConfig.setMaxIdle(Integer.parseInt(env.getProperty("services.cryptolib.timeout")));

		return genericObjectPoolConfig;
	}

	@Bean(initMethod = "createDatabase")
	public DatabaseManager databaseManager(final DatabaseManagerFactory databaseManagerFactory,
			@Value("${database.name}")
			final String databaseName) {
		return databaseManagerFactory.newDatabaseManager(databaseName);
	}

	@Bean
	public PayloadSigner payloadSigner(final AsymmetricServiceAPI asymmetricService) {
		return new CryptolibPayloadSigner(asymmetricService);
	}

	@Bean
	public PayloadSigningCertificateValidator certificateValidator() {
		return new CryptolibPayloadSigningCertificateValidator();
	}

	@Bean
	public PayloadVerifier payloadVerifier(final AsymmetricServiceAPI asymmetricService,
			final PayloadSigningCertificateValidator certificateValidator) {
		return new CryptolibPayloadVerifier(asymmetricService, certificateValidator);
	}

	@Bean
	public StoresServiceAPI storesService() {
		return new PollingStoresServiceFactory().create();
	}

	@Bean
	public StreamSerializableObjectWriterImpl serializableObjectWriter() {
		return new StreamSerializableObjectWriterImpl();
	}

	@Bean
	public ObjectMapper objectMapper() {
		return DomainObjectMapper.getNewInstance();
	}

	@Bean
	public ServiceFactory<CertificatesServiceAPI> certificatesServiceAPIServiceFactory(final GenericObjectPoolConfig genericObjectPoolConfig) {
		return CertificatesServiceFactoryHelper.getFactoryOfThreadSafeServices(genericObjectPoolConfig);
	}

	@Bean
	public CertificatesServiceAPI certificatesServiceAPI(final ServiceFactory<CertificatesServiceAPI> certificatesServiceAPIServiceFactory)
			throws GeneralCryptoLibException {
		return certificatesServiceAPIServiceFactory.create();
	}

	@Bean
	public CryptoAPIRandomString cryptoAPIRandomString(final PrimitivesServiceAPI primitivesServiceAPI) {
		return primitivesServiceAPI.get32CharAlphabetCryptoRandomString();
	}

	@Bean
	public CertificateDataBuilder certificateDataBuilder() {
		return new CertificateDataBuilder();
	}

	@Bean
	public X509CertificateGenerator certificatesGenerator(final CertificatesServiceAPI certificatesService,
			final CertificateDataBuilder certificateDataBuilder) {
		return new X509CertificateGenerator(certificatesService, certificateDataBuilder);
	}

	@Bean
	public KeyStoreReader keyStoreReader() {
		return new KeyStoreReader();
	}

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	@Bean
	public VerificationCardCredentialDataPackGenerator verificationCardCredentialDataPackGenerator(final PrimitivesServiceAPI primitivesService,
			@Qualifier("asymmetricServiceAPI")
			final AsymmetricServiceAPI asymmetricService, final X509CertificateGenerator certificatesGenerator) {

		final ExtendedKeyStoreService keyStoreService = new ExtendedKeyStoreService();
		final CryptoAPIRandomString cryptoRandomString = primitivesService.get32CharAlphabetCryptoRandomString();

		return new VerificationCardCredentialDataPackGenerator(asymmetricService, cryptoRandomString, certificatesGenerator, keyStoreService
		);
	}

	@Bean
	public ProgressManager votersProgressManager() {
		return new ProgressManager();
	}

	@Bean
	public Base64.Encoder encoder() {
		return Base64.getEncoder();
	}

	@Bean
	@Qualifier("urlSafeEncoder")
	public Base64.Encoder urlSafeEncoder() {
		return Base64.getUrlEncoder();
	}

	@Bean
	public ObjectReader readerForDeserialization() {
		final ObjectMapper mapper = mapperForDeserialization();
		return mapper.reader();
	}

	private ObjectMapper mapperForDeserialization() {
		final ObjectMapper mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		mapper.disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES);
		mapper.findAndRegisterModules();
		return mapper;
	}

	@Bean
	@ConditionalOnProperty(name = "smartcards.profile", havingValue = "e2e")
	public SmartCardConfig getSmartCardConfigE2E() {
		return SmartCardConfig.FILE;
	}

	@Bean
	@ConditionalOnProperty(name = "smartcards.profile", havingValue = "default")
	public SmartCardConfig getSmartCardConfigDefault() {
		return SmartCardConfig.SMART_CARD;
	}

	@Bean
	public ServiceFactory<PrimitivesServiceAPI> primitivesServiceAPIServiceFactory(final GenericObjectPoolConfig genericObjectPoolConfig) {
		return PrimitivesServiceFactoryHelper.getFactoryOfThreadSafeServices(genericObjectPoolConfig);
	}

	@Bean
	public ServiceFactory<SymmetricServiceAPI> symmetricServiceAPIServiceFactory(final GenericObjectPoolConfig genericObjectPoolConfig) {
		return SymmetricServiceFactoryHelper.getFactoryOfThreadSafeServices(genericObjectPoolConfig);
	}

	@Bean
	public PrimitivesServiceAPI primitivesServiceAPI(final ServiceFactory<PrimitivesServiceAPI> primitivesServiceAPIServiceFactory)
			throws GeneralCryptoLibException {
		return primitivesServiceAPIServiceFactory.create();
	}

	@Bean
	public SymmetricServiceAPI symmetricServiceAPI(final ServiceFactory<SymmetricServiceAPI> symmetricServiceAPIServiceFactory)
			throws GeneralCryptoLibException {
		return symmetricServiceAPIServiceFactory.create();
	}

	@Bean
	public VoterCodesService voterCodesService(final PrimitivesServiceAPI primitivesService, final SymmetricServiceAPI symmetricService) {
		return new VoterCodesServiceImpl(primitivesService, symmetricService);
	}

	@Bean
	public CryptoPrimitives cryptoPrimitives() {
		return CryptoPrimitivesService.get();
	}

	@Bean
	public Mixnet mixnet() {
		return new MixnetService();
	}

	@Bean
	public ZeroKnowledgeProof zeroKnowledgeProof() {
		return new ZeroKnowledgeProofService();
	}

	@Bean
	@Qualifier("cryptoPrimitivesHashService")
	public HashService cryptoPrimitivesHashService() {
		return HashService.getInstance();
	}

	@Bean
	RandomService randomService() {
		return new RandomService();
	}

	@Bean
	ElGamalService elGamalService() {
		return new ElGamalService();
	}

	@Bean
	KDFService kdfService() {
		return KDFService.getInstance();
	}

	@Bean
	SymmetricService symmetricService() {
		return new SymmetricService();
	}

	@Bean
	ExecutorService executorService() {
		final CallerBlocksPolicy policy = new CallerBlocksPolicy(1000000000);
		final int numberOfThreads = 20;
		return new ThreadPoolExecutor(
				numberOfThreads,
				numberOfThreads,
				0L,
				TimeUnit.MILLISECONDS,
				new LinkedBlockingQueue<>(),
				policy);
	}
}
