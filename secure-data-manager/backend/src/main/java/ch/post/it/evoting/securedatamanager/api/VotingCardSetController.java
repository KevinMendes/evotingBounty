/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.api;

import static ch.post.it.evoting.cryptolib.commons.validations.Validate.validateUUID;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.json.JsonObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import ch.post.it.evoting.domain.election.model.messaging.PayloadSignatureException;
import ch.post.it.evoting.domain.election.model.messaging.PayloadVerificationException;
import ch.post.it.evoting.securedatamanager.config.commons.utils.JsonUtils;
import ch.post.it.evoting.securedatamanager.services.application.exception.ResourceNotFoundException;
import ch.post.it.evoting.securedatamanager.services.application.service.IdleStatusService;
import ch.post.it.evoting.securedatamanager.services.application.service.VotingCardSetComputationService;
import ch.post.it.evoting.securedatamanager.services.application.service.VotingCardSetDownloadService;
import ch.post.it.evoting.securedatamanager.services.application.service.VotingCardSetPrecomputationService;
import ch.post.it.evoting.securedatamanager.services.application.service.VotingCardSetPreparationService;
import ch.post.it.evoting.securedatamanager.services.application.service.VotingCardSetSignService;
import ch.post.it.evoting.securedatamanager.services.domain.model.status.InvalidStatusTransitionException;
import ch.post.it.evoting.securedatamanager.services.domain.model.votingcardset.VotingCardSetUpdateInputData;
import ch.post.it.evoting.securedatamanager.services.infrastructure.JsonConstants;
import ch.post.it.evoting.securedatamanager.services.infrastructure.cc.PayloadStorageException;
import ch.post.it.evoting.securedatamanager.services.infrastructure.votingcardset.VotingCardSetRepository;

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
public class VotingCardSetController {

	private static final Logger LOGGER = LoggerFactory.getLogger(VotingCardSetController.class);

	private static final String VOTING_CARD_SET_URL_PATH = "/electionevent/{electionEventId}/votingcardset/{votingCardSetId}";

	private final IdleStatusService idleStatusService;
	private final VotingCardSetRepository votingCardSetRepository;
	private final VotingCardSetPrecomputationService votingCardSetPrecomputationService;
	private final VotingCardSetSignService votingCardSetSignService;
	private final VotingCardSetDownloadService votingCardSetDownloadService;
	private final VotingCardSetComputationService votingCardSetComputationService;
	private final VotingCardSetPreparationService votingCardSetPreparationService;

	public VotingCardSetController(final IdleStatusService idleStatusService,
			final VotingCardSetRepository votingCardSetRepository,
			final VotingCardSetPrecomputationService votingCardSetPrecomputationService,
			final VotingCardSetSignService votingCardSetSignService,
			final VotingCardSetDownloadService votingCardSetDownloadService,
			final VotingCardSetComputationService votingCardSetComputationService,
			final VotingCardSetPreparationService votingCardSetPreparationService) {
		this.idleStatusService = idleStatusService;
		this.votingCardSetRepository = votingCardSetRepository;
		this.votingCardSetPrecomputationService = votingCardSetPrecomputationService;
		this.votingCardSetSignService = votingCardSetSignService;
		this.votingCardSetDownloadService = votingCardSetDownloadService;
		this.votingCardSetComputationService = votingCardSetComputationService;
		this.votingCardSetPreparationService = votingCardSetPreparationService;
	}

