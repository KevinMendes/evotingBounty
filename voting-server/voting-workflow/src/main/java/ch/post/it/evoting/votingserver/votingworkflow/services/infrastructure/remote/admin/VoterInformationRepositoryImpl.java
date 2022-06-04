/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.infrastructure.remote.admin;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RestClientConnectionManager;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitConsumer;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.commons.util.PropertiesFileReader;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.information.VoterInformation;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.information.VoterInformationRepository;

import retrofit2.Retrofit;

/**
 * The Class VoterInformationRepositoryImpl.
 */
@Stateless(name = "vw-VoterInformationRepositoryImpl")
public class VoterInformationRepositoryImpl implements VoterInformationRepository {

	private static final PropertiesFileReader PROPERTIES = PropertiesFileReader.getInstance();

	private static final String URI_VOTER_MATERIAL = System.getenv("VOTER_MATERIAL_CONTEXT_URL");

	private static final String VOTER_INFORMATION_PATH = PROPERTIES.getPropertyValue("VOTER_INFORMATION_PATH");

	private static final Logger LOGGER = LoggerFactory.getLogger(VoterInformationRepositoryImpl.class);

	@Inject
	private TrackIdInstance trackId;

	private VoterMaterialAdminClient voterMaterialAdminClient;

	@PostConstruct
	public void intializeClients() {
		Retrofit client = RestClientConnectionManager.getInstance().getRestClient(URI_VOTER_MATERIAL);
		voterMaterialAdminClient = client.create(VoterMaterialAdminClient.class);
	}

	/**
	 * Gets the by tenant id election event id voting card id.
	 *
	 * @param tenantId        the tenant id
	 * @param electionEventId the election event id
	 * @param votingCardId    the voting card id
	 * @return the by tenant id election event id voting card id
	 * @throws ResourceNotFoundException the resource not found exception
	 */
	@Override
	public VoterInformation getByTenantIdElectionEventIdVotingCardId(final String tenantId, final String electionEventId, final String votingCardId)
			throws ResourceNotFoundException {

		LOGGER.info("Getting voter information. [tenantId: {}, electionEventId: {}, votingCardId: {}]", tenantId, electionEventId, votingCardId);

		try {
			VoterInformation voterInformation = RetrofitConsumer.processResponse(voterMaterialAdminClient
					.getVoterInformations(trackId.getTrackId(), VOTER_INFORMATION_PATH, tenantId, electionEventId, votingCardId));

			// voting card id data found
			LOGGER.info("Voter information found. [tenantId: {}, electionEventId: {}, votingCardId: {}]", tenantId, electionEventId, votingCardId);

			return voterInformation;
		} catch (ResourceNotFoundException e) {
			// voting card id data not found
			LOGGER.error("Voter information not found. [tenantId: {}, electionEventId: {}, votingCardId: {}]", tenantId, electionEventId,
					votingCardId);
			throw e;
		}
	}

}
