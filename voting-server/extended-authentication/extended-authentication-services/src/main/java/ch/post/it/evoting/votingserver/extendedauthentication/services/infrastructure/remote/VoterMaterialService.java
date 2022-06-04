/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.extendedauthentication.services.infrastructure.remote;

import java.util.Optional;

import ch.post.it.evoting.domain.election.model.Information.VoterInformation;

/**
 * Service for accessing the voterMaterialService material remote service
 */
public interface VoterMaterialService {

	/**
	 * Gets the voter information associated with the specified credential
	 *
	 * @param tenantId        the tenant identifier
	 * @param electionEventId the election event identifier
	 * @param credentialId    the credential identifier
	 * @return the Optional with VoterInformation or Optional.empty in case voter information was not
	 * found
	 */
	Optional<VoterInformation> getVoterInformationByCredentialId(final String tenantId, final String electionEventId, final String credentialId);

}
