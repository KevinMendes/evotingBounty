/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.infrastructure.persistence;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

import ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication.AuthenticationContent;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication.AuthenticationContentRepository;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;

/**
 * Authentication content repository decorator.
 */
@Decorator
public abstract class AuthenticationContentRepositoryDecorator implements AuthenticationContentRepository {

	@Inject
	@Delegate
	private AuthenticationContentRepository authenticationContentRepository;

	@Override
	public AuthenticationContent save(final AuthenticationContent entity) throws DuplicateEntryException {
		return authenticationContentRepository.save(entity);
	}
}
