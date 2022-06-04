/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.api;

import static ch.post.it.evoting.cryptolib.commons.validations.Validate.validateUUID;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import ch.post.it.evoting.cryptolib.api.exceptions.GeneralCryptoLibException;
import ch.post.it.evoting.securedatamanager.configuration.ElectoralBoardService;
import ch.post.it.evoting.securedatamanager.services.application.exception.ResourceNotFoundException;
import ch.post.it.evoting.securedatamanager.services.domain.model.electoralauthority.ElectoralAuthoritySignInputData;
import ch.post.it.evoting.securedatamanager.services.infrastructure.electoralauthority.ElectoralAuthorityRepository;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * The REST endpoint for accessing electoral authority data.
 */
@RestController
@RequestMapping("/sdm-backend/electoralauthorities")
@Api(value = "Electoral authorities REST API")
public class ElectoralAuthorityController {
	private static final Logger LOGGER = LoggerFactory.getLogger(ElectoralAuthorityController.class);

	private final ElectoralAuthorityRepository electoralAuthorityRepository;
	private final ElectoralBoardService electoralBoardService;

	public ElectoralAuthorityController(final ElectoralAuthorityRepository electoralAuthorityRepository,
			final ElectoralBoardService electoralBoardService) {
		this.electoralAuthorityRepository = electoralAuthorityRepository;
		this.electoralBoardService = electoralBoardService;
	}

	/**
	 * Returns a list of electoral authorities identified by an election event identifier.
	 *
	 * @param electionEventId the election event id.
	 * @return a list of electoral authorities belong to an election event.
	 */
	@GetMapping(value = "/electionevent/{electionEventId}", produces = "application/json")
	@ResponseBody
	@ApiOperation(value = "List electoral authorities", response = String.class, notes = "Service to retrieve the list "
			+ "of electoral authorities for a given election event.")
	public String getElectoralAuthorities4ElectionEventId(
			@PathVariable
			final String electionEventId) {

		validateUUID(electionEventId);

		return electoralAuthorityRepository.listByElectionEvent(electionEventId);
	}

	/**
	 * Execute the constitute action: create keypair, split private key into shares and keep them in memory.
	 *
	 * @param electionEventId      the election event id.
	 * @param electoralAuthorityId the electoral authority id.
	 */
	@PostMapping(value = "/constitute/{electionEventId}/{electoralAuthorityId}")
	@ApiOperation(value = "Constitute Service", notes = "Service to generate a key pair and splits the private key into shares.")
	public ResponseEntity<Void> constitute(
			@ApiParam(value = "String", required = true)
			@PathVariable
			final String electionEventId,
			@ApiParam(value = "String", required = true)
			@PathVariable
			final String electoralAuthorityId) {

		validateUUID(electionEventId);
		validateUUID(electoralAuthorityId);

		if (electoralBoardService.constitute(electionEventId, electoralAuthorityId)) {
			return new ResponseEntity<>(HttpStatus.OK);
		}

		return new ResponseEntity<>(HttpStatus.PRECONDITION_FAILED);
	}

	/**
	 * Change the state of the electoral authority from constituted to signed for a given election event and electoral authority id.
	 *
	 * @param electionEventId      the election event id.
	 * @param electoralAuthorityId the electoral authority id.
	 * @return HTTP status code 200 - If the electoral authority is successfully signed. HTTP status code 404 - If the resource is not found. HTTP
	 * status code 412 - If the electoral authority is already signed.
	 */
	@PutMapping(value = "/electionevent/{electionEventId}/electoralauthority/{electoralAuthorityId}")
	@ApiOperation(value = "Sign electoral authority", notes = "Service to change the state of the electoral authority from constituted to signed for a given election event and electoral authority id..")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "Not Found"), @ApiResponse(code = 412, message = "Precondition Failed") })
	public ResponseEntity<Void> signElectoralAuthority(
			@ApiParam(value = "String", required = true)
			@PathVariable
			final String electionEventId,
			@ApiParam(value = "String", required = true)
			@PathVariable
			final String electoralAuthorityId,
			@ApiParam(value = "ElectoralAuthoritySignInputData", required = true)
			@RequestBody
			final ElectoralAuthoritySignInputData inputData) {

		validateUUID(electionEventId);
		validateUUID(electoralAuthorityId);

		try {

			if (electoralBoardService.sign(electionEventId, electoralAuthorityId, inputData.getPrivateKeyPEM())) {
				return new ResponseEntity<>(HttpStatus.OK);
			} else {
				LOGGER.error("An error occurred while fetching the given electoral authority to sign");
				return new ResponseEntity<>(HttpStatus.PRECONDITION_FAILED);
			}
		} catch (final ResourceNotFoundException e) {
			LOGGER.error("An error occurred while fetching the given electoral authority to sign", e);
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		} catch (final GeneralCryptoLibException | IOException e) {
			LOGGER.error("An error occurred while signing the given electoral authority", e);
			return new ResponseEntity<>(HttpStatus.PRECONDITION_FAILED);
		}
	}
}
