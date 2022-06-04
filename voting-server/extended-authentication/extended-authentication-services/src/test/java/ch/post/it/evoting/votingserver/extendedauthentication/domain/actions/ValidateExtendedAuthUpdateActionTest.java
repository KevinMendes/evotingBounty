/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.extendedauthentication.domain.actions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import ch.post.it.evoting.domain.election.model.authentication.AuthenticationToken;
import ch.post.it.evoting.domain.election.model.authentication.ExtendedAuthenticationUpdate;
import ch.post.it.evoting.domain.election.model.authentication.ExtendedAuthenticationUpdateRequest;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.extendedauthentication.domain.model.extendedauthentication.validation.ExtendedAuthValidationService;

@RunWith(MockitoJUnitRunner.class)
public class ValidateExtendedAuthUpdateActionTest {

	@InjectMocks
	private final ValidateExtendedAuthUpdateAction validateExtendedAuthUpdateAction = new ValidateExtendedAuthUpdateAction();
	@Mock
	private ExtendedAuthValidationService mockExtendedAuthValidationService;

	@BeforeClass
	public static void setup() {
		MockitoAnnotations.openMocks(ValidateExtendedAuthUpdateActionTest.class);
	}

	@Test
	public void validateHappyPath() throws ApplicationException, ResourceNotFoundException {

		final ExtendedAuthenticationUpdate mockExtendedAuthenticationUpdate = new ExtendedAuthenticationUpdate();

		final ExtendedAuthenticationUpdateRequest mockExtendedAuthenticationUpdateRequest = new ExtendedAuthenticationUpdateRequest();

		when(mockExtendedAuthValidationService.verifySignature(any(), any())).thenReturn(mockExtendedAuthenticationUpdate);

		when(mockExtendedAuthValidationService.validateToken(anyString(), anyString(), any())).thenReturn(true);
		Mockito.doNothing().when(mockExtendedAuthValidationService)
				.validateTokenWithAuthIdAndCredentialId(any(), any(), anyString(), anyString());

		assertThat(validateExtendedAuthUpdateAction.validate("", "", new AuthenticationToken(), mockExtendedAuthenticationUpdateRequest),
				instanceOf(ExtendedAuthenticationUpdate.class));

	}

}
