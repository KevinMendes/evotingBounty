/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.infrastructure.remote;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.votingserver.commons.beans.authentication.AdminBoardCertificates;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitConsumer;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.commons.util.PropertiesFileReader;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.authentication.AdminBoardCertificateRepository;

/**
 * Implementation of the AuthenticationInformationRepository using a REST client.
 */
@Stateless
public class AdminBoardCertificateRepositoryImpl implements AdminBoardCertificateRepository {

	private static final PropertiesFileReader PROPERTIES = PropertiesFileReader.getInstance();

	private static final String AUTHENTICATION_CERTIFICATES_PATH = PROPERTIES.getPropertyValue("AUTHENTICATION_CERTIFICATES_PATH");

	private static final Logger LOGGER = LoggerFactory.getLogger(AdminBoardCertificateRepositoryImpl.class);

	@Inject
	private TrackIdInstance trackId;

	private AuthenticationClient authenticationClient;

	@Inject
	AdminBoardCertificateRepositoryImpl(final AuthenticationClient authenticationClient) {
		this.authenticationClient = authenticationClient;
	}

	@Override
	public AdminBoardCertificates findByTenantElectionEventCertificates(String tenantId, String electionEventId)
			throws ResourceNotFoundException, ApplicationException {
		LOGGER.info("Getting Admin Board and Tenant certificates. [tenantId: {}, electionEventId: {}]", tenantId, electionEventId);

		try {
			AdminBoardCertificates adminAndTenantCertificates = RetrofitConsumer.processResponse(authenticationClient
					.findByTenantElectionEventCertificates(trackId.getTrackId(), AUTHENTICATION_CERTIFICATES_PATH, tenantId, electionEventId));
			LOGGER.info("Admin Board and Tenant certificates not found. [tenantId: {}, electionEventId: {}]", tenantId, electionEventId);
			return adminAndTenantCertificates;
		} catch (ResourceNotFoundException e) {
			LOGGER.error("Admin Board and Tenant certificates not found. [tenantId: {}, electionEventId: {}]", tenantId, electionEventId);
			throw e;
		}
	}
}
