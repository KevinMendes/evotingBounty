/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.api;

import static ch.post.it.evoting.cryptolib.commons.validations.Validate.validateUUID;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import ch.post.it.evoting.securedatamanager.services.application.exception.ResourceNotFoundException;
import ch.post.it.evoting.securedatamanager.services.application.service.VotingCardSetGenerateService;
import ch.post.it.evoting.securedatamanager.services.domain.model.generator.DataGeneratorResponse;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.InvalidStatusTransitionException;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * The voting card set end-point.
 */
@RestController
@RequestMapping("/sdm-backend/votingcardsets")
@Api(value = "Voting card set REST API")
public class VotingCardSetGenerationController {

	private static final Logger LOGGER = LoggerFactory.getLogger(VotingCardSetGenerationController.class);

	private static final String VOTING_CARD_SET_URL_PATH = "/electionevent/{electionEventId}/votingcardset/{votingCardSetId}";

	private final VotingCardSetGenerateService votingCardSetGenerateService;

	public VotingCardSetGenerationController(final VotingCardSetGenerateService votingCardSetGenerateService) {
		this.votingCardSetGenerateService = votingCardSetGenerateService;
	}

	/**
	 * Stores a list of voting card sets.
	 *
	 * @param electionEventId the election event id.
	 * @param votingCardSetId the voting card set id.
	 * @return a list of ids of the created voting card sets.
	 * @throws InvalidStatusTransitionException
	 */
	@PostMapping(value = VOTING_CARD_SET_URL_PATH, produces = "application/json")
	@ApiOperation(value = "Create voting card set", notes = "Service to create a voting card set.")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "Not Found") })
	public ResponseEntity<Object> generateVotingCardSet(
			@ApiParam(value = "String", required = true)
			@PathVariable
			final String electionEventId,
			@ApiParam(value = "String", required = true)
			@PathVariable
			final String votingCardSetId,
			final UriComponentsBuilder uriBuilder)
			throws ResourceNotFoundException, InvalidStatusTransitionException {

		validateUUID(electionEventId);
		validateUUID(votingCardSetId);

		LOGGER.info("Generating the voting cards. [electionEventId: {}, votingCardSetId: {}]", electionEventId, votingCardSetId);

		final DataGeneratorResponse response = votingCardSetGenerateService.generate(electionEventId, votingCardSetId);

		if (response.isSuccessful()) {
			LOGGER.info("Voting cards successfully generated. [electionEventId: {}, votingCardSetId: {}]", electionEventId, votingCardSetId);
			final URI uri = uriBuilder.path("/electionevent/{electionEventId}/progress/{jobId}")
					.buildAndExpand(electionEventId, votingCardSetId, response.getResult()).toUri();
			return ResponseEntity.created(uri).body(response);
		}

		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
	}

}
