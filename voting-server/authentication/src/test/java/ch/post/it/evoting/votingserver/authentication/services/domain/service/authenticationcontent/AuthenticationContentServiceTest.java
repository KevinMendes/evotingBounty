/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.service.authenticationcontent;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;

import javax.inject.Inject;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.slf4j.Logger;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication.AuthenticationContent;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication.AuthenticationContentRepository;
import ch.post.it.evoting.votingserver.authentication.services.domain.utils.AuthenticationUtils;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;

/**
 * Test Class for the Authentication Content Service
 */
@RunWith(MockitoJUnitRunner.class)
public class AuthenticationContentServiceTest {

	public static final String ELECTION_EVENT_ID = "100";

	public static final String TENANT_ID = "100";

	public static final String JSON = "{}";
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	@Spy
	@InjectMocks
	@Inject
	private AuthenticationContentService authenticationContentService;
	@Mock
	private Logger LOGGER;
	@Mock
	private AuthenticationContentRepository authenticationContentRepository;

	@Before
	public void initMocks() {

		MockitoAnnotations.initMocks(this.getClass());

	}

	@Test
	public void getAuthenticationContent() throws ResourceNotFoundException, IOException, GeneralCryptoLibException {

		final AuthenticationContent AuthenticationContent = AuthenticationUtils.generateAuthenticationContentEntity();
		when(authenticationContentRepository.findByTenantIdElectionEventId(anyString(), anyString())).thenReturn(AuthenticationContent);
		final ch.post.it.evoting.domain.election.model.authentication.AuthenticationContent authenticationContent = authenticationContentService
				.getAuthenticationContent(TENANT_ID, ELECTION_EVENT_ID);
		assertNotNull(authenticationContent);

	}

	@Test
	public void getAuthenticationContentNotFound() throws ResourceNotFoundException {

		expectedException.expect(ResourceNotFoundException.class);
		when(authenticationContentRepository.findByTenantIdElectionEventId(anyString(), anyString()))
				.thenThrow(new ResourceNotFoundException("exception"));
		authenticationContentService.getAuthenticationContent(TENANT_ID, ELECTION_EVENT_ID);

	}

}
