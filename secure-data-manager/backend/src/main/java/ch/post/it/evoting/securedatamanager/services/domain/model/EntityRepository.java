/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.domain.model;

import java.util.Map;

import ch.post.it.evoting.securedatamanager.services.application.exception.DatabaseException;
import ch.post.it.evoting.securedatamanager.services.infrastructure.JsonConstants;

/**
 * EntiryRepository provides basic operations like CRUD on entities stored in the repository.
 * <p>
 * Entities stored in the repository are JSON structures. To retrieve entities from the repository it is necessary to specify a criteria and call a
 * suitable {@code list} or {@code find methods}. Criteria is a map where the keys are attribute paths and the values are corresponding attribute
 * values. An entity is matching given criteria if it has all the attributes from the criteria with the specified values. For example
 *
 * <pre>
 * <code>
 * stringAttribute="Cool"
 * electionEvent.intArrribute=2
 * </code>
 * </pre>
 * <p>
 * is a criteria which can be applied to entities like
 *
 * <pre>
 * <code>
 * {
 *     "id": 1,
 *     "stringAttribute": "Cool",
 *     "electionEvent": {
 *         "intAttribute": 2,
 *         ...
 *     }
 * }
 * </code>
 * </pre>
 * <p>
 * The methods which return a collection of entities return JSOn of the following form:
 *
 * <pre>
 * <code>
 * {
 *     "result": [
 *         <entity in JSON format>,
 *         <entity in JSON format>,
 *         ...
 *     }
 * }
 * </code>
 * </pre>
 * <p>
 * Implementation must be thread-safe.
 */
public interface EntityRepository {

	/**
	 * Deletes the entities matching the specified criteria.
	 *
	 * @param id the identifier
	 * @throws DatabaseException failed to delete entity.
	 */
	void delete(Map<String, Object> criteria);

	/**
	 * Deletes the entity with a given identifier. This is a shortcut for {@code delete(Collections.singletonMap("id", id))}.
	 *
	 * @param id the identifier
	 * @throws DatabaseException failed to delete entity.
	 */
	void delete(String id);

	/**
	 * Finds an entity matching the specified criteria.
	 *
	 * @param criteria the criteria
	 * @return the entity in JSON format or {@link JsonConstants#EMPTY_OBJECT} if the entity does not exist
	 * @throws DatabaseException failed to find the entity
	 */
	String find(Map<String, Object> criteria);

	/**
	 * Finds the entity by given identifier. This is a shortcut for {@code find(Collections.singletonMap("id", id))}.
	 *
	 * @param id the identifier
	 * @return the entity in JSON format or {@link JsonConstants#EMPTY_OBJECT} if the entity does not exist
	 * @throws DatabaseException failed to find the entity.
	 */
	String find(String id);

	/**
	 * Lists all entities. This is a shortcut for {@code list(Collections.emptyMap())}.
	 *
	 * @return the entities in JSON format
	 * @throws DatabaseException failed to list entities.
	 */
	String list();

	/**
	 * Lists the entities matching a given criteria.
	 *
	 * @param criteria the criteria
	 * @return the entities in JSON format
	 * @throws DatabaseException failed to list the entities.
	 */
	String list(Map<String, Object> criteria);

	/**
	 * Saves a given entity.
	 *
	 * @param json the entity in JSON format
	 * @return the actually saved JSON
	 * @throws DatabaseException failed to save the entity.
	 */
	String save(String json);

	/**
	 * Updates a given entity in the repository.
	 *
	 * @param json the entity in JSON format
	 * @return the JSON object with updated fields
	 * @throws DatabaseException failed to update the entity.
	 */
	String update(String json);
}
