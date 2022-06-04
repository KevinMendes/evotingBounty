/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;

import ch.post.it.evoting.cryptolib.certificates.utils.CryptographicOperationException;
import ch.post.it.evoting.domain.election.validation.ValidationResult;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.information.VoterInformation;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.information.VoterInformationRepository;
import ch.post.it.evoting.votingserver.authentication.services.domain.service.exception.AuthenticationTokenGenerationException;
import ch.post.it.evoting.votingserver.authentication.services.domain.service.exception.AuthenticationTokenSigningException;
import ch.post.it.evoting.votingserver.authentication.services.infrastructure.remote.ValidationRepositoryImpl;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.crypto.SignatureForObjectService;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationTokenFactoryTest {

	public static final String CREDENTIAL_ID = "100";

	public static final String ELECTION_EVENT_ID = "100";

	public static final String TENANT_ID = "100";
	private static final byte[] signatureBytes = "signature".getBytes(StandardCharsets.UTF_8);
	@InjectMocks
	private final AuthenticationTokenFactoryImpl factory = new AuthenticationTokenFactoryImpl();
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	@Mock
	private VoterInformationRepository voterInformationRepositoryMock;
	@Mock
	private ValidationRepositoryImpl validationRepository;
	@Mock
	private VoterInformation voterInformationMock;
	@Mock
	private SignatureForObjectService signatureService;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this.getClass());

	}

	@Test
	public void testAuhTokenCreation() throws AuthenticationTokenGenerationException, AuthenticationTokenSigningException, ResourceNotFoundException,
			CryptographicOperationException {

		when(voterInformationRepositoryMock.findByTenantIdElectionEventIdCredentialId(anyString(), anyString(), anyString()))
				.thenReturn(voterInformationMock);
		when(validationRepository.validateElectionInDates(anyString(), anyString(), any())).thenReturn(new ValidationResult(true));
		when(signatureService.sign(anyString(), anyString(), anyString(), anyString(), any())).thenReturn(signatureBytes);

		final AuthenticationTokenMessage authenticationTokenMessage = factory.buildAuthenticationToken(TENANT_ID, ELECTION_EVENT_ID, CREDENTIAL_ID);
		Assert.assertNotNull(authenticationTokenMessage);
	}

	@Test
	public void testAuhTokenCreationCryptoLibException()
			throws AuthenticationTokenGenerationException, AuthenticationTokenSigningException, ResourceNotFoundException,
			CryptographicOperationException {

		expectedException.expect(AuthenticationTokenSigningException.class);
		when(voterInformationRepositoryMock.findByTenantIdElectionEventIdCredentialId(anyString(), anyString(), anyString()))
				.thenReturn(voterInformationMock);
		when(validationRepository.validateElectionInDates(anyString(), anyString(), any())).thenReturn(new ValidationResult(true));
		when(signatureService.sign(anyString(), anyString(), anyString(), anyString(), any()))
				.thenThrow(new CryptographicOperationException("exception"));

		factory.buildAuthenticationToken(TENANT_ID, ELECTION_EVENT_ID, CREDENTIAL_ID);

	}

	@Test
	public void testAuhTokenCreationCryptoGraphicOperationException()
			throws AuthenticationTokenGenerationException, AuthenticationTokenSigningException, ResourceNotFoundException,
			CryptographicOperationException {

		expectedException.expect(AuthenticationTokenSigningException.class);
		when(voterInformationRepositoryMock.findByTenantIdElectionEventIdCredentialId(anyString(), anyString(), anyString()))
				.thenReturn(voterInformationMock);
		when(validationRepository.validateElectionInDates(anyString(), anyString(), any())).thenReturn(new ValidationResult(true));
		when(signatureService.sign(anyString(), anyString(), anyString(), anyString(), any()))
				.thenThrow(new CryptographicOperationException("exception"));

		factory.buildAuthenticationToken(TENANT_ID, ELECTION_EVENT_ID, CREDENTIAL_ID);

	}

}
