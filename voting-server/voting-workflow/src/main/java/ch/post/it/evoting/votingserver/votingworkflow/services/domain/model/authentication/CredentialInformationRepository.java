/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.authentication;

import javax.ejb.Local;

import ch.post.it.evoting.votingserver.commons.beans.authentication.CredentialInformation;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;

/**
 * Repository for handling AuthenticationInformation.
 */
@Local
public interface CredentialInformationRepository {

	/**
	 * Searches for the Authentication information related to given parameters.
	 *
	 * @param tenantId        - the identifier of the tenant.
	 * @param electionEventId - the identifier of the election event.
	 * @param credentialId    - the identifier of the credential.
	 * @return The associated AuthenticationInformation, if found.
	 * @throws ResourceNotFoundException if the authentication information is not found.
	 */
	CredentialInformation findByTenantElectionEventCredential(String tenantId, String electionEventId, String credentialId)
			throws ResourceNotFoundException, ApplicationException;
}
