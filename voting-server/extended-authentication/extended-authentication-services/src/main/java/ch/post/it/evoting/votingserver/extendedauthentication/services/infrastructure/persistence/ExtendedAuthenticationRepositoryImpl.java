/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.extendedauthentication.services.infrastructure.persistence;

import java.util.List;
import java.util.Optional;

import javax.ejb.Stateless;
import javax.persistence.LockModeType;
import javax.persistence.LockTimeoutException;
import javax.persistence.PessimisticLockException;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.infrastructure.persistence.BaseRepositoryImpl;
import ch.post.it.evoting.votingserver.extendedauthentication.domain.model.extendedauthentication.ExtendedAuthentication;
import ch.post.it.evoting.votingserver.extendedauthentication.domain.model.extendedauthentication.ExtendedAuthenticationPK;
import ch.post.it.evoting.votingserver.extendedauthentication.domain.model.extendedauthentication.ExtendedAuthenticationRepository;

/**
 * Implementation of the repository with jpa
 */
@Stateless
public class ExtendedAuthenticationRepositoryImpl extends BaseRepositoryImpl<ExtendedAuthentication, String>
		implements ExtendedAuthenticationRepository {

	private static final Logger LOGGER = LoggerFactory.getLogger(ExtendedAuthenticationRepositoryImpl.class);

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Optional<ExtendedAuthentication> getForUpdate(String tenantId, String authId, String electionEventId) throws ApplicationException {
		ExtendedAuthenticationPK pk = new ExtendedAuthenticationPK(authId, tenantId, electionEventId);
		try {
			ExtendedAuthentication extendedAuthentication = entityManager.find(ExtendedAuthentication.class, pk, LockModeType.PESSIMISTIC_WRITE);

			return Optional.ofNullable(extendedAuthentication);

		} catch (PessimisticLockException | LockTimeoutException e) {
			String errMsg = String.format("Error trying to create a lock on the Extended Authentication entity with authId = %s .", authId);
			LOGGER.error(errMsg, e);
			throw new ApplicationException(errMsg, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Optional<ExtendedAuthentication> getForRead(String tenantId, String authId, String electionEventId) {
		ExtendedAuthenticationPK pk = new ExtendedAuthenticationPK(authId, tenantId, electionEventId);
		ExtendedAuthentication extendedAuthentication = entityManager.find(ExtendedAuthentication.class, pk);

		// To avoid accidental updates to the entity, it is marked as detached.
		if (extendedAuthentication != null) {
			entityManager.detach(extendedAuthentication);
		}

		return Optional.ofNullable(extendedAuthentication);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void delete(ExtendedAuthentication extendedAuthentication) {
		entityManager.remove(extendedAuthentication);
	}

	@Override
	public List<ExtendedAuthentication> findAllExceededExtendedAuthentication(final String tenantId, final String electionEventId,
			final Integer maxAllowedNumberOfAttempts) {
		// we have to return a list because we will have to match each result with a VC that is only
		// available in VM ctx
		final TypedQuery<ExtendedAuthentication> query = entityManager.createQuery("SELECT ea FROM ExtendedAuthentication ea "
						+ "WHERE ea.tenantId = :tenantId AND ea.electionEvent = :eeId AND ea.attempts > :maxAttempts " + "ORDER BY ea.electionEvent",
				ExtendedAuthentication.class);
		query.setParameter("tenantId", tenantId);
		query.setParameter("eeId", electionEventId);
		query.setParameter("maxAttempts", maxAllowedNumberOfAttempts);
		query.setLockMode(LockModeType.NONE);
		return query.getResultList();
	}
}
