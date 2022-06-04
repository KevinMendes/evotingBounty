/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.infrastructure.persistence;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

import ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication.AuthenticationCerts;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication.AuthenticationCertsRepository;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;

/**
 * Authentication certs repository decorator.
 */
@Decorator
public abstract class AuthenticationCertsRepositoryDecorator implements AuthenticationCertsRepository {

	@Inject
	@Delegate
	private AuthenticationCertsRepository authenticationCertsRepository;

	@Override
	public AuthenticationCerts save(final AuthenticationCerts entity) throws DuplicateEntryException {
		return authenticationCertsRepository.save(entity);
	}
}
