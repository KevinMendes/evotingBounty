/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.infrastructure.remote;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.domain.returncodes.VoteAndComputeResults;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.VoteRepositoryException;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitConsumer;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.commons.util.PropertiesFileReader;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.vote.VoteRepository;

import okhttp3.ResponseBody;

/**
 * Implementation of vote repository as a REST client which redirects operations to other Contexts.
 */
@Stateless
public class VoteRepositoryImpl implements VoteRepository {

	private static final PropertiesFileReader PROPERTIES = PropertiesFileReader.getInstance();

	private static final String VOTES_PATH = PROPERTIES.getPropertyValue("VOTES_PATH");

	private static final Logger LOGGER = LoggerFactory.getLogger(VoteRepositoryImpl.class);

	@Inject
	private TrackIdInstance trackId;

	private ElectionInformationClient electionInformationClient;

	@Inject
	VoteRepositoryImpl(final ElectionInformationClient electionInformationClient) {
		this.electionInformationClient = electionInformationClient;
	}

	/**
	 * Saves a vote in the repository. In this case, use a rest call to a RESTful webservice to store the vote in the ballot box.
	 *
	 * @param tenantId                      - the tenant id
	 * @param electionEventId               - the election event id
	 * @param vote                          - the vote information with computation proofs and signatures.
	 * @param authenticationTokenJsonString - the authentication token in json format.
	 * @throws ResourceNotFoundException
	 */
	@Override
	public void save(String tenantId, String electionEventId, VoteAndComputeResults vote, String authenticationTokenJsonString)
			throws ResourceNotFoundException {
		RetrofitConsumer.processResponse(
				electionInformationClient.saveVote(trackId.getTrackId(), VOTES_PATH, tenantId, electionEventId, vote, authenticationTokenJsonString));
	}

	@Override
	public VoteAndComputeResults findByTenantIdElectionEventIdVotingCardId(String tenantId, String electionEventId, String votingCardId)
			throws ResourceNotFoundException, VoteRepositoryException {
		try {
			return RetrofitConsumer
					.processResponse(electionInformationClient.getVote(trackId.getTrackId(), VOTES_PATH, tenantId, electionEventId, votingCardId));
		} catch (RetrofitException rfE) {
			if (rfE.getHttpCode() == 404) {
				throw new ResourceNotFoundException("Error trying to find voting card.", rfE);
			} else {
				throw new VoteRepositoryException("Error trying to find by tenant id election event Id voting card Id.", rfE);
			}
		}
	}

	@Override
	public boolean voteExists(String tenantId, String electionEventId, String votingCardId) throws VoteRepositoryException {
		try (ResponseBody responseBody = RetrofitConsumer
				.processResponse(electionInformationClient.checkVote(trackId.getTrackId(), VOTES_PATH, tenantId, electionEventId, votingCardId))) {
			// 200 OK, vote exists
			return true;
		} catch (RetrofitException rfE) {
			if (rfE.getHttpCode() == 404) {
				return false;
			} else {
				LOGGER.error("Error trying to find if a Vote exists.", rfE);
				throw new VoteRepositoryException("Error trying to find if a Vote exists.", rfE);
			}
		}
	}
}
