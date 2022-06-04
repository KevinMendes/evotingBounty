/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.service;

import java.io.IOException;
import java.nio.file.Path;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.List;

import org.bouncycastle.cms.CMSException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.elgamal.bean.VerifiableElGamalEncryptionParameters;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.commons.PathResolver;
import ch.post.it.evoting.securedatamanager.config.commons.utils.SignatureVerifier;

/**
 * This implementation reads the prime numbers and encryption parameters' associated files and validates their signature.
 */
@Service
public class SignaturesVerifierService {

	@Autowired
	private SignatureVerifier signatureVerifier;

	@Autowired
	private PathResolver pathResolver;

	/**
	 * Requests the verification of the encryption parameters and prime numbers signature
	 *
	 * @param eeId the election event from which the encryption parameters are going to be verified
	 * @return The verified ElGamalEncryptionParameters object
	 * @throws CMSException, GeneralCryptoLibException, IOException, CertificateException
	 */
	public VerifiableElGamalEncryptionParameters verifyEncryptionParams(final String eeId)
			throws CMSException, GeneralCryptoLibException, IOException, CertificateException {

		final Path trustedChainPath = pathResolver.resolve(Constants.CONFIG_FILES_BASE_DIR, eeId, Constants.CONFIG_FILE_NAME_TRUSTED_CHAIN_PEM);

		if (!trustedChainPath.toFile().exists()) {
			signatureVerifier.saveOfflineTrustedChain(verifyPrimeNumbers(eeId), trustedChainPath);
		}

		final Path paramsJwtPath = pathResolver
				.resolve(Constants.CONFIG_FILES_BASE_DIR, eeId, Constants.CONFIG_DIR_NAME_CUSTOMER, Constants.CONFIG_DIR_NAME_OUTPUT,
						Constants.CONFIG_FILE_NAME_ENCRYPTION_PARAMETERS_SIGN_JSON);

		// Can trust the public key used for signing the prime files, it is the same used for the
		// encryption params
		final PublicKey trustedKey = getOfflineTrustedChain(eeId).get(0).getPublicKey();

		return signatureVerifier.verifyJwt(paramsJwtPath, trustedKey);
	}

	/**
	 * Verify a file and its chain. P7
	 *
	 * @param filePath
	 * @param signaturePath
	 * @return The trusted chain
	 * @throws IOException, CMSException, GeneralCryptoLibException, CertificateException
	 */
	public Certificate[] verifyPkcs7(final Path filePath, final Path signaturePath)
			throws IOException, CMSException, GeneralCryptoLibException, CertificateException {
		final Path trustedCAPath = pathResolver.resolve(Constants.CONFIG_FILES_BASE_DIR, Constants.CONFIG_FILE_NAME_TRUSTED_CA_PEM);

		return signatureVerifier.verifyPkcs7(filePath, signaturePath, trustedCAPath);

	}

	private List<Certificate> getOfflineTrustedChain(final String eeId) throws IOException, CertificateException {
		final Path trustedChainPath = pathResolver.resolve(Constants.CONFIG_FILES_BASE_DIR, eeId, Constants.CONFIG_FILE_NAME_TRUSTED_CHAIN_PEM);

		return signatureVerifier.readPemCertificates(trustedChainPath);
	}

	/**
	 * This method verifies and trusts the file with the prime numbers list used for that electionEvent in the system.
	 *
	 * @param eeId election event identifier
	 * @return The verified and trusted certificate chain
	 * @throws CMSException, GeneralCryptoLibException, IOException, CertificateException
	 */
	private Certificate[] verifyPrimeNumbers(final String eeId) throws CMSException, GeneralCryptoLibException, IOException, CertificateException {
		final Path customerOutputPath = pathResolver
				.resolve(Constants.CONFIG_FILES_BASE_DIR, eeId, Constants.CONFIG_DIR_NAME_CUSTOMER, Constants.CONFIG_DIR_NAME_OUTPUT);

		final Path trustedCAPath = pathResolver.resolve(Constants.CONFIG_FILES_BASE_DIR, Constants.CONFIG_FILE_NAME_TRUSTED_CA_PEM);

		// Validate prime numbers
		final Path primesSignaturePath = customerOutputPath.resolve(Constants.CONFIG_FILE_NAME_REPRESENTATIONS_CSV_SIGN);
		final Path primesPath = customerOutputPath.resolve(Constants.CONFIG_FILE_NAME_REPRESENTATIONS_CSV);
		return signatureVerifier.verifyPkcs7(primesPath, primesSignaturePath, trustedCAPath);
	}

}
