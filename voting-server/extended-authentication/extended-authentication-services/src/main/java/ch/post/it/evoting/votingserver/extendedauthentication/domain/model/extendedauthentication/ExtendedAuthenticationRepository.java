/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.extendedauthentication.domain.model.extendedauthentication;

import java.util.List;
import java.util.Optional;

import javax.ejb.Local;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.domain.model.BaseRepository;

/**
 * Provides operations on the election event translation repository.
 */
@Local
public interface ExtendedAuthenticationRepository extends BaseRepository<ExtendedAuthentication, String> {

	/**
	 * Retrieves an Extended Authentication for later update (with locking).
	 *
	 * @param tenantId        The Tenant Id.
	 * @param authId          The Auth Id.
	 * @param electionEventId The Election Event Id.
	 * @return The retrieved Extended Authentication entity.
	 * @throws ApplicationException An exception raised if the locking was unsuccessful.
	 */
	Optional<ExtendedAuthentication> getForUpdate(String tenantId, String authId, String electionEventId) throws ApplicationException;

	/**
	 * Retrieves an Extended Authentication just for read (without locking and detached).
	 *
	 * @param tenantId        The Tenant Id.
	 * @param authId          The Auth Id.
	 * @param electionEventId The Election Event Id.
	 * @return The retrieved and detached Extended Authentication entity.
	 */
	Optional<ExtendedAuthentication> getForRead(String tenantId, String authId, String electionEventId);

	/**
	 * Deletes an Extended Authentication entity.
	 *
	 * @param extendedAuthentication The Extended Authentication entity to be deleted.
	 */
	void delete(ExtendedAuthentication extendedAuthentication);

	/**
	 * Find all Voting cards associated with failed extended authentications
	 *
	 * @param tenantId                   the Tenant identifier
	 * @param electionEventId            the Election Event identifier
	 * @param maxAllowedNumberOfAttempts the max number of allowed auth attempts
	 */
	List<ExtendedAuthentication> findAllExceededExtendedAuthentication(final String tenantId, final String electionEventId,
			final Integer maxAllowedNumberOfAttempts);
}
