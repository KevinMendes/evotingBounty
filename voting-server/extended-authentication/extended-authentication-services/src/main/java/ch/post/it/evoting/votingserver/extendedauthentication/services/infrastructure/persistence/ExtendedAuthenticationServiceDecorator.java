/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.extendedauthentication.services.infrastructure.persistence;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.EntryPersistenceException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.extendedauthentication.domain.model.EncryptedSVK;
import ch.post.it.evoting.votingserver.extendedauthentication.domain.model.extendedauthentication.AllowedAttemptsExceededException;
import ch.post.it.evoting.votingserver.extendedauthentication.domain.model.extendedauthentication.ExtendedAuthentication;

/**
 * Decorator for Extended Authentication Service.
 * {@link ExtendedAuthenticationService}
 */
@Decorator
public class ExtendedAuthenticationServiceDecorator implements ExtendedAuthenticationService {

	@Inject
	@Delegate
	private ExtendedAuthenticationService extendedAuthenticationService;

	/**
	 * {@inheritDoc}
	 *
	 * @throws GeneralCryptoLibException
	 */
	@Override
	public EncryptedSVK authenticate(String tenantId, String authId, String extraParam, String electionEventId)
			throws ResourceNotFoundException, AllowedAttemptsExceededException, AuthenticationException, ApplicationException,
			GeneralCryptoLibException {
		return extendedAuthenticationService.authenticate(tenantId, authId, extraParam, electionEventId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExtendedAuthentication renewExistingExtendedAuthentication(String tenantId, String oldAuthId, String newAuthId, String newSVK,
			String electionEventId) throws ApplicationException, ResourceNotFoundException {
		return extendedAuthenticationService.renewExistingExtendedAuthentication(tenantId, oldAuthId, newAuthId, newSVK, electionEventId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExtendedAuthentication retrieveExistingExtendedAuthenticationForUpdate(String tenantId, String authId, String electionEventId)
			throws ResourceNotFoundException, ApplicationException {
		return extendedAuthenticationService.retrieveExistingExtendedAuthenticationForUpdate(tenantId, authId, electionEventId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ExtendedAuthentication retrieveExistingExtendedAuthenticationForRead(String tenantId, String authId, String electionEventId)
			throws ResourceNotFoundException, ApplicationException {
		return extendedAuthenticationService.retrieveExistingExtendedAuthenticationForRead(tenantId, authId, electionEventId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean saveExtendedAuthenticationFromFile(Path extendedAuthenticationFilePath, String tenantId, String electionEventId,
			String adminBoardId) throws IOException {
		return extendedAuthenticationService
				.saveExtendedAuthenticationFromFile(extendedAuthenticationFilePath, tenantId, electionEventId, adminBoardId);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void updateExistingExtendedAuthentication(ExtendedAuthentication extendedAuthentication) throws EntryPersistenceException {
		extendedAuthenticationService.updateExistingExtendedAuthentication(extendedAuthentication);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void saveNewExtendedAuthentication(ExtendedAuthentication extendedAuthentication) throws DuplicateEntryException {
		extendedAuthenticationService.saveNewExtendedAuthentication(extendedAuthentication);
	}

	@Override
	public void findAndWriteVotingCardsWithFailedAuthentication(final String tenantId, final String electionEventId, final OutputStream stream)
			throws IOException {
		extendedAuthenticationService.findAndWriteVotingCardsWithFailedAuthentication(tenantId, electionEventId, stream);
	}
}
