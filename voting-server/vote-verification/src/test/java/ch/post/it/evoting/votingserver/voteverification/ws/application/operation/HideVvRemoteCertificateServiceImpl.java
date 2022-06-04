/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.voteverification.ws.application.operation;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;

import ch.post.it.evoting.cryptolib.certificates.bean.CertificateParameters;
import ch.post.it.evoting.cryptolib.certificates.cryptoapi.CryptoAPIX509Certificate;
import ch.post.it.evoting.domain.election.model.certificate.CertificateEntity;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.service.RemoteCertificateService;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.service.RemoteCertificateServiceImpl;
import ch.post.it.evoting.votingserver.commons.util.CryptoUtils;
import ch.post.it.evoting.votingserver.voteverification.infrastructure.remote.VvRemoteCertificateService;

@Stateless(name = "vvRemoteCertificateService")
@VvRemoteCertificateService
public class HideVvRemoteCertificateServiceImpl extends RemoteCertificateServiceImpl implements RemoteCertificateService {

	protected static final String ELECTION_EVENT_ID = "67cd59d8183f47a3a4cc64abfcc2916b";
	protected static final String TENANT_ID = "100";
	protected static final String PLATFORM_NAME = "";
	protected static final String CERTIFICATE_NAME = "";
	private KeyPair keyPair;

	@Override
	public CertificateEntity getAdminBoardCertificate(String id) {
		try {
			CryptoAPIX509Certificate cert = CryptoUtils.createCryptoAPIx509Certificate("certificate", CertificateParameters.Type.SIGN, keyPair);
			String certificateContent = new String(cert.getPemEncoded(), StandardCharsets.UTF_8);
			CertificateEntity certificateEntity = new CertificateEntity();
			certificateEntity.setCertificateContent(certificateContent);
			certificateEntity.setCertificateName(CERTIFICATE_NAME);
			certificateEntity.setElectionEventId(ELECTION_EVENT_ID);
			certificateEntity.setPlatformName(PLATFORM_NAME);
			certificateEntity.setTenantId(TENANT_ID);

			return certificateEntity;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@PostConstruct
	@Override
	public void intializeElectionInformationAdminClient() {
		keyPair = VoteVerificationArquillianDeployment.keyPair;
	}

}
