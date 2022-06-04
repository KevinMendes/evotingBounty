/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.service.validation;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

import javax.inject.Inject;
import javax.json.JsonObject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.factory.CryptoX509Certificate;
import ch.post.it.evoting.domain.election.validation.ValidationErrorType;
import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication.AuthenticationCerts;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication.AuthenticationCertsRepository;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication.AuthenticationToken;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.validation.AuthenticationTokenValidation;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.AuthTokenValidationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.util.JsonUtils;

/**
 * This class implements the signature validation of the authentication token.
 */
public class AuthenticationTokenSignatureValidation implements AuthenticationTokenValidation {

	private static final String AUTH_TOKEN_CERT = "authenticationTokenSignerCert";

	private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationTokenSignatureValidation.class);

	@Inject
	AuthenticationCertsRepository authenticationCertsRepository;
	@Inject
	private AsymmetricServiceAPI asymmetricService;

	/**
	 * This method implements the validation of token signature.
	 *
	 * @param tenantId            - the tenant identifier.
	 * @param electionEventId     - the election event identifier.
	 * @param votingCardId        - the voting card identifier.
	 * @param authenticationToken - the authentication token to be validated.
	 * @return true, if successfully validated. Otherwise, false.
	 * @throws ResourceNotFoundException if the authentication content is not found.
	 */
	@Override
	public ValidationResult execute(String tenantId, String electionEventId, String votingCardId, AuthenticationToken authenticationToken)
			throws ResourceNotFoundException, CertificateException {

		ValidationResult validationResult = new ValidationResult(true);
		try {

			AuthenticationCerts certificates = authenticationCertsRepository.findByTenantIdElectionEventId(tenantId, electionEventId);

			JsonObject jsonCertificates = JsonUtils.getJsonObject(certificates.getJson());
			String certVAlue = jsonCertificates.getString(AUTH_TOKEN_CERT);
			CryptoX509Certificate cryptoX509Certificate = convertPEMStringtoCryptoX509Certificate(certVAlue);

			// get public key
			PublicKey publicKey = cryptoX509Certificate.getPublicKey();

			// verify signature
			byte[] authenticationTokenSignatureBytes = Base64.getDecoder().decode(authenticationToken.getSignature());

			byte[] authenticationTokenDataToVerify = StringUtils
					.join(authenticationToken.getId(), authenticationToken.getTimestamp(), authenticationToken.getVoterInformation().getTenantId(),
							authenticationToken.getVoterInformation().getElectionEventId(),
							authenticationToken.getVoterInformation().getVotingCardId(), authenticationToken.getVoterInformation().getBallotId(),
							authenticationToken.getVoterInformation().getCredentialId(),

							authenticationToken.getVoterInformation().getVerificationCardId(),
							authenticationToken.getVoterInformation().getBallotBoxId(),
							authenticationToken.getVoterInformation().getVerificationCardSetId(),
							authenticationToken.getVoterInformation().getVotingCardSetId()).getBytes(StandardCharsets.UTF_8);
			final boolean verified = asymmetricService.verifySignature(authenticationTokenSignatureBytes, publicKey, authenticationTokenDataToVerify);
			if (!verified) {
				throw new AuthTokenValidationException(ValidationErrorType.INVALID_SIGNATURE);
			}
		} catch (GeneralCryptoLibException e) {
			LOGGER.error("Error validating signature of authentication token. [tenantId: {}, electionEventId: {}, votingCardId: {}]", tenantId,
					electionEventId, votingCardId, e);
			throw new AuthTokenValidationException(ValidationErrorType.INVALID_SIGNATURE);
		}
		return validationResult;
	}

	private CryptoX509Certificate convertPEMStringtoCryptoX509Certificate(final String certString)
			throws CertificateException, GeneralCryptoLibException {
		X509Certificate cert = null;

		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		ByteArrayInputStream bis = new ByteArrayInputStream(certString.getBytes(StandardCharsets.UTF_8));
		while (bis.available() > 0) {
			cert = (X509Certificate) cf.generateCertificate(bis);
		}

		return new CryptoX509Certificate(cert);
	}
}
