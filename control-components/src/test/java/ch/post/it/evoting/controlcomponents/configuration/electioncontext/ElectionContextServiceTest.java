/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.controlcomponents.configuration.electioncontext;

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
import java.util.Optional;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Throwables;

import ch.post.it.evoting.controlcomponents.ElectionEventEntity;
import ch.post.it.evoting.controlcomponents.ElectionEventService;
import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.cryptoprimitives.securitylevel.SecurityLevel;
import ch.post.it.evoting.cryptoprimitives.securitylevel.SecurityLevelConfig;
import ch.post.it.evoting.domain.configuration.ElectionEventContext;
import ch.post.it.evoting.domain.configuration.ElectionEventContextPayload;

@DisplayName("ElectionContextServiceTest")
class ElectionContextServiceTest {

	private static final ObjectMapper objectMapper = mock(ObjectMapper.class);
	private static final ElectionContextRepository electionContextRepository = mock(ElectionContextRepository.class);
	private static final ElectionEventService electionEventService = mock(ElectionEventService.class);

	private static ElectionContextService electionContextService;
	private static ElectionEventContext electionEventContext;
	private static ElectionContextEntity electionContextEntity;
	private static String electionEventId;

	@BeforeAll
	static void setUpAll() throws IOException {
		final ObjectMapper mapper = DomainObjectMapper.getNewInstance();
		final URL electionContextPayloadUrl = ElectionContextServiceTest.class.getResource(
				"/configuration/electioncontext/election-event-context-payload.json");
		try (MockedStatic<SecurityLevelConfig> mockedSecurityLevel = mockStatic(SecurityLevelConfig.class)) {
			mockedSecurityLevel.when(SecurityLevelConfig::getSystemSecurityLevel).thenReturn(SecurityLevel.TESTING_ONLY);
			final ElectionEventContextPayload electionEventContextPayload = mapper.readValue(electionContextPayloadUrl,
					ElectionEventContextPayload.class);
			electionEventContext = electionEventContextPayload.getElectionEventContext();
		}
		electionEventId = electionEventContext.getElectionEventId();
		final ElectionEventEntity electionEventEntity = new ElectionEventEntity();

		electionContextEntity = new ElectionContextEntity.Builder()
				.setElectionEventEntity(electionEventEntity)
				.setCombinedControlComponentPublicKey(electionEventId.getBytes(StandardCharsets.UTF_8))
				.setElectoralBoardPublicKey(electionEventId.getBytes(StandardCharsets.UTF_8))
				.setElectionPublicKey(electionEventId.getBytes(StandardCharsets.UTF_8))
				.setChoiceReturnCodesEncryptionPublicKey(electionEventId.getBytes(StandardCharsets.UTF_8))
				.setStartTime(electionEventContext.getStartTime())
				.setFinishTime(electionEventContext.getFinishTime()).build();

		when(electionContextRepository.save(any())).thenReturn(new ElectionContextEntity());
		when(electionContextRepository.findByElectionEventId(electionEventContext.getElectionEventId())).thenReturn(
				Optional.ofNullable(electionContextEntity));
		when(electionEventService.getElectionEventEntity(electionEventId)).thenReturn(electionEventEntity);

		electionContextService = new ElectionContextService(objectMapper, electionEventService, electionContextRepository);
	}

	@Test
	@DisplayName("saving with failed serialization of ElectionEventContext throws UncheckedIOException")
	void failedToSerializeElectionEventContextThrows() throws JsonProcessingException {
		when(objectMapper.writeValueAsBytes(any())).thenThrow(JsonProcessingException.class);
		final UncheckedIOException exception = assertThrows(UncheckedIOException.class, () -> electionContextService.save(electionEventContext));
		assertEquals("Failed to serialize election event context.", exception.getMessage());
	}

	@Test
	@DisplayName("loading non existing ElectionEventContext throws IllegalStateException")
	void loadNonExisting() {
		final String nonExistingElectionId = "e77dbe3c70874ea584c490a0c6ac0ca4";
		final IllegalStateException exception = assertThrows(IllegalStateException.class,
				() -> electionContextService.getElectionContextEntity(nonExistingElectionId));
		assertEquals(String.format("Election context entity not found. [electionEventId: %s]", nonExistingElectionId),
				Throwables.getRootCause(exception).getMessage());
	}

	@Test
	@DisplayName("saving with valid ElectionEventContext does not throw")
	void validElectionEventContextDoesNotThrow() throws JsonProcessingException {
		when(objectMapper.writeValueAsBytes(any())).thenReturn(electionEventId.getBytes(StandardCharsets.UTF_8));
		electionContextService.save(electionEventContext);
		verify(electionContextRepository, times(1)).save(any());
		assertEquals(electionContextEntity, electionContextService.getElectionContextEntity(electionEventId));
	}
}