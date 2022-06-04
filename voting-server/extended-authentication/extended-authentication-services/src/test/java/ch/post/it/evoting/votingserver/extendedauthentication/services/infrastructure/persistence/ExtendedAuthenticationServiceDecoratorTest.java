/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.extendedauthentication.services.infrastructure.persistence;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.EntryPersistenceException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.extendedauthentication.domain.model.EncryptedSVK;
import ch.post.it.evoting.votingserver.extendedauthentication.domain.model.extendedauthentication.AllowedAttemptsExceededException;
import ch.post.it.evoting.votingserver.extendedauthentication.domain.model.extendedauthentication.ExtendedAuthentication;

@RunWith(MockitoJUnitRunner.class)
public class ExtendedAuthenticationServiceDecoratorTest {

	private static final String TENANT_ID = "tenantId";
	private static final String ELECTION_EVENT_ID = "electionEventId";
	private static final String AUTH_ID = "authId";
	private static final String EXTRA_PARAM = "extraParam";

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@InjectMocks
	private ExtendedAuthenticationServiceDecorator extendedAuthenticationServiceDecorator;

	@Mock
	private ExtendedAuthenticationService extendedAuthenticationService;

	@Test
	public void testAuthenticateFails()
			throws ApplicationException, ResourceNotFoundException, AuthenticationException, AllowedAttemptsExceededException,
			GeneralCryptoLibException {

		expectedException.expect(ApplicationException.class);

		when(extendedAuthenticationService.authenticate(TENANT_ID, AUTH_ID, "extraParam", ELECTION_EVENT_ID))
				.thenThrow(new ApplicationException("exception"));

		extendedAuthenticationServiceDecorator.authenticate(TENANT_ID, AUTH_ID, "extraParam", ELECTION_EVENT_ID);
	}

	@Test
	public void testSaveExtendedAuthenticationFromFileSuccess() throws IOException {

		final Path tempFilePath = Files.createTempFile("testSecureLogWhenEntryPersistenceException.csv", null);
		FileUtils.writeStringToFile(tempFilePath.toFile(), "1,2,4,5,6" + System.lineSeparator() + "1,2,3,4,5");

		extendedAuthenticationServiceDecorator.saveExtendedAuthenticationFromFile(tempFilePath, TENANT_ID, ELECTION_EVENT_ID, "adminBoardId");
	}

	@Test(expected = IOException.class)
	public void testSaveExtendedAuthenticationFromFileFailsIO() throws IOException {

		final Path tempFilePath = Files.createTempFile("testSecureLogWhenEntryPersistenceException.csv", null);
		FileUtils.writeStringToFile(tempFilePath.toFile(), "1,2,4,5,6" + System.lineSeparator() + "1,2,3,4,5");

		doThrow(IOException.class).when(extendedAuthenticationService)
				.saveExtendedAuthenticationFromFile(tempFilePath, TENANT_ID, ELECTION_EVENT_ID, "adminBoardId");

		extendedAuthenticationServiceDecorator.saveExtendedAuthenticationFromFile(tempFilePath, TENANT_ID, ELECTION_EVENT_ID, "adminBoardId");
	}

	@Test
	public void testRenewExistingExtendedAuthenticationSuccess() throws ResourceNotFoundException, ApplicationException {

		final ExtendedAuthentication extendedAuthentication = new ExtendedAuthentication();
		extendedAuthentication.setCredentialId("credentialId");
		extendedAuthentication.setElectionEvent(ELECTION_EVENT_ID);

		when(extendedAuthenticationService.renewExistingExtendedAuthentication(TENANT_ID, "oldAuthId", "newAuthId", "newSVK", ELECTION_EVENT_ID))
				.thenReturn(extendedAuthentication);

		extendedAuthenticationServiceDecorator.renewExistingExtendedAuthentication(TENANT_ID, "oldAuthId", "newAuthId", "newSVK", ELECTION_EVENT_ID);
	}

