/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.votingserver.voteverification.ws.application.operation;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.cryptoprimitives.securitylevel.SecurityLevel;
import ch.post.it.evoting.cryptoprimitives.securitylevel.SecurityLevelConfig;
import ch.post.it.evoting.domain.configuration.ElectionContextResponsePayload;
import ch.post.it.evoting.domain.configuration.ElectionEventContextPayload;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.voteverification.domain.model.electioncontext.ElectionEventContextEntity;
import ch.post.it.evoting.votingserver.voteverification.service.ElectionEventContextService;

@DisplayName("ElectionEventContextResource")
@ExtendWith(MockitoExtension.class)
class ElectionEventContextResourceTest {

	private static ElectionEventContextPayload electionEventContextPayload;
	private static List<ElectionContextResponsePayload> electionContextResponsePayloads;
	private static String electionEventId;
	private final String trackingId = new TrackIdInstance().getTrackId();

	@InjectMocks
	ElectionEventContextResource electionEventContextResource;

	@Mock
	private HttpServletRequest mockServletRequest;

	@Mock
	private ElectionEventContextService electionEventContextService;

	@Mock
	private TrackIdInstance tackIdInstance;

	@Mock
	private ObjectMapper objectMapper;

	@BeforeAll
	static void setUpAll() throws IOException {
		final ObjectMapper mapper = DomainObjectMapper.getNewInstance();
		final URL electionContextPayloadUrl = ElectionEventContextResourceTest.class.getResource(
				"/electionEventContextServiceTest/election-event-context-payload.json");
		final URL electionContextResponsePayloadUrl = ElectionEventContextResourceTest.class.getResource(
				"/electionEventContextServiceTest/election-context-response-payloads.json");
		try (final MockedStatic<SecurityLevelConfig> mockedSecurityLevel = mockStatic(SecurityLevelConfig.class)) {
			mockedSecurityLevel.when(SecurityLevelConfig::getSystemSecurityLevel).thenReturn(SecurityLevel.TESTING_ONLY);
			electionEventContextPayload = mapper.readValue(electionContextPayloadUrl, ElectionEventContextPayload.class);
			electionContextResponsePayloads = Arrays.asList(
					mapper.readValue(electionContextResponsePayloadUrl, ElectionContextResponsePayload[].class));
		}

		electionEventId = electionEventContextPayload.getElectionEventContext().getElectionEventId();
	}

	@Test
	@DisplayName("save election event context with valid parameters")
	void saveElectionEventContextHappyPath() throws ResourceNotFoundException, IOException, DuplicateEntryException {

		when(electionEventContextService.saveElectionEventContext(electionEventContextPayload)).thenReturn(electionContextResponsePayloads);

		final Response response = electionEventContextResource.saveElectionEventContext(trackingId, electionEventContextPayload, mockServletRequest);

		verify(electionEventContextService, times(1)).saveElectionEventContext(any());

		assertNotNull(response);
		assertEquals(electionContextResponsePayloads, response.getEntity());
		assertEquals(200, response.getStatus());
	}

	@Test
	@DisplayName("retrieve election event context with valid parameters")
	void retrieveElectionEventContextHappyPath() throws ResourceNotFoundException, ApplicationException, IOException {

		final ElectionEventContextEntity electionEventContextEntity = new ElectionEventContextEntity();
		electionEventContextEntity.setElectionEventId(electionEventId);
		final String expected = DomainObjectMapper.getNewInstance().writeValueAsString(electionEventContextEntity);

		when(electionEventContextService.retrieveElectionEventContext(electionEventId)).thenReturn(electionEventContextEntity);
		when(objectMapper.writeValueAsString(electionEventContextEntity)).thenReturn(expected);

		final Response response = electionEventContextResource.retrieveElectionEventContext(trackingId, electionEventId, mockServletRequest);

		verify(electionEventContextService, times(1)).retrieveElectionEventContext(any());

		assertNotNull(response);
		assertEquals(expected, response.getEntity());
		assertEquals(200, response.getStatus());
	}

	@Test
	@DisplayName("retrieve election event context with invalid parameters throws")
	void retrieveElectionEventContextInvalidParametersThrows() {

		assertAll(
				() -> assertThrows(ApplicationException.class,
						() -> electionEventContextResource.retrieveElectionEventContext(trackingId, null, mockServletRequest)),
				() -> assertThrows(ApplicationException.class,
						() -> electionEventContextResource.retrieveElectionEventContext(trackingId, "invalid electionEventId", mockServletRequest))
		);
	}
}
