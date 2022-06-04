/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.domain.model;

import java.io.Serializable;

import javax.ejb.Local;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.EntryPersistenceException;

/**
 * This interface declares the basic repository operations based on CRUD.
 *
 * @param <T>  the type of the entity to operate on/with.
 * @param <ID> the type of the identifier of the entity.
 */
@Local
public interface BaseRepository<T, ID extends Serializable> {

	/**
	 * Searches for a entity of type T in the repository identified by the given id.
	 *
	 * @param id The identifier of the entity T searched.
	 * @return the entity identified by id.
	 */
	T find(ID id);

	/**
	 * Saves the given entity of type T in the repository.
	 *
	 * @param entity The entity to be saved in the repository.
	 * @return the entity saved together with the generated id from the repository.
	 */
	T save(T entity) throws DuplicateEntryException;

	/**
	 * @param entity
	 * @return
	 * @throws EntryPersistenceException
	 */
	T update(T entity) throws EntryPersistenceException;
}
