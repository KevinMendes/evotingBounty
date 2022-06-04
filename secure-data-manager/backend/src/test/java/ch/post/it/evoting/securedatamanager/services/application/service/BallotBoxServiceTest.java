/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PublicKey;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.cryptolib.certificates.utils.PemUtils;
import ch.post.it.evoting.cryptolib.commons.serialization.JsonSignatureService;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.exceptions.FailedValidationException;
import ch.post.it.evoting.domain.election.BallotBox;
import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.config.commons.domain.common.SignedObject;
import ch.post.it.evoting.securedatamanager.config.commons.utils.ConfigObjectMapper;
import ch.post.it.evoting.securedatamanager.services.application.exception.DatabaseException;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.Status;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.SynchronizeStatus;
import ch.post.it.evoting.securedatamanager.services.infrastructure.PathResolver;
import ch.post.it.evoting.securedatamanager.services.infrastructure.ballotbox.BallotBoxRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.service.ConfigurationEntityStatusService;

@ExtendWith(MockitoExtension.class)
class BallotBoxServiceTest {

	private static final String BALLOT_BOX_ID = "96e";
	private static final String BALLOT_ID = "96f";
	private static final String ELECTION_EVENT_ID = "989";

	@InjectMocks
	private final BallotBoxService ballotBoxService = new BallotBoxService();

	@Mock
	private BallotBoxRepository ballotBoxRepositoryMock;

	@Mock
	private PathResolver pathResolver;

	@Mock
	private ConfigurationEntityStatusService statusServiceMock;

	private ConfigObjectMapper configObjectMapper;

	@BeforeEach
	void init() {
		configObjectMapper = new ConfigObjectMapper();
	}

	@Test
	void sign() throws IOException, GeneralCryptoLibException {

		ballotBoxService.init();

		final String ballotBoxREADY = "{\"result\": [{\"id\": \"96e\", \"ballot\" : { " + "\"id\": \"96f\"}, \"status\": \"READY\"}]}";
		when(ballotBoxRepositoryMock.list(anyMap())).thenReturn(ballotBoxREADY);
		when(pathResolver.resolveBallotBoxPath(any(), any(), any())).thenReturn(Paths.get(
				"src/test/resources/ballotboxservice/" + ELECTION_EVENT_ID + "/ONLINE/electionInformation/ballots/" + BALLOT_ID + "/ballotBoxes/"
						+ BALLOT_BOX_ID));
		when(statusServiceMock.updateWithSynchronizedStatus(Status.SIGNED.name(), BALLOT_BOX_ID, ballotBoxRepositoryMock, SynchronizeStatus.PENDING))
				.thenReturn("");

		assertDoesNotThrow(() -> ballotBoxService.sign(ELECTION_EVENT_ID, BALLOT_BOX_ID, SigningTestData.PRIVATE_KEY_PEM));

		final Path outputPath = Paths
				.get("src/test/resources/ballotboxservice/989/ONLINE/electionInformation" + "/ballots/" + BALLOT_ID + "/ballotBoxes/"
						+ BALLOT_BOX_ID);
		final Path signedBallotBox = Paths.get(outputPath.toString(), Constants.CONFIG_FILE_NAME_SIGNED_BALLOTBOX_JSON);

		assertTrue(Files.exists(signedBallotBox));

		final SignedObject signedBallotBoxObject = configObjectMapper.fromJSONFileToJava(signedBallotBox.toFile(), SignedObject.class);
		final String signatureBallotBox = signedBallotBoxObject.getSignature();

		final PublicKey publicKey = PemUtils.publicKeyFromPem(SigningTestData.PUBLIC_KEY_PEM);

		JsonSignatureService.verify(publicKey, signatureBallotBox, BallotBox.class);

		Files.deleteIfExists(signedBallotBox);
	}

	@Test
	void throwExceptionWhenBallotBoxIsLocked() {

		ballotBoxService.init();

		when(ballotBoxRepositoryMock.list(any())).thenThrow(DatabaseException.class);

		assertThrows(DatabaseException.class, () -> ballotBoxService.sign(ELECTION_EVENT_ID, BALLOT_BOX_ID, SigningTestData.PRIVATE_KEY_PEM));
	}

