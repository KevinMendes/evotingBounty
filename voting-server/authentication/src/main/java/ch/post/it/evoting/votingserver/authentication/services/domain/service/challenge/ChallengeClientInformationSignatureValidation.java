/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.service.challenge;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.utils.CryptographicOperationException;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.validation.ChallengeInformationValidation;
import ch.post.it.evoting.votingserver.commons.beans.challenge.ChallengeInformation;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;

/**
 * This class implements the validation of the server challenge message signature.
 */
public class ChallengeClientInformationSignatureValidation implements ChallengeInformationValidation {

	private static final Logger LOGGER = LoggerFactory.getLogger(ChallengeClientInformationSignatureValidation.class);

	private static final int RULE_ORDER = 2;
	private final int order;
	@Inject
	private AsymmetricServiceAPI asymmetricService;

	public ChallengeClientInformationSignatureValidation() {
		this.order = RULE_ORDER;
	}

	/**
	 * This method implements the validation of challenge signature verification of both client and server challenge signature.
	 *
	 * @param tenantId             - the tenant identifier.
	 * @param electionEventId      - the election event identifier.
	 * @param credentialId         - the voting card identifier.
	 * @param challengeInformation - the challenge information to be validated.
	 * @return true, if successfully validated. Otherwise, false.
	 * @throws ResourceNotFoundException       if the certificate chain source can not be found.
	 * @throws CryptographicOperationException if an error occurs during the certificate chain location or retrieval.
	 */
	@Override
	public boolean execute(final String tenantId, final String electionEventId, final String credentialId,
			final ChallengeInformation challengeInformation) throws ResourceNotFoundException, CryptographicOperationException {

		boolean result = false;

		try {

			// X509 certificate
			String certificateString = challengeInformation.getCertificate();
			InputStream inputStream = new ByteArrayInputStream(certificateString.getBytes(StandardCharsets.UTF_8));
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			X509Certificate certificate = (X509Certificate) cf.generateCertificate(inputStream);
			PublicKey publicKey = certificate.getPublicKey();
			LOGGER.info("Public key obtained from certificate. [tenantId: {}, electionEventId: {}, credentialId: {}]", tenantId, electionEventId,
					credentialId);

			// verify client challenge signature
			String clientSignature = challengeInformation.getClientChallengeMessage().getSignature();
			String serverSignature = challengeInformation.getServerChallengeMessage().getSignature();

			byte[] clientChallengeDataToVerify = StringUtils
					.join(serverSignature, challengeInformation.getClientChallengeMessage().getClientChallenge()).getBytes(StandardCharsets.UTF_8);
			byte[] clientSignatureBytes = Base64.getDecoder().decode(clientSignature);
			if (asymmetricService.verifySignature(clientSignatureBytes, publicKey, clientChallengeDataToVerify)) {
				LOGGER.info("Client challenge signature successfully verified.");

				result = true;
			} else {
				LOGGER.info("Client challenge signature verification fails.");
			}
		} catch (CertificateException | GeneralCryptoLibException e) {
			throw new CryptographicOperationException("An error occured encoding a certificate object:", e);
		}
		return result;
	}

	@Override
	public int getOrder() {
		return order;
	}
}
