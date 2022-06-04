/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votermaterial.infrastructure.persistence;

import java.util.List;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.votermaterial.domain.model.information.VoterInformation;
import ch.post.it.evoting.votingserver.votermaterial.domain.model.information.VoterInformationRepository;

/**
 * A decorator with logger for voterIinformation repository.
 */
@Decorator
public abstract class VoterInformationRepositoryDecorator implements VoterInformationRepository {

	@Inject
	@Delegate
	private VoterInformationRepository voterInformationRepository;

	/**
	 * Returns a voter information for a given tenant, election event and credential. In this
	 * implementation, we use a database query to obtain voter information taking into account the
	 * input parameters.
	 *
	 * @param tenantId        - the tenant identifier.
	 * @param electionEventId - the election event identifier.
	 * @param credentialId    - the credential identifier.
	 * @return The voter information.
	 * @throws ResourceNotFoundException if the voter information is not found.
	 */
	@Override
	public VoterInformation findByTenantIdElectionEventIdCredentialId(final String tenantId, final String electionEventId, final String credentialId)
			throws ResourceNotFoundException {
		return voterInformationRepository.findByTenantIdElectionEventIdCredentialId(tenantId, electionEventId, credentialId);
	}

	/**
	 * Returns a voter information for a given tenant, election event and voting card. In this
	 * implementation, we use a database query to obtain voter information taking into account the
	 * input parameters.
	 *
	 * @param tenantId        - the tenant identifier.
	 * @param electionEventId - the election event identifier.
	 * @param votingCardId    - the voting card identifier.
	 * @return The voter information.
	 * @throws ResourceNotFoundException if the voter information is not found.
	 */
	@Override
	public VoterInformation findByTenantIdElectionEventIdVotingCardId(final String tenantId, final String electionEventId, final String votingCardId)
			throws ResourceNotFoundException {
		return voterInformationRepository.findByTenantIdElectionEventIdVotingCardId(tenantId, electionEventId, votingCardId);
	}

	@Override
	public boolean hasWithTenantIdElectionEventIdVotingCardId(String tenantId, String electionEventId, String votingCardId) {
		return voterInformationRepository.hasWithTenantIdElectionEventIdVotingCardId(tenantId, electionEventId, votingCardId);
	}

	/**
	 * Find by tenant id election event id and search terms.
	 *
	 * @param tenantId        the tenant id
	 * @param electionEventId the election event id
	 * @param searchTerm      the search term
	 * @param pageNumber      the page number
	 * @param pageSize        the page size
	 * @return the list
	 * @throws ResourceNotFoundException the resource not found exception
	 */
	@Override
	public List<VoterInformation> findByTenantIdElectionEventIdAndSearchTerms(final String tenantId, final String electionEventId,
			final String searchTerm, final int pageNumber, final int pageSize) throws ResourceNotFoundException {
		return voterInformationRepository.findByTenantIdElectionEventIdAndSearchTerms(tenantId, electionEventId, searchTerm, pageNumber, pageSize);
	}

	/**
	 * Count by tenant id election event id.
	 *
	 * @param tenantId        the tenant id
	 * @param electionEventId the election event id
	 * @param idSearchTerm    the id search term
	 * @return the long
	 * @throws ResourceNotFoundException the resource not found exception
	 */
	@Override
	public long countByTenantIdElectionEventIdAndSearchTerms(final String tenantId, final String electionEventId, final String idSearchTerm)
			throws ResourceNotFoundException {
		return voterInformationRepository.countByTenantIdElectionEventIdAndSearchTerms(tenantId, electionEventId, idSearchTerm);
	}

	@Override
	public VoterInformation save(final VoterInformation entity) throws DuplicateEntryException {
		return voterInformationRepository.save(entity);
	}
}
