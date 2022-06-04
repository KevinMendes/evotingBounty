/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.voteverification.ws.application.operation;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ch.post.it.evoting.cryptolib.certificates.utils.CryptographicOperationException;
import ch.post.it.evoting.domain.returncodes.ShortChoiceReturnCodeAndComputeResults;
import ch.post.it.evoting.domain.returncodes.VoteAndComputeResults;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.SemanticErrorException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.SyntaxErrorException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.voteverification.service.ChoiceReturnCodesService;

@DisplayName("ChoiceReturnCodesResource")
@ExtendWith(MockitoExtension.class)
class ChoiceReturnCodesResourceTest {

	private final static String fileName = "voteAndComputeResults.json";

	@Mock
	private HttpServletRequest mockServletRequest;

	@Mock
	private ChoiceReturnCodesService mockChoiceReturnCodesService;

	private ChoiceReturnCodesResource choiceReturnCodesResource;
	private Reader reader;

	private static InputStream getResourceAsStream(final String name) {
		return ChoiceReturnCodesResourceTest.class.getResourceAsStream("/choiceReturnCodesServiceTest/" + name);
	}

	@BeforeEach
	void setUp() {

		final TrackIdInstance trackIdInstance = new TrackIdInstance();
		final InputStream is = getResourceAsStream(fileName);

		reader = new InputStreamReader(is, StandardCharsets.UTF_8);
		choiceReturnCodesResource = new ChoiceReturnCodesResource(trackIdInstance, mockChoiceReturnCodesService);
	}

	@Test
	@DisplayName("retrieveShortChoiceReturnCodes with valid parameters and happy path")
	void retrieveShortChoiceReturnCodesHappyPath()
			throws SemanticErrorException, CryptographicOperationException, SyntaxErrorException, IOException,
			ResourceNotFoundException {

		final ShortChoiceReturnCodeAndComputeResults shortChoiceReturnCodeAndComputeResults = new ShortChoiceReturnCodeAndComputeResults();

		/* Expectations */
		when(mockChoiceReturnCodesService.retrieveShortChoiceReturnCodes(anyString(), anyString(), anyString(),
				any(VoteAndComputeResults.class)))
				.thenReturn(shortChoiceReturnCodeAndComputeResults);

		/* Execution */
		final Response response = choiceReturnCodesResource.retrieveShortChoiceReturnCodes("trackingId", "tenantId", "electionEventId",
				"verificationCardId", reader, mockServletRequest);

		/* Verification */
		verify(mockChoiceReturnCodesService).retrieveShortChoiceReturnCodes(anyString(), anyString(), anyString(),
				any(VoteAndComputeResults.class));

		assertNotNull(response);
		assertEquals(shortChoiceReturnCodeAndComputeResults, response.getEntity());
		assertEquals(200, response.getStatus());
	}

	@Test
	@DisplayName("Invalid inputs throws NullPointerException")
	void InvalidInput() {
		/* Execution */
		assertAll(
				() -> assertThrows(NullPointerException.class,
						() -> choiceReturnCodesResource.retrieveShortChoiceReturnCodes("trackingId", null, "electionEventId",
								"verificationCardId", reader, mockServletRequest)),
				() -> assertThrows(NullPointerException.class,
						() -> choiceReturnCodesResource.retrieveShortChoiceReturnCodes("trackingId", "tenantId", null,
								"verificationCardId", reader, mockServletRequest)),
				() -> assertThrows(NullPointerException.class,
						() -> choiceReturnCodesResource.retrieveShortChoiceReturnCodes("trackingId", "tenantId", "electionEventId",
								null, reader, mockServletRequest))
		);
	}

}