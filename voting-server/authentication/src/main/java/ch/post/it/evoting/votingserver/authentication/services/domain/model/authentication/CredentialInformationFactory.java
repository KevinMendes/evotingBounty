/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication;

import java.util.Base64;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.JsonObjectBuilder;

import ch.post.it.evoting.cryptolib.certificates.utils.CryptographicOperationException;
import ch.post.it.evoting.domain.election.model.certificate.CertificateEntity;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.adminboard.AdminBoard;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.adminboard.AdminBoardRepository;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.material.CredentialRepository;
import ch.post.it.evoting.votingserver.authentication.services.infrastructure.remote.AuRemoteCertificateService;
import ch.post.it.evoting.votingserver.commons.beans.authentication.Credential;
import ch.post.it.evoting.votingserver.commons.beans.authentication.CredentialInformation;
import ch.post.it.evoting.votingserver.commons.beans.challenge.ServerChallengeMessage;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.crypto.SignatureForObjectService;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.service.RemoteCertificateService;
import ch.post.it.evoting.votingserver.commons.util.DateUtils;
import ch.post.it.evoting.votingserver.commons.util.JsonUtils;

/**
 * Factory for building authentication information.
 */
@Stateless
public class CredentialInformationFactory {

	public static final String ADMINISTRATION_BOARD_CN_PREFIX = "AdministrationBoard ";
	private static final String KEYSTORE_ALIAS = "privatekey";
	private static final String EMPTY_ID = "";
	@Inject
	private CredentialRepository credentialRepository;
	@Inject
	private SignatureForObjectService signatureService;
	@Inject
	private AuthenticationCertsRepository authenticationCertsRepository;

	@Inject
	private AdminBoardRepository adminBoardRepository;
	@Inject
	@AuRemoteCertificateService
	private RemoteCertificateService remoteCertificateService;

	/**
	 * Builds an credential information according with the set of input parameters.
	 *
	 * @param tenantId          - the identifier of the tenant.
	 * @param electionEventId   - the identifier of the election event.
	 * @param credentialId      - the identifier of the credential.
	 * @param randomValueLength - the length of the generated random value.
	 * @return an credential information setting up with the corresponding information.
	 * @throws ResourceNotFoundException       - if credential data which is part of authentication information can not be found.
	 * @throws CryptographicOperationException - if an error occurred during the signature of the challenge
	 */
	public CredentialInformation buildCredentialInformation(final String tenantId, final String electionEventId, final String credentialId,
			final int randomValueLength) throws ResourceNotFoundException, CryptographicOperationException {

		// get credential data
		Credential credentialData = credentialRepository.findByTenantIdElectionEventIdCredentialId(tenantId, electionEventId, credentialId);

		// build server challenge message
		Challenge challenge = new Challenge(randomValueLength);
		String currentTimestamp = DateUtils.getTimestamp();

		byte[] signedChallenge = signatureService
				.sign(tenantId, electionEventId, EMPTY_ID, KEYSTORE_ALIAS, challenge.getChallengeValue(), currentTimestamp, electionEventId,
						credentialId);
		String base64SignedChallenge = Base64.getEncoder().encodeToString(signedChallenge);
		ServerChallengeMessage serverChallengeMessage = new ServerChallengeMessage(challenge.getChallengeValue(), currentTimestamp,
				base64SignedChallenge);

		AuthenticationCerts certificates = buildCertificatesInfo(tenantId, electionEventId);

		// build credential information
		return new CredentialInformation(credentialData, serverChallengeMessage, certificates.getJson(), certificates.getSignature());
	}

	private AuthenticationCerts buildCertificatesInfo(final String tenantId, final String electionEventId) throws ResourceNotFoundException {
		// get certificates
		AuthenticationCerts certificates = authenticationCertsRepository.findByTenantIdElectionEventId(tenantId, electionEventId);
		AdminBoard adminBoard = adminBoardRepository.findByTenantIdElectionEventId(tenantId, electionEventId);
		final String adminBoardCommonName = ADMINISTRATION_BOARD_CN_PREFIX + adminBoard.getAdminBoardId();
		CertificateEntity adminBoardCertificateEntity = remoteCertificateService.getAdminBoardCertificate(adminBoardCommonName);
		CertificateEntity tenantCA = remoteCertificateService.getTenantCACertificate(tenantId);

		JsonObjectBuilder certificateJsonBuilder = JsonUtils.jsonObjectToBuilder(JsonUtils.getJsonObject(certificates.getJson()));
		certificateJsonBuilder.add("adminBoard", adminBoardCertificateEntity.getCertificateContent());
		certificateJsonBuilder.add("tenantCA", tenantCA.getCertificateContent());
		certificates.setJson(certificateJsonBuilder.build().toString());

		return certificates;
	}

	/**
	 * Builds an credential information according with the set of input parameters.
	 *
	 * @param tenantId        - the identifier of the tenant.
	 * @param electionEventId - the identifier of the election event. - the identifier of the credential.
	 * @return an credential information setting up with the corresponding information.
	 * @throws ResourceNotFoundException - if credential data which is part of authentication information can not be found.
	 */
	public AuthenticationCerts buildAuthenticationCertificates(final String tenantId, final String electionEventId) throws ResourceNotFoundException {

		return buildCertificatesInfo(tenantId, electionEventId);
	}
}
