/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votermaterial.domain.model.information;

import java.util.List;

import javax.ejb.Local;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.domain.model.BaseRepository;

/**
 * Interface defining the operations for handling voter information.
 */
@Local
public interface VoterInformationRepository extends BaseRepository<VoterInformation, Integer> {

	/**
	 * Returns a voter information for a given tenant, election event and credential.
	 *
	 * @param tenantId        The tenant identifier.
	 * @param electionEventId The election event identifier.
	 * @param credentialId    The credential identifier.
	 * @return The voter information.
	 * @throws ResourceNotFoundException if the voter information is not found.
	 */
	VoterInformation findByTenantIdElectionEventIdCredentialId(String tenantId, String electionEventId, String credentialId)
			throws ResourceNotFoundException;

	/**
	 * Returns a voter information for a given tenant, election event and voting card.
	 *
	 * @param tenantId        The tenant identifier.
	 * @param electionEventId The election event identifier.
	 * @param votingCardId    The voting card identifier.
	 * @return The voter information.
	 * @throws ResourceNotFoundException if the voter information is not found.
	 */
	VoterInformation findByTenantIdElectionEventIdVotingCardId(String tenantId, String electionEventId, String votingCardId)
			throws ResourceNotFoundException;

	/**
	 * Returns whether exists a voter information for given tenant, election event and voting card.
	 *
	 * @param tenantId        The tenant identifier
	 * @param electionEventId The election event identifier
	 * @param votingCardId    The voting card identifier
	 * @return The voter information exists.
	 */
	boolean hasWithTenantIdElectionEventIdVotingCardId(String tenantId, String electionEventId, String votingCardId);

	/**
	 * Find by tenant id election event id and search terms.
	 *
	 * @param tenantId        the tenant id
	 * @param electionEventId the election event id
	 * @param idSearchTerm    the id search term
	 * @param pageNumber      the page number
	 * @param pageSize        the page size
	 * @return the list
	 * @throws ResourceNotFoundException the resource not found exception
	 */
	List<VoterInformation> findByTenantIdElectionEventIdAndSearchTerms(String tenantId, String electionEventId, final String idSearchTerm,
			final int pageNumber, final int pageSize) throws ResourceNotFoundException;

	/**
	 * Count by tenant id election event id and searchTerms.
	 *
	 * @param tenantId        the tenant id
	 * @param electionEventId the election event id
	 * @param idSearchTerm    the id search term
	 * @return the long
	 * @throws ResourceNotFoundException the resource not found exception
	 */
	long countByTenantIdElectionEventIdAndSearchTerms(String tenantId, String electionEventId, final String idSearchTerm)
			throws ResourceNotFoundException;

}
