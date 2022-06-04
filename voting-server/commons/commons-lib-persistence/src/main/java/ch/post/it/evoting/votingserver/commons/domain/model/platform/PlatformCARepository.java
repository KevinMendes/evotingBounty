/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.domain.model.platform;

import javax.ejb.Local;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.domain.model.BaseRepository;

/**
 * Repository for handling the platform root ca in each context
 */
@Local
public interface PlatformCARepository extends BaseRepository<PlatformCAEntity, Long> {

	/**
	 * Returns the root ca certificate
	 *
	 * @return
	 * @throws ResourceNotFoundException
	 */
	PlatformCAEntity getRootCACertificate() throws ResourceNotFoundException;
}
