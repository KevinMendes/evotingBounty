/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.service.challenge;

import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.security.cert.Certificate;
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
import ch.post.it.evoting.votingserver.commons.crypto.CertificateChainRepository;
import ch.post.it.evoting.votingserver.commons.crypto.SignatureForObjectService;

/**
 * This class implements the expiration time validation of the server challenge message signature.
 */
public class ChallengeServerInformationSignatureValidation implements ChallengeInformationValidation {

	private static final Logger LOGGER = LoggerFactory.getLogger(ChallengeServerInformationSignatureValidation.class);

	private static final String KEYSTORE_ALIAS = "privatekey";
	private static final String CERT_ALIAS = "AuthTokenSigner ";
	private static final int RULE_ORDER = 3;
	private final int order;
	@Inject
	private AsymmetricServiceAPI asymmetricService;
	@Inject
	private SignatureForObjectService signatureService;
	@Inject
	private CertificateChainRepository certificateChainRepository;

	public ChallengeServerInformationSignatureValidation() {
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

		boolean result;
		try {

			String serverSignature = challengeInformation.getServerChallengeMessage().getSignature();
			// get the certificate chain for tenant and election event for a given alias
			Certificate[] certChain = certificateChainRepository.findByTenantEEIDAlias(tenantId, electionEventId, KEYSTORE_ALIAS);

			// get public key
			PublicKey serverPublicKey = signatureService.getPublicKeyByAliasInCertificateChain(certChain, CERT_ALIAS + electionEventId);

			// verify server challenge signature
			byte[] serverChallengeDataToSignInput = StringUtils.join(challengeInformation.getServerChallengeMessage().getServerChallenge(),
							challengeInformation.getServerChallengeMessage().getTimestamp(), electionEventId, challengeInformation.getCredentialId())
					.getBytes(StandardCharsets.UTF_8);

			// verify signature
			byte[] serverSignatureBytes = Base64.getDecoder().decode(serverSignature);
			result = asymmetricService.verifySignature(serverSignatureBytes, serverPublicKey, new byte[][] { serverChallengeDataToSignInput });
		} catch (CryptographicOperationException | GeneralCryptoLibException e) {
			throw new CryptographicOperationException("An error occured verifying signature:", e);
		}

		LOGGER.info("Server challenge signature verification. [result: {}]", result);

		return result;

	}

	@Override
	public int getOrder() {
		return order;
	}
}
