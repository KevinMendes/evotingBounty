/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.api;

import static ch.post.it.evoting.cryptolib.commons.validations.Validate.validateUUID;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ch.post.it.evoting.securedatamanager.services.application.service.AdminBoardUploadService;
import ch.post.it.evoting.securedatamanager.services.application.service.BallotBoxInformationUploadService;
import ch.post.it.evoting.securedatamanager.services.application.service.BallotUploadService;
import ch.post.it.evoting.securedatamanager.services.application.service.ElectoralAuthoritiesUploadService;
import ch.post.it.evoting.securedatamanager.services.application.service.KeyStoreService;
import ch.post.it.evoting.securedatamanager.services.application.service.VotingCardSetUploadService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

/**
 * The configuration upload end-point.
 */
@RestController
@RequestMapping("/sdm-backend/configurations")
@Api(value = "Configurations REST API")
public class ConfigurationController {

	private static final Logger LOGGER = LoggerFactory.getLogger(ConfigurationController.class);

	private static final String NULL_ELECTION_EVENT_ID = "";

	@Autowired
	private BallotUploadService ballotUploadService;

	@Autowired
	private BallotBoxInformationUploadService ballotBoxInformationUploadService;

	@Autowired
	private VotingCardSetUploadService votingCardSetUploadService;

	@Autowired
	private ElectoralAuthoritiesUploadService electoralAuthoritiesUploadService;

	@Autowired
	private AdminBoardUploadService adminBoardUploadService;

	@Value("${voting.portal.enabled}")
	private boolean isVoterPortalEnabled;

	@Autowired
	private KeyStoreService keystoreService;

	/**
	 * Uploads data to all election events (not yet synchronized)
	 *
	 * @throws IOException if something fails uploading voting card sets or electoral authorities.
	 */
	@PostMapping(produces = "application/json")
	@ResponseStatus(value = HttpStatus.OK)
	@ApiOperation(value = "Upload configuration", notes = "Service to upload the configuration.")
	public void uploadConfiguration() throws IOException {

		if (isVoterPortalEnabled) {
			uploadElectionEventData(NULL_ELECTION_EVENT_ID);
		} else {
			LOGGER.info("The application is configured to not have connectivity to Voter Portal, check if this is the expected behavior");
		}
	}

	/**
	 * Uploads data specific to one election event
	 *
	 * @throws IOException if something fails uploading voting card sets or electoral authorities.
	 */
	@PostMapping(value = "/electionevent/{electionEventId}", produces = "application/json")
	@ResponseStatus(value = HttpStatus.OK)
	@ApiOperation(value = "Election event upload configuration", notes = "Service to upload the configuration for a given election event.")
	public void uploadElectionEventConfigurationConfiguration(
			@ApiParam(value = "String", required = true)
			@PathVariable
			final
			String electionEventId) throws IOException {

		validateUUID(electionEventId);

		if (isVoterPortalEnabled) {
			uploadElectionEventData(electionEventId);
		} else {
			LOGGER.warn("The application is configured to not have connectivity to Voter Portal, check if this is the expected behavior");
		}
	}

	private void uploadElectionEventData(final String electionEventId) throws IOException {
		LOGGER.info("Uploading the configuration of election event {}", electionEventId);

		LOGGER.info("Uploading administration board certificates");
		adminBoardUploadService.uploadSynchronizableAdminBoards(electionEventId, keystoreService.getPrivateKey());
		LOGGER.info("Uploading ballots");
		ballotUploadService.uploadSynchronizableBallots(electionEventId);
		LOGGER.info("Uploading ballot boxes");
		ballotBoxInformationUploadService.uploadSynchronizableBallotBoxInformation(electionEventId);
		LOGGER.info("Uploading voting card sets");
		votingCardSetUploadService.uploadSynchronizableVotingCardSets(electionEventId);
		LOGGER.info("Uploading electoral authorities");
		electoralAuthoritiesUploadService.uploadSynchronizableElectoralAuthorities(electionEventId);

	}

}
