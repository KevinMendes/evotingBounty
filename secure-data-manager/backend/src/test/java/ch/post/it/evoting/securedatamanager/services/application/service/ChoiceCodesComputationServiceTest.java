/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.sun.net.httpserver.HttpServer;

import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.services.infrastructure.cc.PayloadStorageException;
import ch.post.it.evoting.securedatamanager.services.infrastructure.cc.ReturnCodeGenerationRequestPayloadFileSystemRepository;
import ch.post.it.evoting.securedatamanager.services.infrastructure.votingcardset.VotingCardSetRepository;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import retrofit2.Response;

@ExtendWith(MockitoExtension.class)
class ChoiceCodesComputationServiceTest {

	private static final String VCS_LIST = "{\"result\":[{\"verificationCardSetId\":\"123\", \"status\":\"COMPUTING\"}]}";
	private static final String ELECTION_EVENT_ID = "abcd";
	private static final int FREE_PORT_LOOKUP_LOWER_BOUND = 50000;
	private static final int FREE_PORT_LOOKUP_UPPER_BOUND = 60000;

	private static int httpPort;

	@InjectMocks
	private final ChoiceCodesComputationService choiceCodesComputationService = mock(ChoiceCodesComputationService.class);

	@Mock
	private VotingCardSetRepository votingCardSetRepository;

	@Mock
	private ReturnCodeGenerationRequestPayloadFileSystemRepository returnCodeGenerationRequestPayloadRepository;

	private static void setUpHttpServer() {
		try {
			setPorts();

			// setup the socket address
			final InetSocketAddress address = new InetSocketAddress(httpPort);

			// initialise the HTTP server
			final HttpServer httpServer = HttpServer.create(address, 0);
			httpServer.setExecutor(null); // creates a default executor
			httpServer.start();

		} catch (IOException e) {
			System.out.println("Failed to create HTTP server on port 8080 of localhost");
			e.printStackTrace();

		}
	}

	private static void setPorts() throws IOException {
		httpPort = discoverFreePort();
	}

	private static int discoverFreePort() throws IOException {
		int result = 0;
		ServerSocket tempServer = null;

		for (int i = FREE_PORT_LOOKUP_LOWER_BOUND; i <= FREE_PORT_LOOKUP_UPPER_BOUND; i++) {
			try {
				tempServer = new ServerSocket(i);
				result = tempServer.getLocalPort();
				break;

			} catch (IOException ex) {
				// try next port
			}
		}

		if (result == 0) {
			throw new IOException(String.format("No free port found withing the provided range %s - %s.", FREE_PORT_LOOKUP_LOWER_BOUND,
					FREE_PORT_LOOKUP_UPPER_BOUND));
		}

		tempServer.close();
		return result;
	}

	@Test
	void updateComputationStatusComputed() throws IOException, PayloadStorageException {
		setUpHttpServer();

		ReflectionTestUtils.setField(choiceCodesComputationService, "tenantId", Constants.HUNDRED);

		final String orchestratorUrl = String.format("http://localhost:%s/ag-ws-rest/or", httpPort);
		ReflectionTestUtils.setField(choiceCodesComputationService, "orchestratorUrl", orchestratorUrl);

		when(votingCardSetRepository.list(anyMap())).thenReturn(VCS_LIST);

		when(returnCodeGenerationRequestPayloadRepository.getCount(ELECTION_EVENT_ID, "123")).thenReturn(1);

		final String responseJson = "{\"verificationCardSetId\":\"123\",\"status\":\"COMPUTED\"}";
		when(choiceCodesComputationService.executeCall(any()))
				.thenReturn(Response.success(ResponseBody.create(MediaType.get("application/json"), responseJson)));

		doCallRealMethod().when(choiceCodesComputationService).updateChoiceCodesComputationStatus(anyString());

		assertAll(() -> assertDoesNotThrow(() -> choiceCodesComputationService.updateChoiceCodesComputationStatus(ELECTION_EVENT_ID)),
				() -> verify(votingCardSetRepository).update(responseJson));
	}
}
