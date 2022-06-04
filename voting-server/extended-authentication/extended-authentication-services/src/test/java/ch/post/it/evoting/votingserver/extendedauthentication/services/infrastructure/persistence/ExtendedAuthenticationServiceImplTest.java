/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.extendedauthentication.services.infrastructure.persistence;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Collections;
import java.util.Optional;

import javax.persistence.LockTimeoutException;
import javax.persistence.PessimisticLockException;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.bean.CertificateParameters;
import ch.post.it.evoting.cryptolib.certificates.cryptoapi.CryptoAPIX509Certificate;
import ch.post.it.evoting.domain.election.model.Information.VoterInformation;
import ch.post.it.evoting.domain.election.model.certificate.CertificateEntity;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.EntryPersistenceException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.client.RetrofitException;
import ch.post.it.evoting.votingserver.commons.infrastructure.remote.service.RemoteCertificateService;
import ch.post.it.evoting.votingserver.commons.infrastructure.transaction.TransactionContext;
import ch.post.it.evoting.votingserver.commons.infrastructure.transaction.TransactionController;
import ch.post.it.evoting.votingserver.commons.infrastructure.transaction.TransactionalAction;
import ch.post.it.evoting.votingserver.commons.infrastructure.transaction.TransactionalActionException;
import ch.post.it.evoting.votingserver.commons.util.CryptoUtils;
import ch.post.it.evoting.votingserver.commons.verify.CSVVerifier;
import ch.post.it.evoting.votingserver.extendedauthentication.domain.model.extendedauthentication.AllowedAttemptsExceededException;
import ch.post.it.evoting.votingserver.extendedauthentication.domain.model.extendedauthentication.ExtendedAuthentication;
import ch.post.it.evoting.votingserver.extendedauthentication.domain.model.extendedauthentication.ExtendedAuthenticationRepository;
import ch.post.it.evoting.votingserver.extendedauthentication.services.infrastructure.remote.VoterMaterialService;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class ExtendedAuthenticationServiceImplTest {

	public static final String COMMON_NAME = "certificate";
	public static final String INVALID = "invalid";

	private static final String VOTING_CARD_ID_2 = "26f4e529b51bca0f5f4fa6f1339a0245;Fk0vw+glXPNlTUnziNKH8w==";
	private static final String PDKDF_2 = "Fk0vw+glXPNlTUnziNKH8w==";
	private static final String SALT = "Mzk2OTY5NmI2YTZiNjczNjM0NjU3YTM4MzU2ZDczNzk2NjM4Nzk3NTc2MzM3Njc2NzA2MTczMzk2ZDZlNzMzNzM1NzE2ZTM0MzI3NDc5NjE2ZDc1NzMzMjZiMzgzNTc5NzYzNTc0NzQ3NDYyNjM3NTM2NmQ2YjY4NzM2NjY0Njg=";
	private static final String ENCRYPTED_START_VOTING_KEY = "QzZB//+c1Kn0WjppIsJhfQwuF4RUmdTaJf5YoG9iCaeMbW5en/eMxG+1W2NYkv0e";
	private static final String TENANT_ID = "1";
	private static final String ELECTION_EVENT = "2";
	private static final String EXTRA_PARAM = "e1ede18a8e1a4f7a8a65b6104dc0abed";

	@Mock
	private ExtendedAuthenticationRepository authenticationRepository;

	@InjectMocks
	private ExtendedAuthenticationServiceImpl sut;

	@Mock
	private TransactionController transactionController;

	@Mock
	private RemoteCertificateService remoteCertificateService;

	@Mock
	private CSVVerifier verifier;

	@Mock
	private CertificateEntity certificateEntity;

	@Mock
	private VoterMaterialService voterMaterialService;

	@BeforeClass
	public static void setup() {
		MockitoAnnotations.openMocks(ExtendedAuthenticationServiceImplTest.class);
	}

	@Test
	public void testTooManyAttempts() throws ApplicationException {
		ExtendedAuthentication extendedAuthentication = createEntity(TENANT_ID, PDKDF_2, ELECTION_EVENT, ENCRYPTED_START_VOTING_KEY, SALT, 6);
		when(authenticationRepository.getForUpdate(any(), any(), any())).thenReturn(Optional.of(extendedAuthentication));

		assertThrows(AllowedAttemptsExceededException.class, () -> sut.authenticate(TENANT_ID, VOTING_CARD_ID_2, EXTRA_PARAM, ELECTION_EVENT));
	}

	@Test(expected = ApplicationException.class)
	public void testAuthenticationLockTimeoutException()
			throws ResourceNotFoundException, AllowedAttemptsExceededException, AuthenticationException, ApplicationException,
			GeneralCryptoLibException {

		createEntity(TENANT_ID, PDKDF_2, ELECTION_EVENT, ENCRYPTED_START_VOTING_KEY, SALT, 0);
		when(authenticationRepository.getForUpdate(any(), any(), any())).thenThrow(new LockTimeoutException("exception"));

		sut.authenticate(TENANT_ID, VOTING_CARD_ID_2, EXTRA_PARAM, ELECTION_EVENT);
	}

	@Test(expected = ApplicationException.class)
	public void testAuthenticationPessimisticLockException()
			throws ResourceNotFoundException, AllowedAttemptsExceededException, AuthenticationException, ApplicationException,
			GeneralCryptoLibException {

		createEntity(TENANT_ID, PDKDF_2, ELECTION_EVENT, ENCRYPTED_START_VOTING_KEY, SALT, 0);
		when(authenticationRepository.getForUpdate(any(), any(), any())).thenThrow(new PessimisticLockException("exception"));

		sut.authenticate(TENANT_ID, VOTING_CARD_ID_2, EXTRA_PARAM, ELECTION_EVENT);
	}

	@Test
	public void testInvalidExtraParamAuthentication() throws ApplicationException {
		ExtendedAuthentication extendedAuthentication = createEntity(TENANT_ID, PDKDF_2, ELECTION_EVENT, ENCRYPTED_START_VOTING_KEY, SALT, 0);
		when(authenticationRepository.getForUpdate(any(), any(), any())).thenReturn(Optional.of(extendedAuthentication));

		final AuthenticationException authenticationException = assertThrows(AuthenticationException.class,
				() -> sut.authenticate(TENANT_ID, VOTING_CARD_ID_2, "invalid", ELECTION_EVENT));
		assertEquals("invalid extra parameter", authenticationException.getMessage());
	}

	@Test
	public void testSaveExtendedAuthenticationInfoExisting() throws IOException, GeneralCryptoLibException, RetrofitException {
		final Path tempFile = Files.createTempFile("test.csv", null);

		FileUtils.writeStringToFile(tempFile.toFile(), "1,2,4,5,6" + System.lineSeparator() + "1,2,3,4,5");

		mockCertificate();
		sut.saveExtendedAuthenticationFromFile(tempFile, TENANT_ID, ELECTION_EVENT, "");

	}

	@Test
	public void testSaveExtendedAuthenticationInfoNotExisting() throws IOException, GeneralCryptoLibException, RetrofitException {
		final Path tempFile = Files.createTempFile("test.csv", null);
		FileUtils.writeStringToFile(tempFile.toFile(), "1,2,4,5,6" + System.lineSeparator() + "1,2,3,4,5");

		mockCertificate();
		sut.saveExtendedAuthenticationFromFile(tempFile, TENANT_ID, ELECTION_EVENT, "");
	}

	@Test
	public void testSaveExtendedAuthenticationInfoTransactionException() throws IOException, GeneralCryptoLibException, RetrofitException {
		final Path tempFile = Files.createTempFile("test.csv", null);
		FileUtils.writeStringToFile(tempFile.toFile(), "1,2,4,5,6" + System.lineSeparator() + "1,2,3,4,5");

		mockCertificate();
		sut.saveExtendedAuthenticationFromFile(tempFile, TENANT_ID, ELECTION_EVENT, "");
	}

	@Test
	public void testUpdateExistingAuthenticationInfoSuccess() throws ApplicationException, ResourceNotFoundException {
		when(authenticationRepository.getForUpdate(any(), any(), any())).thenReturn(Optional.of(new ExtendedAuthentication()));
		sut.renewExistingExtendedAuthentication(TENANT_ID, "", "", "", ELECTION_EVENT);
	}

	@Test(expected = ApplicationException.class)
	public void testUpdateExistingAuthenticationInfoDuplicate() throws ApplicationException, ResourceNotFoundException, DuplicateEntryException {
		when(authenticationRepository.getForUpdate(any(), any(), any())).thenReturn(Optional.of(new ExtendedAuthentication()));
		when(authenticationRepository.save(any())).thenThrow(new DuplicateEntryException("exception"));
		sut.renewExistingExtendedAuthentication(TENANT_ID, "", "", "", ELECTION_EVENT);
	}

	@Test
	public void testNonExistingVotingCardId2() throws Exception {
		when(authenticationRepository.getForUpdate(any(), any(), any())).thenReturn(Optional.empty());

		final ResourceNotFoundException resourceNotFoundException = assertThrows(ResourceNotFoundException.class,
				() -> sut.authenticate(TENANT_ID, VOTING_CARD_ID_2, EXTRA_PARAM, ELECTION_EVENT));
		final String errorMessage = String
				.format("Not found Extended Authentication with Id=%s, Tenant Id=%s and Election Event Id= %s.", VOTING_CARD_ID_2, TENANT_ID,
						ELECTION_EVENT);
		assertEquals(errorMessage, resourceNotFoundException.getMessage());
	}

	@Test
	public void authenticateFailsOnUpdate() throws ApplicationException, EntryPersistenceException {
		ExtendedAuthentication extendedAuthentication = createEntity(TENANT_ID, PDKDF_2, ELECTION_EVENT, ENCRYPTED_START_VOTING_KEY, SALT, 0);
		when(authenticationRepository.getForUpdate(any(), any(), any())).thenReturn(Optional.of(extendedAuthentication));
		when(authenticationRepository.update(any(ExtendedAuthentication.class))).thenThrow(new EntryPersistenceException("exception"));

		assertThrows(AuthenticationException.class, () -> sut.authenticate(TENANT_ID, VOTING_CARD_ID_2, INVALID, ELECTION_EVENT));
	}

	private ExtendedAuthentication createEntity(String tenantId, String encryptedExtraParam, String electionEvent, String encryptedStartVotingKey,
			String salt, Integer attempts) {
		ExtendedAuthentication extendedAuthentication = new ExtendedAuthentication();
		extendedAuthentication.setAttempts(attempts);
		extendedAuthentication.setElectionEvent(electionEvent);
		extendedAuthentication.setEncryptedStartVotingKey(encryptedStartVotingKey);
		extendedAuthentication.setExtraParam(encryptedExtraParam);
		extendedAuthentication.setTenantId(tenantId);
		extendedAuthentication.setSalt(salt);
		return extendedAuthentication;
	}

	private CryptoAPIX509Certificate createCertificate() throws GeneralCryptoLibException {
		final KeyPair keyPairForSigning = CryptoUtils.getKeyPairForSigning();
		return CryptoUtils.createCryptoAPIx509Certificate(COMMON_NAME, CertificateParameters.Type.SIGN, keyPairForSigning);
	}

	private Answer<Object> defaultAnswer() {
		return invocation -> {
			Object result;
			TransactionalAction<?> action = invocation.getArgument(0, TransactionalAction.class);
			TransactionContext context = mock(TransactionContext.class);
			try {
				result = action.execute(context);
			} catch (RuntimeException e) {
				throw e;
			} catch (Exception e) {
				throw new TransactionalActionException(e);
			}
			return result;
		};
	}

	@Test
	public void testSaveOrUpdateExtendedAuthenticationFromFileSuccess()
			throws IOException, TransactionalActionException, GeneralCryptoLibException, RetrofitException {

		final Path tempFile = Files.createTempFile("testSecureLogWhenEntryPersistenceException.csv", null);

		FileUtils.writeStringToFile(tempFile.toFile(),
				"authId1,extraParam1,encryptedStartVotingKey1,electionEvent1,salt1,credentialId1" + System.lineSeparator()
						+ "authId2,extraParam2,encryptedStartVotingKey2,electionEvent2,salt2,credentialId2" + System.lineSeparator()
						+ "authId1,extraParam1,encryptedStartVotingKey1,electionEvent1,salt1,credentialId1");

		ExtendedAuthentication extendedAuthentication = new ExtendedAuthentication();
		extendedAuthentication.setAuthId("authId1");
		extendedAuthentication.setExtraParam("extraParam1");
		extendedAuthentication.setEncryptedStartVotingKey("encryptedStartVotingKey1");
		extendedAuthentication.setElectionEvent("electionEvent1");
		extendedAuthentication.setSalt("salt1");
		extendedAuthentication.setCredentialId("credentialId1");
		mockCertificate();
		when(verifier.verify(any(PublicKey.class), any(Path.class))).thenReturn(true);

		when(transactionController.doInNewTransaction(any(TransactionalAction.class))).then(defaultAnswer());

		sut.saveExtendedAuthenticationFromFile(tempFile, TENANT_ID, ELECTION_EVENT, "adminBoardId");

	}

	@Test
	public void testExportVotingCardsReturnsDataIfCredentialsAreBlocked() throws Exception {
		String tenantId = "tenantId";
		String electionEventId = "electionEventId";
		String credentialId = "credentialId";
		String votingCardId = "votingCardId";

		VoterInformation testVoterInformation = new VoterInformation();
		testVoterInformation.setElectionEventId(electionEventId);
		testVoterInformation.setCredentialId(credentialId);
		testVoterInformation.setVotingCardId(votingCardId);
		when(voterMaterialService.getVoterInformationByCredentialId(eq(tenantId), eq(electionEventId), eq(credentialId)))
				.thenReturn(Optional.of(testVoterInformation));

		final ExtendedAuthentication mockExtendedAuthenticationEntity = mock(ExtendedAuthentication.class);
		when(mockExtendedAuthenticationEntity.getTenantId()).thenReturn(tenantId);
		when(mockExtendedAuthenticationEntity.getElectionEvent()).thenReturn(electionEventId);
		when(mockExtendedAuthenticationEntity.getCredentialId()).thenReturn(credentialId);

		when(authenticationRepository.findAllExceededExtendedAuthentication(eq(tenantId), eq(electionEventId), anyInt()))
				.thenReturn(Collections.singletonList(mockExtendedAuthenticationEntity));

		byte[] bytes;
		try (ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {
			sut.findAndWriteVotingCardsWithFailedAuthentication(tenantId, electionEventId, outStream);
			bytes = outStream.toByteArray();
		}

		String stringData = new String(bytes, StandardCharsets.UTF_8);
		assertTrue(stringData.contains(votingCardId));
	}

	@Test
	public void testExportVotingCardsReturnsEmptyIfNoCredentialsAreBlocked() throws Exception {
		String tenantId = "tenantId";
		String electionEventId = "electionEventId";

		when(authenticationRepository.findAllExceededExtendedAuthentication(anyString(), any(), anyInt())).thenReturn(Collections.emptyList());

		byte[] bytes;
		try (ByteArrayOutputStream outStream = new ByteArrayOutputStream()) {
			sut.findAndWriteVotingCardsWithFailedAuthentication(tenantId, electionEventId, outStream);
			bytes = outStream.toByteArray();
		}

		assertEquals(0, bytes.length);
		String stringData = new String(bytes, StandardCharsets.UTF_8);
		assertTrue(stringData.isEmpty());
	}

	private void mockCertificate() throws GeneralCryptoLibException, RetrofitException {
		final CryptoAPIX509Certificate certificate = createCertificate();
		when(certificateEntity.getCertificateContent()).thenReturn(new String(certificate.getPemEncoded(), StandardCharsets.UTF_8));
		when(remoteCertificateService.getAdminBoardCertificate(anyString())).thenReturn(certificateEntity);
	}

	@Test
	public void testAuthenticateInvalidExtraParamsNoExceptionThrown()
			throws ApplicationException, ResourceNotFoundException, AuthenticationException, AllowedAttemptsExceededException,
			GeneralCryptoLibException {
		String authId = "authId1";
		ExtendedAuthentication extendedAuthentication = new ExtendedAuthentication();
		extendedAuthentication.setAuthId(authId);
		extendedAuthentication.setSalt("");
		extendedAuthentication.setExtraParam(null);

		when(authenticationRepository.getForUpdate(TENANT_ID, authId, ELECTION_EVENT)).thenReturn(Optional.of(extendedAuthentication));

		sut.authenticate(TENANT_ID, authId, null, ELECTION_EVENT);
	}

}
