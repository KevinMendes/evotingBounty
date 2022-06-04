/*
 * (c) Copyright 2022. Swiss Post Ltd
 */
package ch.post.it.evoting.votingserver.voteverification.ws.application.operation;

import static ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationExceptionMessages.EXCEPTION_MESSAGE_QUERY_PARAMETER_IS_NULL_OR_INVALID;
import static ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationExceptionMessages.EXCEPTION_MESSAGE_QUERY_PARAMETER_NOT_MATCH_PAYLOAD;
import static ch.post.it.evoting.votingserver.voteverification.ws.application.operation.ReturnCodesMappingTableResource.PARAMETER_VALUE_ELECTION_EVENT_ID;
import static ch.post.it.evoting.votingserver.voteverification.ws.application.operation.ReturnCodesMappingTableResource.PARAMETER_VALUE_VERIFICATION_CARD_SET_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.net.URL;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.domain.configuration.ReturnCodesMappingTablePayload;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ApplicationException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.tracking.TrackIdInstance;
import ch.post.it.evoting.votingserver.voteverification.domain.model.content.ReturnCodesMappingTableRepository;
import ch.post.it.evoting.votingserver.voteverification.service.ReturnCodesMappingTableService;

@DisplayName("ReturnCodesMappingTableResource")
@ExtendWith(MockitoExtension.class)
class ReturnCodesMappingTableResourceTest {

	private static final String NOT_MATCHING_ID = "0123456789abcdef0123456789abcdef";
	private static ReturnCodesMappingTablePayload returnCodesMappingTablePayload;
	private static String electionEventId;
	private static String verificationCardSetId;
	private final String trackingId = new TrackIdInstance().getTrackId();

	@InjectMocks
	ReturnCodesMappingTableResource returnCodesMappingTableResource;

	@Mock
	private HttpServletRequest httpServletRequest;

	@Mock
	private ReturnCodesMappingTableService returnCodesMappingTableService;

	@Mock
	private ReturnCodesMappingTableRepository returnCodesMappingTableRepository;

	@Mock
	private TrackIdInstance trackIdInstance;

	@BeforeAll
	static void setUpAll() throws IOException {
		final ObjectMapper mapper = DomainObjectMapper.getNewInstance();
		final URL returnCodesMappingTablePayloadUrl = ReturnCodesMappingTableResourceTest.class.getResource(
				"/returnCodesMappingTableResourceTest/returnCodesMappingTablePayload.json");
		returnCodesMappingTablePayload = mapper.readValue(returnCodesMappingTablePayloadUrl, ReturnCodesMappingTablePayload.class);
		electionEventId = returnCodesMappingTablePayload.getElectionEventId();
		verificationCardSetId = returnCodesMappingTablePayload.getVerificationCardSetId();
	}

	@Test
	@DisplayName("calling save with valid parameters")
	void saveReturnCodesMappingTableHappyPath() throws DuplicateEntryException, ApplicationException {
		final Response response = returnCodesMappingTableResource.saveReturnCodesMappingTable(trackingId, electionEventId, verificationCardSetId,
				returnCodesMappingTablePayload, httpServletRequest);

		verify(returnCodesMappingTableService, times(1)).saveReturnCodesMappingTable(any());
		assertNotNull(response);
		assertEquals(200, response.getStatus());
	}

	@Test
	@DisplayName("calling save on already saved entity leads to no content response")
	void saveReturnCodesMappingTableAlreadySavedNoContent() throws DuplicateEntryException, ApplicationException {
		doThrow(DuplicateEntryException.class).when(returnCodesMappingTableService).saveReturnCodesMappingTable(any());
		final Response response = returnCodesMappingTableResource.saveReturnCodesMappingTable(trackingId, electionEventId, verificationCardSetId,
				returnCodesMappingTablePayload, httpServletRequest);

		assertNotNull(response);
		assertEquals(204, response.getStatus());
	}

	static Stream<Arguments> nullOrInvalidProvider() {
		return Stream.of(
				Arguments.of(PARAMETER_VALUE_ELECTION_EVENT_ID, null, electionEventId),
				Arguments.of(PARAMETER_VALUE_ELECTION_EVENT_ID, "invalid", electionEventId),
				Arguments.of(PARAMETER_VALUE_VERIFICATION_CARD_SET_ID, null, verificationCardSetId),
				Arguments.of(PARAMETER_VALUE_VERIFICATION_CARD_SET_ID, "invalid", verificationCardSetId)
		);
	}

	@ParameterizedTest
	@MethodSource("nullOrInvalidProvider")

	@DisplayName("calling validation with null or invalid parameter ids")
	void validateNullOrInvalidParameterIds(final String field, final String pathId, final String payloadId) {
		final ApplicationException exception = assertThrows(ApplicationException.class,
				() -> returnCodesMappingTableResource.validateInput(field, pathId, payloadId));
		assertEquals(EXCEPTION_MESSAGE_QUERY_PARAMETER_IS_NULL_OR_INVALID, exception.getMessage());
	}

	static Stream<Arguments> notMatchingProvider() {
		return Stream.of(
				Arguments.of(PARAMETER_VALUE_ELECTION_EVENT_ID, NOT_MATCHING_ID, electionEventId),
				Arguments.of(PARAMETER_VALUE_VERIFICATION_CARD_SET_ID, NOT_MATCHING_ID, verificationCardSetId)
		);
	}

	@ParameterizedTest
	@MethodSource("notMatchingProvider")

	@DisplayName("calling validation with parameter ids not matching payload")
	void validateNotMatchingParameterIds(final String field, final String pathId, final String payloadId) {
		final ApplicationException exception = assertThrows(ApplicationException.class,
				() -> returnCodesMappingTableResource.validateInput(field, pathId, payloadId));
		assertEquals(EXCEPTION_MESSAGE_QUERY_PARAMETER_NOT_MATCH_PAYLOAD, exception.getMessage());
	}
}