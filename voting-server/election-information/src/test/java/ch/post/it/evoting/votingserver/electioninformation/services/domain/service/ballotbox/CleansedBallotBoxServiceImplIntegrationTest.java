/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.electioninformation.services.domain.service.ballotbox;

import static ch.post.it.evoting.votingserver.electioninformation.services.domain.model.util.Constants.BALLOT_BOX_ID;
import static ch.post.it.evoting.votingserver.electioninformation.services.domain.model.util.Constants.ELECTION_EVENT_ID;
import static ch.post.it.evoting.votingserver.electioninformation.services.domain.model.util.Constants.TENANT_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.Scanner;
import java.util.stream.Stream;

import javax.naming.InvalidNameException;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptolib.api.asymmetric.AsymmetricServiceAPI;
import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricService;
import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.MixnetInitialPayload;
import ch.post.it.evoting.cryptoprimitives.hashing.HashService;
import ch.post.it.evoting.domain.election.model.ballotbox.BallotBoxIdImpl;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.CleansedBallotBoxServiceException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.sign.TestCertificateGenerator;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.BallotBoxInformation;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.BallotBoxInformationRepository;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.CleansedBallotBox;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.ballotbox.CleansedBallotBoxRepository;
import ch.post.it.evoting.votingserver.electioninformation.services.domain.model.tenant.EiTenantSystemKeys;

import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

@ExtendWith({ SystemStubsExtension.class, MockitoExtension.class })
class CleansedBallotBoxServiceImplIntegrationTest {

	private static final short TESTING_VOTE_SET_MAX_SIZE = 10;
	private static final BallotBoxIdImpl ballotBoxId = new BallotBoxIdImpl(TENANT_ID, ELECTION_EVENT_ID, BALLOT_BOX_ID);
	private static final CleansedBallotBoxRepository cleansedBallotBoxRepository = mock(CleansedBallotBoxRepository.class);
	private static final BallotBoxInformationRepository ballotBoxInformationRepository = mock(BallotBoxInformationRepository.class);
	private static final EiTenantSystemKeys eiTenantSystemKeys = mock(EiTenantSystemKeys.class);

	private static TestCertificateGenerator testCertificateGenerator;

	@SystemStub
	private static EnvironmentVariables environmentVariables;

	@Spy
	private static AsymmetricServiceAPI asymmetricService;

	@Spy
	private static ObjectMapper objectMapper;

	@Mock
	private HashService hashService;

	@Spy
	@InjectMocks
	private CleansedBallotBoxServiceImpl cleansedBallotBoxService;

	@BeforeAll
	public static void setUpAll() throws GeneralCryptoLibException, IOException, InvalidNameException, ResourceNotFoundException {

		// Initialise actual services.
		asymmetricService = new AsymmetricService();
		testCertificateGenerator = TestCertificateGenerator.createDefault();
		objectMapper = DomainObjectMapper.getNewInstance();

		// Initialise mocks.
		setUpEiTenantSystemKeysMock();
		setUpBallotBoxInformationRepositoryMock();

		environmentVariables.set("VOTE_SET_MAX_SIZE", TESTING_VOTE_SET_MAX_SIZE);
	}

	private static void setUpEiTenantSystemKeysMock() throws InvalidNameException, GeneralCryptoLibException {

		final KeyPair signingKeyPair = asymmetricService.getKeyPairForSigning();
		final X509Certificate signingCertificate = testCertificateGenerator.createCACertificate(signingKeyPair, "Signing certificate");

		// Set up the certificate chain for signing.
		final X509Certificate[] certificateChain = { signingCertificate, testCertificateGenerator.getRootCertificate() };
		when(eiTenantSystemKeys.getSigningCertificateChain(anyString())).thenReturn(certificateChain);

		// Set up the signing key.
		when(eiTenantSystemKeys.getSigningPrivateKey(anyString())).thenReturn(signingKeyPair.getPrivate());
	}

	private static void setUpBallotBoxInformationRepositoryMock() throws ResourceNotFoundException, IOException {
		final String jsonString;
		try (final InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("ballot_box_information-empty.json");
				final Scanner scanner = new Scanner(is)) {
			assertNotNull(is);
			jsonString = scanner.useDelimiter("\\A").next();
			assertFalse(jsonString.isEmpty());
		}
		final BallotBoxInformation bbi = mock(BallotBoxInformation.class);
		when(bbi.getJson()).thenReturn(jsonString);
		when(ballotBoxInformationRepository.findByTenantIdElectionEventIdBallotBoxId(anyString(), anyString(), anyString())).thenReturn(bbi);
	}

	@Test
	void testPayloadWithValidVoteSet() throws ResourceNotFoundException, CleansedBallotBoxServiceException {
		assertEquals(TESTING_VOTE_SET_MAX_SIZE, CleansedBallotBox.CHUNK_SIZE);

		final Stream<String> voteStream = Stream.generate(() -> "{\"gamma\": \"0x4\", \"phis\": [\"0x9\"]}").limit(TESTING_VOTE_SET_MAX_SIZE);
		when(cleansedBallotBoxRepository.getVoteSet(any(), anyInt(), anyInt())).thenReturn(voteStream);

		final byte[] bytes = new byte[] { 1, 2, 3 };
		when(hashService.recursiveHash(any())).thenReturn(bytes);

		final MixnetInitialPayload mixnetInitialPayload = cleansedBallotBoxService.getMixnetInitialPayload(ballotBoxId);

		// Ensure the payload contains the expected number of votes.
		assertEquals(TESTING_VOTE_SET_MAX_SIZE, mixnetInitialPayload.getEncryptedVotes().size());
	}

}
