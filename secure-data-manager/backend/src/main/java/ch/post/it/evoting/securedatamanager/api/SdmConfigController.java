/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.api;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ch.post.it.evoting.securedatamanager.services.application.service.SdmConfigService;
import ch.post.it.evoting.securedatamanager.services.domain.model.sdmconfig.SdmConfigData;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/sdm-backend/sdm-config")
@Api(value = "SDM Configurations REST API")
public class SdmConfigController {

	private static final Logger LOGGER = LoggerFactory.getLogger(SdmConfigController.class);

	@Autowired
	private SdmConfigService sdmConfigService;

	@GetMapping(produces = "application/json")
	@ApiOperation(value = "Get SDM configuration service", notes = "", response = String.class)
	@ApiResponses({ @ApiResponse(code = 500, message = "Internal Server Error") })
	public ResponseEntity<Object> getSdmConfig() {
		final SdmConfigData config = new SdmConfigData();
		try {
			config.setConfig(sdmConfigService.getConfig());
		} catch (final IOException e) {
			LOGGER.error("Error trying to get SDM configuration.", e);
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
		return ResponseEntity.ok(config);
	}

	@GetMapping(produces = "application/json", value = "/langs/{langCode}")
	@ApiOperation(value = "Get SDM language", notes = "", response = String.class)
	@ApiResponses({ @ApiResponse(code = 500, message = "Internal Server Error") })
	public ResponseEntity<Object> loadLanguage(
			@PathVariable(value = "langCode")
			final
			String languageCode) {

		try {
			final FileSystemResource languageFile = sdmConfigService.loadLanguage(languageCode);
			if (languageFile.exists()) {
				return ResponseEntity.ok(languageFile);
			} else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
			}
		} catch (final IllegalArgumentException e) {
			LOGGER.info("Error trying to get SDM language.", e);
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.toString());
		}
	}
}
