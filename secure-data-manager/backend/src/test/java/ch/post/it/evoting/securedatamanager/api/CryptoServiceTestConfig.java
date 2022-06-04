/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.api;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.api.primitives.PrimitivesServiceAPI;
import ch.post.it.evoting.cryptolib.certificates.utils.CryptolibPayloadSigningCertificateValidator;
import ch.post.it.evoting.cryptolib.certificates.utils.PayloadSigningCertificateValidator;
import ch.post.it.evoting.cryptolib.primitives.service.PrimitivesService;
import ch.post.it.evoting.domain.election.payload.verify.CryptolibPayloadVerifier;
import ch.post.it.evoting.domain.election.payload.verify.PayloadVerifier;
import ch.post.it.evoting.domain.returncodes.safestream.StreamSerializableObjectWriterImpl;

/**
 * MixDecValController bean for tests.
 */
@Configuration
class CryptoServiceTestConfig {

	@Bean
	PrimitivesServiceAPI primitivesService() {
		return new PrimitivesService();
	}

	@Bean
	PayloadVerifier payloadVerifier(final AsymmetricServiceAPI asymmetricService, final PayloadSigningCertificateValidator certificateValidator) {
		return new CryptolibPayloadVerifier(asymmetricService, certificateValidator);
	}

	@Bean
	PayloadSigningCertificateValidator certificateValidator() {
		return new CryptolibPayloadSigningCertificateValidator();
	}

	@Bean
	public StreamSerializableObjectWriterImpl serializableObjectWriter() {
		return new StreamSerializableObjectWriterImpl();
	}
}
