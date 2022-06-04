/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.infrastructure.remote;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.votingserver.commons.beans.authentication.CredentialInformation;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitConsumer;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.commons.util.PropertiesFileReader;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.authentication.CredentialInformationRepository;

/**
 * Implementation of the AuthenticationInformationRepository using a REST client.
 */
@Stateless
public class CredentialInformationRepositoryImpl implements CredentialInformationRepository {

	private static final PropertiesFileReader PROPERTIES = PropertiesFileReader.getInstance();

	private static final String AUTHENTICATION_INFORMATION_PATH = PROPERTIES.getPropertyValue("AUTHENTICATION_INFORMATION_PATH");

	private static final Logger LOGGER = LoggerFactory.getLogger(CredentialInformationRepositoryImpl.class);

	@Inject
	private TrackIdInstance trackId;

	private AuthenticationClient authenticationClient;

	@Inject
	CredentialInformationRepositoryImpl(final AuthenticationClient authenticationClient) {
		this.authenticationClient = authenticationClient;
	}

	/**
	 * Searches the associated authentication information for the given parameters using a Rest client.
	 *
	 * @param tenantId        - identifier of the tenant.
	 * @param electionEventId - identifier of the election event .
	 * @param credentialId    - identifier of the credential .
	 * @return an AuthenticationInformation object if found
	 * @throws ResourceNotFoundException
	 */
	@Override
	public CredentialInformation findByTenantElectionEventCredential(String tenantId, String electionEventId, String credentialId)
			throws ResourceNotFoundException, ApplicationException {

		LOGGER.info("Getting authentication information. [tenantId: {}, electionEventId: {}, credentialId: {}]", tenantId, electionEventId,
				credentialId);

		try {
			CredentialInformation credentialInformation = RetrofitConsumer.processResponse(authenticationClient
					.findByTenantElectionEventCredential(trackId.getTrackId(), AUTHENTICATION_INFORMATION_PATH, tenantId, electionEventId,
							credentialId));
			LOGGER.info("Authentication information found. [tenantId: {}, electionEventId: {}, credentialId: {}]", tenantId, electionEventId,
					credentialId);

			return credentialInformation;
		} catch (ResourceNotFoundException e) {
			LOGGER.error("Authentication information not found. [tenantId: {}, electionEventId: {}, credentialId: {}]", tenantId, electionEventId,
					credentialId);
			throw e;
		}
	}
}
