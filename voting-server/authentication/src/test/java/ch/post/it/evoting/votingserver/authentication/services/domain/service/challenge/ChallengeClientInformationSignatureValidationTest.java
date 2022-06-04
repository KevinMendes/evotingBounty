/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.authentication.services.domain.service.challenge;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Base64;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.bean.CertificateParameters;
import ch.post.it.evoting.cryptolib.certificates.cryptoapi.CryptoAPIX509Certificate;
import ch.post.it.evoting.cryptolib.certificates.utils.CryptographicOperationException;
import ch.post.it.evoting.votingserver.commons.beans.challenge.ChallengeInformation;
import ch.post.it.evoting.votingserver.commons.beans.challenge.ClientChallengeMessage;
import ch.post.it.evoting.votingserver.commons.beans.challenge.ServerChallengeMessage;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.util.CryptoUtils;

@RunWith(MockitoJUnitRunner.class)
public class ChallengeClientInformationSignatureValidationTest {

	public static final String TENANT_ID = "1";
	public static final String ELECTION_EVENT_ID = "1";
	public static final String VOTING_CARD_ID = "1";
	public static final String CHALLENGE_MESSAGE = "message";

	@InjectMocks
	private final ChallengeClientInformationSignatureValidation validation = new ChallengeClientInformationSignatureValidation();

	@Rule
	public ExpectedException expectedException = ExpectedException.none();

	@Mock
	private ChallengeInformation challengeInformationMock;

	@Mock
	private ServerChallengeMessage serverChallengeMessageMock;

	@Mock
	private ClientChallengeMessage clientChallengeMessageMock;

	@Mock
	private AsymmetricServiceAPI asymmetricServiceMock;

	@Before
	public void setup() throws GeneralCryptoLibException {
		when(challengeInformationMock.getServerChallengeMessage()).thenReturn(serverChallengeMessageMock);
		commonPreparation();
	}

	@Test
	public void givenChallengeInformationThenValidationSuccess()
			throws ResourceNotFoundException, CryptographicOperationException, GeneralCryptoLibException {

		when(clientChallengeMessageMock.getSignature()).thenReturn(fakeSignatureClient());
		when(serverChallengeMessageMock.getSignature()).thenReturn(fakeSignatureClient());

		when(asymmetricServiceMock.verifySignature(any(byte[].class), any(PublicKey.class), any(byte[].class))).thenReturn(true);

		assertTrue(validation.execute(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID, challengeInformationMock));
	}

	@Test
	public void signatureClientChallengeFailed() throws ResourceNotFoundException, CryptographicOperationException, GeneralCryptoLibException {

		when(clientChallengeMessageMock.getSignature()).thenReturn(fakeSignatureClient());
		when(serverChallengeMessageMock.getSignature()).thenReturn(fakeSignatureClient());
		when(asymmetricServiceMock.verifySignature(any(byte[].class), any(PublicKey.class), any(byte[].class))).thenReturn(false);
		assertFalse(validation.execute(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID, challengeInformationMock));
	}

	@Test
	public void signatureThrowsCryptoException() throws ResourceNotFoundException, CryptographicOperationException, GeneralCryptoLibException {

		expectedException.expect(CryptographicOperationException.class);

		when(clientChallengeMessageMock.getSignature()).thenReturn(fakeSignatureClient());
		when(serverChallengeMessageMock.getSignature()).thenReturn(fakeSignatureClient());
		when(asymmetricServiceMock.verifySignature(any(byte[].class), any(PublicKey.class), any(byte[].class)))
				.thenThrow(new GeneralCryptoLibException("exception"));

		validation.execute(TENANT_ID, ELECTION_EVENT_ID, VOTING_CARD_ID, challengeInformationMock);
	}

	private void commonPreparation() throws GeneralCryptoLibException {

		final KeyPair keyPair = CryptoUtils.getKeyPairForSigning();
		final CryptoAPIX509Certificate certificate = CryptoUtils
				.createCryptoAPIx509Certificate("commonName", CertificateParameters.Type.SIGN, keyPair);
		final String pem = new String(certificate.getPemEncoded(), StandardCharsets.UTF_8);

		when(challengeInformationMock.getCertificate()).thenReturn(pem);
		when(challengeInformationMock.getClientChallengeMessage()).thenReturn(clientChallengeMessageMock);
		when(challengeInformationMock.getServerChallengeMessage()).thenReturn(serverChallengeMessageMock);
		when(clientChallengeMessageMock.getClientChallenge()).thenReturn(CHALLENGE_MESSAGE);
	}

	private String fakeSignatureClient() {
		return Base64.getEncoder().encodeToString("signatureclient".getBytes(StandardCharsets.UTF_8));
	}

}
