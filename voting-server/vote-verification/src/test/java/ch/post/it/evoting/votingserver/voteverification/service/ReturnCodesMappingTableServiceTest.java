/*
 * (c) Copyright 2022. Swiss Post Ltd
 */
package ch.post.it.evoting.votingserver.voteverification.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.post.it.evoting.cryptoprimitives.domain.mapper.DomainObjectMapper;
import ch.post.it.evoting.cryptoprimitives.domain.mixnet.exceptions.FailedValidationException;
import ch.post.it.evoting.domain.configuration.ReturnCodesMappingTablePayload;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.DuplicateEntryException;
import ch.post.it.evoting.votingserver.commons.beans.exceptions.ResourceNotFoundException;
import ch.post.it.evoting.votingserver.voteverification.domain.model.content.ReturnCodesMappingTableEntity;
import ch.post.it.evoting.votingserver.voteverification.domain.model.content.ReturnCodesMappingTableRepository;

@DisplayName("ReturnCodesMappingTableService")
class ReturnCodesMappingTableServiceTest {

	private static final ObjectMapper objectMapper = mock(ObjectMapper.class);
	private static final ReturnCodesMappingTableRepository returnCodesMappingTableRepository = mock(ReturnCodesMappingTableRepository.class);

	private static ReturnCodesMappingTableService returnCodesMappingTableService;
	private static ReturnCodesMappingTablePayload returnCodesMappingTablePayload;
	private static String electionEventId;
	private static String verificationCardSetId;

	@BeforeAll
	static void setUpAll() throws IOException {
		final ObjectMapper mapper = DomainObjectMapper.getNewInstance();
		final URL returnCodesMappingTableUrl = ReturnCodesMappingTableServiceTest.class.getResource(
				"/returnCodesMappingTableServiceTest/returnCodesMappingTablePayload.json");
		returnCodesMappingTablePayload = mapper.readValue(returnCodesMappingTableUrl, ReturnCodesMappingTablePayload.class);

		electionEventId = returnCodesMappingTablePayload.getElectionEventId();
		verificationCardSetId = returnCodesMappingTablePayload.getVerificationCardSetId();
		returnCodesMappingTableService = new ReturnCodesMappingTableService(objectMapper, returnCodesMappingTableRepository);
	}

	@AfterEach
	void tearDown() {
		reset(objectMapper, returnCodesMappingTableRepository);
	}

	@Test
	@DisplayName("Saving with null parameters throws NullPointerException")
	void savingNullThrows() {
		assertThrows(NullPointerException.class, () -> returnCodesMappingTableService.saveReturnCodesMappingTable(null));
	}

	@Test
	@DisplayName("Saving an already saved return codes mapping table throws DuplicateEntryException")
	void savingDuplicateThrows() throws DuplicateEntryException {
		when(returnCodesMappingTableRepository.save(any())).thenThrow(DuplicateEntryException.class);
		assertThrows(DuplicateEntryException.class, () -> returnCodesMappingTableService.saveReturnCodesMappingTable(returnCodesMappingTablePayload));
	}

	@Test
	@DisplayName("Failed serialization of a return codes mapping table throws UncheckedIOException")
	void savingReturnCodesMappingTableFailedToSerializeThrows() throws JsonProcessingException {
		when(objectMapper.writeValueAsBytes(any())).thenThrow(JsonProcessingException.class);
		final UncheckedIOException exception = assertThrows(UncheckedIOException.class,
				() -> returnCodesMappingTableService.saveReturnCodesMappingTable(returnCodesMappingTablePayload));
		assertEquals("Failed to serialize return codes mapping table.", exception.getMessage());
	}

	@Test
	@DisplayName("Saving a valid return codes mapping table does not throw")
	void savingValidReturnCodesMappingTableDoesNotThrow() throws DuplicateEntryException {
		assertDoesNotThrow(() -> returnCodesMappingTableService.saveReturnCodesMappingTable(returnCodesMappingTablePayload));
		verify(returnCodesMappingTableRepository, times(1)).save(any());
	}

	@Test
	@DisplayName("Retrieving a return code mapping table with an invalid verification card set id throws")
	void retrievingInvalidVerificationCardSetThrows() {
		assertThrows(NullPointerException.class, () -> returnCodesMappingTableService.retrieveReturnCodesMappingTable(null));
		assertThrows(FailedValidationException.class, () -> returnCodesMappingTableService.retrieveReturnCodesMappingTable("123"));
	}

	@Test
	@DisplayName("Retrieving not saved return code mapping table throws")
	void retrievingNotSavedReturnCodesMappingTableThrows() {
		assertThrows(ResourceNotFoundException.class,
				() -> returnCodesMappingTableService.retrieveReturnCodesMappingTable("abcdef0123456789abcdef0123456789"));
	}

	@Test
	@DisplayName("Retrieving a saved return code mapping table does not throw")
	void retrievingSavedReturnCodesMappingTableDoesNotThrow() {
		final ReturnCodesMappingTableEntity returnCodesMappingTableEntity = new ReturnCodesMappingTableEntity();
		returnCodesMappingTableEntity.setElectionEventId(electionEventId);
		returnCodesMappingTableEntity.setVerificationCardSetId(verificationCardSetId);
		when(returnCodesMappingTableRepository.find(verificationCardSetId)).thenReturn(returnCodesMappingTableEntity);

		final ReturnCodesMappingTableEntity result = assertDoesNotThrow(
				() -> returnCodesMappingTableService.retrieveReturnCodesMappingTable(verificationCardSetId));

		verify(returnCodesMappingTableRepository, times(1)).find(any());
		assertEquals(returnCodesMappingTableEntity, result);
	}
}