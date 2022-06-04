/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.service.certificate;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.bean.X509CertificateType;
import ch.post.it.evoting.cryptolib.certificates.bean.X509DistinguishedName;
import ch.post.it.evoting.cryptolib.certificates.factory.CryptoX509Certificate;
import ch.post.it.evoting.cryptolib.certificates.utils.X509CertificateChainValidator;
import ch.post.it.evoting.domain.election.validation.ValidationError;
import ch.post.it.evoting.domain.election.validation.ValidationErrorType;
import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication.AuthenticationCerts;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication.AuthenticationCertsRepository;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.SemanticErrorException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.SyntaxErrorException;
import ch.post.it.evoting.votingserver.commons.util.JsonUtils;
import ch.post.it.evoting.votingserver.commons.util.ValidationUtils;

/**
 * Service for certificate chain validation.
 */
@Stateless
public class CertificateChainValidationService {

	private static final String ELECTION_ROOT_CA = "electionRootCA";

	private static final String CREDENTIALS_CA = "credentialsCA";

	private static final Logger LOGGER = LoggerFactory.getLogger(CertificateChainValidationService.class);

	@Inject
	private AuthenticationCertsRepository authenticationCertsRepository;

	// Create a credential factory to convert string to X509 certificate
	private CertificateFactory cf;

	/**
	 * Validates a certificate chain. CredentialID Auth Certificate vs CredentialsCA.
	 *
	 * @param tenantId          - the tenant id.
	 * @param electionEventId   - the election event id.
	 * @param certificateString - the certificate to be validated.
	 * @return a result of validation.
	 */
	public ValidationResult validate(final String tenantId, final String electionEventId, final String certificateString) {
		LOGGER.info("Starting certificate chain validation.");

		final ValidationResult result = new ValidationResult(true);

		try {
			cf = CertificateFactory.getInstance("X.509");

			// Get Auth Certificates from DB
			final AuthenticationCerts authCerts = authenticationCertsRepository.findByTenantIdElectionEventId(tenantId, electionEventId);
			validateCerts(authCerts);
			final String authCertsJson = authCerts.getJson();

			// Trusted certificate -> Election Root CA
			final String rootString = JsonUtils.getJsonObject(authCertsJson).getString(ELECTION_ROOT_CA);

			// Get X509 certificate
			final X509Certificate electionRootCA = getX509Cert(rootString);
			final X509DistinguishedName distinguishedNameElectionRootCA = getDistinguishName(electionRootCA);

			// Root vs Root chain validation
			final X509Certificate[] certificateChainRoot = {};
			final X509DistinguishedName[] subjectDnsRoot = {};

			// Validate
			final boolean validateCertRootResult = validateCert(electionRootCA, distinguishedNameElectionRootCA,
					X509CertificateType.CERTIFICATE_AUTHORITY,
					certificateChainRoot, subjectDnsRoot, electionRootCA);

			LOGGER.info("Certificate root chain validation. [result: {}]", validateCertRootResult);

			if (validateCertRootResult) {
				// Leaf certificate -> CredentialId Auth Certificate
				// Get X509 certificate
				final X509Certificate credentialIdAuthCert = getX509Cert(certificateString);
				final X509DistinguishedName subjectDnCredentialAuthCert = getDistinguishName(credentialIdAuthCert);

				// Intermediate certificate -> Credentials CA
				final String credentialsCAString = JsonUtils.getJsonObject(authCertsJson).getString(CREDENTIALS_CA);

				// Get X509 certificate
				final X509Certificate credentialsCA = getX509Cert(credentialsCAString);
				final X509DistinguishedName distinguishedNameCredentialsCA = getDistinguishName(credentialsCA);

				// CredentialID Auth Certificate vs CredentialsCA chain validation
				final X509Certificate[] certificateChainCredential = { credentialsCA };
				final X509DistinguishedName[] subjectDnsCredential = { distinguishedNameCredentialsCA };

				// Validate
				final boolean validateCertResult = validateCert(credentialIdAuthCert, subjectDnCredentialAuthCert, X509CertificateType.SIGN,
						certificateChainCredential, subjectDnsCredential, electionRootCA);

				result.setResult(validateCertResult);

				if (validateCertResult) {
					LOGGER.info("Certificate chain validation. [result: {}]", validateCertResult);

				} else {
					LOGGER.warn("Certificate chain validation. [result: {}]", validateCertResult);

					final ValidationError validationError = new ValidationError();
					validationError.setValidationErrorType(ValidationErrorType.INVALID_CERTIFICATE_CHAIN);
					result.setValidationError(validationError);
				}

			} else {
				result.setResult(false);
				result.setValidationError(new ValidationError(ValidationErrorType.INVALID_CERTIFICATE_ROOT_CA));
			}

		} catch (final CertificateException | GeneralCryptoLibException e1) {
			LOGGER.error("Cryptography error.", e1);
			result.setResult(false);
			result.setValidationError(new ValidationError(ValidationErrorType.INVALID_CERTIFICATE_CHAIN));
		} catch (final ResourceNotFoundException e2) {
			LOGGER.error("An error occurred getting auth certificates.", e2);
			result.setResult(false);
			result.setValidationError(new ValidationError(ValidationErrorType.INVALID_CERTIFICATE_CHAIN));
		} catch (final SyntaxErrorException e3) {
			LOGGER.error("Syntax error.", e3);
			result.setResult(false);
			result.setValidationError(new ValidationError(ValidationErrorType.INVALID_CERTIFICATE_CHAIN));
		} catch (final SemanticErrorException e4) {
			LOGGER.error("Semantic error.", e4);
			result.setResult(false);
			result.setValidationError(new ValidationError(ValidationErrorType.INVALID_CERTIFICATE_CHAIN));
		}

		return result;
	}

