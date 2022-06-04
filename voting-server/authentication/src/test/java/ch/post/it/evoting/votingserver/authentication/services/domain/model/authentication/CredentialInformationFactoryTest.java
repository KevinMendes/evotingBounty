/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.model.authentication;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;

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
import ch.post.it.evoting.domain.election.model.certificate.CertificateEntity;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.adminboard.AdminBoard;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.adminboard.AdminBoardRepository;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.material.CredentialRepository;
import ch.post.it.evoting.votingserver.commons.beans.authentication.Credential;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.crypto.SignatureForObjectService;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.service.RemoteCertificateService;

@RunWith(MockitoJUnitRunner.class)
public class CredentialInformationFactoryTest {

	public static final String ELECTION_EVENT_ID = "100";
	public static final String TENANT_ID = "100";
	public static final String ADMIN_BOARD_ID = "100";
	public static final String CREDENTIAL_ID = "100";
	public static final int RANDOM_VALUE_LENGTH = 16;
	public static final byte[] BYTES = "signature".getBytes(StandardCharsets.UTF_8);
	private static final String JSON = "{}";

	@InjectMocks
	private final CredentialInformationFactory credentialInformationFactory = new CredentialInformationFactory();

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Mock
	private CredentialRepository credentialRepository;

	@Mock
	private SignatureForObjectService signatureService;

	@Mock
	private AuthenticationCertsRepository authenticationCertsRepository;

	@Mock
	private AdminBoardRepository adminBoardRepository;

	@Mock
	private RemoteCertificateService remoteCertificateService;

	@Mock
	private AuthenticationCerts authenticationCerts;

	@Mock
	private CertificateEntity certificateEntity;

	@Mock
	private AdminBoard adminBoard;

	@Mock
	private Credential credential;

	@Before
	public void init() {
		MockitoAnnotations.initMocks(this.getClass());
	}

	@Test
	public void buildCertificates() throws ResourceNotFoundException {

		mockCertificatesInfo();
		assertNotNull(credentialInformationFactory.buildAuthenticationCertificates(TENANT_ID, ELECTION_EVENT_ID));
	}

	@Test
	public void buildCertificatesResourceNotFound() throws ResourceNotFoundException {

		expectedException.expect(ResourceNotFoundException.class);
		when(authenticationCertsRepository.findByTenantIdElectionEventId(anyString(), anyString()))
				.thenThrow(new ResourceNotFoundException("exception"));
		credentialInformationFactory.buildAuthenticationCertificates(TENANT_ID, ELECTION_EVENT_ID);
	}

	@Test
	public void buildCredentialInformation() throws CryptographicOperationException, ResourceNotFoundException {

		when(credentialRepository.findByTenantIdElectionEventIdCredentialId(anyString(), anyString(), anyString())).thenReturn(credential);
		when(signatureService.sign(anyString(), anyString(), anyString(), anyString(), any())).thenReturn(BYTES);
		mockCertificatesInfo();
		assertNotNull(credentialInformationFactory.buildCredentialInformation(TENANT_ID, ELECTION_EVENT_ID, CREDENTIAL_ID, RANDOM_VALUE_LENGTH));

	}

	@Test
	public void buildCredentialInformationNotFoundCredentials() throws CryptographicOperationException, ResourceNotFoundException {

		expectedException.expect(ResourceNotFoundException.class);
		when(credentialRepository.findByTenantIdElectionEventIdCredentialId(anyString(), anyString(), anyString()))
				.thenThrow(new ResourceNotFoundException("exception"));

		credentialInformationFactory.buildCredentialInformation(TENANT_ID, ELECTION_EVENT_ID, CREDENTIAL_ID, RANDOM_VALUE_LENGTH);

	}

	@Test
	public void buildCredentialInformationCryptoLibException() throws CryptographicOperationException, ResourceNotFoundException {

		expectedException.expect(CryptographicOperationException.class);
		when(credentialRepository.findByTenantIdElectionEventIdCredentialId(anyString(), anyString(), anyString())).thenReturn(credential);
		when(signatureService.sign(anyString(), anyString(), anyString(), anyString(), any()))
				.thenThrow(new CryptographicOperationException("exception"));
		mockCertificatesInfo();
		assertNotNull(credentialInformationFactory.buildCredentialInformation(TENANT_ID, ELECTION_EVENT_ID, CREDENTIAL_ID, RANDOM_VALUE_LENGTH));

	}

	@Test
	public void buildCredentialInformationCrypoGraphicOperationException() throws CryptographicOperationException, ResourceNotFoundException {

		expectedException.expect(CryptographicOperationException.class);
		when(credentialRepository.findByTenantIdElectionEventIdCredentialId(anyString(), anyString(), anyString())).thenReturn(credential);
		when(signatureService.sign(anyString(), anyString(), anyString(), anyString(), any()))
				.thenThrow(new CryptographicOperationException("exception"));
		mockCertificatesInfo();
		assertNotNull(credentialInformationFactory.buildCredentialInformation(TENANT_ID, ELECTION_EVENT_ID, CREDENTIAL_ID, RANDOM_VALUE_LENGTH));

	}

	private void mockCertificatesInfo() throws ResourceNotFoundException {
		when(authenticationCertsRepository.findByTenantIdElectionEventId(anyString(), anyString())).thenReturn(authenticationCerts);
		when(adminBoardRepository.findByTenantIdElectionEventId(anyString(), anyString())).thenReturn(adminBoard);
		when(adminBoard.getAdminBoardId()).thenReturn(ADMIN_BOARD_ID);
		when(authenticationCerts.getJson()).thenReturn(JSON);
		when(remoteCertificateService.getAdminBoardCertificate(anyString())).thenReturn(certificateEntity);
		when(remoteCertificateService.getTenantCACertificate(anyString())).thenReturn(certificateEntity);
		when(certificateEntity.getCertificateContent()).thenReturn("certificateContent");
	}

}
