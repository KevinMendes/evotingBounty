/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.service.challenge;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.security.PublicKey;
import java.util.Base64;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricService;
import ch.post.it.evoting.cryptolib.certificates.utils.CryptographicOperationException;
import ch.post.it.evoting.votingserver.commons.beans.challenge.ChallengeInformation;
import ch.post.it.evoting.votingserver.commons.beans.challenge.ServerChallengeMessage;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.crypto.CertificateChainRepository;
import ch.post.it.evoting.votingserver.commons.crypto.SignatureForObjectService;

@RunWith(MockitoJUnitRunner.class)
public class ChallengeServerInformationSignatureValidationTest {

	public static final String TENANT_ID = "1";
	public static final String ELECTION_EVENT_ID = "1";
	public static final String VOTING_CARD_ID = "1";

	private static AsymmetricServiceAPI notMockedAsymmetricService;

	@InjectMocks
	private final ChallengeServerInformationSignatureValidation validation = new ChallengeServerInformationSignatureValidation();

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Mock
	private ChallengeInformation challengeInformationMock;

	@Mock
	private ServerChallengeMessage serverChallengeMessageMock;

	@Mock
	private CertificateChainRepository certificateChainRepository;

	@Mock
	private AsymmetricServiceAPI asymmetricServiceMock;

	private PublicKey publicKeyMock;

	@Mock
	private SignatureForObjectService signatureForObjectService;

	@BeforeClass
	public static void setUp() {
		notMockedAsymmetricService = new AsymmetricService();
	}

	@Before
	public void setup() {
		when(challengeInformationMock.getServerChallengeMessage()).thenReturn(serverChallengeMessageMock);
		commonPreparation();
	}

	@Test
	public void givenChallengeInformationThenValidationSuccess()
			throws ResourceNotFoundException, CryptographicOperationException, GeneralCryptoLibException {

		when(serverChallengeMessageMock.getSignature()).thenReturn(fakeSignatureServer());
		when(signatureForObjectService.getPublicKeyByAliasInCertificateChain(any(), anyString())).thenReturn(publicKeyMock);
		when(asymmetricServiceMock.verifySignature(any(byte[].class), any(PublicKey.class), any(byte[].class))).thenReturn(true);

		assertTrue(validation.execute(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID, challengeInformationMock));
	}

	@Test
	public void signatureClientChallengeFailed() throws ResourceNotFoundException, CryptographicOperationException, GeneralCryptoLibException {

		when(serverChallengeMessageMock.getSignature()).thenReturn(fakeSignatureServer());
		when(signatureForObjectService.getPublicKeyByAliasInCertificateChain(any(), anyString())).thenReturn(publicKeyMock);
		when(asymmetricServiceMock.verifySignature(any(byte[].class), any(PublicKey.class), any(byte[].class))).thenReturn(false);

		assertFalse(validation.execute(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID, challengeInformationMock));
	}

	@Test
	public void signatureThrowsCryptoException() throws ResourceNotFoundException, CryptographicOperationException, GeneralCryptoLibException {

		expectedException.expect(CryptographicOperationException.class);

		when(serverChallengeMessageMock.getSignature()).thenReturn(fakeSignatureServer());
		when(signatureForObjectService.getPublicKeyByAliasInCertificateChain(any(), anyString())).thenReturn(publicKeyMock);
		when(asymmetricServiceMock.verifySignature(any(byte[].class), any(PublicKey.class), any(byte[].class)))
				.thenThrow(new GeneralCryptoLibException("exception"));

		validation.execute(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID, challengeInformationMock);
	}

	private void commonPreparation() {

		publicKeyMock = notMockedAsymmetricService.getKeyPairForSigning().getPublic();

		when(challengeInformationMock.getServerChallengeMessage()).thenReturn(serverChallengeMessageMock);
	}

	private String fakeSignatureServer() {
		return Base64.getEncoder().encodeToString("signatureclient".getBytes(StandardCharsets.UTF_8));
	}

}
