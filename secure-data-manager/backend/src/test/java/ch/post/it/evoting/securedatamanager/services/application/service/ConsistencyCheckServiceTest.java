/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.json.JsonObject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptolib.elgamal.bean.VerifiableElGamalEncryptionParameters;
import ch.post.it.evoting.securedatamanager.services.infrastructure.JsonConstants;

@ExtendWith(MockitoExtension.class)
class ConsistencyCheckServiceTest {

	private static final String VERIFIABLE_ENCRYPTION_PARAMS_JSON = "{\"p\":121,\"q\":11,\"g\":2,\"seed\":\"MDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMDAwMA==\",\"pCounter\":0,\"qCounter\":975660}";
	private final ObjectMapper mapper = new ObjectMapper();

	@InjectMocks
	@Spy
	private final ConsistencyCheckService consistencyCheckService = new ConsistencyCheckService();

	private VerifiableElGamalEncryptionParameters params;
	private JsonObject jsonObjectMock;

	private static Path getPathOfFileInResources(final String path) throws URISyntaxException {
		final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
		final URL resource = classLoader.getResource(path);
		return Paths.get(resource.toURI());
	}

	@BeforeEach
	void setUp() throws IOException {
		jsonObjectMock = mock(JsonObject.class);
		params = mapper.readValue(VERIFIABLE_ENCRYPTION_PARAMS_JSON, VerifiableElGamalEncryptionParameters.class);
	}

	@Test
	void encryptionParamsConsistent() {
		when(jsonObjectMock.getString(JsonConstants.P)).thenReturn("121");
		when(jsonObjectMock.getString(JsonConstants.Q)).thenReturn("11");
		when(jsonObjectMock.getString(JsonConstants.G)).thenReturn("2");

		final boolean res = consistencyCheckService.encryptionParamsConsistent(jsonObjectMock, params.getGroup());

		assertTrue(res, "Encryption params aren't consistent, expected them to be");
	}

	@Test
	void encryptionParamsNotConsistent() {
		when(jsonObjectMock.getString(JsonConstants.P)).thenReturn("122");
		when(jsonObjectMock.getString(JsonConstants.Q)).thenReturn("11");
		when(jsonObjectMock.getString(JsonConstants.G)).thenReturn("2");

		final boolean res = consistencyCheckService.encryptionParamsConsistent(jsonObjectMock, params.getGroup());

		assertFalse(res, "Encryption params are consistent, expected them not to be");
	}

	@Test
	void representationsConsistent() throws URISyntaxException, IOException {
		final Path primesTestPath = getPathOfFileInResources("primes.csv");

		final String electionOption = "{ \"representation\": \"29\"}";
		final String electionOption2 = "{ \"representation\": \"31\"}";
		final String contest = "{ \"options\": [" + electionOption + "," + electionOption2 + "]}";
		final String ballotJson = "{ \"contests\": [" + contest + "]}";

		final boolean res = consistencyCheckService.representationsConsistent(ballotJson, primesTestPath);

		assertTrue(res, "Prime numbers from test ballot aren't consistent, expected them to be");
	}

	@Test
	void representationsNotConsistent() throws URISyntaxException, IOException {
		final Path primesTestPath = getPathOfFileInResources("primes.csv");

		final String electionOption = "{ \"representation\": \"30\"}";
		final String electionOption2 = "{ \"representation\": \"31\"}";
		final String contest = "{ \"options\": [" + electionOption + "," + electionOption2 + "]}";
		final String ballotJson = "{ \"contests\": [" + contest + "]}";

		final boolean res = consistencyCheckService.representationsConsistent(ballotJson, primesTestPath);

		assertFalse(res, "Prime numbers from test ballot are consistent, expecting them not to be");
	}
}
