/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.service;

import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.sun.net.httpserver.HttpServer;

import ch.post.it.evoting.cryptolib.asymmetric.service.AsymmetricService;
import ch.post.it.evoting.securedatamanager.services.infrastructure.ballotbox.BallotBoxRepository;

class BallotBoxMixingServiceTest {

	private static final String ELECTION_EVENT_ID = "0b149cfdaad04b04b990c3b1d4ca7639";
	private static final String BB_LIST = "{\"result\":[{\"id\":\"123\", \"status\":\"SIGNED\"}]}";
	private static final int FREE_PORT_LOOKUP_LOWER_BOUND = 50000;
	private static final int FREE_PORT_LOOKUP_UPPER_BOUND = 60000;
	private static final String TENANT_ID = "100";

	private static int httpPort;
	private static BallotBoxMixingService ballotBoxMixingService;
	private static BallotBoxRepository ballotBoxRepositoryMock;
	private static KeyStoreService keyStoreServiceMock;

	@BeforeAll
	static void setUpAll() {
		setUpHttpServer();

		ballotBoxRepositoryMock = Mockito.mock(BallotBoxRepository.class);
		keyStoreServiceMock = Mockito.mock(KeyStoreService.class);
		final String orchestratorUrl = String.format("http://localhost:%s/ag-ws-rest/or", httpPort);

		ballotBoxMixingService = new BallotBoxMixingService(ballotBoxRepositoryMock, keyStoreServiceMock, TENANT_ID, orchestratorUrl);
	}

	private static void setUpHttpServer() {
		try {
			setPorts();

			// setup the socket address
			final InetSocketAddress address = new InetSocketAddress(httpPort);

			// initialise the HTTP server
			final HttpServer httpServer = HttpServer.create(address, 0);
			httpServer.createContext("/", new BallotBoxServiceTest.MyHttpHandler());
			httpServer.setExecutor(null); // creates a default executor
			httpServer.start();

		} catch (final Exception e) {
			System.out.println("Failed to create HTTP server on port 8080 of localhost");
			e.printStackTrace();

		}
	}

	private static void setPorts() throws IOException {
		httpPort = discoverFreePorts();
	}

	private static int discoverFreePorts() throws IOException {
		int result = 0;
		ServerSocket tempServer = null;

		for (int i = FREE_PORT_LOOKUP_LOWER_BOUND; i <= FREE_PORT_LOOKUP_UPPER_BOUND; i++) {
			try {
				tempServer = new ServerSocket(i);
				result = tempServer.getLocalPort();
				break;

			} catch (final IOException ex) {
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
	void updateMixingStatus() throws IOException {
		when(keyStoreServiceMock.getPrivateKey()).thenReturn(new AsymmetricService().getKeyPairForSigning().getPrivate());
		when(ballotBoxRepositoryMock.list(anyMap())).thenReturn(BB_LIST);

		ballotBoxMixingService.updateBallotBoxesMixingStatus(ELECTION_EVENT_ID);

		verify(ballotBoxRepositoryMock).update("{\"id\":\"123\",\"status\":\"MIXED\"}");
	}

}
