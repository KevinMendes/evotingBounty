/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.extendedauthentication.domain.model.extendedauthentication.validation;

import java.security.cert.Certificate;
import java.util.ArrayList;
import java.util.Collection;

import javax.ejb.Stateless;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.utils.PemUtils;
import ch.post.it.evoting.domain.election.model.authentication.AuthenticationToken;
import ch.post.it.evoting.domain.election.model.authentication.ExtendedAuthenticationUpdate;
import ch.post.it.evoting.domain.election.model.authentication.ExtendedAuthenticationUpdateRequest;
import ch.post.it.evoting.domain.election.validation.ValidationErrorType;
import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.AuthTokenRepositoryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.AuthTokenValidationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ExtendedAuthValidationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitConsumer;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitException;
import ch.post.it.evoting.votingserver.commons.verify.JSONVerifier;
import ch.post.it.evoting.votingserver.extendedauthentication.domain.model.extendedauthentication.ExtendedAuthentication;
import ch.post.it.evoting.votingserver.extendedauthentication.domain.model.extendedauthentication.validation.rules.CertificateValidation;
import ch.post.it.evoting.votingserver.extendedauthentication.services.infrastructure.persistence.ExtendedAuthenticationService;
import ch.post.it.evoting.votingserver.extendedauthentication.services.infrastructure.remote.client.AuthenticationClient;
import ch.post.it.evoting.votingserver.extendedauthentication.services.infrastructure.remote.client.CertificateChainValidationRequest;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.UnsupportedJwtException;

/**
 * This service provides the functionality to validateToken a authentication token based on the predefined rules.
 */
@Stateless
public class ExtendedAuthValidationServiceImpl implements ExtendedAuthValidationService {

	private static final String ERROR_VALIDATING_THE_TOKEN = "Error validating the token";
	private static final Logger LOGGER = LoggerFactory.getLogger(ExtendedAuthValidationServiceImpl.class);
	private static final JSONVerifier jsonVerifier = new JSONVerifier();

	private final Collection<CertificateValidation> validations = new ArrayList<>();

	@Inject
	private AuthenticationTokenService authenticationTokenService;

	@Inject
	private ExtendedAuthenticationService extendedAuthenticationService;

	private AuthenticationClient auRestClient;

	@Inject
	public ExtendedAuthValidationServiceImpl(final AuthenticationClient auRestClient) {
		this.auRestClient = auRestClient;
	}

	@Inject
	@Any
	void setValidations(final Instance<CertificateValidation> instance) {
		for (final CertificateValidation validation : instance) {
			validations.add(validation);
		}
	}

	/**
	 * @see ExtendedAuthValidationService#validateToken(String, String, AuthenticationToken)
	 */
	@Override
	public boolean validateToken(final String tenantId, final String electionEventId, final AuthenticationToken authenticationToken) {

		try {

			final ValidationResult validationResult = authenticationTokenService.validateToken(tenantId, electionEventId, authenticationToken);

			if (!validationResult.isResult()) {
				LOGGER.error(ERROR_VALIDATING_THE_TOKEN);
				throw new AuthTokenValidationException(ValidationErrorType.FAILED);
			}

		} catch (final AuthTokenRepositoryException e) {
			LOGGER.error(ERROR_VALIDATING_THE_TOKEN, e);
			throw new AuthTokenValidationException(ValidationErrorType.FAILED);
		}
		return true;
	}

	/**
	 * @see ExtendedAuthValidationService#validateCertificate(String, AuthenticationToken)
	 */
	@Override
	public boolean validateCertificate(final String certificate, final AuthenticationToken authenticationToken) {
		for (final CertificateValidation validation : validations) {
			validation.validateCertificate(certificate, authenticationToken);
		}
		return true;
	}

