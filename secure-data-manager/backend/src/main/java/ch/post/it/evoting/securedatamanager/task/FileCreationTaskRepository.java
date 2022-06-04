/*
 * (c) Copyright 2022. Swiss Post Ltd
 */
package ch.post.it.evoting.securedatamanager.task;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Repository;

import ch.post.it.evoting.securedatamanager.services.infrastructure.AbstractEntityRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.DatabaseManager;

@Repository
public class FileCreationTaskRepository extends AbstractEntityRepository {

	public FileCreationTaskRepository(final DatabaseManager manager) {
		super(manager);
	}

	@PostConstruct
	@Override
	public void initialize() {
		super.initialize();
	}

	@Override
	protected String entityName() {
		return FileCreationTask.class.getSimpleName();
	}
}
