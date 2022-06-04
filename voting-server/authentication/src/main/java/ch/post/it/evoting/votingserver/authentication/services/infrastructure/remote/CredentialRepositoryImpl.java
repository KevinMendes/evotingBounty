/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.infrastructure.remote;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.votingserver.authentication.services.domain.model.material.CredentialRepository;
import ch.post.it.evoting.votingserver.commons.beans.authentication.Credential;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitConsumer;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.commons.util.PropertiesFileReader;

/**
 * The implementation of the operations on the credential repository.
 */
@Stateless(name = "au-CredentialRepositoryImpl")
public class CredentialRepositoryImpl implements CredentialRepository {

	private static final Logger LOGGER = LoggerFactory.getLogger(CredentialRepositoryImpl.class);

	// The name of the property path credentials.
	private static final String PROPERTY_PATH_CREDENTIAL = "path.credentials";

	// The value of the property path materials.
	private static final String PATH_MATERIALS = PropertiesFileReader.getInstance().getPropertyValue(PROPERTY_PATH_CREDENTIAL);

	@Inject
	private TrackIdInstance trackId;

	private VoterMaterialClient voterMaterialClient;

	@Inject
	CredentialRepositoryImpl(final VoterMaterialClient voterMaterialClient) {
		this.voterMaterialClient = voterMaterialClient;
	}

	/**
	 * Searches for voter credential data identified by the given tenant identifier, credential identifier and election event identifier. The
	 * implementation is a REST Client invoking to an get operation of a REST web service to obtain the credential data for the given parameters. If
	 * the web service returns a HTTP NOT FOUND status (404), then the method throws a ResourceNotFoundException exception.
	 *
	 * @param tenantId        - the identifier of the tenant.
	 * @param electionEventId - the identifier of the election event.
	 * @param credentialId    - the identifier of the credential.
	 * @return a Credential object containing credential data of a voter.
	 * @throws ResourceNotFoundException if no voter material is found.
	 */
	@Override
	public Credential findByTenantIdElectionEventIdCredentialId(String tenantId, String electionEventId, String credentialId)
			throws ResourceNotFoundException {
		LOGGER.info("Finding credential. [tenantId: {}, electionEventId: {}, credentialId: {}]", tenantId, electionEventId, credentialId);

		try {
			Credential credential = RetrofitConsumer.processResponse(
					voterMaterialClient.getCredential(trackId.getTrackId(), PATH_MATERIALS, tenantId, electionEventId, credentialId));
			LOGGER.info("Credential found. [tenantId: {}, electionEventId: {}, credentialId: {}]", tenantId, electionEventId, credentialId);
			return credential;
		} catch (RetrofitException e) {
			throw e;
		} catch (ResourceNotFoundException e) {
			LOGGER.error("Credential not found. [tenantId: {}, electionEventId: {}, credentialId: {}]", tenantId, electionEventId, credentialId);
			throw e;
		}
	}
}
