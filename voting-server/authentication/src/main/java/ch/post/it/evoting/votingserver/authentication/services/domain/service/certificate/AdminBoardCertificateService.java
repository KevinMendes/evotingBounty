/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.service.certificate;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObjectBuilder;

import ch.post.it.evoting.domain.election.model.certificate.CertificateEntity;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.adminboard.AdminBoard;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.adminboard.AdminBoardRepository;
import ch.post.it.evoting.votingserver.authentication.services.infrastructure.remote.AuRemoteCertificateService;
import ch.post.it.evoting.votingserver.commons.beans.authentication.AdminBoardCertificates;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.service.RemoteCertificateService;

/**
 * Service for retrieving the admin board certificate
 */
@Stateless
public class AdminBoardCertificateService {

	public static final String ADMINISTRATION_BOARD_CN_PREFIX = "AdministrationBoard ";

	@Inject
	private AdminBoardRepository adminBoardRepository;

	@Inject
	@AuRemoteCertificateService
	private RemoteCertificateService remoteCertificateService;

	/**
	 * Retrieves admin board and tenant CA certificates.
	 *
	 * @param tenantId        - the identifier of the tenant.
	 * @param electionEventId - the identifier of the election event. - the identifier of the credential.
	 * @return Json representation of the admin and tenant certificates
	 * @throws ResourceNotFoundException - if credential data which is part of authentication information can not be found.
	 */
	public AdminBoardCertificates getAdminBoardAndTenantCA(final String tenantId, final String electionEventId) throws ResourceNotFoundException {
		// get certificate information
		final AdminBoard adminBoard = adminBoardRepository.findByTenantIdElectionEventId(tenantId, electionEventId);
		final String adminBoardCommonName = ADMINISTRATION_BOARD_CN_PREFIX + adminBoard.getAdminBoardId();
		final CertificateEntity adminBoardCertificateEntity = remoteCertificateService.getAdminBoardCertificate(adminBoardCommonName);
		final CertificateEntity tenantCA = remoteCertificateService.getTenantCACertificate(tenantId);

		final JsonObjectBuilder certificateJsonBuilder = Json.createObjectBuilder();
		certificateJsonBuilder.add("adminBoard", adminBoardCertificateEntity.getCertificateContent());
		certificateJsonBuilder.add("tenantCA", tenantCA.getCertificateContent());
		return new AdminBoardCertificates(certificateJsonBuilder.build().toString());
	}

}
