/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.infrastructure.remote;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitConsumer;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.commons.util.PropertiesFileReader;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.verification.Verification;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.verification.VerificationRepository;

/**
 * Implementation of the VerificationRepository using a REST client.
 */
@Stateless(name = "vw-VerificationRepositoryImpl")
public class VerificationRepositoryImpl implements VerificationRepository {

	/**
	 * The properties file reader.
	 */
	private static final PropertiesFileReader PROPERTIES = PropertiesFileReader.getInstance();

	/**
	 * The path to the resource verification.
	 */
	private static final String VERIFICATION_PATH = PROPERTIES.getPropertyValue("VERIFICATION_PATH");

	private static final Logger LOGGER = LoggerFactory.getLogger(VerificationRepositoryImpl.class);

	private final VerificationClient verificationClient;
	@Inject
	private TrackIdInstance trackId;

	@Inject
	VerificationRepositoryImpl(final VerificationClient verificationClient) {
		this.verificationClient = verificationClient;
	}

	/**
	 * Searches the associated verification data for the given parameters using a Rest client.
	 *
	 * @param tenantId           - the identifier of the tenant.
	 * @param electionEventId    - the identifier of the election event.
	 * @param verificationCardId - the identifier of the verification card.
	 * @return a Verification object if found.
	 * @throws ResourceNotFoundException if the resource is not found.
	 */
	@Override
	public Verification findByTenantElectionEventVotingCard(String tenantId, String electionEventId, String verificationCardId)
			throws ResourceNotFoundException {

		LOGGER.info("Getting verification data. [tenantId: {}, electionEventId: {}, verificationCardId: {}]", tenantId, electionEventId,
				verificationCardId);

		try {
			Verification verification = RetrofitConsumer.processResponse(verificationClient
					.findVerificationByTenantElectionEventVerificationCard(trackId.getTrackId(), VERIFICATION_PATH, tenantId, electionEventId,
							verificationCardId));
			LOGGER.info("Verification data found. [tenantId: {}, electionEventId: {}, verificationCardId: {}]", tenantId, electionEventId,
					verificationCardId);

			return verification;

		} catch (ResourceNotFoundException e) {
			LOGGER.error("Verification data not found. [tenantId: {}, electionEventId: {}, verificationCardId: {}]", tenantId, electionEventId,
					verificationCardId);
			throw e;
		}

	}

}
