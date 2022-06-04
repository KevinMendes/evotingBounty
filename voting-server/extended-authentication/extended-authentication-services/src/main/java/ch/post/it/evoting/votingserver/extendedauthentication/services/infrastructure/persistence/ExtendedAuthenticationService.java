/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.extendedauthentication.services.infrastructure.persistence;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.EntryPersistenceException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.extendedauthentication.domain.model.EncryptedSVK;
import ch.post.it.evoting.votingserver.extendedauthentication.domain.model.extendedauthentication.AllowedAttemptsExceededException;
import ch.post.it.evoting.votingserver.extendedauthentication.domain.model.extendedauthentication.ExtendedAuthentication;

/**
 * A Extended Authentication Service Interface offering several methods to work with Extended
 * Authentication objects.
 */
public interface ExtendedAuthenticationService {

	/**
	 * @param tenantId        Tenant Id.
	 * @param authId          Auth Id.
	 * @param extraParam      extra parameters
	 * @param electionEventId Election Event Id.
	 * @return An Encrypted Start Voting Key object.
	 * @throws ResourceNotFoundException
	 * @throws AllowedAttemptsExceededException
	 * @throws AuthenticationException
	 * @throws ApplicationException
	 * @throws GeneralCryptoLibException
	 */
	EncryptedSVK authenticate(final String tenantId, final String authId, final String extraParam, final String electionEventId)
			throws ResourceNotFoundException, AllowedAttemptsExceededException, AuthenticationException, ApplicationException,
			GeneralCryptoLibException;

	/**
	 * This method renews (delete + save) an existing Extended-Authentication entity with the info
	 * passed as parameter.
	 *
	 * @param tenantId        Tenant Id.
	 * @param oldAuthId       The old AuthId to be renewed.
	 * @param newAuthId       The new AuthId.
	 * @param newSVK          The new Start Voting Key.
	 * @param electionEventId Election Event Id.
	 * @return The renewed Extended-Authentication entity.
	 * @throws ApplicationException
	 * @throws ResourceNotFoundException
	 */
	ExtendedAuthentication renewExistingExtendedAuthentication(final String tenantId, final String oldAuthId, final String newAuthId,
			final String newSVK, final String electionEventId) throws ApplicationException, ResourceNotFoundException;

	/**
	 * This method retrieves an existing Extended Authentication entity from repository for update
	 * operations (with locking).
	 *
	 * @param tenantId        Tenant Id.
	 * @param authId          Auth Id.
	 * @param electionEventId Election Event Id.
	 * @return The renewed Extended-Authentication entity.
	 * @throws ResourceNotFoundException
	 * @throws ApplicationException
	 */
	ExtendedAuthentication retrieveExistingExtendedAuthenticationForUpdate(final String tenantId, final String authId, final String electionEventId)
			throws ResourceNotFoundException, ApplicationException;

	/**
	 * This method retrieves an existing Extended Authentication entity from repository just for read
	 * operations (without locking).
	 *
	 * @param tenantId        Tenant Id.
	 * @param authId          Auth Id.
	 * @param electionEventId Election Event Id.
	 * @return The renewed Extended-Authentication entity.
	 * @throws ResourceNotFoundException
	 * @throws ApplicationException
	 */
	ExtendedAuthentication retrieveExistingExtendedAuthenticationForRead(final String tenantId, final String authId, final String electionEventId)
			throws ResourceNotFoundException, ApplicationException;

	/**
	 * This method saves a new Extended-Authentication entity from the file passed as a parameter.
	 *
	 * @param extendedAuthenticationFilePath File containing the Extended Authentication data to be
	 *                                       saved.
	 * @param tenantId                       Tenant Id.
	 * @param electionEventId                Election Event Id.
	 * @param adminBoardId                   Admin Board Id.
	 * @return True if the signature of the file is valid, false otherwise.
	 * @throws IOException If there is an error when dealing with the file containing the EAs.
	 */
	boolean saveExtendedAuthenticationFromFile(final Path extendedAuthenticationFilePath, final String tenantId, final String electionEventId,
			final String adminBoardId) throws IOException;

	/**
	 * Updates an existing Extended Authentication object.
	 *
	 * @param extendedAuthentication The Extended Authentication to be saved.
	 * @throws EntryPersistenceException
	 */
	void updateExistingExtendedAuthentication(ExtendedAuthentication extendedAuthentication) throws EntryPersistenceException;

	/**
	 * Saves a new Extended Authentication object.
	 *
	 * @param extendedAuthentication The Extended Authentication to be saved.
	 * @throws DuplicateEntryException
	 */

	void saveNewExtendedAuthentication(ExtendedAuthentication extendedAuthentication) throws DuplicateEntryException;

	/**
	 * Find (and write to stream) all voting cards associted with the credentials that have exceeded
	 * the max authentication attempts
	 *
	 * @param tenantId        the tenant identifier
	 * @param electionEventId the election event identifier
	 * @param stream          the output stream
	 */
	void findAndWriteVotingCardsWithFailedAuthentication(final String tenantId, final String electionEventId, final OutputStream stream)
			throws IOException;
}
