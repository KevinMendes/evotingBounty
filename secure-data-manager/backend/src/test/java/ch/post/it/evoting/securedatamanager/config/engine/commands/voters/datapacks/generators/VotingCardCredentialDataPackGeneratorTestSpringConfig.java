/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */

package ch.post.it.evoting.securedatamanager.config.engine.commands.voters.datapacks.generators;

import static org.mockito.Mockito.mock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ch.post.it.evoting.cryptolib.api.extendedkeystore.KeyStoreService;
import ch.post.it.evoting.cryptolib.api.securerandom.CryptoAPIRandomString;
import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricService;
import ch.post.it.evoting.cryptolib.certificates.factory.X509CertificateGenerator;

@Configuration
public class VotingCardCredentialDataPackGeneratorTestSpringConfig {

	@Bean
	public VotingCardCredentialDataPackGenerator votingCardCredentialDataPackGenerator() {
		return new VotingCardCredentialDataPackGenerator(asymmetricService(), certificateGenerator(), storesService(), cryptoRandomString());
	}

	@Bean
	public AsymmetricService asymmetricService() {
		return new AsymmetricService();
	}

	@Bean
	public X509CertificateGenerator certificateGenerator() {
		return mock(X509CertificateGenerator.class);
	}

	@Bean
	public KeyStoreService storesService() {
		return mock(KeyStoreService.class);
	}

	@Bean
	public CryptoAPIRandomString cryptoRandomString() {
		return mock(CryptoAPIRandomString.class);
	}
}
