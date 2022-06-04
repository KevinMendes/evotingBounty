/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.api;

import static ch.post.it.evoting.cryptolib.commons.validations.Validate.validateUUID;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ch.post.it.evoting.securedatamanager.services.application.service.ChoiceCodesComputationService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * Controller for checking the status of the choice codes computation.
 */
@RestController
@RequestMapping("/sdm-backend/choicecodes")
@Api(value = "Computed values REST API")
public class ChoiceCodesComputationController {

	@Autowired
	ChoiceCodesComputationService choiceCodesComputationService;

	/**
	 * Check if computed choice codes are ready.
	 *
	 * @throws IOException if something fails uploading voting card sets or electoral authorities.
	 */
	@PostMapping(value = "/electionevent/{electionEventId}/status", produces = "application/json")
	@ResponseStatus(value = HttpStatus.OK)
	@ApiOperation(value = "Check if computed choice codes are ready and update its status", notes = "Service to check the status of computed choice codes.")
	public void updateChoiceCodesComputationStatus(final @ApiParam(value = "String", required = true)
	@PathVariable
			String electionEventId) throws IOException {

		validateUUID(electionEventId);

		choiceCodesComputationService.updateChoiceCodesComputationStatus(electionEventId);
	}
}
