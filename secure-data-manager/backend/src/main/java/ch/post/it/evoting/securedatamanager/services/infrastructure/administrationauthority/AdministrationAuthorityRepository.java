/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.infrastructure.administrationauthority;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import ch.post.it.evoting.securedatamanager.services.domain.model.administrationauthority.AdministrationAuthority;
import ch.post.it.evoting.securedatamanager.services.infrastructure.AbstractEntityRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.DatabaseManager;

/**
 * Implementation of operations with election event.
 */
@Repository
public class AdministrationAuthorityRepository extends AbstractEntityRepository {

	/**
	 * Constructor.
	 *
	 * @param databaseManager
	 */
	@Autowired
	public AdministrationAuthorityRepository(final DatabaseManager databaseManager) {
		super(databaseManager);
	}

	@PostConstruct
	@Override
	public void initialize() {
		super.initialize();
	}

	@Override
	protected String entityName() {
		return AdministrationAuthority.class.getSimpleName();
	}
}
