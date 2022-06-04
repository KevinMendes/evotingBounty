/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votingworkflow.services.infrastructure.remote;

import javax.ejb.Stateless;
import javax.inject.Inject;

import ch.post.it.evoting.domain.returncodes.ShortChoiceReturnCodeAndComputeResults;
import ch.post.it.evoting.domain.returncodes.VoteAndComputeResults;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitConsumer;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.commons.util.PropertiesFileReader;
import ch.post.it.evoting.votingserver.votingworkflow.services.domain.model.choicecode.ChoiceCodeRepository;

/**
 * Implementation of the ChoiceCodeRepository using a REST client.
 */
@Stateless
public class ChoiceCodeRepositoryImpl implements ChoiceCodeRepository {

	/**
	 * The properties file reader.
	 */
	private static final PropertiesFileReader PROPERTIES = PropertiesFileReader.getInstance();

	/**
	 * The path to the resource verification.
	 */
	private static final String CHOICE_CODE_PATH = PROPERTIES.getPropertyValue("CHOICE_CODE_PATH");
	private final VerificationClient verificationClient;
	@Inject
	private TrackIdInstance trackId;

	@Inject
	ChoiceCodeRepositoryImpl(final VerificationClient verificationClient) {
		this.verificationClient = verificationClient;
	}

	/**
	 * Generates the choice codes taking into account a tenant, election event and verification card
	 * for a given encrypted vote. This implementation is based on a rest client which call to a web
	 * service rest operation.
	 *
	 * @param tenantId           - the identifier of the tenant.
	 * @param electionEventId    - the identifier of the election event.
	 * @param verificationCardId - the identifier of the verification card.
	 * @param vote               - the encrypted vote.
	 * @return The choice codes generated and all compute results.
	 * @throws ResourceNotFoundException if the choice codes can not be generated.
	 */
	@Override
	public ShortChoiceReturnCodeAndComputeResults generateChoiceCodes(String tenantId, String electionEventId, String verificationCardId,
			VoteAndComputeResults vote) throws ResourceNotFoundException {
		return RetrofitConsumer.processResponse(
				verificationClient.generateChoiceCodes(trackId.getTrackId(), CHOICE_CODE_PATH, tenantId, electionEventId, verificationCardId, vote));
	}

}
