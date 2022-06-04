/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.api;

import static ch.post.it.evoting.cryptolib.commons.validations.Validate.validateUUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.securedatamanager.services.application.exception.ResourceNotFoundException;
import ch.post.it.evoting.securedatamanager.services.application.service.BallotService;
import ch.post.it.evoting.securedatamanager.services.domain.model.ballot.BallotSignInputData;
import ch.post.it.evoting.securedatamanager.services.infrastructure.ballot.BallotRepository;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * Endpoint for managing ballot information.
 */
@RestController
@RequestMapping("/sdm-backend/ballots")
@Api(value = "Ballot REST API")
public class BallotController {

	private static final Logger LOGGER = LoggerFactory.getLogger(BallotController.class);

	@Autowired
	private BallotRepository ballotRepository;

	@Autowired
	private BallotService ballotService;

	@Value("${tenantID}")
	private String tenantId;

	/**
	 * Returns a list of ballots identified by an election event identifier.
	 *
	 * @param electionEventId the election event id.
	 * @return An election event identified by its id.
	 */
	@GetMapping(value = "/electionevent/{electionEventId}", produces = "application/json")
	@ResponseBody
	@ApiOperation(value = "List of ballots", notes = "Service to retrieve the list of ballots.", response = String.class)
	public String getBallots(
			@ApiParam(value = "String", required = true)
			@PathVariable
			final
			String electionEventId) {

		validateUUID(electionEventId);

		return ballotRepository.listByElectionEvent(electionEventId);
	}

	/**
	 * Change the state of the ballot from locked to signed for a given election event and ballot id.
	 *
	 * @param electionEventId the election event id.
	 * @param ballotId        the ballot id.
	 * @return HTTP status code 200 - If the ballot is successfully signed. HTTP status code 404 - If the resource is not found. HTTP status code 412
	 * - If the ballot is already signed.
	 */
	@PutMapping(value = "/electionevent/{electionEventId}/ballot/{ballotId}")
	@ApiOperation(value = "Sign ballot", notes = "Service to change the state of the ballot from locked to signed "
			+ "for a given election event and ballot id..", response = Void.class)
	@ApiResponses(value = { @ApiResponse(code = 404, message = "Not Found"), @ApiResponse(code = 412, message = "Precondition Failed") })
	public ResponseEntity<Void> signBallot(
			@ApiParam(value = "String", required = true)
			@PathVariable
			final
			String electionEventId,
			@ApiParam(value = "String", required = true)
			@PathVariable
			final
			String ballotId,
			@ApiParam(value = "BallotSignInputData", required = true)
			@RequestBody
			final BallotSignInputData input) {

		validateUUID(electionEventId);
		validateUUID(ballotId);

		try {

			ballotService.sign(electionEventId, ballotId, input.getPrivateKeyPEM());

		} catch (final ResourceNotFoundException e) {
			LOGGER.error("An error occurred while fetching the ballot to sign", e);
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} catch (final GeneralCryptoLibException e) {
			LOGGER.error("An error occurred while signing the ballot", e);
			return new ResponseEntity<>(HttpStatus.PRECONDITION_FAILED);
		}

		return new ResponseEntity<>(HttpStatus.OK);
	}
}