	/**
	 * Validates a authCerts object from DB.
	 *
	 * @param authCerts - the AuthenticationCerts object from DB.
	 * @throws SyntaxErrorException   when a syntax exception occurs.
	 * @throws SemanticErrorException when a semantic exception occurs.
	 */
	public void validateCerts(final AuthenticationCerts authCerts) throws SyntaxErrorException, SemanticErrorException {
		ValidationUtils.validate(authCerts);
	}

	private boolean validateCert(final X509Certificate certLeaf, final X509DistinguishedName subjectDnsLeaf, final X509CertificateType certType,
			final X509Certificate[] certChain, final X509DistinguishedName[] subjectDns, final X509Certificate certTrusted)
			throws GeneralCryptoLibException {
		final X509CertificateChainValidator certificateChainValidator = createCertificateChainValidator(certLeaf, subjectDnsLeaf, certType, certChain,
				subjectDns, certTrusted);
		final List<String> validationResult = certificateChainValidator.validate();
		return validationResult == null || validationResult.isEmpty();
	}

	/**
	 * Create a new instance of a X509CertificateChainValidator.
	 *
	 * @param certLeaf       - the leaf certificate.
	 * @param subjectDnsLeaf - the leaf certificate subject distinguished name.
	 * @param certType       - the leaf certificate type.
	 * @param certChain      - the certificate chain of the leaf certificate.
	 * @param subjectDns     - the subject distinguished names of the certificate chain.
	 * @param certTrusted    - the trusted certificate.
	 * @return X509CertificateChainValidator.
	 * @throws GeneralCryptoLibException if fails trying to create a new object.
	 */
	public X509CertificateChainValidator createCertificateChainValidator(final X509Certificate certLeaf, final X509DistinguishedName subjectDnsLeaf,
			final X509CertificateType certType, final X509Certificate[] certChain, final X509DistinguishedName[] subjectDns,
			final X509Certificate certTrusted)
			throws GeneralCryptoLibException {
		return new X509CertificateChainValidator(certLeaf, certType, subjectDnsLeaf, new Date(), certChain, subjectDns, certTrusted);
	}

	private X509DistinguishedName getDistinguishName(final X509Certificate x509Cert) throws GeneralCryptoLibException {
		final CryptoX509Certificate wrappedCertificate = new CryptoX509Certificate(x509Cert);
		return wrappedCertificate.getSubjectDn();
	}

	private X509Certificate getX509Cert(final String certificateString) throws CertificateException {
		final InputStream inputStream = new ByteArrayInputStream(certificateString.getBytes(StandardCharsets.UTF_8));
		return (X509Certificate) cf.generateCertificate(inputStream);
	}
}
