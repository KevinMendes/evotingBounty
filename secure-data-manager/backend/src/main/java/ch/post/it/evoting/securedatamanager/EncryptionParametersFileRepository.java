/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Map;
import java.util.Optional;

import org.bouncycastle.cms.CMSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.elgamal.bean.VerifiableElGamalEncryptionParameters;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.exceptions.FailedValidationException;
import ch.post.it.evoting.cryptoprimitives.math.GqGroup;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.config.commons.domain.common.SignedObject;
import ch.post.it.evoting.securedatamanager.config.commons.utils.SignatureVerifier;
import ch.post.it.evoting.securedatamanager.services.infrastructure.PathResolver;

import io.jsonwebtoken.Jwts;

/**
 * Allows performing operations with the encryption parameters of an election. The encryption parameters retrieved from the file system of the SDM, in
 * its workspace.
 */
@Repository
public class EncryptionParametersFileRepository {

	private static final Logger LOGGER = LoggerFactory.getLogger(EncryptionParametersFileRepository.class);

	private final ObjectMapper objectMapper;
	private final PathResolver pathResolver;
	private final SignatureVerifier signatureVerifier;

	public EncryptionParametersFileRepository(
			final ObjectMapper objectMapper,
			final PathResolver pathResolver,
			final SignatureVerifier signatureVerifier) {
		this.objectMapper = objectMapper;
		this.pathResolver = pathResolver;
		this.signatureVerifier = signatureVerifier;
	}

	/**
	 * Retrieves from the file system encryption parameters by their election event id.
	 *
	 * @param electionEventId the election event id associated to the encryption parameters.
	 * @return the encryption parameters as a {@link GqGroup} or {@link Optional#empty} if none found.
	 * @throws FailedValidationException if {@code electionEventId} is invalid.
	 * @throws IllegalStateException     if reading the trusted certificate fails.
	 * @throws UncheckedIOException      if the deserialization of the encryption parameters fails.
	 */
	Optional<GqGroup> load(final String electionEventId) {
		validateUUID(electionEventId);

		final Path outputPath = pathResolver.resolveOutputPath(electionEventId);
		final Path encryptionParametersPath = outputPath.resolve(Constants.CONFIG_FILE_NAME_ENCRYPTION_PARAMETERS_SIGN_JSON);

		if (!Files.exists(encryptionParametersPath)) {
			LOGGER.debug("Requested encryption parameters do not exist. [electionEventId: {}, path: {}]", electionEventId, encryptionParametersPath);
			return Optional.empty();
		}

		final Path electionEventPath = pathResolver.resolveElectionEventPath(electionEventId);
		final Path trustedChainPath = electionEventPath.resolve(Constants.CONFIG_FILE_NAME_TRUSTED_CHAIN_PEM);

		if (!Files.exists(trustedChainPath)) {
			try {
				signatureVerifier.saveOfflineTrustedChain(verifyPrimeNumbers(electionEventId), trustedChainPath);
			} catch (IOException e) {
				throw new UncheckedIOException(
						String.format("Failed to save trusted chain. [electionEventId: %s, trustedChainPath: %s]", electionEventId,
								trustedChainPath), e);
			}
		}

		final PublicKey publicKey;
		try {
			publicKey = signatureVerifier.readPemCertificates(trustedChainPath).get(0).getPublicKey();
		} catch (final IOException | CertificateException e) {
			throw new IllegalStateException(
					String.format("Failed to read trusted key. [electionEventId: %s, trustedChainPath: %s]", electionEventId, trustedChainPath));
		}

		final SignedObject jwtObject;
		try {
			jwtObject = objectMapper.readValue(encryptionParametersPath.toFile(), SignedObject.class);
		} catch (final IOException e) {
			throw new UncheckedIOException(
					String.format("Failed to deserialize encryption parameters file. [electionEventId: %s, path: %s]", electionEventId,
							encryptionParametersPath), e);
		}
		final Map<String, Object> claimMapRecovered = Jwts.parser()
				.setSigningKey(publicKey)
				.parseClaimsJws(jwtObject.getSignature())
				.getBody();

		final Object recoveredSignedObject = claimMapRecovered.get("objectToSign");
		final VerifiableElGamalEncryptionParameters verifiableElGamalEncryptionParameters = objectMapper.convertValue(recoveredSignedObject,
				VerifiableElGamalEncryptionParameters.class);

		final BigInteger p = verifiableElGamalEncryptionParameters.getP();
		final BigInteger q = verifiableElGamalEncryptionParameters.getQ();
		final BigInteger g = verifiableElGamalEncryptionParameters.getG();

		return Optional.of(new GqGroup(p, q, g));
	}

	private Certificate[] verifyPrimeNumbers(final String electionEventId) {
		final Path outputPath = pathResolver.resolveOutputPath(electionEventId);

		final Path primesPath = outputPath.resolve(Constants.CONFIG_FILE_NAME_REPRESENTATIONS_CSV);
		final Path primesSignaturePath = outputPath.resolve(Constants.CONFIG_FILE_NAME_REPRESENTATIONS_CSV_SIGN);
		final Path integrationCAPath = pathResolver.resolveConfigPath().resolve(Constants.CONFIG_FILE_NAME_TRUSTED_CA_PEM);
		try {
			return signatureVerifier.verifyPkcs7(primesPath, primesSignaturePath, integrationCAPath);
		} catch (IOException | CertificateException | CMSException | GeneralCryptoLibException e) {
			throw new IllegalStateException(
					String.format(
							"Failed to verify primes signature and get trusted chain. [electionEventId: %s, primesPath: %s, integrationCAPath: %s",
							electionEventId, primesPath, integrationCAPath), e);
		}
	}

}
