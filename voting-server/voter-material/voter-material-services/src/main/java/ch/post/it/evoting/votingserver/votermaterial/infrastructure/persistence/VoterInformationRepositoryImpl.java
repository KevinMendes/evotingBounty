/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.votermaterial.infrastructure.persistence;

import java.util.List;

import javax.ejb.Stateless;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.ParameterExpression;
import javax.persistence.criteria.Root;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.infrastructure.persistence.BaseRepositoryImpl;
import ch.post.it.evoting.votingserver.votermaterial.domain.model.information.VoterInformation;
import ch.post.it.evoting.votingserver.votermaterial.domain.model.information.VoterInformationRepository;

/**
 * Implementation for VoterInformationRepository.
 */
@Stateless
public class VoterInformationRepositoryImpl extends BaseRepositoryImpl<VoterInformation, Integer> implements VoterInformationRepository {

	// The name of parameter tenant id
	private static final String PARAMETER_TENANT_ID = "tenantId";

	// The name of parameter election event id
	private static final String PARAMETER_ELECTION_EVENT_ID = "electionEventId";

	// The name of parameter credential id
	private static final String PARAMETER_CREDENTIAL_ID = "credentialId";

	// The name of parameter voting card id
	private static final String PARAMETER_VOTING_CARD_ID = "votingCardId";

	/* The sql escape character */
	private static final char SQL_ESCAPE_CHARACTER = '~';

	/* The sql match any string character */
	private static final char SQL_MATCH_ANY_CHARACTER = '%';

	/* The empty stringr */
	private static final String EMTPY_STRING = "";

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
		TypedQuery<VoterInformation> query = entityManager.createQuery(
				"SELECT vi FROM VoterInformation vi WHERE vi.tenantId = :tenantId AND vi.electionEventId = :electionEventId AND vi.credentialId = :credentialId",
				VoterInformation.class);
		query.setParameter(PARAMETER_TENANT_ID, tenantId);
		query.setParameter(PARAMETER_ELECTION_EVENT_ID, electionEventId);
		query.setParameter(PARAMETER_CREDENTIAL_ID, credentialId);

		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			throw new ResourceNotFoundException("", e);
		}
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
		TypedQuery<VoterInformation> query = entityManager.createQuery(
				"SELECT vi FROM VoterInformation vi WHERE vi.tenantId = :tenantId AND vi.electionEventId = :electionEventId AND vi.votingCardId = :votingCardId",
				VoterInformation.class);
		query.setParameter(PARAMETER_TENANT_ID, tenantId);
		query.setParameter(PARAMETER_ELECTION_EVENT_ID, electionEventId);
		query.setParameter(PARAMETER_VOTING_CARD_ID, votingCardId);

		try {
			return query.getSingleResult();
		} catch (NoResultException e) {
			throw new ResourceNotFoundException("", e);
		}
	}

	@Override
	public boolean hasWithTenantIdElectionEventIdVotingCardId(String tenantId, String electionEventId, String votingCardId) {
		TypedQuery<Long> query = entityManager.createQuery(
				"SELECT COUNT(vi) FROM VoterInformation vi WHERE vi.tenantId = :tenantId AND vi.electionEventId = :electionEventId AND vi.votingCardId = :votingCardId",
				Long.class);
		query.setParameter(PARAMETER_TENANT_ID, tenantId);
		query.setParameter(PARAMETER_ELECTION_EVENT_ID, electionEventId);
		query.setParameter(PARAMETER_VOTING_CARD_ID, votingCardId);
		return query.getSingleResult() > 0;
	}

	/**
	 * Find by tenant id election event id and search terms.
	 *
	 * @param tenantId         the tenant id
	 * @param electionEventId  the election event id
	 * @param termVotingCardid the term voting Card id
	 * @param pageNumber       the page number
	 * @param pageSize         the page size
	 * @return the list
	 * @throws ResourceNotFoundException the resource not found exception
	 */
	@Override
	public List<VoterInformation> findByTenantIdElectionEventIdAndSearchTerms(final String tenantId, final String electionEventId,
			final String termVotingCardid, final int pageNumber, final int pageSize) throws ResourceNotFoundException {

		String parameterTermId;

		// Match term start and everything after
		if (termVotingCardid != null) {
			parameterTermId = termVotingCardid;
		} else {
			parameterTermId = EMTPY_STRING;
		}

		parameterTermId = parameterTermId + SQL_MATCH_ANY_CHARACTER;

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<VoterInformation> cq = cb.createQuery(VoterInformation.class);
		Root<VoterInformation> voterInformationRoot = cq.from(VoterInformation.class);

		ParameterExpression<String> tenantIdParam = cb.parameter(String.class);
		ParameterExpression<String> electionEventIdParam = cb.parameter(String.class);
		ParameterExpression<String> votingCardIdParam = cb.parameter(String.class);

		cq.select(voterInformationRoot).where(cb.equal(voterInformationRoot.get(PARAMETER_TENANT_ID), tenantIdParam),
				cb.equal(voterInformationRoot.get(PARAMETER_ELECTION_EVENT_ID), electionEventIdParam),
				cb.like(cb.lower(voterInformationRoot.get(PARAMETER_VOTING_CARD_ID)), votingCardIdParam, SQL_ESCAPE_CHARACTER));

		TypedQuery<VoterInformation> query = entityManager.createQuery(cq);
		query.setParameter(tenantIdParam, tenantId);
		query.setParameter(electionEventIdParam, electionEventId);
		query.setParameter(votingCardIdParam, parameterTermId.toLowerCase());
		query.setFirstResult((pageNumber) * pageSize);
		query.setMaxResults(pageSize);

		return query.getResultList();
	}

	/**
	 * Count by tenant id election event id and searchTerms.
	 *
	 * @param tenantId        the tenant id
	 * @param electionEventId the election event id
	 * @param idSearchTerm    the id search term
	 * @return the long
	 * @throws ResourceNotFoundException the resource not found exception
	 */
	@Override
	public long countByTenantIdElectionEventIdAndSearchTerms(final String tenantId, final String electionEventId, final String termVotingCardid)
			throws ResourceNotFoundException {

		String parameterTermId;

		// Match term start and everything after
		if (termVotingCardid != null) {
			parameterTermId = termVotingCardid;
		} else {
			parameterTermId = EMTPY_STRING;
		}

		parameterTermId = parameterTermId + SQL_MATCH_ANY_CHARACTER;

		CriteriaBuilder cb = entityManager.getCriteriaBuilder();
		CriteriaQuery<Long> cq = cb.createQuery(Long.class);
		Root<VoterInformation> voterInformationRoot = cq.from(VoterInformation.class);

		ParameterExpression<String> tenantIdParam = cb.parameter(String.class);
		ParameterExpression<String> electionEventIdParam = cb.parameter(String.class);
		ParameterExpression<String> votingCardIdParam = cb.parameter(String.class);

		cq.select(cb.count(voterInformationRoot)).where(cb.equal(voterInformationRoot.get(PARAMETER_TENANT_ID), tenantIdParam),
				cb.equal(voterInformationRoot.get(PARAMETER_ELECTION_EVENT_ID), electionEventIdParam),
				cb.like(cb.lower(voterInformationRoot.get(PARAMETER_VOTING_CARD_ID)), votingCardIdParam, SQL_ESCAPE_CHARACTER));

		TypedQuery<Long> query = entityManager.createQuery(cq);
		query.setParameter(tenantIdParam, tenantId);
		query.setParameter(electionEventIdParam, electionEventId);
		query.setParameter(votingCardIdParam, parameterTermId.toLowerCase());

		return query.getSingleResult();
	}
}
