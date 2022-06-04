/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.api;

import static ch.post.it.evoting.cryptolib.commons.validations.Validate.validateUUID;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ch.post.it.evoting.securedatamanager.services.domain.model.config.VotingCardGenerationJobStatus;
import ch.post.it.evoting.securedatamanager.services.domain.service.ProgressManagerService;

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
public class VotingCardSetProgressController {

	private static final Logger LOGGER = LoggerFactory.getLogger(VotingCardSetProgressController.class);

	private final ProgressManagerService<VotingCardGenerationJobStatus> progressManagerService;

	public VotingCardSetProgressController(final ProgressManagerService<VotingCardGenerationJobStatus> progressManagerService) {
		this.progressManagerService = progressManagerService;
	}

	/**
	 * Get the status/progress of the specified voting card generation job.
	 *
	 * @param electionEventId the election event id.
	 * @param jobId           the job execution id.
	 * @return HTTP status code 200 - If we got the voting card status. HTTP status code 404 - If the resource is not found.
	 */
	@GetMapping(value = "/electionevent/{electionEventId}/progress/{jobId}")
	@ApiOperation(value = "Get voting card generation status", notes = "Service to get the status/progress of a specific voting card generation job", response = VotingCardGenerationJobStatus.class)
	@ApiResponses(value = { @ApiResponse(code = 404, message = "Not Found"), @ApiResponse(code = 412, message = "Precondition Failed") })
	public ResponseEntity<VotingCardGenerationJobStatus> getJobProgress(
			@ApiParam(value = "String", required = true)
			@PathVariable
			final String electionEventId,
			@ApiParam(value = "String", required = true)
			@PathVariable
			final String jobId) {

		validateUUID(electionEventId);
		validateUUID(jobId);

		LOGGER.info("Getting the job progress status. [electionEventId: {}, jobId: {}]", electionEventId, jobId);

		final VotingCardGenerationJobStatus status = progressManagerService.getForJob(jobId);
		return ResponseEntity.ok(status);
	}

	/**
	 * Get the status/progress of all voting card generation jobs with a specific status (started by default) .
	 *
	 * @return HTTP status code 200 - If we got the voting card status. HTTP status code 404 - If the resource is not found.
	 */
	@GetMapping(value = "/progress/jobs")
	@ApiOperation(value = "Get voting card generation status", notes = "Service to get the status/progress of all started voting card generation job", response = VotingCardGenerationJobStatus.class)
	@ApiResponses(value = { @ApiResponse(code = 404, message = "Not Found"), @ApiResponse(code = 412, message = "Precondition Failed") })
	public ResponseEntity<List<VotingCardGenerationJobStatus>> getJobsProgress(
			@RequestParam(value = "status", required = false)
			final String jobStatus) {

		LOGGER.info("Getting voting card generation status. [jobStatus: {}]", jobStatus);

		final List<VotingCardGenerationJobStatus> jobsProgressByStatus = StringUtils.isBlank(jobStatus) ?
				progressManagerService.getAll() :
				progressManagerService.getAllByStatus(jobStatus);

		return ResponseEntity.ok(jobsProgressByStatus);
	}

}
