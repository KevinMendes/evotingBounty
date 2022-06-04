/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votermaterial.domain.model.credential;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;

/**
 * Service for handling credential data.
 */
@Stateless
public class CredentialDataService {

	@Inject
	private Logger logger;

	@Inject
	private CredentialRepository credentialRepository;

	/**
	 * Gets the credential data base on the tenant, election event and credential identifier.
	 *
	 * @param tenantId        The tenant identifier.
	 * @param electionEventId The election event identifier.
	 * @param credentialId    The credential identifier.
	 * @return The credential data found.
	 * @throws ResourceNotFoundException if resource not found.
	 */
	public Credential getCredentialData(String tenantId, String electionEventId, String credentialId) throws ResourceNotFoundException {
		logger.info("Getting the voter credential for tenant: {} election event: {} and credential: {}.", tenantId, electionEventId, credentialId);

		Credential credential = credentialRepository.findByTenantIdElectionEventIdCredentialId(tenantId, electionEventId, credentialId);

		logger.info("Credential data for tenant: {} election event: {} and voting card: {} found.", tenantId, electionEventId, credentialId);

		return credential;
	}

}
