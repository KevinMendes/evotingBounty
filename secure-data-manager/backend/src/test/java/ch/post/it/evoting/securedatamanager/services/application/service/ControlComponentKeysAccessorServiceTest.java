/*
 * (c) Copyright 2022 Swiss Post Ltd.
 */
package ch.post.it.evoting.securedatamanager.services.application.service;

import static ch.post.it.evoting.securedatamanager.config.commons.utils.JsonUtils.getJsonArray;
import static ch.post.it.evoting.securedatamanager.config.commons.utils.JsonUtils.getJsonObject;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.json.JsonArray;
import javax.json.JsonObject;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import ch.post.it.evoting.securedatamanager.commons.Constants;
import ch.post.it.evoting.securedatamanager.services.application.exception.ResourceNotFoundException;
import ch.post.it.evoting.securedatamanager.services.infrastructure.electoralauthority.ElectoralAuthorityRepository;

@ExtendWith(MockitoExtension.class)
@SpringJUnitConfig(ControlComponentKeysAccessorServiceTestSpringConfig.class)
class ControlComponentKeysAccessorServiceTest {

	private static final String ELECTORAL_AUTHORITY_ID = "16e020d934594544a6e17d1e410da513";
	private static final String ELECTORAL_AUTHORITY_REPOSITORY_PATH = "/electoralAuthorityRepository.json";

	private static JsonObject electoralAuthorityRepositoryJson;
	private static JsonArray mixingKeysJsonArray;

	@Autowired
	private ControlComponentKeysAccessorService controlComponentKeysAccessorService;

	@Autowired
	private ElectoralAuthorityRepository electoralAuthorityRepositoryMock;

	@BeforeAll
	static void init() throws IOException, URISyntaxException {

		final URL repoUrl = ControlComponentKeysAccessorServiceTest.class.getResource(ELECTORAL_AUTHORITY_REPOSITORY_PATH);
		final Path repoPath = Paths.get(repoUrl.toURI());
		electoralAuthorityRepositoryJson = getJsonObject(new String(Files.readAllBytes(repoPath), StandardCharsets.UTF_8));
		mixingKeysJsonArray = getJsonArray(electoralAuthorityRepositoryJson.getString(Constants.MIX_DEC_KEY_LABEL));
	}

	@Test
	void downloadMixingKeys() throws ResourceNotFoundException {
		when(electoralAuthorityRepositoryMock.find(anyString())).thenReturn(electoralAuthorityRepositoryJson.toString());

		final JsonArray mixingKeysJsonArray = controlComponentKeysAccessorService.downloadMixingKeys(ELECTORAL_AUTHORITY_ID);

		assertEquals(mixingKeysJsonArray.toString(), ControlComponentKeysAccessorServiceTest.mixingKeysJsonArray.toString());
	}
}
