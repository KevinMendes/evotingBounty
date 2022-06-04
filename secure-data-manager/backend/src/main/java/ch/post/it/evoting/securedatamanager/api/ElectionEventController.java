/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.api;

import static ch.post.it.evoting.cryptoprimitives.domain.validations.UUIDValidations.validateUUID;

import java.io.IOException;

import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import ch.post.it.evoting.securedatamanager.config.commons.utils.JsonUtils;
import ch.post.it.evoting.securedatamanager.services.application.exception.CCKeysAlreadyExistException;
import ch.post.it.evoting.securedatamanager.services.application.exception.CCKeysNotExistException;
import ch.post.it.evoting.securedatamanager.services.application.service.ElectionEventService;
import ch.post.it.evoting.securedatamanager.services.domain.model.generator.DataGeneratorResponse;
import ch.post.it.evoting.securedatamanager.services.infrastructure.electionevent.ElectionEventRepository;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

/**
 * The election event end-point.
 */
@RestController
@RequestMapping("/sdm-backend/electionevents")
@Api(value = "Election event REST API")
public class ElectionEventController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ElectionEventController.class);
	private static final String KEYS_ALREADY_EXIST = "KEYS_ALREADY_EXIST";
	private static final String KEYS_ALREADY_EXIST_DESCRIPTION = "The Control Components keys already exist for this election event.";
	private static final String KEYS_NOT_EXIST = "KEYS_NOT_EXIST";
	private static final String KEYS_NOT_EXIST_DESCRIPTION = "The Control Components keys do not exist for this election event.";

	private final ElectionEventService electionEventService;
	private final ElectionEventRepository electionEventRepository;

	public ElectionEventController(
			final ElectionEventService electionEventService,
			final ElectionEventRepository electionEventRepository) {
		this.electionEventService = electionEventService;
		this.electionEventRepository = electionEventRepository;
	}

	/**
	 * Runs the securization of an election event.
	 *
	 * @param electionEventId the election event id.
	 * @return a list of ids of the created election events.
	 */
	@PostMapping(value = "/{electionEventId}", produces = "application/json")
	@ApiOperation(value = "Secure election event", notes = "Service to secure an election event.")
	@ApiResponses(value = { @ApiResponse(code = 404, message = "Not Found") })
	public ResponseEntity<String> secureElectionEvent(
			@ApiParam(value = "String", required = true)
			@PathVariable
			final
			String electionEventId) throws IOException {

		validateUUID(electionEventId);

		final DataGeneratorResponse response;
		try {
			response = electionEventService.create(electionEventId);
		} catch (final CCKeysNotExistException e) {
			final ObjectNode errorJson = prepareJsonError(electionEventId, KEYS_NOT_EXIST, KEYS_NOT_EXIST_DESCRIPTION);
			return new ResponseEntity<>(errorJson.toString(), HttpStatus.BAD_REQUEST);
		}

		if (response.isSuccessful()) {
			return new ResponseEntity<>(HttpStatus.CREATED);
		}

		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	/**
	 * Returns an election event identified by its id.
	 *
	 * @param electionEventId the election event id.
	 * @return An election event identified by its id.
	 */
	@GetMapping(value = "/{electionEventId}", produces = "application/json")
	@ResponseBody
	@ApiOperation(value = "Get election event", notes = "Service to retrieve a given election event.", response = String.class)
	@ApiResponses(value = { @ApiResponse(code = 404, message = "Not Found") })
	public ResponseEntity<String> getElectionEvent(
			@ApiParam(value = "String", required = true)
			@PathVariable
			final String electionEventId) {

		validateUUID(electionEventId);

		final String result = electionEventRepository.find(electionEventId);

		final JsonObject jsonObject = JsonUtils.getJsonObject(result);
		if (!jsonObject.isEmpty()) {
			return new ResponseEntity<>(result, HttpStatus.OK);
		}

		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
	}

	@PostMapping(value = "/{electionEventId}/keys", produces = "application/json")
	public ResponseEntity<String> requestCCKeys(
			@PathVariable
			final String electionEventId) {

		validateUUID(electionEventId);

		try {
			electionEventService.requestCCKeys(electionEventId);
		} catch (final CCKeysAlreadyExistException e) {
			final ObjectNode errorJson = prepareJsonError(electionEventId, KEYS_ALREADY_EXIST, KEYS_ALREADY_EXIST_DESCRIPTION);
			return new ResponseEntity<>(errorJson.toString(), HttpStatus.BAD_REQUEST);
		}

		return new ResponseEntity<>(HttpStatus.OK);
	}

	/**
	 * Returns a list of all election events.
	 *
	 * @return The list of election events.
	 */
	@GetMapping(produces = "application/json")
	@ResponseBody
	@ApiOperation(value = "Get election events", notes = "Service to retrieve the list of events.", response = String.class)
	public ResponseEntity<String> getElectionEvents() {
		return ResponseEntity.ok(electionEventRepository.list());
	}

	@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(Exception.class)
	public ResponseEntity<String> error(final HttpServletRequest req, final Exception exception) {
		LOGGER.error("Failed to process request to '{}.", req.getRequestURI(), exception);
		return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).contentType(MediaType.TEXT_PLAIN)
				.body(ExceptionUtils.getRootCauseMessage(exception));
	}

	private ObjectNode prepareJsonError(final String electionEventId, final String code, final String description) {
		final ObjectNode rootNode = JsonNodeFactory.instance.objectNode();
		final ObjectNode errorNode = rootNode.putObject("error");
		errorNode.put("code", code);
		errorNode.put("description", description);
		final ObjectNode context = errorNode.putObject("context");
		context.put("electionEventId", electionEventId);

		return rootNode;
	}
}