	@Test
	void isDownloadedBallotBoxEmptyInvalidInputTest() {

		ballotBoxService.init();

		final String electionEventId = "12e590cc85ad49af96b15ca761dfe49d";
		final String ballotId = "9ef69a395b104f6aac655ede2501c1e0";
		final String ballotBoxId = "6996c614575a41a296d2026e30fac4a0";

		assertAll(() -> assertThrows(NullPointerException.class, () -> ballotBoxService.isDownloadedBallotBoxEmpty(null, ballotId, ballotBoxId)),
				() -> assertThrows(NullPointerException.class, () -> ballotBoxService.isDownloadedBallotBoxEmpty(electionEventId, null, ballotBoxId)),
				() -> assertThrows(NullPointerException.class, () -> ballotBoxService.isDownloadedBallotBoxEmpty(electionEventId, ballotId, null)),
				() -> assertThrows(FailedValidationException.class,
						() -> ballotBoxService.isDownloadedBallotBoxEmpty("electionEventId", ballotId, ballotBoxId)),
				() -> assertThrows(FailedValidationException.class,
						() -> ballotBoxService.isDownloadedBallotBoxEmpty(electionEventId, "ballotId", ballotBoxId)),
				() -> assertThrows(FailedValidationException.class,
						() -> ballotBoxService.isDownloadedBallotBoxEmpty(electionEventId, ballotId, "ballotBoxId")));
	}

	@Test
	void isDownloadedBallotBoxEmptyTrueTest() {

		ballotBoxService.init();

		final String electionEventId = "12e590cc85ad49af96b15ca761dfe49d";
		final String ballotId = "9ef69a395b104f6aac655ede2501c1e0";
		final String ballotBoxId = "6996c614575a41a296d2026e30fac4a0";

		when(pathResolver.resolveBallotBoxPath(electionEventId, ballotId, ballotBoxId)).thenReturn(Paths.get(
				"src/test/resources/ballotboxservice/" + electionEventId + "/ONLINE/electionInformation/ballots/" + ballotId + "/ballotBoxes/"
						+ ballotBoxId));

		assertTrue(ballotBoxService.isDownloadedBallotBoxEmpty(electionEventId, ballotId, ballotBoxId));
	}

	@Test
	void isDownloadedBallotBoxEmptyFalseTest() {

		ballotBoxService.init();

		final String electionEventId = "12e590cc85ad49af96b15ca761dfe49d";
		final String ballotId = "aae39176ad9a44e7a360ed78a7240f55";
		final String ballotBoxId = "a8f0302590454d068cc40051d4100da7";

		when(pathResolver.resolveBallotBoxPath(electionEventId, ballotId, ballotBoxId)).thenReturn(Paths.get(
				"src/test/resources/ballotboxservice/" + electionEventId + "/ONLINE/electionInformation/ballots/" + ballotId + "/ballotBoxes/"
						+ ballotBoxId));

		assertFalse(ballotBoxService.isDownloadedBallotBoxEmpty(electionEventId, ballotId, ballotBoxId));
	}

	@Test
	void doesDownloadedBallotBoxContainConfirmedVotesFalseTest() {

		ballotBoxService.init();

		final String electionEventId = "263832206e5e40d0beac0573727cd481";
		final String ballotId = "3b4880568e8b4ff8a51974a3cdcc8b99";
		final String ballotBoxId = "e8070fbccecf4ed1ae9849814afbb8f7";

		when(pathResolver.resolveBallotBoxPath(electionEventId, ballotId, ballotBoxId)).thenReturn(Paths.get(
				"src/test/resources/ballotboxservice/" + electionEventId + "/ONLINE/electionInformation/ballots/" + ballotId + "/ballotBoxes/"
						+ ballotBoxId));

		assertFalse(ballotBoxService.hasDownloadedBallotBoxConfirmedVotes(electionEventId, ballotId, ballotBoxId));
	}

	@Test
	void hasDownloadedBallotBoxConfirmedVotesTrueTest() {

		ballotBoxService.init();

		final String electionEventId = "263832206e5e40d0beac0573727cd481";
		final String ballotId = "3b4880568e8b4ff8a51974a3cdcc8b99";
		final String ballotBoxId = "80b8567306be4dcca975e6256bf3013e";

		when(pathResolver.resolveBallotBoxPath(electionEventId, ballotId, ballotBoxId)).thenReturn(Paths.get(
				"src/test/resources/ballotboxservice/" + electionEventId + "/ONLINE/electionInformation/ballots/" + ballotId + "/ballotBoxes/"
						+ ballotBoxId));

		assertTrue(ballotBoxService.hasDownloadedBallotBoxConfirmedVotes(electionEventId, ballotId, ballotBoxId));
	}

	static class MyHttpHandler implements HttpHandler {
		@Override
		public void handle(final HttpExchange httpExchange) throws IOException {
			final String response = "{\"status\":\"MIXED\"}";
			httpExchange.getResponseHeaders().add("Content-Type", "application/json");
			httpExchange.sendResponseHeaders(200, response.length());

			final OutputStream outputStream = httpExchange.getResponseBody();
			outputStream.write(response.getBytes(StandardCharsets.UTF_8));
			outputStream.close();
		}
	}
}
