/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.commons.infrastructure.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.io.Serializable;

import javax.persistence.EntityManager;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.domain.model.BaseRepository;

/**
 * Test case for the class {@link BaseRepositoryImpl}. It is declared as abstract because the junit classes which extend it will also launch its
 * defined junits, otherwise these unit tests are not launched.
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class BaseRepositoryImplTest<T, ID extends Serializable> {

	private final T objectMock;
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	@Mock
	protected EntityManager entityManagerMock;
	protected Class<T> persistentClassMock;
	@InjectMocks
	protected BaseRepository<T, Serializable> baseRepository;
	private ID idMock;
	private ID id;
	private T object;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public BaseRepositoryImplTest(final Class<T> entityClass, final Class<?> repositoryClass) throws InstantiationException, IllegalAccessException {
		this.persistentClassMock = entityClass;
		objectMock = entityClass.newInstance();
		baseRepository = (BaseRepository) repositoryClass.newInstance();
	}

	@Test
	public void findNull() {
		id = null;
		when(entityManagerMock.find(persistentClassMock, idMock)).thenReturn(null);

		assertNull(baseRepository.find(id));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void find() {
		idMock = (ID) new Integer(1);
		id = idMock;
		when(entityManagerMock.find(persistentClassMock, idMock)).thenReturn(objectMock);

		T result = baseRepository.find(id);
		assertNotNull(result);
		assertEquals(objectMock, result);
	}

	@Test
	public void saveNull() throws DuplicateEntryException {
		object = null;

		assertNull(baseRepository.save(object));
	}

	@Test
	public void save() throws DuplicateEntryException {
		doNothing().when(entityManagerMock).persist(objectMock);
		object = objectMock;

		assertNotNull(baseRepository.save(object));
	}
}