	/**
	 * @see ExtendedAuthValidationService#verifySignature(ExtendedAuthenticationUpdateRequest, AuthenticationToken)
	 * (ExtendedAuthenticationUpdateRequest) (String, AuthenticationToken) This verification comes from a plain json signed in javascript. In this
	 * scenario is possible to simply deserialize the object using directly the map that the signature contains. In other scenarios may be better to
	 * include the whole signed object in a single entry of the map(e.g. "signedObject") , and during the verifying process try to claim the object
	 * using that key
	 */
	@Override
	public ExtendedAuthenticationUpdate verifySignature(final ExtendedAuthenticationUpdateRequest extendedAuthenticationUpdateRequest,
			final AuthenticationToken authenticationToken) {

		try {
			final Certificate certificate = PemUtils.certificateFromPem(extendedAuthenticationUpdateRequest.getCertificate());

			final ExtendedAuthenticationUpdate verify = jsonVerifier
					.verifyFromMap(certificate.getPublicKey(), extendedAuthenticationUpdateRequest.getSignature(),
							ExtendedAuthenticationUpdate.class);

			if (!verify.getAuthenticationTokenSignature().equals(authenticationToken.getSignature())) {

				final String errMsg = "The signature has not been properly verified";
				LOGGER.error(errMsg);
				throw new ExtendedAuthValidationException(ValidationErrorType.INVALID_SIGNATURE);
			}
			return verify;

		} catch (final GeneralCryptoLibException e) {
			LOGGER.error("error reading the PEM certificate");
			throw new ExtendedAuthValidationException(ValidationErrorType.INVALID_CERTIFICATE, e);
		} catch (final SignatureException | IllegalStateException | UnsupportedJwtException | ExpiredJwtException e) {
			throw new ExtendedAuthValidationException(ValidationErrorType.INVALID_SIGNATURE, e);
		}

	}

	/**
	 * @see ExtendedAuthValidationService#validateTokenWithAuthIdAndCredentialId(AuthenticationToken, ExtendedAuthenticationUpdate, String, String)
	 */
	@Override
	public void validateTokenWithAuthIdAndCredentialId(final AuthenticationToken authenticationToken,
			final ExtendedAuthenticationUpdate extendedAuthenticationUpdate, final String tenantId, final String electionEventId)
			throws ResourceNotFoundException, ApplicationException {

		final ExtendedAuthentication extendedAuthentication = extendedAuthenticationService
				.retrieveExistingExtendedAuthenticationForRead(tenantId, extendedAuthenticationUpdate.getOldAuthID(), electionEventId);
		if (!extendedAuthentication.getCredentialId().equals(authenticationToken.getVoterInformation().getCredentialId())) {
			final String errMsg = "INVALID CREDENTIAL ID IN AUTHID";
			LOGGER.error(errMsg);
			throw new ExtendedAuthValidationException(ValidationErrorType.INVALID_CREDENTIAL_ID_IN_AUTHID);
		}

	}

	/**
	 * @inheritDoc
	 */
	@Override
	public void validateCertificateChain(final String tenantId, final String electionEventId, final String certificate, final AuthenticationToken authenticationToken) {

		final CertificateChainValidationRequest certificateChainValidationRequest = new CertificateChainValidationRequest();
		certificateChainValidationRequest.setCertificateContent(certificate);

		try {
			final ValidationResult validationResult = RetrofitConsumer
					.processResponse(auRestClient.validateCertificateChain(tenantId, electionEventId, certificateChainValidationRequest));

			if (validationResult.isResult()) {
				LOGGER.info("The certificate chain is VALID.");
			} else {
				final String errMsg = "The certificate chain is INVALID.";
				LOGGER.error(errMsg);
				throw new ExtendedAuthValidationException(ValidationErrorType.INVALID_CERTIFICATE_CHAIN);
			}

		} catch (final RetrofitException error) {
			final String errMsg = "Validate certificate chain failed: " + error;
			LOGGER.error(errMsg);
			throw new ExtendedAuthValidationException(ValidationErrorType.FAILED);
		}
	}
}
