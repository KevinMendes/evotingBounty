/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.integration.jdbc.lock.DefaultLockRepository;
import org.springframework.integration.jdbc.lock.JdbcLockRegistry;
import org.springframework.integration.jdbc.lock.LockRepository;
import org.springframework.integration.support.locks.LockRegistry;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.api.certificates.CertificatesServiceAPI;
import ch.post.it.evoting.cryptolib.api.primitives.PrimitivesServiceAPI;
import ch.post.it.evoting.cryptolib.api.stores.StoresServiceAPI;
import ch.post.it.evoting.cryptolib.asymmetric.service.PollingAsymmetricServiceFactory;
import ch.post.it.evoting.cryptolib.certificates.service.PollingCertificatesServiceFactory;
import ch.post.it.evoting.cryptolib.certificates.utils.CryptolibPayloadSigningCertificateValidator;
import ch.post.it.evoting.cryptolib.certificates.utils.PayloadSigningCertificateValidator;
import ch.post.it.evoting.cryptolib.primitives.service.PollingPrimitivesServiceFactory;
import ch.post.it.evoting.cryptolib.stores.service.PollingStoresServiceFactory;
import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.cryptoprimitives.hashing.HashService;
import ch.post.it.evoting.cryptoprimitives.math.RandomService;
import ch.post.it.evoting.cryptoprimitives.mixnet.Mixnet;
import ch.post.it.evoting.cryptoprimitives.mixnet.MixnetService;
import ch.post.it.evoting.cryptoprimitives.utils.KDFService;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ZeroKnowledgeProof;
import ch.post.it.evoting.cryptoprimitives.zeroknowledgeproofs.ZeroKnowledgeProofService;
import ch.post.it.evoting.domain.election.payload.sign.CryptolibPayloadSigner;
import ch.post.it.evoting.domain.election.payload.sign.PayloadSigner;
import ch.post.it.evoting.domain.election.payload.verify.CryptolibPayloadVerifier;
import ch.post.it.evoting.domain.election.payload.verify.PayloadVerifier;

@Configuration
public class ControlComponentsConfig {

	@Bean
	public AsymmetricServiceAPI asymmetricService() {
		return new PollingAsymmetricServiceFactory().create();
	}

	@Bean
	public CertificatesServiceAPI certificatesService() {
		return new PollingCertificatesServiceFactory().create();
	}

	@Bean
	public PrimitivesServiceAPI primitivesService() {
		return new PollingPrimitivesServiceFactory().create();
	}

	@Bean
	public StoresServiceAPI storesService() {
		return new PollingStoresServiceFactory().create();
	}

	@Bean
	public HashService hashService() {
		return HashService.getInstance();
	}

	@Bean
	public ZeroKnowledgeProof zeroKnowledgeProof() {
		return new ZeroKnowledgeProofService();
	}

	@Bean
	public RandomService randomService() {
		return new RandomService();
	}

	@Bean
	public static PropertySourcesPlaceholderConfigurer propertyConfigInDev() {
		return new PropertySourcesPlaceholderConfigurer();
	}

	@Bean
	Mixnet mixnet() {
		return new MixnetService();
	}

	@Bean
	public PayloadSigner payloadSigner(final AsymmetricServiceAPI asymmetricService) {
		return new CryptolibPayloadSigner(asymmetricService);
	}

	@Bean
	public PayloadSigningCertificateValidator certificateChainValidator() {
		return new CryptolibPayloadSigningCertificateValidator();
	}

	@Bean
	public PayloadVerifier payloadVerifier(final AsymmetricServiceAPI asymmetricService,
			final PayloadSigningCertificateValidator certificateChainValidator) {
		return new CryptolibPayloadVerifier(asymmetricService, certificateChainValidator);
	}

	@Bean
	ObjectMapper objectMapper() {
		return DomainObjectMapper.getNewInstance();
	}

	@Bean
	DefaultLockRepository defaultLockRepository(final DataSource datasource) {
		return new DefaultLockRepository(datasource);
	}

	@Bean
	LockRegistry jdbcLockRegistry(final LockRepository lockRepository) {
		return new JdbcLockRegistry(lockRepository);
	}

	@Bean
	KDFService kdfService() {
		return KDFService.getInstance();
	}
}
