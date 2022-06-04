/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.api;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;

/**
 * RSA keys required for the mix-dec-val integration test.
 */
@Configuration
public class KeyTestConfig {

	@Bean
	KeyPair signingKeyPair(final AsymmetricServiceAPI asymmetricService) throws NoSuchAlgorithmException {
		return asymmetricService.getKeyPairForSigning();
	}

}
