/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.authentication;

import javax.ejb.Local;

import ch.post.it.evoting.votingserver.commons.beans.authentication.AdminBoardCertificates;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;

/**
 * Repository for handling admin certificates
 */
@Local
public interface AdminBoardCertificateRepository {

	/**
	 * Gets the admin board and tenant certificates.
	 *
	 * @param tenantId        - identifier of the tenant.
	 * @param electionEventId - identifier of the election event .
	 * @return Json representation of the certificates
	 * @throws ResourceNotFoundException if authentication token can not be successfully build.
	 */
	AdminBoardCertificates findByTenantElectionEventCertificates(String tenantId, String electionEventId)
			throws ResourceNotFoundException, ApplicationException;

}