	@Test
	public void testRenewExistingExtendedAuthenticationFail() throws ResourceNotFoundException, ApplicationException {

		expectedException.expect(ApplicationException.class);

		when(extendedAuthenticationService.renewExistingExtendedAuthentication(TENANT_ID, "oldAuthId", "newAuthId", "newSVK", ELECTION_EVENT_ID))
				.thenThrow(new ApplicationException("exception"));

		extendedAuthenticationServiceDecorator.renewExistingExtendedAuthentication(TENANT_ID, "oldAuthId", "newAuthId", "newSVK", ELECTION_EVENT_ID);
	}

	@Test
	public void testUpdateExistingExtendedAuthenticationSuccess() throws EntryPersistenceException {
		final ExtendedAuthentication extendedAuthentication = new ExtendedAuthentication();
		extendedAuthentication.setCredentialId("credentialId");
		extendedAuthentication.setElectionEvent(ELECTION_EVENT_ID);

		extendedAuthenticationServiceDecorator.updateExistingExtendedAuthentication(extendedAuthentication);
	}

	@Test
	public void testUpdateExistingExtendedAuthenticationFail() throws EntryPersistenceException {

		expectedException.expect(EntryPersistenceException.class);

		final ExtendedAuthentication extendedAuthentication = new ExtendedAuthentication();
		extendedAuthentication.setCredentialId("credentialId");
		extendedAuthentication.setElectionEvent(ELECTION_EVENT_ID);

		doThrow(EntryPersistenceException.class).when(extendedAuthenticationService).updateExistingExtendedAuthentication(extendedAuthentication);

		extendedAuthenticationServiceDecorator.updateExistingExtendedAuthentication(extendedAuthentication);
	}

	@Test
	public void testSaveNewExtendedAuthenticationSuccess() throws DuplicateEntryException {

		final ExtendedAuthentication extendedAuthentication = new ExtendedAuthentication();
		extendedAuthentication.setCredentialId("credentialId");
		extendedAuthentication.setElectionEvent(ELECTION_EVENT_ID);

		extendedAuthenticationServiceDecorator.saveNewExtendedAuthentication(extendedAuthentication);
	}

	@Test
	public void testSaveNewExtendedAuthenticationFail() throws DuplicateEntryException {

		expectedException.expect(DuplicateEntryException.class);

		final ExtendedAuthentication extendedAuthentication = new ExtendedAuthentication();
		extendedAuthentication.setCredentialId("credentialId");
		extendedAuthentication.setElectionEvent(ELECTION_EVENT_ID);

		doThrow(DuplicateEntryException.class).when(extendedAuthenticationService).saveNewExtendedAuthentication(extendedAuthentication);

		extendedAuthenticationServiceDecorator.saveNewExtendedAuthentication(extendedAuthentication);
	}

	@Test
	public void test_whenAuthenticateSuccessSecureLoggerRegistersEvent()
			throws ApplicationException, ResourceNotFoundException, AuthenticationException, AllowedAttemptsExceededException,
			GeneralCryptoLibException {

		final EncryptedSVK encryptedStartVotingKey = new EncryptedSVK("encryptedSVK");

		when(extendedAuthenticationService.authenticate(any(), any(), any(), any())).thenReturn(encryptedStartVotingKey);

		extendedAuthenticationServiceDecorator.authenticate(TENANT_ID, AUTH_ID, EXTRA_PARAM, ELECTION_EVENT_ID);
	}

	@Test
	public void test_whenAuthAttemptsReachMaximumSecureLoggerRegistersEvent() throws Exception {

		// given
		expectedException.expect(AllowedAttemptsExceededException.class);

		when(extendedAuthenticationService.authenticate(any(), any(), any(), eq(ELECTION_EVENT_ID)))
				.thenThrow(new AllowedAttemptsExceededException());

		// when
		extendedAuthenticationServiceDecorator.authenticate(TENANT_ID, AUTH_ID, EXTRA_PARAM, ELECTION_EVENT_ID);
	}

}
