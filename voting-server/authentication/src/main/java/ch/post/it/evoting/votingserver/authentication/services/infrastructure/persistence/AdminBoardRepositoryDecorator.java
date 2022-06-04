/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.infrastructure.persistence;

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

import ch.post.it.evoting.votingserver.authentication.services.domain.model.adminboard.AdminBoard;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.adminboard.AdminBoardRepository;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;

/**
 * Authentication certs repository decorator.
 */
@Decorator
public abstract class AdminBoardRepositoryDecorator implements AdminBoardRepository {

	@Inject
	@Delegate
	private AdminBoardRepository adminBoardRepository;

	@Override
	public AdminBoard save(final AdminBoard entity) throws DuplicateEntryException {
		return adminBoardRepository.save(entity);
	}
}