	/**
	 * Changes the status of a list of voting card sets by performing the appropriate operations. The HTTP call uses a PUT request to the voting card
	 * set endpoint with the desired status parameter. If the requested status cannot be transitioned to from the current one, the call will fail.
	 *
	 * @param electionEventId the election event id.
	 * @param votingCardSetId the voting card set id.
	 * @return a list of ids of the created voting card sets.
	 * @throws PayloadStorageException
	 * @throws PayloadSignatureException
	 * @throws PayloadVerificationException
	 */
	@PutMapping(value = VOTING_CARD_SET_URL_PATH, consumes = "application/json", produces = "application/json")
	@ApiOperation(value = "Change the status of a voting card set", notes = "Change the status of a voting card set, performing the necessary operations for the transition")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "Not Found") })
	public ResponseEntity<Object> setVotingCardSetStatus(
			@ApiParam(value = "String", required = true)
			@PathVariable
			final String electionEventId,
			@ApiParam(value = "String", required = true)
			@PathVariable
			final String votingCardSetId,
			@ApiParam(required = true)
			@RequestBody
			final VotingCardSetUpdateInputData requestBody)
			throws ResourceNotFoundException, IOException, GeneralCryptoLibException, PayloadStorageException, PayloadSignatureException,
			PayloadVerificationException {

		validateUUID(electionEventId);
		validateUUID(votingCardSetId);

		ResponseEntity<Object> response;
		try {
			switch (requestBody.getStatus()) {
			case PRECOMPUTED:
				votingCardSetPreparationService.prepare(electionEventId, votingCardSetId, requestBody.getPrivateKeyPEM());
				votingCardSetPrecomputationService
						.precompute(votingCardSetId, electionEventId, requestBody.getPrivateKeyPEM(), requestBody.getAdminBoardId());
				response = new ResponseEntity<>(HttpStatus.NO_CONTENT);
				break;
			case COMPUTING:
				votingCardSetComputationService.compute(votingCardSetId, electionEventId);
				response = new ResponseEntity<>(HttpStatus.NO_CONTENT);
				break;
			case VCS_DOWNLOADED:
				votingCardSetDownloadService.download(votingCardSetId, electionEventId);
				response = new ResponseEntity<>(HttpStatus.NO_CONTENT);
				break;
			case SIGNED:
				response = new ResponseEntity<>(signVotingCardSet(electionEventId, votingCardSetId, requestBody.getPrivateKeyPEM()));
				break;
			default:
				response = new ResponseEntity<>("Status is not supported", HttpStatus.BAD_REQUEST);
			}

		} catch (final InvalidStatusTransitionException e) {
			LOGGER.info("Error trying to set voting card set status.", e);
			response = ResponseEntity.badRequest().build();
		}
		return response;
	}

	/**
	 * Returns an voting card set identified by election event and its id.
	 *
	 * @param electionEventId the election event id.
	 * @param votingCardSetId the voting card set id.
	 * @return An voting card set identified by election event and its id.
	 */
	@GetMapping(value = VOTING_CARD_SET_URL_PATH, produces = "application/json")
	@ResponseBody
	@ApiOperation(value = "Get voting card set", notes = "Service to retrieve a given voting card set.", response = String.class)
	@ApiResponses(value = { @ApiResponse(code = 404, message = "Not Found") })
	public ResponseEntity<String> getVotingCardSet(
			@ApiParam(value = "String", required = true)
			@PathVariable
			final String electionEventId,
			@ApiParam(value = "String", required = true)
			@PathVariable
			final String votingCardSetId) {

		validateUUID(electionEventId);
		validateUUID(votingCardSetId);

		final Map<String, Object> attributeValueMap = new HashMap<>();
		attributeValueMap.put(JsonConstants.ID, votingCardSetId);
		attributeValueMap.put(JsonConstants.ELECTION_EVENT_DOT_ID, electionEventId);
		final String result = votingCardSetRepository.find(attributeValueMap);
		final JsonObject jsonObject = JsonUtils.getJsonObject(result);
		if (!jsonObject.isEmpty()) {
			return new ResponseEntity<>(result, HttpStatus.OK);
		}
		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	/**
	 * Returns a list of all voting card sets.
	 *
	 * @param electionEventId the election event id.
	 * @return The list of voting card sets.
	 */
	@GetMapping(value = "/electionevent/{electionEventId}", produces = "application/json")
	@ResponseBody
	@ApiOperation(value = "List voting card sets", notes = "Service to retrieve the list of voting card sets.", response = String.class)
	public String getVotingCardSets(
			@PathVariable
			final String electionEventId) {

		validateUUID(electionEventId);

		final Map<String, Object> attributeValueMap = new HashMap<>();
		attributeValueMap.put(JsonConstants.ELECTION_EVENT_DOT_ID, electionEventId);
		return votingCardSetRepository.list(attributeValueMap);
	}

	/**
	 * Change the state of the voting card set from generated to signed for a given election event and voting card set id.
	 *
	 * @param electionEventId the election event id.
	 * @param votingCardSetId the voting card set id.
	 * @return HTTP status code 200 - If the voting card set is successfully signed. HTTP status code 404 - If the resource is not found. HTTP status
	 * code 412 - If the votig card set is already signed.
	 */
	private HttpStatus signVotingCardSet(final String electionEventId, final String votingCardSetId, final String privateKeyPEM) {

		if (!idleStatusService.getIdLock(votingCardSetId)) {
			return HttpStatus.OK;
		}

		final String fetchingErrorMessage = "An error occurred while fetching the given voting card set to sign";
		final String signingErrorMessage = "An error occurred while signing the given voting card set";
		try {
			if (votingCardSetSignService.sign(electionEventId, votingCardSetId, privateKeyPEM)) {
				return HttpStatus.OK;
			} else {
				LOGGER.error(fetchingErrorMessage);

				return HttpStatus.PRECONDITION_FAILED;
			}
		} catch (final ResourceNotFoundException e) {
			LOGGER.error(fetchingErrorMessage, e);
			return HttpStatus.NOT_FOUND;
		} catch (final GeneralCryptoLibException | IOException e) {
			LOGGER.error(signingErrorMessage, e);

			return HttpStatus.PRECONDITION_FAILED;
		} finally {
			idleStatusService.freeIdLock(votingCardSetId);
		}
	}

}
