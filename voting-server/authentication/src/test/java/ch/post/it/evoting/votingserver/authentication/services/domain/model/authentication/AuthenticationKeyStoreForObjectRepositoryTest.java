/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationKeyStoreForObjectRepositoryTest {

	public static final String ELECTION_EVENT_ID = "100";
	public static final String TENANT_ID = "100";
	public static final String OBJECT_ID = "";
	private static final String JSON = "{\"authenticationTokenSignerKeystore\":\"asdfasdf\"}";
	@InjectMocks
	private final AuthenticationKeyStoreForObjectRepository sut = new AuthenticationKeyStoreForObjectRepository();
	@Mock
	private AuthenticationContentRepository authenticationContentRepository;
	@Mock
	private AuthenticationContent authenticationContentMock;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this.getClass());
	}

	@Test
	public void getKeystore() throws ResourceNotFoundException {

		when(authenticationContentRepository.findByTenantIdElectionEventId(anyString(), anyString())).thenReturn(authenticationContentMock);
		when(authenticationContentMock.getJson()).thenReturn(JSON);
		sut.getJsonByTenantEEIDObjectId(TENANT_ID, ELECTION_EVENT_ID, OBJECT_ID);

	}
}
