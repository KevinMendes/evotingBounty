/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.api;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ch.post.it.evoting.securedatamanager.services.infrastructure.preconfiguration.PreconfigurationRepository;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * The election event end-point.
 */
@RestController
@RequestMapping("/sdm-backend/preconfiguration")
@Api(value = "Pre-configuration REST API")
public class PreconfigurationController {

	private static final Logger LOGGER = LoggerFactory.getLogger(PreconfigurationController.class);

	// The election event repository
	@Autowired
	private PreconfigurationRepository preconfigurationRepository;

	// The configuration of contests / elections
	@Value("${elections.config.filename}")
	private String configFile;

	@Value("${admin.portal.enabled}")
	private boolean isAdminPortalEnabled;

	@PostMapping(produces = "application/json")
	@ResponseStatus(value = HttpStatus.CREATED)
	@ApiOperation(value = "Get configuration", notes = "Service to retrieve the configuration of administration boards and election events.", response = String.class)
	public String createElectionEvent() throws IOException {

		String result = null;
		if (!isAdminPortalEnabled) {
			LOGGER.info("The application is configured to not have connectivity to Admin Portal, check if this is the expected behavior");
		}
		// call to end point to download data from administration portal
		else if (preconfigurationRepository.download(configFile)) {
			// process the download data
			result = preconfigurationRepository.readFromFileAndSave(configFile);
		}

		return result;
	}
}
