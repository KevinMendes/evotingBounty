/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import javax.json.JsonObject;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import ch.post.it.evoting.domain.election.model.authentication.AuthenticationContent;
import ch.post.it.evoting.votingserver.authentication.services.domain.service.authenticationcontent.AuthenticationContentService;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.util.JsonUtils;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationKeystoreRepositoryTest {

	public static final String ELECTION_EVENT_ID = "100";
	public static final String TENANT_ID = "100";

	@InjectMocks
	private final AuthenticationKeystoreRepository authenticationKeystoreRepository = new AuthenticationKeystoreRepository();

	@Mock
	private AuthenticationContentService authenticationContentService;

	@Mock
	private AuthenticationContent authenticationContent;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this.getClass());
	}

	@Test
	public void getAuthenticationKeystore() throws ResourceNotFoundException {

		final JsonObject jsonObject = JsonUtils.getJsonObject("{}");
		when(authenticationContentService.getAuthenticationContent(anyString(), anyString())).thenReturn(authenticationContent);
		when(authenticationContent.getKeystore()).thenReturn(jsonObject);
		final String jsonByTenantEEID = authenticationKeystoreRepository.getJsonByTenantEEID(TENANT_ID, ELECTION_EVENT_ID);
		Assert.assertNotNull(jsonByTenantEEID);
	}
}
