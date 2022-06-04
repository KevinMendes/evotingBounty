/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.extendedauthentication.services.infrastructure.remote;

import java.util.Optional;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.domain.election.model.Information.VoterInformation;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitConsumer;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.extendedauthentication.services.infrastructure.remote.client.VoterMaterialServiceClient;

/**
 * Remote Service for handling voter material
 */

@Stateless
public class RemoteVoterMaterialServiceImpl implements VoterMaterialService {

	private static final String PATH_VOTER_INFORMATION = "informations";
	private static final Logger LOGGER = LoggerFactory.getLogger(RemoteVoterMaterialServiceImpl.class);
	private final VoterMaterialServiceClient voterMaterialServiceClient;
	@Inject
	protected TrackIdInstance trackIdInstance;

	@Inject
	RemoteVoterMaterialServiceImpl(final VoterMaterialServiceClient voterMaterialServiceClient) {
		this.voterMaterialServiceClient = voterMaterialServiceClient;
	}

	@Override
	public Optional<VoterInformation> getVoterInformationByCredentialId(final String tenantId, final String electionEventId,
			final String credentialId) {
		try {
			return Optional.of(RetrofitConsumer.processResponse(voterMaterialServiceClient
					.getVoterInformationByCredentialId(PATH_VOTER_INFORMATION, trackIdInstance.getTrackId(), tenantId, electionEventId,
							credentialId)));
		} catch (RetrofitException e) {
			LOGGER.error(String.format("Failed to get VoterInformation associated with credential [tenantId=%s, electionEventId=%s, credentialId=%s]",
					tenantId, electionEventId, credentialId), e);
			return Optional.empty();
		}
	}
}
