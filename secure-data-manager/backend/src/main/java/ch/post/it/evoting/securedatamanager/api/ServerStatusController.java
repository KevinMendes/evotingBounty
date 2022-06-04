/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.api;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import ch.post.it.evoting.securedatamanager.services.infrastructure.JsonConstants;
import ch.post.it.evoting.securedatamanager.services.infrastructure.OrientManager;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * The server status end-point.
 */
@RestController
@RequestMapping("/sdm-backend")
@Api(value = "Server status REST API")
public class ServerStatusController {
	private static final String OPEN_STATUS = "OPEN";
	private static final String CLOSED_STATUS = "CLOSED";
	private static final String ONLINE_MODE = "ONLINE";
	private static final String OFFLINE_MODE = "OFFLINE";

	@Value("${admin.portal.enabled}")
	private boolean isAdminPortalEnabled;

	@Autowired
	private OrientManager manager;

	/**
	 * Returns the server status [{@value ServerStatusController#OPEN_STATUS} | {@value ServerStatusController#CLOSED_STATUS}] and host mode [{@value
	 * ServerStatusController#ONLINE_MODE} | {@value ServerStatusController#OFFLINE_MODE}].
	 */
	@GetMapping(value = "/status", produces = "application/json")
	@ResponseStatus(value = HttpStatus.OK)
	@ApiOperation(value = "Health Check Service", response = String.class, notes = "Service to validate application is up & running.")
	public String getStatus() {
		final JsonObjectBuilder builder = Json.createObjectBuilder();
		final String status = manager.isActive() ? OPEN_STATUS : CLOSED_STATUS;
		builder.add(JsonConstants.STATUS, status);
		final String mode = isAdminPortalEnabled ? ONLINE_MODE : OFFLINE_MODE;
		builder.add(JsonConstants.HOST_MODE, mode);
		return builder.build().toString();
	}

	/**
	 * Closes the database.
	 */
	@PostMapping(value = "/close", produces = "application/json")
	@ResponseStatus(value = HttpStatus.OK)
	@ApiOperation(value = "Close Database", response = String.class, notes = "Service to close the database.")
	public String closeDatabase() {
		manager.shutdown();
		final JsonObjectBuilder builder = Json.createObjectBuilder();
		builder.add(JsonConstants.STATUS, CLOSED_STATUS);

		return builder.build().toString();
	}
}
