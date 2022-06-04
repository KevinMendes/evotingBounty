/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.infrastructure.service;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

import org.springframework.stereotype.Service;

import ch.post.it.evoting.securedatamanager.services.domain.model.EntityRepository;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.SynchronizeStatus;
import ch.post.it.evoting.securedatamanager.services.infrastructure.JsonConstants;

/**
 * This class manages the status of a configuration entity. As we are working with entity in format json, the type is string.
 */
@Service
public class ConfigurationEntityStatusService {

	/**
	 * Updates the status of the given entity using the given repository.
	 *
	 * @param newStatus  the new state of the entity.
	 * @param id         the id of the entity to which to update the status.
	 * @param repository the specific entity repository to be used for updating the status.
	 * @return the content of the fields that were updated.
	 */
	public String update(final String newStatus, final String id, final EntityRepository repository) {
		final JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
		jsonObjectBuilder.add(JsonConstants.ID, id);
		jsonObjectBuilder.add(JsonConstants.STATUS, newStatus);
		jsonObjectBuilder.add(JsonConstants.SYNCHRONIZED, Boolean.FALSE.toString());
		return repository.update(jsonObjectBuilder.build().toString());
	}

	/**
	 * Updates the status of the given entity using the given repository.
	 *
	 * @param newStatus   the new state of the entity.
	 * @param id          the id of the entity to which to update the status.
	 * @param repository  the specific entity repository to be used for updating the status.
	 * @param syncDetails - details of the status of synchronization.
	 * @return the content of the fields that were updated.
	 */
	public String updateWithSynchronizedStatus(final String newStatus, final String id, final EntityRepository repository, final SynchronizeStatus syncDetails) {
		final JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
		jsonObjectBuilder.add(JsonConstants.ID, id);
		jsonObjectBuilder.add(JsonConstants.STATUS, newStatus);
		jsonObjectBuilder.add(JsonConstants.SYNCHRONIZED, syncDetails.getIsSynchronized().toString());
		jsonObjectBuilder.add(JsonConstants.DETAILS, syncDetails.getStatus());
		return repository.update(jsonObjectBuilder.build().toString());
	}

}
