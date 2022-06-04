/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.voteverification.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.exceptions.FailedValidationException;
import ch.post.it.evoting.cryptoprimitives.securitylevel.SecurityLevel;
import ch.post.it.evoting.cryptoprimitives.securitylevel.SecurityLevelConfig;
import ch.post.it.evoting.domain.configuration.ElectionContextResponsePayload;
import ch.post.it.evoting.domain.configuration.ElectionEventContextPayload;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.voteverification.domain.model.electioncontext.ElectionEventContextEntity;
import ch.post.it.evoting.votingserver.voteverification.domain.model.electioncontext.ElectionEventContextRepository;
import ch.post.it.evoting.votingserver.voteverification.infrastructure.remote.MessageBrokerOrchestratorClient;

import retrofit2.Call;
import retrofit2.Response;

@DisplayName("ElectionEventContextServiceTest")
class ElectionEventContextServiceTest {

	private static final ObjectMapper objectMapper = mock(ObjectMapper.class);
	private static final ElectionEventContextRepository electionEventContextRepository = mock(ElectionEventContextRepository.class);
	private static final MessageBrokerOrchestratorClient messageBrokerOrchestratorClient = mock(MessageBrokerOrchestratorClient.class);

	private static ElectionEventContextService electionEventContextService;
	private static ElectionEventContextPayload electionEventContextPayload;
	private static List<ElectionContextResponsePayload> electionContextResponsePayloads;
	private static String electionEventId;

	@BeforeAll
	static void setUpAll() throws IOException {
		final ObjectMapper mapper = DomainObjectMapper.getNewInstance();
		final URL electionContextPayloadUrl = ElectionEventContextServiceTest.class.getResource(
				"/electionEventContextServiceTest/election-event-context-payload.json");
		final URL electionContextResponsePayloadUrl = ElectionEventContextServiceTest.class.getResource(
				"/electionEventContextServiceTest/election-context-response-payloads.json");
		try (MockedStatic<SecurityLevelConfig> mockedSecurityLevel = mockStatic(SecurityLevelConfig.class)) {
			mockedSecurityLevel.when(SecurityLevelConfig::getSystemSecurityLevel).thenReturn(SecurityLevel.TESTING_ONLY);
			electionEventContextPayload = mapper.readValue(electionContextPayloadUrl, ElectionEventContextPayload.class);
			electionContextResponsePayloads = Arrays.asList(
					mapper.readValue(electionContextResponsePayloadUrl, ElectionContextResponsePayload[].class));
		}

		electionEventId = electionEventContextPayload.getElectionEventContext().getElectionEventId();
		electionEventContextService = new ElectionEventContextService(objectMapper, electionEventContextRepository,
				messageBrokerOrchestratorClient);
	}

	@Test
	@DisplayName("Saving with null parameters throws NullPointerException")
	void savingNullThrows() {
		assertThrows(NullPointerException.class, () -> electionEventContextService.saveElectionEventContext(null));
	}

	@Test
	@DisplayName("Saving an already saved election event context throws DuplicateEntryException")
	void savingDuplicateThrows() throws DuplicateEntryException {
		when(electionEventContextRepository.save(any())).thenThrow(DuplicateEntryException.class);
		assertThrows(DuplicateEntryException.class, () -> electionEventContextService.saveElectionEventContext(electionEventContextPayload));
	}

	@Test
	@DisplayName("Failed serialization of an election event context throws UncheckedIOException")
	void savingElectionEventContextFailedToSerializeThrows() throws JsonProcessingException {
		when(objectMapper.writeValueAsBytes(any())).thenThrow(JsonProcessingException.class);
		final UncheckedIOException exception = assertThrows(UncheckedIOException.class,
				() -> electionEventContextService.saveElectionEventContext(electionEventContextPayload));
		assertEquals("Failed to serialize election event context.", exception.getMessage());
	}

	@Test
	@DisplayName("Saving a valid election event context does not throw")
	void savingValidElectionEventContextDoesNotThrow() throws IOException, DuplicateEntryException {
		final Response<List<ElectionContextResponsePayload>> electionContextResponsePayloadsSuccess = Response.success(
				electionContextResponsePayloads);
		final Call<List<ElectionContextResponsePayload>> callMockElectionContextResponsePayloads = (Call<List<ElectionContextResponsePayload>>) mock(
				Call.class);
		when(callMockElectionContextResponsePayloads.execute()).thenReturn(electionContextResponsePayloadsSuccess);
		when(messageBrokerOrchestratorClient.saveElectionEventContext(any(), any())).thenReturn(callMockElectionContextResponsePayloads);
		when(objectMapper.writeValueAsBytes(any())).thenReturn(electionEventId.getBytes(StandardCharsets.UTF_8));

		final List<ElectionContextResponsePayload> result = assertDoesNotThrow(
				() -> electionEventContextService.saveElectionEventContext(electionEventContextPayload));

		verify(electionEventContextRepository, times(1)).save(any());
		verify(messageBrokerOrchestratorClient, times(1)).saveElectionEventContext(any(), any());
		assertEquals(electionContextResponsePayloads, result);
	}

	@Test
	@DisplayName("Retrieving an election event context with an invalid electionEventId throws")
	void retrievingInvalidElectionEventIdThrows() {
		assertThrows(NullPointerException.class, () -> electionEventContextService.retrieveElectionEventContext(null));
		assertThrows(FailedValidationException.class, () -> electionEventContextService.retrieveElectionEventContext("123"));
	}

	@Test
	@DisplayName("Retrieving not saved election event context throws")
	void retrievingNotSavedElectionEventContextThrows() throws ResourceNotFoundException {
		when(electionEventContextRepository.findByElectionEventId(any())).thenThrow(ResourceNotFoundException.class);
		assertThrows(ResourceNotFoundException.class, () -> electionEventContextService.retrieveElectionEventContext(electionEventId));
	}

	@Test
	@DisplayName("Retrieving a saved election event context does not throw")
	void retrievingSavedElectionEventContextDoesNotThrow() throws ResourceNotFoundException {
		final ElectionEventContextEntity electionEventContextEntity = new ElectionEventContextEntity();
		electionEventContextEntity.setElectionEventId(electionEventId);
		when(electionEventContextRepository.findByElectionEventId(electionEventId)).thenReturn(electionEventContextEntity);

		final ElectionEventContextEntity result = assertDoesNotThrow(() -> electionEventContextService.retrieveElectionEventContext(electionEventId));

		verify(electionEventContextRepository, times(1)).findByElectionEventId(any());
		assertEquals(electionEventContextEntity, result);
	}
}