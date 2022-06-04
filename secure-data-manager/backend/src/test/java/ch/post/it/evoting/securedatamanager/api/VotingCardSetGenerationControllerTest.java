/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.util.UriComponentsBuilder;

import ch.post.it.evoting.securedatamanager.services.application.exception.ResourceNotFoundException;
import ch.post.it.evoting.securedatamanager.services.application.service.VotingCardSetGenerateService;
import ch.post.it.evoting.securedatamanager.services.domain.model.generator.DataGeneratorResponse;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.InvalidStatusTransitionException;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration
class VotingCardSetGenerationControllerTest {

	private final String VALID_ELECTION_EVENT_ID = "17ccbe962cf341bc93208c26e911090c";
	private final String VALID_VOTING_CARD_SET_ID = "17ccbe962cf341bc93208c26e911090c";

	@Mock
	VotingCardSetGenerateService votingCardSetGenerateService;

	@InjectMocks
	VotingCardSetGenerationController votingCardSetGenerationController;

	@Test
	void testGenerate() throws ResourceNotFoundException, InvalidStatusTransitionException {
		final DataGeneratorResponse dataGeneratorResponse = mock(DataGeneratorResponse.class);
		when(votingCardSetGenerateService.generate(any(), any())).thenReturn(dataGeneratorResponse);
		when(dataGeneratorResponse.isSuccessful()).thenReturn(true);

		final UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("");
		final ResponseEntity<?> response = votingCardSetGenerationController.generateVotingCardSet(VALID_ELECTION_EVENT_ID, VALID_VOTING_CARD_SET_ID,
				uriBuilder);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
	}

}
