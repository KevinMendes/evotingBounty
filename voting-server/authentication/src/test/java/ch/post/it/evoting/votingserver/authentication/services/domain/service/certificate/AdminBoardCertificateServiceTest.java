/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.service.certificate;

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
import ch.post.it.evoting.domain.election.model.certificate.CertificateEntity;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.adminboard.AdminBoard;
import ch.post.it.evoting.votingserver.authentication.services.domain.model.adminboard.AdminBoardRepository;
import ch.post.it.evoting.votingserver.commons.beans.authentication.AdminBoardCertificates;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.service.RemoteCertificateService;

/**
 * Test Class for the Authentication Content Service
 */
@RunWith(MockitoJUnitRunner.class)
public class AdminBoardCertificateServiceTest {

	public static final String ELECTION_EVENT_ID = "100";

	public static final String TENANT_ID = "100";
	@Rule
	public ExpectedException expectedException = ExpectedException.none();
	@Spy
	@InjectMocks
	@Inject
	private AdminBoardCertificateService adminBoardCertificateService;
	@Mock
	private Logger LOGGER;
	// The authentication content repository.
	@Mock
	private AdminBoardRepository adminBoardRepository;
	@Mock
	private RemoteCertificateService remoteCertificateService;

	@Before
	public void initMocks() {

		MockitoAnnotations.initMocks(this.getClass());

	}

	@Test
	public void getAdminBoardCertificates() throws ResourceNotFoundException, IOException, GeneralCryptoLibException {

		AdminBoard adminBoard = new AdminBoard();
		CertificateEntity e = new CertificateEntity();
		e.setCertificateContent("CERT");

		when(adminBoardRepository.findByTenantIdElectionEventId(anyString(), anyString())).thenReturn(adminBoard);

		when(remoteCertificateService.getAdminBoardCertificate(anyString())).thenReturn(e);
		when(remoteCertificateService.getTenantCACertificate(anyString())).thenReturn(e);

		final AdminBoardCertificates adminBoardCertificates = adminBoardCertificateService.getAdminBoardAndTenantCA(TENANT_ID, ELECTION_EVENT_ID);
		assertNotNull(adminBoardCertificates);
	}

	@Test
	public void getAdminBoardCertificatesNotFoundAdminBoard() throws ResourceNotFoundException {

		expectedException.expect(ResourceNotFoundException.class);
		when(adminBoardRepository.findByTenantIdElectionEventId(anyString(), anyString())).thenThrow(new ResourceNotFoundException("exception"));

		adminBoardCertificateService.getAdminBoardAndTenantCA(TENANT_ID, ELECTION_EVENT_ID);
	}

}
