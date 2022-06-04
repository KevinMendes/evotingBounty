/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.infrastructure.persistence;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;

import javax.ejb.EJBTransactionRolledbackException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;

import org.hibernate.exception.ConstraintViolationException;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.EntryPersistenceException;
import ch.post.it.evoting.votingserver.commons.domain.model.BaseRepository;

/**
 * Implements the basic operations on a corresponding database repository.
 *
 * @param <T>  the type of the entity to operate on/with.
 * @param <ID> the type of the identifier of the entity.
 */
public abstract class BaseRepositoryImpl<T, ID extends Serializable> implements BaseRepository<T, ID> {

	// The parameterized type.
	private final Class<T> persistentClass;
	/**
	 * The entity manager.
	 */
	@PersistenceContext(name = "persistenceUnitJdbc")
	protected EntityManager entityManager;

	/**
	 * Determines and sets the class of the parameterized type.
	 */
	@SuppressWarnings("unchecked")
	public BaseRepositoryImpl() {
		final ParameterizedType type = (ParameterizedType) getClass().getGenericSuperclass();
		persistentClass = (Class<T>) type.getActualTypeArguments()[0];
	}

	/**
	 * Searches in the database repository for an entity T identified with the given id.
	 *
	 * @see BaseRepository#find(java.io.Serializable)
	 */
	@Override
	public T find(final ID id) {
		return entityManager.find(persistentClass, id);
	}

	/**
	 * Saves the given entity T in the corresponding database repository.
	 *
	 * @see BaseRepository#save(Object)
	 */
	@Override
	public T save(final T entity) throws DuplicateEntryException {
		try {
			entityManager.persist(entity);
			entityManager.flush();
		} catch (EJBTransactionRolledbackException | PersistenceException e) {
			final Throwable cause = e.getCause();
			if (cause instanceof ConstraintViolationException) {
				throw new DuplicateEntryException("Duplicate entry found for entity: " + entity.toString(), e);
			}
			throw e;
		}
		return entity;
	}

	/**
	 * Saves or updates the given entity T in the corresponding database repository.
	 *
	 * @see BaseRepository#save(Object)
	 */
	@Override
	public T update(final T entity) throws EntryPersistenceException {
		try {
			entityManager.merge(entity);
		} catch (EJBTransactionRolledbackException | PersistenceException e) {
			throw new EntryPersistenceException("Error persisting or updating the entity", e);
		}
		return entity;
	}
}
